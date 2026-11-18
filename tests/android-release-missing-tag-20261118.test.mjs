import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';

const script=readFileSync('scripts/release-android.ps1','utf8');
test('versão ausente é detectada por listagem em vez de release view que aborta',()=>{
 assert.match(script,/gh release list --repo \$repo --json tagName/);
 assert.doesNotMatch(script,/gh release view/);
 assert.match(script,/Where-Object \{ \$_\.tagName -eq "v\$version" \}/);
 assert.match(script,/gh release create "v\$version" \$outputApk/);
});
test('mantém verificação da assinatura antes da publicação',()=>{
 assert.match(script,/Get-SigningDigest \$oldApk/);
 assert.match(script,/Get-SigningDigest \$built/);
 assert.match(script,/\$newDigest -ne \$previousDigest/);
 assert.match(script,/testDebugUnitTest assembleRelease/);
});
