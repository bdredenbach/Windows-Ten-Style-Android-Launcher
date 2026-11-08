package com.nthexperience.desktop;
import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

/** Native first-preview regression. No network request or cloud quota is consumed. */
public final class TenSmoke extends Instrumentation {
 MainActivity home;View root;DesktopStore store;
 @Override public void onCreate(Bundle args){super.onCreate(args);start();}
 @Override public void onStart(){Bundle result=new Bundle();try{
  store=new DesktopStore(getTargetContext());store.prefs.edit().putInt("startup_mode",2).putBoolean("welcome_on_start",false).putBoolean("reduce_motion",true).putBoolean("sidebar_visible",false).putBoolean("taskbar_apps",false).commit();
  getTargetContext().getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();getTargetContext().getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
  home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(getTargetContext(),MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();waitForIdleSync();
  check(getTargetContext().getPackageName().equals("com.nthexperience.desktop.ten"),"Separate Ten identity");
  ui(()->{home.open("@notes");home.open("@search");StartMenu start=find(root,StartMenu.class);check(start!=null,"Start present");start.search.setText("Nth Nota");View row=description(start,"Open Nth Nota");check(row!=null,"Nota searchable");row.performClick();});waitForIdleSync();
  ui(()->{TenNotaPanel p=find(root,TenNotaPanel.class);check(p!=null,"Nota opens");try{check(!new JSONObject(p.cloud.state()).optBoolean("allowed"),"Cloud defaults off");}catch(Exception e){throw new RuntimeException(e);}description(p,"New chat").performClick();p.send.performClick();check(p.input.getError()!=null,"Empty input rejected");p.input.setText("Draft a thank-you note");p.pending="fixture";p.send.setEnabled(false);try{p.result(new JSONObject().put("id","fixture").put("event","result").put("text","A saved writing reply").put("provider","fixture"));}catch(Exception e){throw new RuntimeException(e);}check(p.pending.isEmpty()&&p.send.isEnabled(),"Reply completes");View save=text(p,"Save to Notepad");save.performClick();check(store.prefs.getString("note","").contains("A saved writing reply"),"Reply saved to Notepad");check(((EditText)description(root,"Notepad text")).getText().toString().contains("A saved writing reply"),"Open Notepad updated");});shot("ten-nota");
  ui(()->{home.open("@settings");TenSettings settings=find(root,TenSettings.class);check(settings!=null,"Settings opens");settings.navigate("personalization");check(text(settings,"Background")!=null,"Background choice");});shot("ten-settings");
  ui(()->{home.open("@files");check(description(root,"File Explorer ribbon")!=null,"Explorer ribbon present");});shot("ten-explorer");
  ui(()->home.open("@taskview"));checkUi(()->frontOverlay("Task View"),"Task View above floating windows");shot("ten-task-view");
  ui(()->{View button=description(root,"Action Center");check(button!=null&&button.isShown(),"Taskbar Action Center button");button.performClick();});checkUi(()->frontOverlay("Ten Action Center"),"Action Center above floating windows");shot("ten-action-center");
  ui(()->{home.open("@search");StartMenu start=find(root,StartMenu.class);start.hide();start.show(false);check(description(start,"Nth Nota, Medium Start tile")!=null,"Pinned Nota tile");store.prefs.edit().putString("ten_size_@nota","Wide").commit();start.show(false);check(description(start,"Nth Nota, Wide Start tile")!=null,"Wide tile persisted");});shot("ten-start-portrait");
  ui(()->{try{DesktopBackup.Plan plan=DesktopBackup.parse(DesktopBackup.snapshot(store).toString());check(plan.values.get("ten_size_@nota").equals("Wide"),"Tile size backup roundtrip");}catch(Exception e){throw new RuntimeException(e);}home.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);});Thread.sleep(1500);waitForIdleSync();ui(()->home.open("@search"));shot("ten-start-landscape");
  result.putString("stream","PASS: Ten identity, Start search/tiles, Nota opt-in and save, Settings, Explorer ribbon, Task View, Action Center, backup, portrait and landscape\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){try{shot("ten-failure");}catch(Exception ignored){}result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}}
 boolean frontOverlay(String name){View v=description(root,name);if(v==null||!v.isShown())return false;float z=0;for(View current=v;current!=null;current=current.getParent() instanceof View?(View)current.getParent():null)z+=current.getElevation();return z>Aero.dp(home,16);}
 interface Check {boolean get();}
 void checkUi(Check c,String message)throws Exception{ui(()->check(c.get(),message));}
 void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 void ui(Runnable run)throws Exception{FutureTask<Void> task=new FutureTask<>(run,null);home.runOnUiThread(task);task.get(15,TimeUnit.SECONDS);}
 void shot(String name)throws Exception{waitForIdleSync();SystemClock.sleep(250);Bitmap b=getUiAutomation().takeScreenshot();if(b!=null){try(OutputStream out=new FileOutputStream(new File(getTargetContext().getFilesDir(),name+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}finally{b.recycle();}}}
 static <T extends View>T find(View v,Class<T> type){if(type.isInstance(v))return type.cast(v);if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){T result=find(((ViewGroup)v).getChildAt(i),type);if(result!=null)return result;}return null;}
 static View description(View v,String s){if(s.contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View r=description(((ViewGroup)v).getChildAt(i),s);if(r!=null)return r;}return null;}
 static View text(View v,String s){if(v instanceof TextView&&s.contentEquals(((TextView)v).getText()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View r=text(((ViewGroup)v).getChildAt(i),s);if(r!=null)return r;}return null;}
}
