import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';

const json=JSON.parse(readFileSync(new URL('../dist/release.json',import.meta.url),'utf8'));
const page=readFileSync(new URL('../dist/index.html',import.meta.url),'utf8');
const gradle=readFileSync(new URL('../android/app/build.gradle',import.meta.url),'utf8');

test('download manifest and installation page point to the same current APK',()=>{
 assert.equal(json.available,true);
 assert.match(json.version,/^\d+\.\d+\.\d+$/);
 assert.ok(page.includes(json.url));
 assert.ok(page.includes('versão '+json.version));
 assert.match(json.sha256,/^[0-9a-f]{64}$/);
 assert.ok(json.size>100000);
 assert.ok(gradle.includes("versionName '"+json.version+"'"));
});

test('launcher opens the online site and keeps the offline module separate',()=>{
 const manifest=readFileSync(new URL('../android/app/src/main/AndroidManifest.xml',import.meta.url),'utf8');
 const online=readFileSync(new URL('../android/app/src/main/java/br/com/joaovictor/meuritmolite/OnlineActivity.java',import.meta.url),'utf8');
 assert.match(manifest,/<activity android:name="\.OnlineActivity"[^>]*><intent-filter><action android:name="android.intent.action.MAIN"/);
 assert.match(online,/web\.loadUrl\("https:\/\/meuritmo\.net\/app\?source=android-app"\)/);
 assert.match(online,/new Intent\(this,MainActivity\.class\)/);
});
