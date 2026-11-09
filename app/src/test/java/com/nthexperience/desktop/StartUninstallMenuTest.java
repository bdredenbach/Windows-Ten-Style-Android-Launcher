package com.nthexperience.desktop;
import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import java.util.*;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
public class StartUninstallMenuTest {
 private static final String ID="example.startapp/.Main",NAME="Start test app";
 private ActivityController<Activity> controller;private Activity activity;private DesktopStore store;private FrameLayout root;private AeroWallpaper wallpaper;private StartMenu menu;private String opened;
 private final StartMenu.Actions actions=new StartMenu.Actions(){public void open(String id){opened=id;}public void options(View anchor,String id){ContextMenus.centered(anchor,(x,y)->ShortcutMenus.show(activity,anchor,x,y,store,id,"desktop",NAME,new ShortcutMenus.Host(){public void open(String value){opened=value;}public void rename(String value){}public void changed(){}}));}public void home(){}public void settings(){}public boolean isHome(){return true;}};
 @Before public void setup(){controller=Robolectric.buildActivity(Activity.class).setup().visible();activity=controller.get();store=new DesktopStore(activity);store.prefs.edit().clear().commit();store.startPins(Collections.singletonList(ID));ApplicationInfo app=new ApplicationInfo();app.packageName="example.startapp";app.nonLocalizedLabel=NAME;PackageInfo info=new PackageInfo();info.packageName=app.packageName;info.applicationInfo=app;shadowOf(activity.getPackageManager()).installPackage(info);root=new FrameLayout(activity);activity.setContentView(root);wallpaper=new AeroWallpaper(activity,11);menu=new StartMenu(activity,actions,store,wallpaper);root.addView(menu,new FrameLayout.LayoutParams(420,700));root.measure(View.MeasureSpec.makeMeasureSpec(480,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(800,View.MeasureSpec.EXACTLY));root.layout(0,0,480,800);}
 @After public void close(){ menu.hide();wallpaper.close();controller.pause().stop().destroy();}
 private StartSearch.Result item(){return new StartSearch.Result(ID,NAME,"Installed apps","programs",new ColorDrawable(),true);}
 private View anchor(){View view=new View(activity);root.addView(view,new FrameLayout.LayoutParams(40,40));view.layout(0,0,40,40);return view;}
 private View find(View view,String label){if(label.contentEquals(view.getContentDescription()==null?"":view.getContentDescription())||view instanceof TextView&&label.contentEquals(((TextView)view).getText()))return view;if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View found=find(((ViewGroup)view).getChildAt(i),label);if(found!=null)return found;}return null;}
 private PopupWindow popup(){PopupWindow popup=shadowOf(RuntimeEnvironment.getApplication()).getLatestPopupWindow();assertNotNull(popup);assertNotNull(find(popup.getContentView(),"Uninstall"));return popup;}
 private void assertDelete(){org.robolectric.shadows.ShadowActivity.IntentForResult result=shadowOf(activity).getNextStartedActivityForResult();assertNotNull(result);assertEquals(Intent.ACTION_DELETE,result.intent.getAction());assertEquals("package:example.startapp",result.intent.getDataString());assertEquals(AppUninstaller.REQUEST,result.requestCode);}
 @Test public void installedAppAndPinnedTileOfferDirectUninstall(){for(String method:new String[]{"appOptions","tileOptions"}){View anchor=anchor();ReflectionHelpers.callInstanceMethod(menu,method,from(View.class,anchor),from(StartSearch.Result.class,item()));PopupMenu popup=org.robolectric.shadows.ShadowPopupMenu.getLatestPopupMenu();assertNotNull(popup.getMenu().findItem(AppUninstaller.MENU_ID));popup.getMenu().performIdentifierAction(AppUninstaller.MENU_ID,0);assertDelete();popup.dismiss();}}

}
