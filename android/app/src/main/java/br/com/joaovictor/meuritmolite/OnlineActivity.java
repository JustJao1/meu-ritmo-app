package br.com.joaovictor.meuritmolite;
import android.Manifest;import android.content.pm.PackageManager;import android.app.*;import android.content.*;import android.graphics.Color;import android.net.Uri;import android.os.Bundle;import android.webkit.*;import android.view.*;import android.widget.*;
/** Online account, plans, community and weather. Native offline alarms remain in MainActivity. */
public final class OnlineActivity extends Activity {
 private WebView web;private UpdateGate gate;private LinearLayout root;private GeolocationPermissions.Callback geoCallback;private String geoOrigin;private static final int LOCATION_REQUEST=204, FILE_REQUEST=205;private ValueCallback<Uri[]> fileCallback;
 @Override public void onCreate(Bundle state){super.onCreate(state);
  gate=new UpdateGate(this);
  web=new WebView(this);web.setBackgroundColor(Color.rgb(250,249,245));
  WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);
  settings.setAllowFileAccess(false);settings.setAllowContentAccess(true);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  settings.setSafeBrowsingEnabled(true);settings.setSupportMultipleWindows(true);settings.setJavaScriptCanOpenWindowsAutomatically(true);
  CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
  web.setWebChromeClient(new WebChromeClient(){
   // Checkout Asaas is opened from a direct tap with window.open('about:blank').
   // Allow the same-origin blank window, then hand HTTPS destinations to the
   // device browser rather than embedding payments in an untrusted WebView.
   @Override public boolean onCreateWindow(WebView parent,boolean isDialog,boolean isUserGesture,android.os.Message resultMsg){
    if(!isUserGesture)return false;
    final WebView child=new WebView(OnlineActivity.this);
    WebSettings childSettings=child.getSettings();
    childSettings.setJavaScriptEnabled(true);
    childSettings.setDomStorageEnabled(true);
    childSettings.setAllowFileAccess(false);
    childSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
    childSettings.setSupportMultipleWindows(false);
    child.setWebViewClient(new WebViewClient(){
     boolean handedOff=false;
     private void openBrowser(WebView popup,Uri url){
      if(handedOff||url==null||!"https".equals(url.getScheme()))return;
      handedOff=true;
      try{startActivity(new Intent(Intent.ACTION_VIEW,url).addCategory(Intent.CATEGORY_BROWSABLE));}
      catch(Exception e){Toast.makeText(OnlineActivity.this,"Não foi possível abrir o pagamento no navegador.",Toast.LENGTH_LONG).show();}
      popup.post(()->{popup.stopLoading();popup.destroy();});
     }
     @Override public boolean shouldOverrideUrlLoading(WebView popup,WebResourceRequest request){
      Uri target=request.getUrl();
      if("about".equals(target.getScheme()))return false;
      openBrowser(popup,target);
      return true;
     }
     @Override public void onPageStarted(WebView popup,String url,android.graphics.Bitmap favicon){
      Uri target=Uri.parse(url);
      if(!"about".equals(target.getScheme()))openBrowser(popup,target);
     }
    });
    WebView.WebViewTransport transport=(WebView.WebViewTransport)resultMsg.obj;
    transport.setWebView(child);
    resultMsg.sendToTarget();
    return true;
   }
   @Override public void onCloseWindow(WebView window){if(window!=null&&window!=web)window.destroy();}
   @Override public boolean onShowFileChooser(WebView view,ValueCallback<Uri[]> callback,FileChooserParams params){
    if(fileCallback!=null){fileCallback.onReceiveValue(null);fileCallback=null;}
    fileCallback=callback;
    try{
     Intent intent=params.createIntent();
     intent.addCategory(Intent.CATEGORY_OPENABLE);
     startActivityForResult(intent,FILE_REQUEST);
     return true;
    }catch(Exception e){fileCallback=null;callback.onReceiveValue(null);Toast.makeText(OnlineActivity.this,"Não foi possível abrir as imagens.",Toast.LENGTH_LONG).show();return true;}
   }

   @Override public void onGeolocationPermissionsShowPrompt(String origin,GeolocationPermissions.Callback callback){
    Uri uri=Uri.parse(origin);
    if(!"https".equals(uri.getScheme())||!("meuritmo.net".equals(uri.getHost())||"www.meuritmo.net".equals(uri.getHost()))){callback.invoke(origin,false,false);return;}
    if(android.os.Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
     if(geoCallback!=null)geoCallback.invoke(geoOrigin,false,false);
     geoCallback=callback;geoOrigin=origin;
     requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_REQUEST);
    }else callback.invoke(origin,true,false);
   }
   @Override public void onGeolocationPermissionsHidePrompt(){ }
  });
  web.setWebViewClient(new WebViewClient(){
   @Override public void onReceivedError(WebView view,WebResourceRequest req,WebResourceError error){if(req.isForMainFrame())Toast.makeText(OnlineActivity.this,"Sem conexão com o Meu Ritmo. Verifique sua internet e tente novamente.",Toast.LENGTH_LONG).show();}

   @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
    Uri uri=request.getUrl();String host=uri.getHost();
    if("https".equals(uri.getScheme())&&("meuritmo.net".equals(host)||"www.meuritmo.net".equals(host)))return false;
    if(request.isForMainFrame()){try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(Exception ignored){}return true;}
    return false;
   }
  });
  root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
  if(android.os.Build.VERSION.SDK_INT>=35)root.setOnApplyWindowInsetsListener((view,insets)->{
   android.graphics.Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());
   view.setPadding(safe.left,safe.top,safe.right,safe.bottom);
   return insets;
  });
  root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
  // Compact, theme-matched native actions; never use platform-default raised buttons.
  final float density=getResources().getDisplayMetrics().density;
  final int buttonHeight=Math.round(48*density);
  final int gap=Math.round(10*density);
  final int inset=Math.round(14*density);
  LinearLayout controls=new LinearLayout(this);
  controls.setOrientation(LinearLayout.HORIZONTAL);
  controls.setGravity(Gravity.CENTER_VERTICAL|Gravity.END);
  controls.setPadding(inset,Math.round(8*density),inset,Math.round(10*density));
  controls.setBackgroundColor(0xfffaf9f5);
  Button alarmButton=new Button(this);
  alarmButton.setAllCaps(false);
  alarmButton.setText("Alarmes do aparelho");
  if(getResources().getConfiguration().screenWidthDp<360)alarmButton.setText("Alarmes");
  alarmButton.setContentDescription("Abrir alarmes locais do aparelho");
  alarmButton.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP,13);
  alarmButton.setTextColor(0xff2b302b);
  alarmButton.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);
  alarmButton.setGravity(Gravity.CENTER);
  alarmButton.setSingleLine(true);
  alarmButton.setEllipsize(android.text.TextUtils.TruncateAt.END);
  alarmButton.setMinHeight(0);alarmButton.setMinimumHeight(0);
  alarmButton.setMinWidth(0);alarmButton.setMinimumWidth(0);
  alarmButton.setPadding(Math.round(10*density),0,Math.round(10*density),0);
  alarmButton.setCompoundDrawablePadding(Math.round(7*density));
  alarmButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mr_alarm,0,0,0);
  alarmButton.setBackgroundResource(R.drawable.mr_control_secondary);
  alarmButton.setStateListAnimator(null);
  alarmButton.setElevation(0f);
  alarmButton.setOnClickListener(v->startActivity(new Intent(this,MainActivity.class)));
  Button refreshButton=new Button(this);
  refreshButton.setAllCaps(false);
  refreshButton.setText("Atualizar");
  refreshButton.setContentDescription("Recarregar Meu Ritmo online");
  refreshButton.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP,13);
  refreshButton.setTextColor(0xfffaf9f5);
  refreshButton.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);
  refreshButton.setGravity(Gravity.CENTER);
  refreshButton.setSingleLine(true);
  refreshButton.setMinHeight(0);refreshButton.setMinimumHeight(0);
  refreshButton.setMinWidth(0);refreshButton.setMinimumWidth(0);
  refreshButton.setPadding(Math.round(10*density),0,Math.round(10*density),0);
  refreshButton.setCompoundDrawablePadding(Math.round(7*density));
  refreshButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mr_refresh,0,0,0);
  refreshButton.setBackgroundResource(R.drawable.mr_control_primary);
  refreshButton.setStateListAnimator(null);
  refreshButton.setElevation(0f);
  refreshButton.setOnClickListener(v->web.reload());
  LinearLayout.LayoutParams alarmParams;
  LinearLayout.LayoutParams refreshParams;
  if(getResources().getConfiguration().screenWidthDp>=600){
   alarmParams=new LinearLayout.LayoutParams(Math.round(220*density),buttonHeight);
   refreshParams=new LinearLayout.LayoutParams(Math.round(142*density),buttonHeight);
  }else{
   alarmParams=new LinearLayout.LayoutParams(0,buttonHeight,1.8f);
   refreshParams=new LinearLayout.LayoutParams(0,buttonHeight,1f);
  }
  alarmParams.setMargins(0,0,gap,0);
  controls.addView(alarmButton,alarmParams);
  controls.addView(refreshButton,refreshParams);
  View separator=new View(this);
  separator.setBackgroundColor(0xffe5e5df);
  root.addView(separator,new LinearLayout.LayoutParams(-1,Math.max(1,Math.round(density))));
  root.addView(controls,new LinearLayout.LayoutParams(-1,-2));
  setContentView(root);gate.protect();gate.check();web.loadUrl("https://meuritmo.net/app?source=android-app");
 }
 @Override public boolean onCreateOptionsMenu(Menu menu){menu.add(0,1,0,"Alarmes do aparelho");menu.add(0,2,1,"Atualizar");return true;}
 @Override public boolean onOptionsItemSelected(MenuItem item){if(item.getItemId()==1){startActivity(new Intent(this,MainActivity.class));return true;}if(item.getItemId()==2){web.reload();return true;}return super.onOptionsItemSelected(item);}
 @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode==FILE_REQUEST){ValueCallback<Uri[]> callback=fileCallback;fileCallback=null;if(callback!=null){Uri[] selected=resultCode==RESULT_OK?WebChromeClient.FileChooserParams.parseResult(resultCode,data):null;callback.onReceiveValue(selected);}}}
 @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
  super.onRequestPermissionsResult(request,permissions,results);
  if(request==LOCATION_REQUEST&&geoCallback!=null){
   geoCallback.invoke(geoOrigin,results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED,false);
   geoCallback=null;geoOrigin=null;
  }
 }
 @Override protected void onResume(){super.onResume();if(gate!=null)gate.check();}
 @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){if(gate!=null)gate.stop();if(geoCallback!=null){geoCallback.invoke(geoOrigin,false,false);geoCallback=null;}if(fileCallback!=null){fileCallback.onReceiveValue(null);fileCallback=null;}if(web!=null){web.destroy();web=null;}super.onDestroy();}
}
