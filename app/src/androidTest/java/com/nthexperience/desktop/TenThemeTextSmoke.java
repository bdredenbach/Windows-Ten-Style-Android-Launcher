package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.view.inspector.WindowInspector;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Audits painted controls, including separate popup windows. It never repairs a view before checking it. */
public final class TenThemeTextSmoke extends TenDarkModeSmoke {
    final List<TenThemes.Preset> presets=new ArrayList<>();
    final Set<View> baseline=Collections.newSetFromMap(new IdentityHashMap<>());
    int combinations,popups,stateChecks,expected;String orientationFilter="all",modeFilter="all";String path,popupName,stage="startup";List<View> opened;View semanticRoot;int semanticFill;
    final ExecutorService idleWaiter=Executors.newSingleThreadExecutor(r->{Thread thread=new Thread(r,"theme-audit-idle");thread.setDaemon(true);return thread;});
    @Override public void waitForIdleSync(){try{idleWaiter.submit(()->super.waitForIdleSync()).get(8,TimeUnit.SECONDS);}catch(Exception error){throw new AssertionError("UI did not settle: "+stage,error);}}
    @Override public void onCreate(Bundle args){if(args!=null){orientationFilter=args.getString("orientation","all");modeFilter=args.getString("mode","all");}super.onCreate(args);}
    @Override public void onStart(){Bundle result=new Bundle();try{
        check(Arrays.asList("all","portrait","landscape").contains(orientationFilter)&&Arrays.asList("all","light","dark").contains(modeFilter),"Valid matrix partition");expected=17*(orientationFilter.equals("all")?2:1)*(modeFilter.equals("all")?2:1);
        store=new DesktopStore(getTargetContext());store.prefs.edit().putInt("startup_mode",2).putBoolean("welcome_on_start",false).putBoolean("reduce_motion",true).putBoolean("taskbar_apps",false).putBoolean("weather_follow",false).putString("feed_url","").remove(TenNotaDrafts.KEY).putString("ten_nota_draft","").commit();
        getTargetContext().getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();getTargetContext().getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        presets.addAll(TenThemes.TEN);presets.addAll(TenThemes.AERO);TenThemes.TEN.get(0).apply(store);
        home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(getTargetContext(),MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();await(()->TenSmoke.description(root,"Start menu")!=null,"Desktop ready");
        for(int orientation:new int[]{ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE}){
            String direction=orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT?"portrait":"landscape";if(!orientationFilter.equals("all")&&!orientationFilter.equals(direction))continue;
            ui(()->home.setRequestedOrientation(orientation));await(()->(root.getWidth()>root.getHeight())==(orientation==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),"Audit orientation");
            for(int i=0;i<presets.size();i++)for(boolean dark:new boolean[]{false,true}){
                if(!modeFilter.equals("all")&&!modeFilter.equals(dark?"dark":"light"))continue;
                final TenThemes.Preset preset=presets.get(i);path=(orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT?"portrait":"landscape")+"/"+preset.name+"/"+(dark?"dark":"light");
                ui(()->{preset.apply(store);store.prefs.edit().putBoolean("ten_apps_dark",dark).commit();home.applyTenAppearance();});waitForIdleSync();
                bodies();settings();choices(i,orientation,dark);menus();dialogs();shell();combinations++;save();Bundle progress=new Bundle();progress.putString("stream","Checked "+path+" ("+combinations+"/"+expected+")\n");sendStatus(0,progress);
                if(!failures.isEmpty())throw new AssertionError("Theme text failures: "+String.join("; ",failures.values()).substring(0,Math.min(5000,String.join("; ",failures.values()).length())));
            }
        }
        check(combinations==expected,"Complete requested theme partition");result.putString("stream","PASS: all 17 themes; app mode "+modeFilter+", orientation "+orientationFilter+" ("+combinations+" combinations), "+surfaces.size()+" native surfaces, "+checked+" text and "+hints+" hint checks, "+popups+" real popup windows and "+stateChecks+" pressed/hovered control states; all built-in tools and 48 Settings pages, source lists, sounds, Recycle Bin capacity, utility selectors, Run suggestions, native menu items and dialogs; recycled choices, first-frame palettes and retained live light/dark popup transitions. No explicit recoloring occurs in the audit.\n");finish(Activity.RESULT_OK,result);
    }catch(Throwable error){try{save();shot("ten21-theme-text-failure");for(View v:extraRoots())paint(v,"ten21-theme-popup-failure");}catch(Exception ignored){}result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);}}
    @Override void scan(String name,View view){surfaces.add(name);walk(name,view);List<View> controls=new ArrayList<>();stateControls(view,controls);for(View control:controls){boolean pressed=control.isPressed(),hovered=control.isHovered();control.setHovered(false);control.setPressed(true);walk(name+"/pressed",control);control.setPressed(false);control.setHovered(true);walk(name+"/hovered",control);control.setHovered(hovered);control.setPressed(pressed);stateChecks+=2;}}
    void stateControls(View view,List<View> controls){if(view.getVisibility()!=View.VISIBLE)return;if(view.isEnabled()&&view.getBackground() instanceof StateListDrawable)controls.add(view);if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)stateControls(group.getChildAt(i),controls);}}
    @Override int[] fills(Drawable drawable){if(drawable instanceof TenStyle.Hover)((TenStyle.Hover)drawable).appearance();return super.fills(drawable);}
    @Override int[] backgrounds(View view){View current=view;while(current!=null){if(current.getBackground() instanceof AeroGlass){Drawable glass=current.getBackground();return new int[]{(Integer)field(glass,"style")==AeroGlass.WINDOW?TenStyle.captionSurface(home,(Boolean)field(glass,"active")):TenStyle.surface(home)};}int[] colors=fills(current.getBackground());if(colors!=null)return colors;if(current==semanticRoot)return new int[]{semanticFill};ViewParent parent=current.getParent();current=parent instanceof View?(View)parent:null;}return new int[]{TenAppearance.background(home)};}
    void checkPaint(String name,View view)throws Exception{stage=path+"/"+name;waitForIdleSync();ui(()->scan(path+"/"+name,view));}
    void bodies()throws Exception{
        ArrayList<String> ids=new ArrayList<>(Arrays.asList(tools));for(String kind:GadgetCatalog.IDS)if(GadgetCatalog.extra(kind))ids.add("@gadget:"+kind);
        for(String id:ids){stage=path+"/"+id;final View[] content={null};ui(()->{home.open(id);if(id.equals(MediaLibrary.CENTER)){content[0]=(View)field(home,"mediaCenter");return;}DesktopWindow w=windows().get(id);if(w==null)for(DesktopWindow candidate:windows().values())if(candidate.getParent()!=null&&candidate.body instanceof DesktopOrganizer)w=candidate;check(w!=null,"Tool opens: "+id);w.maximize();content[0]=w;});checkPaint(id,content[0]);if(content[0] instanceof DesktopWindow){DesktopWindow w=(DesktopWindow)content[0];ui(()->{w.active(false);scan(path+"/"+id+"/inactive-caption",w.getChildAt(0));w.active(true);});}ui(()->{if(id.equals(MediaLibrary.CENTER)){invoke(home,"closeMediaCenter",new Class<?>[]{});return;}for(DesktopWindow w:new ArrayList<>(windows().values()))if(w.getParent()!=null)w.closeWindow();});}
    }
    void settings()throws Exception{
        ui(()->{home.open("@settings");windows().get("@settings").maximize();});TenSettings settings=(TenSettings)windows().get("@settings").body;
        for(TenSettingsCatalog.Page page:TenSettingsCatalog.PAGES){ui(()->settings.navigate(page.id));checkPaint("settings/"+page.id,settings);}
        ui(()->windows().get("@settings").closeWindow());
        ui(()->{home.open("@legacycontrol");windows().get("@legacycontrol").maximize();});VistaControlPanel control=(VistaControlPanel)windows().get("@legacycontrol").body;
        for(String page:new String[]{"home","personalization","background","colors","icons","accounts","system","clock","taskbar","grouping","sounds","startup"}){ui(()->control.navigate(page));checkPaint("control/"+page,control);}
        ui(()->windows().get("@legacycontrol").closeWindow());
    }
    void choices(int index,int orientation,boolean dark)throws Exception{
        ui(()->{home.open("@nota");windows().get("@nota").maximize();TenSmoke.description(windows().get("@nota").body,"New chat").performClick();});TenNotaPanel nota=(TenNotaPanel)windows().get("@nota").body;
        spinner(nota.task,"nota",()->{if(orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)paintUnchecked(opened.get(0),"ten21-nota-"+index+"-"+dark);});
        if(index==0){
            openPopup("nota-live",()->nota.task.performClick());List<View> rows=new ArrayList<>(opened);for(boolean mode:new boolean[]{!dark,dark}){ui(()->{store.prefs.edit().putBoolean("ten_apps_dark",mode).commit();home.applyTenAppearance();});waitForIdleSync();ui(()->{for(View row:rows)scan(path+"/nota-live/"+mode,row);});}closePopup();
        }
        ui(()->windows().get("@nota").closeWindow());
        ui(()->{home.open("@legacycontrol");windows().get("@legacycontrol").maximize();((VistaControlPanel)windows().get("@legacycontrol").body).navigate("background");});View body=windows().get("@legacycontrol").body;
        for(String description:new String[]{"Background picture location","Picture position","Change picture every"})spinner((Spinner)TenSmoke.description(body,description),description,null);
        ui(()->((VistaControlPanel)windows().get("@legacycontrol").body).navigate("sounds"));spinner((Spinner)TenSmoke.description(windows().get("@legacycontrol").body,"Desktop sound scheme"),"sounds",null);ui(()->windows().get("@legacycontrol").closeWindow());
        ui(()->{home.open("@recycle");windows().get("@recycle").maximize();((RecyclePanel)windows().get("@recycle").body).properties();});waitForIdleSync();Spinner[] capacity={null};ui(()->{for(View extra:extraRoots()){View found=TenSmoke.description(extra,"Recycle Bin maximum size");if(found instanceof Spinner)capacity[0]=(Spinner)found;}});spinner(capacity[0],"recycle-capacity",null);backToMain();ui(()->windows().get("@recycle").closeWindow());
        for(String kind:new String[]{"worldclock","converter"}){ui(()->{home.open("@gadget:"+kind);windows().get("@gadget:"+kind).maximize();});body=windows().get("@gadget:"+kind).body;for(String description:kind.equals("worldclock")?new String[]{"World clock zone 1","World clock zone 2"}:new String[]{"Conversion category","From unit","To unit"}){waitForIdleSync();String utilityId="@gadget:"+kind;await(()->TenSmoke.description(windows().get(utilityId).body,description) instanceof Spinner,"Real utility selector exists: "+description);Spinner[] value={null};ui(()->value[0]=(Spinner)TenSmoke.description(windows().get(utilityId).body,description));spinner(value[0],description,null);}ui(()->windows().get("@gadget:"+kind).closeWindow());}
        ui(()->{store.prefs.edit().putString("ten_run_history","[\"notepad\",\"calculator\",\"explorer\"]").commit();home.open("@run");});waitForIdleSync();AutoCompleteTextView[] entry={null};ui(()->{for(View extra:extraRoots()){AutoCompleteTextView found=TenSmoke.find(extra,AutoCompleteTextView.class);if(found!=null)entry[0]=found;}});check(entry[0]!=null,"Real Run editor exists");ui(()->entry[0].dismissDropDown());await(()->extraRoots().size()==1,"Initial Run suggestions dismissed");openPopup("run-suggestions",()->entry[0].showDropDown());closePopup();backToMain();
    }
    void spinner(Spinner spinner,String name,Runnable snapshot)throws Exception{
        check(spinner!=null,"Spinner exists: "+name);stage=path+"/spinner/"+name;waitForIdleSync();ui(()->spinner.requestRectangleOnScreen(new Rect(0,0,spinner.getWidth(),spinner.getHeight()),true));waitForIdleSync();int selection=spinner.getSelectedItemPosition();
        openPopup(name,()->spinner.performClick());if(snapshot!=null)ui(snapshot);
        ListView[] list={null};ui(()->{for(View view:opened){ListView found=TenSmoke.find(view,ListView.class);if(found!=null)list[0]=found;}check(list[0]!=null,"Popup has native list: "+name);list[0].setSelection(Math.max(0,list[0].getCount()-1));});waitForIdleSync();ui(()->{for(View view:opened)scan(path+"/popup/"+name+"/recycled",view);});closePopup();check(spinner.getSelectedItemPosition()==selection,"Inspecting a dropdown preserves selection: "+name);
    }
    void menus()throws Exception{
        ui(()->{home.open("@gadget:calculator");windows().get("@gadget:calculator").maximize();});View calculator=windows().get("@gadget:calculator").body;
        openPopup("calculator-menu",()->TenSmoke.description(calculator,"Calculator menu").performClick());closePopup();ui(()->windows().get("@gadget:calculator").closeWindow());
        ui(()->home.open("@search"));StartMenu start=TenSmoke.find(root,StartMenu.class);checkPaint("search-shell",start);
        openPopup("search-more",()->TenSmoke.description(start,"More ▾").performClick());closePopup();
        StartSearch.Result item=new StartSearch.Result("@browser","Nth Browser","Programs","browser",null,true);
        openPopup("start-app-options",()->invoke(start,"appOptions",new Class<?>[]{View.class,StartSearch.Result.class},start,item));closePopup();
        openPopup("start-tile-options",()->invoke(start,"tileOptions",new Class<?>[]{View.class,StartSearch.Result.class},start,item));closePopup();ui(()->start.hide());
        ui(()->{home.open("@notes");home.open("@taskview");});TenTaskView tasks=(TenTaskView)field(home,"tenTasks");TenDesktops desktops=(TenDesktops)field(home,"tenDesktops");
        waitForIdleSync();ui(()->{semanticRoot=tasks.overlay;semanticFill=0xff202020;scan(path+"/task-view",tasks.overlay);semanticRoot=null;});
        openPopup("desktop-menu",()->invoke(tasks,"desktopMenu",new Class<?>[]{View.class,TenDesktops.Desk.class},tasks.overlay,desktops.desks.get(desktops.current())));closePopup();
        openPopup("window-menu",()->invoke(tasks,"windowMenu",new Class<?>[]{View.class,DesktopWindow.class},tasks.overlay,windows().get("@notes")));closePopup();ui(()->{tasks.dismiss();windows().get("@notes").closeWindow();});
    }
    void dialogs()throws Exception{
        AlertDialog[] d={null};ui(()->d[0]=Aero.dialog(home).setTitle("Theme choices").setSingleChoiceItems(new String[]{"First choice","Second choice","Third choice"},1,(dialog,which)->{}).setPositiveButton("Keep",null).setNegativeButton("Cancel",null).show());checkPaint("choice-dialog",d[0].getWindow().getDecorView());ui(()->d[0].dismiss());
        ui(()->{EditText value=new EditText(home);value.setHint("Enter a name");value.setText("Retained text");d[0]=Aero.dialog(home).setTitle("Theme input").setView(value).setPositiveButton("Save",null).setNegativeButton("Cancel",null).show();});checkPaint("input-dialog",d[0].getWindow().getDecorView());ui(()->d[0].dismiss());
        ui(()->TenSmoke.description(root,"Start menu").performContextClick());waitForIdleSync();if(!extraRoots().isEmpty()){ui(()->{for(View view:extraRoots())scan(path+"/start-context",view);});backToMain();}
    }
    void shell()throws Exception{
        ui(()->home.open("@notes"));DesktopWindow window=windows().get("@notes");await(()->TenSmoke.description(root,"Minimize "+window.caption())!=null,"Running taskbar preview target");openPopup("taskbar-preview",()->TenSmoke.description(root,"Minimize "+window.caption()).performLongClick());closePopup();ui(window::closeWindow);
        checkPaint("taskbar",TenSmoke.description(root,"Desktop taskbar"));
        SystemTray tray=(SystemTray)field(home,"tray");for(String page:new String[]{"controls","battery","network","volume","brightness","calendar","actioncenter"}){ui(()->tray.show(page));checkPaint("tray/"+page,(View)field(tray,"frame"));ui(tray::dismiss);}
        ui(()->home.open("@news"));TenNewsFlyout news=(TenNewsFlyout)field(home,"tenNews");checkPaint("news",news.frame);ui(news::dismiss);
        ui(()->home.open("@actioncenter"));TenActionCenter actions=(TenActionCenter)field(home,"tenActions");checkPaint("action-center",actions.panel);ui(actions::dismiss);
    }
    void openPopup(String name,Runnable show)throws Exception{
        popupName=name;stage=path+"/popup/"+name;ui(()->{baseline.clear();baseline.addAll(WindowInspector.getGlobalWindowViews());show.run();});await(()->!newRoots().isEmpty(),"Popup window opens: "+path+"/"+name);waitForIdleSync();ui(()->{opened=new ArrayList<>(newRoots());check(!opened.isEmpty(),"Rendered popup exists: "+name);int before=checked;for(View view:opened)scan(path+"/popup/"+name,view);check(checked>before,"Actual popup text inspected: "+name);popups++;});
    }
    List<View> newRoots(){ArrayList<View> result=new ArrayList<>();for(View view:WindowInspector.getGlobalWindowViews())if(!baseline.contains(view)&&view.isShown())result.add(view);return result;}
    List<View> extraRoots(){ArrayList<View> result=new ArrayList<>();for(View view:WindowInspector.getGlobalWindowViews())if(view!=root&&view.isShown())result.add(view);return result;}
    boolean popupVisible(List<View> views){for(View view:views)if(view.isAttachedToWindow()&&view.isShown())return true;return false;}
    void closePopup()throws Exception{List<View> old=new ArrayList<>(opened);ui(()->{for(View view:old){long now=SystemClock.uptimeMillis();MotionEvent event=MotionEvent.obtain(now,now,MotionEvent.ACTION_OUTSIDE,-1,-1,0);try{view.dispatchTouchEvent(event);}finally{event.recycle();}}});await(()->!popupVisible(old),"Popup dismissed: "+path+"/"+popupName);}
    void backToMain()throws Exception{for(int i=0;i<4&&!extraRoots().isEmpty();i++){boolean[] cancelled={false};ui(()->{for(View view:extraRoots()){View cancel=view.findViewById(android.R.id.button2);if(cancel!=null&&cancel.isShown()){cancel.performClick();cancelled[0]=true;break;}}});if(!cancelled[0]){opened=new ArrayList<>(extraRoots());closePopup();}waitForIdleSync();}await(()->extraRoots().isEmpty(),"Dialog dismissed: "+path);}
    void invoke(Object object,String name,Class<?>[] types,Object... args){try{Method method=object.getClass().getDeclaredMethod(name,types);method.setAccessible(true);method.invoke(object,args);}catch(Exception e){throw new RuntimeException(e);}}
    void paintUnchecked(View view,String name){try{paint(view,name);}catch(IOException e){throw new RuntimeException(e);}}
    void paint(View view,String name)throws IOException{if(view.getWidth()<1||view.getHeight()<1)return;Bitmap image=Bitmap.createBitmap(view.getWidth(),view.getHeight(),Bitmap.Config.ARGB_8888);view.draw(new Canvas(image));try(OutputStream out=new FileOutputStream(new File(getTargetContext().getFilesDir(),name+".png"))){image.compress(Bitmap.CompressFormat.PNG,100,out);}finally{image.recycle();}}
    @Override void save()throws IOException{try(Writer out=new OutputStreamWriter(new FileOutputStream(new File(getTargetContext().getFilesDir(),"ten21-theme-text-audit.tsv")),StandardCharsets.UTF_8)){out.write(report.toString());}}
}
