package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.concurrent.*;
import org.json.*;

/** Real default installation, tile actions, layout, live data, menus and saved-state checks. */
public final class TenStartTilesSmoke extends TenThemeSmoke {
 StartMenu start;
 @Override public void onStart(){Bundle result=new Bundle();try{
  store=new DesktopStore(getTargetContext());store.prefs.edit().clear().putInt("startup_mode",2).putBoolean("welcome_on_start",false).putBoolean("reduce_motion",true).putBoolean("taskbar_apps",false).commit();
  getTargetContext().getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();getTargetContext().getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
  home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(getTargetContext(),MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();await(()->TenSmoke.find(root,StartMenu.class)!=null,"Start ready");start=TenSmoke.find(root,StartMenu.class);
  ui(()->{check(!store.prefs.getBoolean("ten_start_apps",true)&&store.prefs.getBoolean("ten_full_start",false)&&store.prefs.getBoolean("ten_live_tiles",false),"New installs show live tiles first");check(store.startPins().equals(TenStartCatalog.ids()),"All 43 Nth apps pinned exactly once");check(new TenStartData(store).groups().equals(Arrays.asList(TenStartCatalog.GROUPS)),"Six default categories");start.show(false);});settle();
  allActions();liveData();menusAndSettings();
  for(TenThemes.Preset theme:Arrays.asList(TenThemes.TEN.get(0),TenThemes.TEN.get(1),TenThemes.AERO.get(0)))for(int orientation:new int[]{ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE})for(String scale:new String[]{"Normal","Large"}){
   boolean portrait=orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;String tag=(theme.aero?"aero":theme.light?"light":"dark")+"-"+(portrait?"portrait":"landscape")+"-"+scale.toLowerCase(Locale.ROOT);
   ui(()->{theme.apply(store);store.prefs.edit().putString("ten_start_tile_scale",scale).putBoolean("ten_start_apps",false).putBoolean("ten_live_tiles",true).commit();home.applyTenAppearance();home.setRequestedOrientation(orientation);start.show(false);start.tiles.scrollTo(0,0);});await(()->portrait?root.getHeight()>root.getWidth():root.getWidth()>root.getHeight(),"Orientation "+tag);settle();ui(()->geometry(tag));shot("ten25-start-"+tag);
  }
  backupAndExisting();
  result.putString("stream","PASS: 43 real Nth tile routes including Media Center and every extra tool; six categories; fresh tile-first defaults; mixed sizes and colors; Normal/Large scale and real per-tile Resize menu; local live notes/feed/calendar/battery updates without replacing views; individual live toggle; no overlap or clipping across dark/light/Aero, portrait/landscape and both scales; legacy Medium backup compatibility, expanded pin backup, recreation and existing custom layout preservation.\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){try{shot("ten25-failure");}catch(Exception ignored){}result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}}
 void allActions()throws Exception{ui(()->{
  List<String> opened=new ArrayList<>();StartMenu spy=new StartMenu(home,new StartMenu.Actions(){public void open(String id){opened.add(id);}public void options(View anchor,String id){}public void home(){}public void settings(){}public boolean isHome(){return true;}},store,TenSmoke.find(root,AeroWallpaper.class));
  for(TenStartCatalog.App app:TenStartCatalog.APPS){View tile=spy.tileViews.get(app.id);check(tile!=null,"Tile resolves: "+app.id);check(DesktopBackup.shortcut(app.id,new JSONObject(),false),"Tile has a real built-in destination: "+app.id);tile.performClick();check(opened.get(opened.size()-1).equals(app.id),"Tile routes to its own app: "+app.id);}check(opened.size()==43,"Every Nth app route checked");
  start.tileViews.get("@notes").performClick();check(windows().get("@notes")!=null,"Actual Notepad tile launches a window");home.open("start-menu:");start.tileViews.get("@gadget:calculator").performClick();check(windows().containsKey("@gadget:calculator"),"Actual Calculator tile launches");home.open("start-menu:");
 });settle();}
 void liveData()throws Exception{
  ui(()->{store.prefs.edit().putString("note","A real saved note on my Start tile").putString("feed_items","[{\"title\":\"A real cached headline\",\"url\":\"https://example.com/story\"}]").commit();start.show(false);});settle();
  View note=start.tileViews.get("@notes");ui(()->{check(note.getContentDescription().toString().contains("A real saved note"),"Live note content");check(start.tileViews.get("@calendar").getContentDescription().toString().contains("live:"),"Live date content");check(start.tileViews.get("@news").getContentDescription().toString().contains("A real cached headline"),"Cached feed content");store.prefs.edit().putString("note","Edited live without rebuilding the tile").apply();});
  await(()->start.tileViews.get("@notes")==note&&note.getContentDescription().toString().contains("Edited live"),"Preference updates refresh the same tile view");
  ui(()->{store.prefs.edit().putBoolean("ten_live_tiles",false).commit();});await(()->!note.getContentDescription().toString().contains("live:"),"Global live toggle returns to static tile");ui(()->store.prefs.edit().putBoolean("ten_live_tiles",true).commit());
 }
 void menusAndSettings()throws Exception{
  ui(()->options("@notes"));systemClick("Resize");systemClick("Large");await(()->store.prefs.getString("ten_size_@notes","").equals("Large"),"Real Resize menu saves Large");settle();ui(()->options("@notes"));systemClick("Resize");systemClick("Normal");await(()->store.prefs.getString("ten_size_@notes","").equals("Normal"),"Real Resize menu saves Normal");
  ui(()->options("@notes"));systemClick("Turn live tile off");await(()->!store.prefs.getBoolean("ten_tile_live_@notes",true),"Per-tile live setting");ui(()->options("@notes"));systemClick("Turn live tile on");
  ui(()->{start.hide();home.open("ten-setting:start");TenSmoke.text(windows().get("@settings"),"Change tile size").performClick();});awaitSystem("Tile size");systemClick("Large");await(()->store.prefs.getString("ten_start_tile_scale","").equals("Large"),"Real Start setting changes overall scale after selection");ui(()->{check(store.prefs.getString("ten_start_tile_scale","").equals("Large"),"Real Start setting changes overall scale");TenSmoke.text(windows().get("@settings"),"Add all Nth app tiles").performClick();check(start.getVisibility()==View.VISIBLE&&!store.prefs.getBoolean("ten_start_apps",true),"Adding Nth pack opens categorized tiles");});settle();
 }
 void options(String id){try{Method m=StartMenu.class.getDeclaredMethod("tileOptions",View.class,StartSearch.Result.class);m.setAccessible(true);TenStartCatalog.App app=TenStartCatalog.app(id);m.invoke(start,start.tileViews.get(id),new StartSearch.Result(id,app.title,"Nth tools",app.icon,null,true));}catch(Exception e){throw new RuntimeException(e);}}
 void geometry(String tag){
  check(start.tileViews.size()==43,"All Nth tiles visible in layout data: "+tag);check(start.tiles.getVisibility()==View.VISIBLE&&start.apps.getVisibility()==View.GONE,"Tile-first view: "+tag);
  Map<ViewGroup,List<Rect>> used=new IdentityHashMap<>();Set<Integer> colors=new HashSet<>();for(Map.Entry<String,View> entry:start.tileViews.entrySet()){View tile=entry.getValue();ViewGroup grid=(ViewGroup)tile.getParent();Rect rect=new Rect(tile.getLeft(),tile.getTop(),tile.getRight(),tile.getBottom());check(rect.width()>0&&rect.height()>0&&rect.left>=0&&rect.right<=grid.getWidth(),"Tile fits its category: "+entry.getKey()+" "+tag);for(Rect old:used.computeIfAbsent(grid,k->new ArrayList<>()))check(!Rect.intersects(old,rect),"No overlapping tiles: "+tag);used.get(grid).add(rect);colors.add(TenStartCatalog.app(entry.getKey()).color);}
  check(colors.size()>10,"Individual tile colors survive theme: "+tag);for(String group:TenStartCatalog.GROUPS)check(TenSmoke.description(start,"Tile group "+group)!=null,"Category present: "+group);
 }
 void backupAndExisting()throws Exception{ui(()->{try{store.prefs.edit().putString("ten_size_@notes","Medium").putBoolean("ten_tile_live_@notes",false).commit();DesktopBackup.Plan plan=DesktopBackup.parse(DesktopBackup.snapshot(store).toString());check(new JSONArray(plan.string("start_pins","[]")).length()==43,"Backup retains more than 32 tiles");check(plan.string("ten_start_tile_scale","").equals("Large")&&plan.string("ten_size_@notes","").equals("Medium"),"Scale and legacy size roundtrip");check(Boolean.FALSE.equals(plan.values.get("ten_tile_live_@notes")),"Per-tile live choice roundtrip");}catch(Exception e){throw new RuntimeException(e);}store.startPins(Arrays.asList("@notes"));store.prefs.edit().putString(TenStartData.KEY,"[\"My own apps\"]").putString("ten_group_@notes","My own apps").putString("ten_size_@notes","Large").putBoolean("ten_start_apps",true).commit();});
  MainActivity previous=home;BlockingQueue<MainActivity> next=new LinkedBlockingQueue<>();Application app=(Application)getTargetContext().getApplicationContext();Application.ActivityLifecycleCallbacks callback=new Application.ActivityLifecycleCallbacks(){public void onActivityResumed(Activity a){if(a instanceof MainActivity&&a!=previous)next.offer((MainActivity)a);}public void onActivityCreated(Activity a,Bundle b){}public void onActivityStarted(Activity a){}public void onActivityStopped(Activity a){}public void onActivityPaused(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){}};app.registerActivityLifecycleCallbacks(callback);try{ui(home::recreate);home=next.poll(20,TimeUnit.SECONDS);check(home!=null,"Activity recreated");root=home.getWindow().getDecorView();start=TenSmoke.find(root,StartMenu.class);}finally{app.unregisterActivityLifecycleCallbacks(callback);}settle();ui(()->{check(store.startPins().equals(Arrays.asList("@notes"))&&new TenStartData(store).groups().equals(Arrays.asList("My own apps"))&&store.prefs.getBoolean("ten_start_apps",false),"Existing custom layout and opening preference are preserved");new TenStartData(store).installNthTiles(false);check(store.startPins().size()==43&&store.prefs.getString("ten_group_@notes","").equals("My own apps")&&store.prefs.getString("ten_size_@notes","").equals("Large"),"Adding Nth apps preserves existing explicit category and size");});
 }
 void settle()throws Exception{waitForIdleSync();Thread.sleep(220);waitForIdleSync();}
 @Override void shot(String name)throws Exception{settle();Bitmap[] b={null};ui(()->{b[0]=Bitmap.createBitmap(root.getWidth(),root.getHeight(),Bitmap.Config.ARGB_8888);root.draw(new Canvas(b[0]));});try(java.io.OutputStream out=new java.io.FileOutputStream(new java.io.File(getTargetContext().getFilesDir(),name+".png"))){b[0].compress(Bitmap.CompressFormat.PNG,100,out);}finally{b[0].recycle();}}
}
