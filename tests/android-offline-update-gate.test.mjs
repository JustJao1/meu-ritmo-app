import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';

const code=readFileSync(new URL('../android/app/src/main/java/br/com/joaovictor/meuritmolite/UpdateGate.java',import.meta.url),'utf8');

test('offline access relies on a previously verified minimum version',()=>{
 assert.match(code,/getSharedPreferences\("mr-update-policy",Context\.MODE_PRIVATE\)/);
 assert.match(code,/putInt\("min-version",min\)/);
 assert.match(code,/cachedMin>0&&cachedMin<=installedVersion\(\)/);
 assert.match(code,/required=false;verified=true;/);
 assert.match(code,/handler\.postDelayed\(this::check,30000\)/);
});

test('new installs and outdated versions still block if update policy is unavailable',()=>{
 assert.match(code,/getInt\("min-version",Integer\.MAX_VALUE\)/);
 assert.match(code,/required=true;downloadUrl=cached\.getString\("download-url",""\)/);
 assert.match(code,/protect\(\);show\("Não foi possível verificar a versão do aplicativo/);
});
