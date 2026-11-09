package com.nthexperience.desktop;

import android.content.*;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
public class StartPerformanceTest {
    private Context context;private DesktopStore store;private int folderReads,aliasReads;private StartMenu menu;private AeroWallpaper wallpaper;
    @Before public void setup()throws Exception{
        Context actual=RuntimeEnvironment.getApplication();actual.getSharedPreferences("desktop",0).edit().clear().commit();
        SharedPreferences original=actual.getSharedPreferences("desktop",0);
        SharedPreferences counted=(SharedPreferences)Proxy.newProxyInstance(SharedPreferences.class.getClassLoader(),new Class[]{SharedPreferences.class},(proxy,method,args)->{
            if(method.getName().equals("getString")){if("app_folders".equals(args[0]))folderReads++;if("shortcut_names".equals(args[0]))aliasReads++;}
            return method.invoke(original,args);
        });
        context=new ContextWrapper(actual){@Override public SharedPreferences getSharedPreferences(String name,int mode){return name.equals("desktop")?counted:super.getSharedPreferences(name,mode);}};
        store=new DesktopStore(context);store.prefs.edit().putBoolean("ten_full_start",true).putBoolean("ten_start_apps",false).putBoolean("reduce_motion",true).commit();new TenStartData(store).installNthTiles(true);
        JSONObject folders=new JSONObject(),aliases=new JSONObject();
        for(int i=0;i<32;i++){JSONArray items=new JSONArray();for(int j=0;j<12;j++)items.put("com.example.app"+j+"/.Main");folders.put("folder:"+UUID.nameUUIDFromBytes(("folder"+i).getBytes()),new JSONObject().put("name","Folder "+i).put("items",items));}
        for(int i=0;i<128;i++)aliases.put("com.example.app"+i+"/.Main","App "+i);
        store.prefs.edit().putString("app_folders",folders.toString()).putString("shortcut_names",aliases.toString()).commit();
    }
    private void create(){wallpaper=new AeroWallpaper(context,11);menu=new StartMenu(context,new StartMenu.Actions(){public void open(String id){}public void options(View v,String id){}public void home(){}public void settings(){}public boolean isHome(){return true;}},store,wallpaper);}
    private void layout(){menu.measure(View.MeasureSpec.makeMeasureSpec(420,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY));menu.layout(0,0,420,720);}
    @After public void close(){if(menu!=null)menu.hide();if(wallpaper!=null)wallpaper.close();}
    @Test public void profileFullNthTileSet()throws Exception{
        long start=System.nanoTime();create();int constructorTiles=menu.tileViews.size();menu.setVisibility(View.GONE);layout();menu.show(false);layout();
        System.out.println("START_PROFILE constructorTiles="+constructorTiles+" tiles="+menu.tileViews.size()+" folderReads="+folderReads+" aliasReads="+aliasReads+" elapsedMs="+(System.nanoTime()-start)/1000000);
        assertEquals(0,constructorTiles);assertEquals(TenStartCatalog.APPS.size(),menu.tileViews.size());assertEquals(0,menu.appBuildCount());assertTrue(folderReads<200);assertTrue(aliasReads<200);
        Map<String,View> tiles=new HashMap<>(menu.tileViews);int builds=menu.tileBuildCount();menu.hide();menu.show(false);layout();assertEquals(builds,menu.tileBuildCount());for(String id:tiles.keySet())assertSame(tiles.get(id),menu.tileViews.get(id));
    }
    @Test public void hiddenCatalogChangesAreDeferredAndUnchangedCatalogKeepsTiles(){
        create();List<AppCatalog.Entry> apps=new ArrayList<>();for(int i=0;i<200;i++)apps.add(new AppCatalog.Entry("com.example.app"+i+"/.Main","App "+i,new ColorDrawable()));menu.entries(apps);assertEquals(0,menu.tileBuildCount());assertEquals(0,menu.appBuildCount());layout();menu.show(false);layout();assertEquals(43,menu.tileViews.size());assertEquals(0,menu.appList.getChildCount());int builds=menu.tileBuildCount();menu.entries(new ArrayList<>(apps));assertEquals(builds,menu.tileBuildCount());
        menu.hide();apps.add(new AppCatalog.Entry("com.example.new/.Main","New app",new ColorDrawable()));menu.entries(apps);assertEquals(builds,menu.tileBuildCount());menu.show(false);layout();assertTrue(menu.tileBuildCount()>builds);assertEquals(0,menu.appBuildCount());
        menu.search.setText("New app");assertTrue(menu.appBuildCount()>0);assertTrue(menu.appList.getChildCount()>0);
    }
    @Test public void renamedResizedAndRegroupedTilesInvalidateWhileHidden(){
        create();layout();menu.show(false);layout();String id=TenStartCatalog.APPS.get(0).id;View before=menu.tileViews.get(id);int builds=menu.tileBuildCount();menu.hide();store.rename(id,"My renamed tool");store.prefs.edit().putString("ten_size_"+id,"Wide").putString("ten_group_"+id,TenStartCatalog.GROUPS[1]).commit();assertEquals(builds,menu.tileBuildCount());menu.show(false);layout();assertTrue(menu.tileBuildCount()>builds);assertNotSame(before,menu.tileViews.get(id));assertEquals(43,menu.tileViews.size());assertTrue(text(menu.tileViews.get(id)).contains("My renamed tool"));
    }
    private String text(View view){StringBuilder value=new StringBuilder();if(view instanceof TextView)value.append(((TextView)view).getText());if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)value.append(text(((ViewGroup)view).getChildAt(i)));return value.toString();}
}
