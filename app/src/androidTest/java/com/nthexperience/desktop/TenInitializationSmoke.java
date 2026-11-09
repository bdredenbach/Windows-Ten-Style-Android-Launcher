package com.nthexperience.desktop;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import java.util.*;
import java.util.concurrent.*;

/** Native optional setup, coarse-only location and Android-owned uninstall confirmation. */
public final class TenInitializationSmoke extends TenThemeSmoke {
 private boolean complete;
 @Override public void onCreate(Bundle args){complete=args!=null&&"complete".equals(args.getString("phase"));super.onCreate(args);}
 @Override public void onStart(){Bundle result=new Bundle();try{
  store=new DesktopStore(getTargetContext());store.prefs.edit().clear().putInt("startup_mode",2).putBoolean("welcome_on_start",false).putBoolean("reduce_motion",true).putBoolean("taskbar_apps",false).putString("weather_city","Saved city").commit();
  getTargetContext().getSharedPreferences(InitializationState.PREFS,0).edit().clear().commit();
  getTargetContext().getSharedPreferences(StorageAccess.PREFS,0).edit().clear().commit();
  getTargetContext().getSharedPreferences(StartupExperience.SESSION,0).edit().clear().putBoolean("seen",true).putBoolean("welcome_seen",false).commit();
  home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(getTargetContext(),MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();
  for(String title:new String[]{"Default Launcher","Set up Nth Explorer","Set up Usage Access","Allow Notifications Access","Modify System Settings (Brightness)"}){
   awaitSystem(title);
   check(TenSmoke.find(root,WelcomeCenter.class)!=null,"Classic Welcome Center remains behind setup");
   if(title.equals("Set up Nth Explorer"))shot("ten28-file-guide-portrait");
   if(title.equals("Set up Usage Access")){
    shot("ten28-usage-guide-portrait");
    if(Build.VERSION.SDK_INT>=33){check(node(getUiAutomation().getRootInActiveWindow(),"Open App info")!=null,"Illustrated guide exposes App info recovery");}
    ui(()->home.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));await(()->root.getWidth()>root.getHeight(),"Usage guide landscape");awaitSystem(title);shot("ten28-usage-guide-landscape");
    ui(()->home.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));await(()->root.getHeight()>root.getWidth(),"Usage guide portrait");awaitSystem(title);
   }
   if(title.equals("Set up Nth Explorer")){recreateHome();awaitSystem(title);}
   systemClick("Later");
  }
  awaitSystem("Allow Location");shot("ten28-location-guide");
  if(complete){
   systemClick(Build.VERSION.SDK_INT>=31?"Choose location access":"Use approximate location");
   if(Build.VERSION.SDK_INT>=31)systemClick("Approximate");
   systemClick("While using the app");
  }else systemClick("Later");
  await(()->new InitializationState(home).step()==InitializationState.Step.DONE,"Optional setup completes");
  check(!StorageAccess.has(home)&&!ExternalTasks.permitted(home),"Declines leave optional access off");
  check(store.prefs.getString("weather_city","").equals("Saved city"),"Setup keeps the saved weather city");
  if(complete){check(LocationAccess.permitted(home)&&!LocationAccess.precise(home),"Android approximate-only grant is accepted");}
  recreateHome();await(()->home.hasWindowFocus(),"Focus after recreation");
  check(new InitializationState(home).step()==InitializationState.Step.DONE,"Completed setup does not replay after recreation");
  if(complete)uninstall();
  result.putString("stream","PASS: classic Welcome Center and illustrated scrollable guides; native ordered setup; Later leaves optional access off; in-progress recreation; completion survives recreation; saved city retained"+(complete?"; Android approximate-only grant; real Start tile uninstall menu, cancellation preserves pins, confirmation removes only the disposable probe and its pin":"")+"\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable error){try{shot("ten27-setup-failure");}catch(Exception ignored){}result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);}}
 private void recreateHome()throws Exception{
  MainActivity previous=home;BlockingQueue<MainActivity> next=new LinkedBlockingQueue<>();Application app=(Application)getTargetContext().getApplicationContext();
  Application.ActivityLifecycleCallbacks callback=new Application.ActivityLifecycleCallbacks(){public void onActivityResumed(Activity a){if(a instanceof MainActivity&&a!=previous)next.offer((MainActivity)a);}public void onActivityCreated(Activity a,Bundle b){}public void onActivityStarted(Activity a){}public void onActivityStopped(Activity a){}public void onActivityPaused(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};
  app.registerActivityLifecycleCallbacks(callback);try{ui(home::recreate);home=next.poll(20,TimeUnit.SECONDS);check(home!=null,"Home recreated");root=home.getWindow().getDecorView();}finally{app.unregisterActivityLifecycleCallbacks(callback);}
 }
 @Override android.view.accessibility.AccessibilityNodeInfo node(android.view.accessibility.AccessibilityNodeInfo current,String text){
  if(current==null)return null;
  if((text.equals("OK")&&"Uninstall".equalsIgnoreCase(String.valueOf(current.getText())))||text.equalsIgnoreCase(String.valueOf(current.getText()))||text.equalsIgnoreCase(String.valueOf(current.getContentDescription())))return current;
  for(int i=0;i<current.getChildCount();i++){android.view.accessibility.AccessibilityNodeInfo found=node(current.getChild(i),text);if(found!=null)return found;}return null;
 }
 @Override void systemClick(String label)throws Exception{
  android.view.accessibility.AccessibilityNodeInfo item=awaitSystem(label);
  android.graphics.Rect bounds=new android.graphics.Rect();item.getBoundsInScreen(bounds);
  for(int n=0;item!=null&&n<6;n++,item=item.getParent())if(item.isClickable()&&item.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)){waitForIdleSync();Thread.sleep(180);return;}
  long now=SystemClock.uptimeMillis();MotionEvent down=MotionEvent.obtain(now,now,0,bounds.centerX(),bounds.centerY(),0),up=MotionEvent.obtain(now,now+60,1,bounds.centerX(),bounds.centerY(),0);
  down.setSource(InputDevice.SOURCE_TOUCHSCREEN);up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
  try{check(getUiAutomation().injectInputEvent(down,true)&&getUiAutomation().injectInputEvent(up,true),"Click system control: "+label);}finally{down.recycle();up.recycle();}waitForIdleSync();Thread.sleep(180);
 }
 private boolean installed(String pkg){try{home.getPackageManager().getApplicationInfo(pkg,0);return true;}catch(PackageManager.NameNotFoundException missing){return false;}}
 private void uninstall()throws Exception{
  String pkg="com.nthexperience.desktop.fixture",id=new ComponentName(pkg,pkg+".TaskActivity").flattenToString();
  check(installed(pkg),"Disposable uninstall probe installed");
  List<AppCatalog.Entry> catalog=AppCatalog.load(home);check(catalog.stream().anyMatch(app->app.id.equals(id)),"Fixture has a real launcher catalog entry");
  for(boolean confirm:new boolean[]{false,true}){
   ui(()->{
    store.startPins(Collections.singletonList(id));store.prefs.edit().putBoolean("ten_start_apps",false).commit();
    StartMenu start=TenSmoke.find(root,StartMenu.class);start.entries(catalog);start.show(false);
   });waitForIdleSync();await(()->TenSmoke.find(root,StartMenu.class).tileViews.containsKey(id),"Installed fixture tile available after catalog refresh");
   ui(()->{View tile=TenSmoke.find(root,StartMenu.class).tileViews.get(id);check(tile!=null,"Real installed-app tile rendered");check(tile.performLongClick(),"Tile hold opens its context menu");});
   systemClick("Uninstall");awaitSystem("Cancel");
   if(!confirm){systemClick("Cancel");await(()->home.hasWindowFocus(),"Canceled uninstall returns to Home");check(installed(pkg)&&store.startPins().contains(id),"Cancellation keeps app and pin");}
   else{systemClick("OK");await(()->!installed(pkg),"Android removes the disposable probe");await(()->!store.startPins().contains(id),"Confirmed removal prunes its Start pin");}
  }
 }
}
