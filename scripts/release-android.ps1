param(
    [switch]$Publish,
    [string]$KeystorePath = (Join-Path $env:USERPROFILE 'meu-ritmo-release.jks')
)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$repo = 'JustJao1/meu-ritmo-app'
$version = '1.0.2'
$expectedPreviousHash = '7c2ac22c23a35ca451dc8ca1389cf0a1e6a0a6cc504d4ff8d294080a97d0e6e3'
$previousUrl = 'https://github.com/JustJao1/meu-ritmo-app/releases/download/v1.0.1/Meu-Ritmo-1.0.1.apk'
$oldApk = Join-Path $env:TEMP 'mr-original-v1.0.1-check.apk'
$originalLocation = Get-Location

if (-not (Test-Path -LiteralPath $KeystorePath -PathType Leaf)) {
    throw "A chave de assinatura anterior nao foi encontrada em $KeystorePath. Nao crie uma chave nova: ela impediria a atualizacao por cima da versao instalada."
}
if (-not (Get-Command gradle -ErrorAction SilentlyContinue)) {
    throw 'Gradle nao esta disponivel no PATH. Use a instalacao que compilou a versao 1.0.1.'
}

$sdk = @($env:ANDROID_SDK_ROOT, $env:ANDROID_HOME, (Join-Path $env:LOCALAPPDATA 'Android\Sdk')) |
    Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
if (-not $sdk) { throw 'Android SDK nao encontrado.' }
# A verificacao Android Lint pode sinalizar PropertyEscape mesmo com o caminho
# sdk.dir devidamente escapado em builds locais do Windows. A supressao abaixo
# fica restrita a essa propriedade de maquina, mantendo todas as demais regras.
$sdk = [string]$sdk
$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk
$localProperties = Join-Path $root 'android\local.properties'
$sdkProperty = $sdk.Replace('\', '/').Replace(':', '\:')
$localContent = "#noinspection PropertyEscape`nsdk.dir=$sdkProperty`n"
[System.IO.File]::WriteAllText($localProperties, $localContent, [System.Text.Encoding]::ASCII)
if (-not (Test-Path (Join-Path $sdk 'platforms\android-36'))) {
    throw 'Android SDK Platform 36 nao instalado.'
}
Write-Host "SDK Android verificado em $sdk"
$signer = Join-Path $sdk 'build-tools\36.0.0\apksigner.bat'
$aapt = Join-Path $sdk 'build-tools\36.0.0\aapt.exe'
if (-not (Test-Path $signer) -or -not (Test-Path $aapt)) {
    throw 'Android SDK Build Tools 36.0.0 nao encontrado.'
}

function Get-SigningDigest([string]$file) {
    $output = (& $signer verify --verbose --print-certs $file 2>&1) -join [Environment]::NewLine
    if ($LASTEXITCODE -ne 0) { throw "Falha na verificacao da assinatura do APK: $output" }
    $match = [regex]::Match($output, '(?im)Signer #1 certificate SHA-256 digest:\s*([a-f0-9:]+)')
    if (-not $match.Success) { throw "Nao foi possivel comparar o certificado do arquivo $file." }
    return $match.Groups[1].Value.Replace(':', '').ToLowerInvariant()
}

$storeSecret = Read-Host 'Senha do keystore original' -AsSecureString
$keySecret = Read-Host 'Senha da chave meu-ritmo (Enter separado se for igual)' -AsSecureString
$env:ANDROID_SIGNING_STORE = (Resolve-Path -LiteralPath $KeystorePath).Path
$env:ANDROID_SIGNING_STORE_PASSWORD = [System.Net.NetworkCredential]::new('', $storeSecret).Password
$enteredKeyPassword = [System.Net.NetworkCredential]::new('', $keySecret).Password
$env:ANDROID_SIGNING_KEY_PASSWORD = if ($enteredKeyPassword) { $enteredKeyPassword } else { $env:ANDROID_SIGNING_STORE_PASSWORD }
$env:ANDROID_SIGNING_KEY_ALIAS = 'meu-ritmo'

try {
    Push-Location (Join-Path $root 'android')
    & gradle --no-daemon lintRelease testDebugUnitTest assembleRelease
    if ($LASTEXITCODE -ne 0) { throw 'A compilacao ou os testes falharam. Nada foi publicado.' }
    Pop-Location

    $built = Join-Path $root 'android\app\build\outputs\apk\release\app-release.apk'
    if (-not (Test-Path $built)) { throw 'O Gradle nao produziu app-release.apk.' }

    $badging = (& $aapt dump badging $built | Select-Object -First 1) -join ''
    if ($badging -notmatch "name='br\.com\.joaovictor\.meuritmolite'" -or
        $badging -notmatch "versionCode='5'" -or
        $badging -notmatch "versionName='1\.0\.2'") {
        throw "Metadados do APK inesperados: $badging"
    }

    $newDigest = Get-SigningDigest $built
    Invoke-WebRequest -Uri $previousUrl -OutFile $oldApk
    if ((Get-FileHash -Algorithm SHA256 $oldApk).Hash.ToLowerInvariant() -ne $expectedPreviousHash) {
        throw 'O arquivo 1.0.0 publicado nao corresponde ao hash esperado. Nada foi publicado.'
    }
    $previousDigest = Get-SigningDigest $oldApk
    if ($newDigest -ne $previousDigest) {
        throw 'Certificado de assinatura diferente da versao 1.0.0. Nao publique: a atualizacao nao sera instalavel por cima.'
    }

    $outputDir = Join-Path $root 'release-output'
    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
    $outputApk = Join-Path $outputDir "Meu-Ritmo-$version.apk"
    Copy-Item -LiteralPath $built -Destination $outputApk -Force
    $hash = (Get-FileHash -Algorithm SHA256 $outputApk).Hash.ToLowerInvariant()
    $size = (Get-Item $outputApk).Length
    Write-Host "APK assinado e verificado: $outputApk"
    Write-Host "SHA256: $hash"
    Write-Host "Tamanho: $size bytes"
    Write-Host 'Certificado confere com a versao 1.0.0.'

    if ($Publish) {
        if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
            throw 'GitHub CLI (gh) nao encontrado. O APK foi gerado, mas nao publicado.'
        }
        # Uma release inexistente e' esperada. "gh release view" escreve no
        # stderr e PowerShell com ErrorActionPreference=Stop interrompe a
        # execucao antes de permitir sua criacao. Consultar a lista evita isso.
        $existingReleasesJson = (& gh release list --repo $repo --json tagName --limit 100) -join [Environment]::NewLine
        if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel consultar as releases no GitHub.' }
        $existingReleases = @($existingReleasesJson | ConvertFrom-Json)
        if (@($existingReleases | Where-Object { $_.tagName -eq "v$version" }).Count -gt 0) {
            throw "A release v$version ja existe. Nao sera sobrescrita."
        }
        & gh release create "v$version" $outputApk --repo $repo --target main --title "MR: Meu Ritmo App $version" --notes 'Atualizacao do aplicativo Android. O ambiente online apresenta as funcoes e os temas atuais de meuritmo.net. Mantida a assinatura da versao 1.0.0 para atualizacao sem reinstalacao.'
        if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel publicar a release no GitHub.' }
        Write-Host "Release publicada: https://github.com/$repo/releases/tag/v$version"
        Write-Host 'Ainda e preciso atualizar a pagina de download e a politica de atualizacao do site, apos conferir o arquivo publicado.'
    }
} finally {
    Set-Location $originalLocation
    Remove-Item Env:\ANDROID_SIGNING_STORE -ErrorAction SilentlyContinue
    Remove-Item Env:\ANDROID_SIGNING_STORE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:\ANDROID_SIGNING_KEY_ALIAS -ErrorAction SilentlyContinue
    Remove-Item Env:\ANDROID_SIGNING_KEY_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $oldApk -ErrorAction SilentlyContinue
}
