import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';

const script=readFileSync('scripts/release-android.ps1','utf8');

test('script normaliza SDK local antes de executar lintRelease',()=>{
 const configure=script.indexOf('$localProperties = Join-Path $root');
 const lint=script.indexOf('lintRelease testDebugUnitTest assembleRelease');
 assert(configure>=0&&lint>configure);
 assert(script.includes("$sdk = [string]$sdk"));
 assert(script.includes("$sdk.Replace('\\', '/').Replace(':', '\\:')"));
 assert(script.includes('sdk.dir=$sdkProperty'));
 assert(script.includes('#noinspection PropertyEscape'));
 assert(script.includes('WriteAllText($localProperties, $localContent'));
 assert(script.includes("platforms\\android-36"));
});

test('supressão não desliga os demais erros do Android Lint nem a assinatura',()=>{
 assert(script.includes('lintRelease testDebugUnitTest assembleRelease'));
 assert(script.includes('Get-SigningDigest $built'));
 assert(script.includes('Get-SigningDigest $oldApk'));
 assert(script.includes('if ($newDigest -ne $previousDigest)'));
 assert(!script.includes('lint.abortOnError = false'));
 assert(!script.includes('lint { disable'));
});

test('o Lint ignora exclusivamente a inspeção PropertyEscape do SDK local no Windows',()=>{
 const gradle=readFileSync('android/app/build.gradle','utf8');
 assert.match(gradle,/lint\s*\{\s*disable\s*\+=\s*\['PropertyEscape'\]/);
 assert.doesNotMatch(gradle,/abortOnError\s*(?:=|\s+)\s*false/);
 assert.doesNotMatch(gradle,/\bcheckReleaseBuilds\s*=\s*false/);
 assert.match(gradle,/signingConfig signingConfigs\.release/);
});
