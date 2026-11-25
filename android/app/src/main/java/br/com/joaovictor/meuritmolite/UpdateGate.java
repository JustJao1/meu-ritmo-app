package br.com.joaovictor.meuritmolite;
import android.app.*;import android.content.*;import android.net.Uri;import android.os.*;import android.view.*;import android.widget.*;import org.json.JSONObject;import java.net.*;import java.io.*;
/** Verifica a versão mínima exigida pelo servidor; não permite ignorar uma atualização obrigatória. */
public final class UpdateGate {
 private static final String POLICY="https://meuritmo.net/android-version.json";
 private static final long INTERVAL=15*60*1000L;
 private final Activity activity;private final Handler handler=new Handler(Looper.getMainLooper());
 private AlertDialog dialog;private View blocker;private boolean checking=false;private boolean required=false;private boolean verified=false;private String downloadUrl="";
 public UpdateGate(Activity activity){this.activity=activity;}
 public void protect(){if(blocker!=null)return;FrameLayout screen=activity.findViewById(android.R.id.content);FrameLayout layer=new FrameLayout(activity);layer.setBackgroundColor(0xfffaf9f5);layer.setClickable(true);layer.setFocusable(true);TextView status=new TextView(activity);status.setText("Verificando versão do Meu Ritmo…");status.setTextSize(16);status.setTextColor(0xff252a28);status.setGravity(Gravity.CENTER);layer.addView(status,new FrameLayout.LayoutParams(-1,-1));screen.addView(layer,new FrameLayout.LayoutParams(-1,-1));blocker=layer;}
 private void unlock(){if(blocker!=null){android.view.ViewParent parent=blocker.getParent();if(parent instanceof android.view.ViewGroup)((android.view.ViewGroup)parent).removeView(blocker);blocker=null;}}
 public void check(){if(checking||activity.isFinishing())return;checking=true;new Thread(()->{
  try{HttpURLConnection c=(HttpURLConnection)new URL(POLICY).openConnection();c.setConnectTimeout(9000);c.setReadTimeout(9000);c.setInstanceFollowRedirects(false);
   if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());
   ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(InputStream in=c.getInputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){bytes.write(b,0,n);if(bytes.size()>16384)throw new IOException("Policy too large");}}
   JSONObject v=new JSONObject(bytes.toString("UTF-8"));int min=v.getInt("minSupportedVersionCode");String url=v.getString("apkUrl");String message=v.optString("message","Atualize para continuar usando o aplicativo.");
   if(min<1||!url.startsWith("https://"))throw new IOException("Invalid update policy");
   // A successfully checked policy is safe to reuse while offline. New installs
   // still require one successful online check before accessing local alarms.
   activity.getSharedPreferences("mr-update-policy",Context.MODE_PRIVATE).edit()
    .putInt("min-version",min).putString("download-url",url).apply();
   handler.post(()->{checking=false;verified=true;if(min>installedVersion()){required=true;downloadUrl=url;protect();show(message);}else{required=false;if(dialog!=null&&dialog.isShowing())dialog.dismiss();unlock();schedule();}});
  }catch(Exception e){handler.post(()->{
   checking=false;
   android.content.SharedPreferences cached=activity.getSharedPreferences("mr-update-policy",Context.MODE_PRIVATE);
   int cachedMin=cached.getInt("min-version",Integer.MAX_VALUE);
   if(cachedMin>0&&cachedMin<=installedVersion()){
    // The latest online minimum was satisfied; allow offline alarm management
    // and retry the policy check when connectivity returns.
    required=false;verified=true;
    if(dialog!=null&&dialog.isShowing())dialog.dismiss();
    unlock();
    handler.postDelayed(this::check,30000);
   }else{
    required=true;downloadUrl=cached.getString("download-url","");
    protect();show("Não foi possível verificar a versão do aplicativo. Conecte-se à internet e tente novamente.");
    handler.postDelayed(this::check,30000);
   }
  });}
 }).start();}
 private void schedule(){handler.removeCallbacksAndMessages(null);handler.postDelayed(this::check,INTERVAL);}
 private void show(String message){if(activity.isFinishing()||(dialog!=null&&dialog.isShowing()))return;
  dialog=new AlertDialog.Builder(activity).setTitle("Atualização obrigatória").setMessage(message)
  .setCancelable(false).setPositiveButton(downloadUrl.isEmpty()?"Tentar novamente":"Atualizar agora",(d,w)->{if(downloadUrl.isEmpty()){check();}else try{activity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(downloadUrl)));}catch(Exception ignored){}handler.postDelayed(()->show(message),500);}).create();
  dialog.setCanceledOnTouchOutside(false);dialog.show();}
 private long installedVersion(){try{android.content.pm.PackageInfo p=activity.getPackageManager().getPackageInfo(activity.getPackageName(),0);return android.os.Build.VERSION.SDK_INT>=28?p.getLongVersionCode():p.versionCode;}catch(Exception e){return 0;}}
 public boolean isRequired(){return required||!verified;}
 public void stop(){handler.removeCallbacksAndMessages(null);if(dialog!=null)dialog.dismiss();unlock();}
}
