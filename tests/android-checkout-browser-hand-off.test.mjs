import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';

const online=readFileSync('android/app/src/main/java/br/com/joaovictor/meuritmolite/OnlineActivity.java','utf8');
const gradle=readFileSync('android/app/build.gradle','utf8');
const release=readFileSync('.github/workflows/android-release.yml','utf8');
const metadata=JSON.parse(readFileSync('dist/release.json','utf8'));

test('o pagamento abre no navegador sem fechar a WebView do Meu Ritmo',()=>{
 assert(online.includes('settings.setSupportMultipleWindows(true)'));
 assert(online.includes('settings.setJavaScriptCanOpenWindowsAutomatically(true)'));
 assert(online.includes('onCreateWindow(WebView parent,boolean isDialog,boolean isUserGesture,android.os.Message resultMsg)'));
 assert(online.includes('if(!isUserGesture)return false'));
 assert(online.includes('"about".equals(target.getScheme())'));
 assert(online.includes('"https".equals(url.getScheme())'));
 assert(online.includes('new Intent(Intent.ACTION_VIEW,url).addCategory(Intent.CATEGORY_BROWSABLE)'));
 assert(online.includes('transport.setWebView(child)'));
 assert(online.includes('web.loadUrl("https://meuritmo.net/app?source=android-app")'));
});

test('publicação nativa exige a mesma chave e certificado da versão instalada',()=>{
 assert(gradle.includes("versionCode 5"));
 assert(gradle.includes("versionName '1.0.2'"));
 assert(release.includes('ANDROID_KEYSTORE_BASE64'));
 assert(release.includes('ANDROID_SIGNING_STORE_PASSWORD'));
 assert(release.includes('OLD_CERT'));
 assert(release.includes('NEW_CERT'));
 assert(release.includes('test "$OLD_CERT" = "$NEW_CERT"'));
 assert(release.includes('exit 1'));
 assert(release.includes('A versão 1.0.2 NÃO foi publicada.'));
 assert.equal(metadata.version,'1.0.1'); // don't advertise an unreleased APK
});
