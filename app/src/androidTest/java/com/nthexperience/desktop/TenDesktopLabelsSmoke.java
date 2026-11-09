package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import org.json.*;

/** Dense, saved desktop layouts keep complete label bounds above both taskbar sizes. */
public final class TenDesktopLabelsSmoke extends TenThemeSmoke {
 DesktopIconGrid grid;List<String> folders=new ArrayList<>();
 @Override public void onStart(){Bundle result=new Bundle();try{
  store=new DesktopStore(getTargetContext());store.prefs.edit().clear().putInt("startup_mode",2).putBoolean("welcome_on_start",false).putBoolean("reduce_motion",true).putBoolean("taskbar_apps",false).putBoolean("desktop_labels",true).putBoolean("snap_desktop",true).putInt("desktop_columns_portrait",5).putInt("desktop_rows_portrait",8).putInt("desktop_columns_landscape",8).putInt("desktop_rows_landscape",3).commit();
  getTargetContext().getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();getTargetContext().getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        ShellTestSetup.complete(getTargetContext());
  home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(getTargetContext(),MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();await(()->TenSmoke.description(root,"Start menu")!=null,"Desktop ready");
  ui(()->{for(int i=0;i<24;i++)folders.add(store.createFolder(String.format(Locale.ROOT,"Folder %02d with a long label",i+1),Collections.emptyList()));store.desktop(folders);home.refreshTen();});settle();
  for(TenThemes.Preset theme:Arrays.asList(TenThemes.TEN.get(0),TenThemes.TEN.get(1),TenThemes.AERO.get(0)))for(int orientation:new int[]{ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE}){
   boolean portrait=orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;String axis=portrait?"portrait":"landscape",tag=(theme.aero?"aero":theme.light?"light":"dark")+"-"+axis;
   ui(()->{theme.apply(store);store.prefs.edit().putBoolean("ten_taskbar_extended",false).putBoolean("snap_desktop",true).commit();TenDesktopItems.positions(store,"portrait",new JSONObject());TenDesktopItems.positions(store,"landscape",new JSONObject());home.applyTenAppearance();home.setRequestedOrientation(orientation);});await(()->portrait?root.getHeight()>root.getWidth():root.getWidth()>root.getHeight(),"Orientation "+tag);settle();
   ui(()->{grid=TenSmoke.find(root,DesktopIconGrid.class);View first=grid.getChildAt(0);grid.place(String.valueOf(first.getTag()),first.getLeft(),first.getTop());});settle();Map<String,Rect> normal=positions();String saved=TenDesktopItems.positions(store,axis).toString();
   for(int pass=0;pass<3;pass++){
    toggle(true);ui(()->labels(tag));check(saved.equals(TenDesktopItems.positions(store,axis).toString()),"Taskbar changes do not rewrite saved icon positions: "+tag);
    Map<String,Rect> fitted=positions();for(String id:normal.keySet()){Rect a=normal.get(id),b=fitted.get(id);check(a.left==b.left&&a.right==b.right&&b.top<=a.top&&b.height()<=a.height(),"Columns and row order preserved: "+tag);}
    if(pass==0)shot("ten26-labels-"+tag);
    toggle(false);check(normal.equals(positions()),"Normal taskbar restores the exact original grid: "+tag);ui(()->labels("normal-"+tag));
   }
   toggle(true);Map<String,Rect> before=positions();String first=folders.get(0),last=folders.get(folders.size()-1);ui(()->grid.swapPositions(first,last));settle();Map<String,Rect> swapped=positions();check(swapped.get(first).equals(before.get(last))&&swapped.get(last).equals(before.get(first)),"Dragging/swapping in the fitted grid uses the correct saved rows: "+tag);String moved=TenDesktopItems.positions(store,axis).toString();toggle(false);toggle(true);check(swapped.equals(positions())&&moved.equals(TenDesktopItems.positions(store,axis).toString()),"Moved positions survive both taskbar sizes without drift: "+tag);ui(()->labels("moved-"+tag));
  }
  freePlacement();result.putString("stream","PASS: 24 saved folders with two-line labels fit above the unchanged 48/96dp taskbar; five columns/eight rows in portrait and eight columns/three rows in landscape across Ten dark/light/Aero; repeated toggles preserve canonical coordinates, columns and row order and restore exact normal bounds; fitted-grid swap/drag conversion and free placement roundtrip without drift.\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){try{shot("ten26-failure");}catch(Exception ignored){}result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}}
 void labels(String tag){
  Rect bar=screen(TenSmoke.description(root,"Desktop taskbar"));boolean extended=store.prefs.getBoolean("ten_taskbar_extended",false);check(bar.height()==Aero.dp(home,extended?96:48),"Taskbar keeps comfortable row heights: "+tag);
  List<Rect> cells=new ArrayList<>();for(int i=0;i<grid.getChildCount();i++){LinearLayout cell=(LinearLayout)grid.getChildAt(i);Rect bounds=screen(cell);for(Rect old:cells)check(!Rect.intersects(old,bounds),"Fitted cells do not overlap: "+tag);cells.add(bounds);TextView label=(TextView)cell.getChildAt(1);Rect full=screen(label),visible=new Rect();check(label.getGlobalVisibleRect(visible)&&full.equals(visible),"Entire label view is visible: "+label.getText()+" "+tag+" full="+full+" visible="+visible);check(full.bottom<=bar.top-Aero.dp(home,4)&&full.bottom<=bounds.bottom,"Complete label stays clear of taskbar and cell edge: "+tag);}
 }
 void freePlacement()throws Exception{
  ui(()->{store.prefs.edit().putBoolean("snap_desktop",false).commit();home.refreshTen();});settle();String id=folders.get(0);ui(()->{View cell=grid.getChildAt(0);grid.place(id,cell.getLeft(),grid.viewportHeight()-grid.cellHeight()-Aero.dp(home,2));});settle();Rect placed=positions().get(id);String axis=home.getResources().getConfiguration().orientation==android.content.res.Configuration.ORIENTATION_PORTRAIT?"portrait":"landscape",saved=TenDesktopItems.positions(store,axis).toString();toggle(false);toggle(true);Rect restored=positions().get(id);check(Math.abs(placed.top-restored.top)<=1&&placed.left==restored.left&&saved.equals(TenDesktopItems.positions(store,axis).toString()),"Free placement is converted once and restored without drift");
 }
 void toggle(boolean value)throws Exception{ui(()->{home.open("ten-setting:taskbar");Switch control=(Switch)TenSmoke.text(windows().get("@settings"),"Extended taskbar");check(control.isChecked()!=value,"Taskbar setting ready");control.performClick();windows().get("@settings").minimize();});settle();}
 Map<String,Rect> positions()throws Exception{Map<String,Rect> result=new LinkedHashMap<>();ui(()->{for(int i=0;i<grid.getChildCount();i++){View v=grid.getChildAt(i);result.put(String.valueOf(v.getTag()),new Rect(v.getLeft(),v.getTop(),v.getRight(),v.getBottom()));}});return result;}
 Rect screen(View v){int[] p=new int[2];v.getLocationOnScreen(p);return new Rect(p[0],p[1],p[0]+v.getWidth(),p[1]+v.getHeight());}
 void settle()throws Exception{waitForIdleSync();Thread.sleep(200);waitForIdleSync();}
 @Override void shot(String name)throws Exception{settle();Bitmap[] image={null};ui(()->{image[0]=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);root.draw(new Canvas(image[0]));});try(java.io.OutputStream out=new java.io.FileOutputStream(new java.io.File(getTargetContext().getFilesDir(),name+".png"))){image[0].compress(Bitmap.CompressFormat.PNG,100,out);}finally{image[0].recycle();}}
}
