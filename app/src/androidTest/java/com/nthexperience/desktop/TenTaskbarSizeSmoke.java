package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/** Real controls, row geometry, saved state and fresh-install defaults. */
public final class TenTaskbarSizeSmoke extends Instrumentation {
 MainActivity home;View root;DesktopStore store;
 @Override public void onCreate(Bundle args){super.onCreate(args);start();}
 @Override public void onStart(){Bundle result=new Bundle();try{
  store=new DesktopStore(getTargetContext());
  store.prefs.edit().clear().putInt("startup_mode",2).putBoolean("welcome_on_start",false).putBoolean("reduce_motion",true).putBoolean("taskbar_apps",false).commit();
  getTargetContext().getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();
  getTargetContext().getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        ShellTestSetup.complete(getTargetContext());
  home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(getTargetContext(),MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();settle();
  ui(()->{
   check(!store.prefs.getBoolean("ten_accent_shell",true)&&!store.prefs.getBoolean("ten_accent_titles",true),"Fresh shell and captions have no accent fill");
   check(TenStyle.accent(home)==Color.WHITE&&TenStyle.text(home)==Color.WHITE&&!TenStyle.light(home),"Fresh neutral shell has white text and selected White swatch");
   home.open("ten-setting:start");TenSettings settings=TenSmoke.find(root,TenSettings.class);
   for(String label:new String[]{"Use Start full screen","Show app list when Start opens","Show live local tile details"})check(((Switch)TenSmoke.text(settings,label)).isChecked()!=label.equals("Show app list when Start opens"),"Fresh Start default: "+label);
   check(!store.prefs.getBoolean("ten_taskbar_extended",false),"Normal taskbar remains initial size");
   home.open("@notes");((EditText)TenSmoke.description(root,"Notepad text")).setText("Keep this note across taskbar changes");
   store.quickLaunch(Arrays.asList("@browser","@files","@notes","@calendar","@photos","@settings","@weather","@clock","@taskmanager","@recycle",MediaLibrary.PLAYER,"@gadget:calculator"));
  });shot("ten22-first-start-defaults");
  for(TenThemes.Preset theme:Arrays.asList(TenThemes.TEN.get(0),TenThemes.TEN.get(1),TenThemes.AERO.get(0)))for(int orientation:new int[]{ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE}){
   boolean portrait=orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;String tag=(theme.aero?"aero":theme.light?"light":"dark")+"-"+(portrait?"portrait":"landscape");
   ui(()->{theme.apply(store);store.prefs.edit().putBoolean("ten_taskbar_extended",false).commit();home.applyTenAppearance();home.setRequestedOrientation(orientation);});
   await(()->portrait?root.getHeight()>root.getWidth():root.getWidth()>root.getHeight(),"Orientation "+tag);settle();
   ui(()->{check(bar().getHeight()==dp(48),"Normal height: "+tag);home.open("ten-setting:taskbar");((Switch)TenSmoke.text(TenSmoke.find(root,TenSettings.class),"Extended taskbar")).performClick();});settle();
   ui(()->{geometry(true,tag);try{check(Boolean.TRUE.equals(DesktopBackup.parse(DesktopBackup.snapshot(store).toString()).values.get("ten_taskbar_extended")),"Backup retains extended size");}catch(Exception e){throw new RuntimeException(e);}check(((EditText)TenSmoke.description(root,"Notepad text")).getText().toString().equals("Keep this note across taskbar changes"),"Taskbar change preserves open document");});
   shot("ten22-extended-"+tag);
   ui(()->{home.open("setting:page:taskbar");VistaControlPanel panel=TenSmoke.find(root,VistaControlPanel.class);check(((CheckBox)TenSmoke.text(panel,"Extended taskbar")).isChecked(),"Control Panel reflects extended size");((CheckBox)TenSmoke.text(panel,"Extended taskbar")).performClick();});settle();ui(()->geometry(false,"Control Panel "+tag));
   ui(()->((CheckBox)TenSmoke.text(TenSmoke.find(root,VistaControlPanel.class),"Extended taskbar")).performClick());settle();ui(()->geometry(true,"Control Panel "+tag));
   ui(()->{store.prefs.edit().putBoolean("taskbar_show_labels",true).commit();home.applyTenAppearance();});settle();
   ui(()->{HorizontalScrollView strip=(HorizontalScrollView)root.findViewWithTag("taskbar-items");strip.scrollTo(10000,0);});settle();
   ui(()->{check(((HorizontalScrollView)root.findViewWithTag("taskbar-items")).getScrollX()>0,"Extended app row scrolls "+tag);check(TenSmoke.description(bar(),"Search").isShown()&&TenSmoke.description(bar(),"Task View").isShown(),"Lower controls remain visible while app row scrolls");});
   ui(()->{store.prefs.edit().putBoolean("taskbar_show_labels",false).commit();home.applyTenAppearance();});settle();ui(()->TenSmoke.description(bar(),"Taskbar controls").performLongClick());menu("Extended taskbar");settle();
   ui(()->{geometry(false,tag);home.open("ten-setting:taskbar");check(!((Switch)TenSmoke.text(TenSmoke.find(root,TenSettings.class),"Extended taskbar")).isChecked(),"Menu choice reflected in Settings");});
   shot("ten22-normal-"+tag);
  }
  ui(()->{store.prefs.edit().putBoolean("ten_taskbar_extended",true).putBoolean("ten_full_start",false).putBoolean("ten_start_apps",false).putBoolean("ten_live_tiles",false).putInt("ten_accent",0xff107c10).putBoolean("ten_accent_titles",true).commit();home.applyTenAppearance();});recreate();
  ui(()->{geometry(true,"relaunch");check(!store.prefs.getBoolean("ten_full_start",true)&&!store.prefs.getBoolean("ten_start_apps",true)&&!store.prefs.getBoolean("ten_live_tiles",true)&&TenStyle.accent(home)==0xff107c10&&store.prefs.getBoolean("ten_accent_titles",false),"Existing user Start and color preferences survive initialization");
   home.open("ten-setting:taskbar");((Switch)TenSmoke.text(TenSmoke.find(root,TenSettings.class),"Extended taskbar")).performClick();});settle();
  ui(()->{geometry(false,"restored");TenThemes.TEN.get(0).apply(store);store.prefs.edit().putBoolean("ten_live_tiles",true).commit();home.applyTenAppearance();});
  result.putString("stream","PASS: fresh color/three Start defaults; normal and doubled taskbar via Settings and hold menu; app scrolling, workspace bounds, backup, document preservation and recreation in Ten dark/light/Aero, portrait/landscape\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){try{shot("ten22-failure");}catch(Exception ignored){}result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}}
 View bar(){return TenSmoke.description(root,"Desktop taskbar");}
 int dp(int value){return Aero.dp(home,value);}
 Rect rect(View v){int[] p=new int[2];v.getLocationOnScreen(p);return new Rect(p[0],p[1],p[0]+v.getWidth(),p[1]+v.getHeight());}
 void geometry(boolean extended,String label){
  check(bar().getHeight()==dp(extended?96:48),"Taskbar exact height "+label);
  Rect bar=rect(bar()),controls=rect(TenSmoke.description(bar(),"Taskbar controls")),apps=rect(root.findViewWithTag("taskbar-items"));
  check(controls.bottom==bar.bottom&&controls.height()==dp(48),"Controls aligned to bottom "+label);
  check(extended?apps.bottom==controls.top&&apps.top==bar.top:apps.top==controls.top,"App row placement "+label);
  for(String command:new String[]{"Start menu","Search","Task View","Device controls","Clock and calendar","Action Center","Show desktop"}){
   Rect r=rect(TenSmoke.description(bar(),command));check(r.width()>0&&r.left>=bar.left&&r.right<=bar.right&&r.top>=controls.top&&r.bottom<=controls.bottom,"Control within lower row: "+command+" "+label);
  }
  check(rect(TenSmoke.description(root,"Desktop workspace")).bottom<=bar.top,"Desktop ends above both taskbar rows "+label);
 }
 void recreate()throws Exception{
  MainActivity previous=home;BlockingQueue<Activity> resumed=new LinkedBlockingQueue<>();Application app=(Application)getTargetContext().getApplicationContext();
  Application.ActivityLifecycleCallbacks callback=new Application.ActivityLifecycleCallbacks(){public void onActivityResumed(Activity a){if(a instanceof MainActivity&&a!=previous)resumed.offer(a);}public void onActivityCreated(Activity a,Bundle b){}public void onActivityStarted(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};
  app.registerActivityLifecycleCallbacks(callback);try{ui(home::recreate);Activity next=resumed.poll(20,TimeUnit.SECONDS);check(next instanceof MainActivity,"Recreated activity resumes");home=(MainActivity)next;root=home.getWindow().getDecorView();}finally{app.unregisterActivityLifecycleCallbacks(callback);}settle();
 }
 void menu(String label)throws Exception{await(()->node(getUiAutomation().getRootInActiveWindow(),label)!=null,"Hold menu item "+label);AccessibilityNodeInfo v=node(getUiAutomation().getRootInActiveWindow(),label);while(v!=null&&!v.isClickable())v=v.getParent();check(v!=null&&v.performAction(AccessibilityNodeInfo.ACTION_CLICK),"Hold-menu toggle works");}
 AccessibilityNodeInfo node(AccessibilityNodeInfo n,String value){if(n==null)return null;if(value.contentEquals(n.getText()==null?"":n.getText()))return n;for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo found=node(n.getChild(i),value);if(found!=null)return found;}return null;}
 interface Condition{boolean get();}
 void await(Condition condition,String message)throws Exception{long end=SystemClock.uptimeMillis()+10000;while(SystemClock.uptimeMillis()<end){if(condition.get())return;SystemClock.sleep(80);}throw new AssertionError(message);}
 void settle(){waitForIdleSync();SystemClock.sleep(350);}
 void ui(Runnable run)throws Exception{FutureTask<Void> task=new FutureTask<>(run,null);home.runOnUiThread(task);task.get(15,TimeUnit.SECONDS);}
 void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 void shot(String name)throws Exception{settle();final Bitmap[] image=new Bitmap[1];ui(()->{image[0]=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);root.draw(new Canvas(image[0]));});Bitmap b=image[0];if(b!=null)try(OutputStream out=new FileOutputStream(new File(getTargetContext().getFilesDir(),name+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}finally{b.recycle();}}
}
