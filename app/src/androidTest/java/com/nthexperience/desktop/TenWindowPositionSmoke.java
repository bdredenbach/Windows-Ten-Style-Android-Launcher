package com.nthexperience.desktop;

import android.app.Activity;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Finger-driven movement, live work-area changes and desktop icon stability. */
public final class TenWindowPositionSmoke extends TenThemeSmoke {
 FrameLayout workspace;DesktopWindow window;long down;
 @Override public void onStart(){Bundle result=new Bundle();try{
  store=new DesktopStore(getTargetContext());store.prefs.edit().clear().putInt("startup_mode",2).putBoolean("welcome_on_start",false).putBoolean("reduce_motion",true).putBoolean("taskbar_apps",false).putBoolean("ten_snap",false).commit();
  getTargetContext().getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();
  getTargetContext().getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
  home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(getTargetContext(),MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();
  await(()->TenSmoke.description(root,"Start menu")!=null,"Desktop ready");
  for(TenThemes.Preset theme:Arrays.asList(TenThemes.TEN.get(0),TenThemes.TEN.get(1),TenThemes.AERO.get(0)))for(int orientation:new int[]{ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE}){
   boolean portrait=orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;String label=(theme.aero?"aero":theme.light?"light":"dark")+"-"+(portrait?"portrait":"landscape");
   ui(()->{for(DesktopWindow w:new ArrayList<>(windows().values()))w.closeWindow();theme.apply(store);store.prefs.edit().putBoolean("ten_taskbar_extended",false).putBoolean("ten_snap",false).putInt("desktop_rows_portrait",8).putInt("desktop_rows_landscape",3).commit();store.desktop(Arrays.asList("@computer","@recycle","@notes","@calendar","@photos","@settings","@browser","@weather","@taskmanager","@files"));home.applyTenAppearance();home.setRequestedOrientation(orientation);});
   await(()->portrait?root.getHeight()>root.getWidth():root.getWidth()>root.getHeight(),"Orientation "+label);settle();
   ui(()->{home.open("setting:page:taskbar");window=windows().get("@legacycontrol");workspace=(FrameLayout)window.getParent();window.arrange(dp(16),workspace.getHeight()-dp(210),Math.min(dp(310),workspace.getWidth()-dp(24)),dp(190));});settle();
   Rect before=bounds();dragUp(dp(portrait?140:45));Rect moved=bounds();
   check(moved.top<before.top-dp(portrait?110:25),"Finger actually moves caption upward: "+label);settle();check(moved.equals(bounds()),"Released window keeps dragged position: "+label);
   ui(()->{root.requestApplyInsets();workspace.requestLayout();});settle();check(moved.equals(bounds()),"Repeated inset/layout pass keeps released position: "+label);
   Map<String,Rect> icons=icons();
   toggle(true);check(moved.equals(bounds()),"Extended taskbar keeps floating window at the same pixels: "+label);checkFittedIcons(icons,label);
   toggle(false);check(moved.equals(bounds()),"Normal taskbar keeps floating window position: "+label);check(icons.equals(icons()),"Normal taskbar keeps desktop icon positions and sizes: "+label);
   // Change the work area while a finger is still down, as an inset update can do.
   Thread.sleep(350);Rect title=titleBounds();down=SystemClock.uptimeMillis();input(MotionEvent.ACTION_DOWN,title.centerX(),title.centerY());input(MotionEvent.ACTION_MOVE,title.centerX(),title.centerY()-dp(25));Thread.sleep(40);input(MotionEvent.ACTION_MOVE,title.centerX(),title.centerY()-dp(25));Thread.sleep(40);input(MotionEvent.ACTION_MOVE,title.centerX(),title.centerY()-dp(25));settle();Rect live=bounds();trace("Before resize");toggle(true);trace("After resize");check(live.equals(bounds()),"Work-area change cannot reset an active caption drag: "+label);input(MotionEvent.ACTION_UP,title.centerX(),title.centerY()-dp(25));settle();trace("After release");check(live.equals(bounds()),"Release after a work-area change keeps the moved bounds: "+label+" expected="+live+" actual="+bounds());
   toggle(false);Rect saved=bounds();ui(()->{window.minimize();window.restore();window.maximize();});settle();check(bounds().height()==workspace.getHeight(),"Maximized window fits normal taskbar: "+label);toggle(true);check(bounds().height()==workspace.getHeight(),"Maximized window fits extended taskbar: "+label);ui(window::restoreDown);settle();check(saved.equals(bounds()),"Restore returns to saved floating position: "+label);
   shot("ten24-window-"+label);
  }
  result.putString("stream","PASS: physical upward caption drags and release; repeated layouts/insets; window and custom-row desktop icon stability across real extended taskbar controls; active drag survives workspace resize; minimize/restore and maximize/restore in Ten dark/light/Aero, portrait/landscape.\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable error){try{shot("ten24-window-failure");}catch(Exception ignored){}result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);}}
 @Override void shot(String name)throws Exception{settle();android.graphics.Bitmap[] image={null};ui(()->{image[0]=android.graphics.Bitmap.createBitmap(root.getWidth(),root.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);root.draw(new android.graphics.Canvas(image[0]));});try(java.io.OutputStream out=new java.io.FileOutputStream(new java.io.File(getTargetContext().getFilesDir(),name+".png"))){image[0].compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}finally{image[0].recycle();}}
 void trace(String label)throws Exception{ui(()->{String state=label+" x="+window.getX()+" y="+window.getY()+" geometry="+window.geometry();for(String field:new String[]{"captionTracking","captionDragging","gripX","gripY","downX","downY","dragHeight","floatingTop"})try{java.lang.reflect.Field f=DesktopWindow.class.getDeclaredField(field);f.setAccessible(true);state+=" "+field+"="+f.get(window);}catch(Exception e){throw new RuntimeException(e);}Bundle status=new Bundle();status.putString("stream",state+"\n");sendStatus(1,status);});}
 void checkFittedIcons(Map<String,Rect> before,String label)throws Exception{Map<String,Rect> after=icons();check(before.keySet().equals(after.keySet()),"Same desktop shortcuts: "+label);for(String id:before.keySet()){Rect a=before.get(id),b=after.get(id);check(a.left==b.left&&a.right==b.right&&b.top<=a.top&&b.height()<=a.height(),"Desktop rows fit vertically without changing columns: "+id+" "+label);}}
 void toggle(boolean extended)throws Exception{ui(()->{CheckBox box=(CheckBox)TenSmoke.text(window.body,"Extended taskbar");check(box!=null&&box.isChecked()!=extended,"Taskbar control ready");box.performClick();});settle();}
 void dragUp(int distance)throws Exception{Thread.sleep(350);Rect title=titleBounds();down=SystemClock.uptimeMillis();input(MotionEvent.ACTION_DOWN,title.centerX(),title.centerY());for(int i=1;i<=8;i++){Thread.sleep(20);input(MotionEvent.ACTION_MOVE,title.centerX(),title.centerY()-distance*i/8);}input(MotionEvent.ACTION_UP,title.centerX(),title.centerY()-distance);settle();}
 void input(int action,float x,float y){MotionEvent e=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);sendPointerSync(e);e.recycle();}
 Rect titleBounds()throws Exception{Rect[] r={null};ui(()->r[0]=screen(TenSmoke.description(window,window.caption()+". Drag to move window.")));return r[0];}
 Rect bounds()throws Exception{Rect[] r={null};ui(()->r[0]=screen(window));return r[0];}
 Map<String,Rect> icons()throws Exception{Map<String,Rect> out=new LinkedHashMap<>();ui(()->{DesktopIconGrid grid=TenSmoke.find(root,DesktopIconGrid.class);for(int i=0;i<grid.getChildCount();i++){View v=grid.getChildAt(i);out.put(String.valueOf(v.getTag()),new Rect(v.getLeft(),v.getTop(),v.getRight(),v.getBottom()));}});return out;}
 Rect screen(View view){int[] at=new int[2];view.getLocationOnScreen(at);return new Rect(at[0],at[1],at[0]+view.getWidth(),at[1]+view.getHeight());}
 int dp(float value){return Aero.dp(home,value);}
 void settle()throws Exception{waitForIdleSync();Thread.sleep(250);waitForIdleSync();}
}
