package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.webkit.WebView;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Native text/surface audit, retained browser inputs and actual rendered control artwork. */
public final class TenDarkModeSmoke extends TenThemeSmoke {
    final LinkedHashMap<String,String> failures=new LinkedHashMap<>();
    final StringBuilder report=new StringBuilder("Surface\tText\tForeground\tBackground\tContrast\tRole\tResult\n");
    final List<String> surfaces=new ArrayList<>();
    int checked,hints;BrowserPanel browser;EditText address;AlertDialog dialog;
    final String[] tools={"@computer","@files","@documents","@pictures","@music","@notes","@calendar","@clock","@browser","@nota","@taskmanager","@recycle","@sticky","@photos","@weather","@backup","@recent","@filemanagers","@welcome","@organize","@quicklaunch",MediaLibrary.PLAYER,MediaLibrary.CENTER,"@events","@devices","@management","@powershell","@powershell-admin"};
    @Override public void onStart(){Bundle result=new Bundle();try{
        store=new DesktopStore(getTargetContext());store.prefs.edit().putInt("startup_mode",2).putBoolean("welcome_on_start",false).putBoolean("reduce_motion",true).putBoolean("taskbar_apps",false).putBoolean("weather_follow",false).putString("feed_url","").commit();TenThemes.TEN.get(0).apply(store);
        getTargetContext().getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();getTargetContext().getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(getTargetContext(),MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();await(()->TenSmoke.description(root,"Start menu")!=null,"Desktop ready");
        for(int orientation:new int[]{ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE}){
            boolean portrait=orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;String suffix=portrait?"portrait":"landscape";
            ui(()->{home.setRequestedOrientation(orientation);store.prefs.edit().putBoolean("ten_apps_dark",true).commit();home.applyTenAppearance();});await(()->portrait?root.getHeight()>root.getWidth():root.getWidth()>root.getHeight(),"Dark audit orientation");
            for(String id:tools)inspectTool(id,suffix);
            for(String id:GadgetCatalog.IDS)if(GadgetCatalog.extra(id))inspectTool("@gadget:"+id,suffix);
            for(TenSettingsCatalog.Page page:TenSettingsCatalog.PAGES){ui(()->{home.open("ten-setting:"+page.id);scan("settings/"+page.id+"/"+suffix,windows().get("@settings").body);});}
            for(String page:new String[]{"home","personalization","background","colors","icons","accounts","system","clock","taskbar","grouping","sounds","startup"}){
                ui(()->{home.open("setting:page:"+page);DesktopWindow window=windows().get("@legacycontrol");if(window!=null&&window.getParent()!=null)scan("control/"+page+"/"+suffix,window.body);});
            }
            retainedBrowser(suffix);inputDialog(suffix);calendarPixels(suffix);picker(suffix,orientation);
        }
        save();check(failures.isEmpty(),"Dark-mode contrast failures ("+failures.size()+"): "+String.join("; ",new ArrayList<>(failures.values()).subList(0,Math.min(18,failures.size()))));
        result.putString("stream","PASS: "+surfaces.size()+" dark native surfaces; "+checked+" enabled text and "+hints+" hint checks; every Settings page, desktop Control Panel, built-in tools/utilities, retained browser text and selection across all 17 themes and light/dark restoration; actual browser/photo glyph and calendar date pixels; dialog fields, menu and literal preview protection, in portrait and landscape.\n");finish(Activity.RESULT_OK,result);
    }catch(Throwable error){try{save();shot("ten18-dark-failure");}catch(Exception ignored){}result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);}}
    void inspectTool(String id,String suffix)throws Exception{
        ui(()->{home.open(id);if(id.equals(MediaLibrary.CENTER)){View center=(View)field(home,"mediaCenter");check(center!=null&&center.getParent()!=null,"Native media center opens");scan(id+"/"+suffix,center);try{Method close=MainActivity.class.getDeclaredMethod("closeMediaCenter");close.setAccessible(true);close.invoke(home);}catch(Exception error){throw new RuntimeException(error);}return;}DesktopWindow window=windows().get(id);if(window==null&&(id.equals("@organize")||id.equals("@quicklaunch"))){for(DesktopWindow candidate:windows().values())if(candidate.getParent()!=null&&candidate.body instanceof DesktopOrganizer)window=candidate;}
            check(window!=null&&window.getParent()!=null,"Native tool opens: "+id);window.maximize();scan(id+"/"+suffix,window.body);if(id.startsWith("@powershell")){EditText command=(EditText)TenSmoke.description(window.body,"Nth PowerShell command");command.setText("help");command.setSelection(1,3);for(boolean dark:new boolean[]{false,true}){store.prefs.edit().putBoolean("ten_apps_dark",dark).commit();home.applyTenAppearance();scan(id+"/retained/"+dark+"/"+suffix,window.body);check(command.getText().toString().equals("help")&&command.getSelectionStart()==1&&command.getSelectionEnd()==3,"Console input survives mode changes");check(backgrounds(command)[0]==0xff012456,"Console retains its blue surface");}View run=TenSmoke.description(window.body,"Run command");run.requestFocus();scan(id+"/focused/"+suffix,window.body);run.performClick();check(command.getText().length()==0,"Console command submits");scan(id+"/submitted/"+suffix,window.body);}if(!id.equals("@browser"))window.closeWindow();});
    }
    void scan(String name,View view){TenAppearance.apply(view);surfaces.add(name);walk(name,view);}
    void walk(String path,View view){
        if(view.getVisibility()!=View.VISIBLE||view instanceof WebView||view instanceof ImageView)return;
        CharSequence description=view.getContentDescription();if(description!=null&&description.toString().startsWith("Theme appearance sample:"))return;
        if(view instanceof TextView&&view.isEnabled()&&view.getAlpha()>=.95f){TextView text=(TextView)view;
            if(text.length()>0)contrast(path,text,text.getText().toString(),text.getCurrentTextColor(),false);
            if(text instanceof EditText&&text.getHint()!=null)contrast(path,text,text.getHint().toString(),text.getCurrentHintTextColor(),true);
        }
        if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)walk(path,group.getChildAt(i));}
    }
    void contrast(String path,TextView view,String text,int ink,boolean hint){
        if(Color.alpha(ink)==0)return;int[] backgrounds=backgrounds(view);double ratio=100;int worst=backgrounds[0];
        for(int bg:backgrounds){int opaque=blend(ink,bg);double current=TenAppearance.contrast(opaque,bg);if(current<ratio){ratio=current;worst=bg;}}
        String label=text.replace('\n',' ').replace('\t',' ');if(label.length()>85)label=label.substring(0,85);
        if(hint)hints++;else checked++;boolean pass=ratio>=4.45;
        report.append(path).append('\t').append(label).append('\t').append(Integer.toHexString(ink)).append('\t').append(Integer.toHexString(worst)).append('\t').append(String.format(Locale.ROOT,"%.2f",ratio)).append('\t').append(hint?"hint":"text").append('\t').append(pass?"PASS":"FAIL").append('\n');
        if(!pass)failures.putIfAbsent(path+"/"+label,path+" / "+label+" / "+Integer.toHexString(ink)+" on "+Integer.toHexString(worst)+" = "+String.format(Locale.ROOT,"%.2f",ratio));
    }
    int blend(int ink,int bg){float a=Color.alpha(ink)/255f;return TenStyle.mix(bg,ink,a);}
    int[] backgrounds(View view){View current=view;while(current!=null){int[] colors=fills(current.getBackground());if(colors!=null)return colors;ViewParent p=current.getParent();current=p instanceof View?(View)p:null;}return new int[]{TenAppearance.background(home)};}
    int[] fills(Drawable drawable){
        if(drawable==null)return null;if(drawable instanceof Aero.ThemePanel)((Aero.ThemePanel)drawable).appearance();
        int[] colors=null;
        if(drawable instanceof ColorDrawable)colors=new int[]{((ColorDrawable)drawable).getColor()};
        else if(drawable instanceof GradientDrawable){GradientDrawable d=(GradientDrawable)drawable;colors=d.getColors();if(colors==null&&d.getColor()!=null)colors=new int[]{d.getColor().getDefaultColor()};}
        else if(drawable instanceof StateListDrawable)return fills(drawable.getCurrent());
        else if(drawable instanceof InsetDrawable)return fills(((InsetDrawable)drawable).getDrawable());
        else if(drawable instanceof LayerDrawable){LayerDrawable d=(LayerDrawable)drawable;for(int i=0;i<d.getNumberOfLayers();i++){colors=fills(d.getDrawable(i));if(colors!=null)return colors;}}
        if(colors==null||colors.length==0)return null;for(int color:colors)if(Color.alpha(color)!=255)return null;return colors;
    }
    void retainedBrowser(String suffix)throws Exception{
        ui(()->{for(DesktopWindow window:new ArrayList<>(windows().values()))if(window.getParent()!=null&&!(window.body instanceof BrowserPanel))window.closeWindow();home.open("@browser");windows().get("@browser").maximize();browser=(BrowserPanel)windows().get("@browser").body;browser.home();if(browser.tabCount()==1)TenSmoke.description(browser,"New browser tab").performClick();address=(EditText)TenSmoke.description(browser,"Browser address");address.requestFocus();address.setText("youtube");address.setSelection(2,6);});
        ArrayList<TenThemes.Preset> themes=new ArrayList<>();themes.addAll(TenThemes.TEN);themes.addAll(TenThemes.AERO);int tabs=browser.tabCount();
        for(TenThemes.Preset theme:themes)for(boolean dark:new boolean[]{false,true,false,true}){
            ui(()->{theme.apply(store);store.prefs.edit().putBoolean("ten_apps_dark",dark).commit();home.applyTenAppearance();check(browser==windows().get("@browser").body&&browser.tabCount()==tabs,"Browser instance and tabs survive theme change");check(address.getText().toString().equals("youtube")&&address.getSelectionStart()==2&&address.getSelectionEnd()==6,"Typed address and selection survive appearance changes");scan("browser/"+theme.name+"/"+dark+"/"+suffix,browser);
                View field=(View)address.getParent();int fill=fills(field.getBackground())[0];check(dark?TenAppearance.luma(fill)<90:TenAppearance.luma(fill)>220,"Rounded address surface follows app mode");
                View literal=new View(home);literal.setBackground(Aero.literal(home,new int[]{Color.WHITE,0xff87ceeb},7,0));TenAppearance.apply(literal);check(((GradientDrawable)literal.getBackground()).getColors()[0]==Color.WHITE,"Literal artwork is not recolored");
            });
        }
        ui(()->{TenThemes.TEN.get(0).apply(store);store.prefs.edit().putBoolean("ten_apps_dark",true).commit();home.applyTenAppearance();glyph(TenSmoke.description(browser,"Browser Refresh or stop"),"Browser reload");glyph(TenSmoke.description(browser,"New browser tab"),"Browser new tab");PhotoGlyph photo=new PhotoGlyph(home,"rotateLeft","Photo rotate",()->{});photo.layout(0,0,Aero.dp(home,44),Aero.dp(home,44));glyph(photo,"Photo rotate");});
        shot("ten18-browser-dark-"+suffix);
        ui(()->TenSmoke.description(browser,"Settings and more").performClick());waitForIdleSync();
        ui(()->{Object menu=field(browser,"browserMenu");PopupWindow popup=(PopupWindow)field(menu,"popup");check(popup.isShowing(),"Real browser menu opens");scan("browser-menu/"+suffix,popup.getContentView());popup.dismiss();});
    }
    void glyph(View view,String label){
        int width=Math.max(1,view.getWidth()),height=Math.max(1,view.getHeight());if(width==1||height==1){width=height=Aero.dp(home,40);view.layout(0,0,width,height);}
        Bitmap image=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(image);int bg=TenAppearance.background(home);c.drawColor(bg);view.draw(c);double maximum=1;
        int[] pixels=new int[width*height];image.getPixels(pixels,0,width,0,0,width,height);for(int color:pixels)maximum=Math.max(maximum,TenAppearance.contrast(color,bg));image.recycle();check(maximum>=3,label+" has visible rendered strokes on dark surfaces");
    }
    void inputDialog(String suffix)throws Exception{
        EditText[] inputs=new EditText[2];ui(()->{LinearLayout form=Aero.column(home);Aero.pad(form,16);inputs[0]=new EditText(home);inputs[0].setHint("Dialog search hint");inputs[0].setContentDescription("Dark audit input");inputs[0].setText("Keep this text");inputs[0].setSelection(2,6);inputs[1]=new EditText(home);inputs[1].setHint("Rounded field hint");GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(Aero.dp(home,12));inputs[1].setBackground(bg);form.addView(inputs[0]);form.addView(inputs[1]);dialog=Aero.dialog(home).setTitle("Appearance check").setView(form).setPositiveButton("Keep",null).setNegativeButton("Cancel",null).show();});
        waitForIdleSync();for(boolean dark:new boolean[]{true,false,true})ui(()->{store.prefs.edit().putBoolean("ten_apps_dark",dark).commit();home.applyTenAppearance();scan("input-dialog/"+dark+"/"+suffix,dialog.getWindow().getDecorView());check(inputs[0].getText().toString().equals("Keep this text")&&inputs[0].getSelectionStart()==2&&inputs[0].getSelectionEnd()==6,"Dialog text and selection stay intact through light/dark changes");});shot("ten18-dialog-dark-"+suffix);ui(()->dialog.dismiss());
    }
    void calendarPixels(String suffix)throws Exception{
        ui(()->{home.open("@calendar");windows().get("@calendar").maximize();});waitForIdleSync();ui(()->{View calendar=TenSmoke.find(windows().get("@calendar").body,MonthCalendarView.class);check(calendar!=null&&calendar.getWidth()>0,"Native calendar exists");Bitmap image=Bitmap.createBitmap(calendar.getWidth(),calendar.getHeight(),Bitmap.Config.ARGB_8888);calendar.draw(new Canvas(image));int[] pixels=new int[image.getWidth()*image.getHeight()];image.getPixels(pixels,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());int bright=0;for(int color:pixels)if(color==0xfff2f2f2)bright++;image.recycle();check(bright>20,"Calendar date text draws a readable dark-mode foreground");});shot("ten18-calendar-dark-"+suffix);
    }
    void picker(String suffix,int orientation)throws Exception{
        final Activity[] active={null};
        Application.ActivityLifecycleCallbacks lifecycle=new Application.ActivityLifecycleCallbacks(){
            public void onActivityCreated(Activity activity,Bundle saved){if(activity instanceof ExplorerPickerActivity&&activity.getRequestedOrientation()!=orientation)activity.setRequestedOrientation(orientation);}
            public void onActivityResumed(Activity activity){if(activity instanceof ExplorerPickerActivity)active[0]=activity;}
            public void onActivityDestroyed(Activity activity){if(active[0]==activity)active[0]=null;}
            public void onActivityStarted(Activity activity){}public void onActivityPaused(Activity activity){}public void onActivityStopped(Activity activity){}public void onActivitySaveInstanceState(Activity activity,Bundle state){}
        };
        ui(()->home.getApplication().registerActivityLifecycleCallbacks(lifecycle));
        try{
            startActivitySync(new Intent(PickerSpec.ACTION).setComponent(new ComponentName(getTargetContext(),ExplorerPickerActivity.class)).setType("*/*").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            await(()->active[0]!=null&&(orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT?active[0].getWindow().getDecorView().getHeight()>active[0].getWindow().getDecorView().getWidth():active[0].getWindow().getDecorView().getWidth()>active[0].getWindow().getDecorView().getHeight()),"File picker orientation");
            Activity picker=active[0];View content=((ViewGroup)picker.findViewById(android.R.id.content)).getChildAt(0);EditText search=TenSmoke.find(content,EditText.class);
            ui(()->{check(search!=null,"File picker search exists");search.setText("pictures");search.setSelection(1,4);});
            for(boolean dark:new boolean[]{true,false,true}){
                ui(()->{store.prefs.edit().putBoolean("ten_apps_dark",dark).commit();TenAppearance.context(picker);content.invalidate();});
                await(()->{int fill=backgrounds(search)[0];return dark?TenAppearance.luma(fill)<90:TenAppearance.luma(fill)>220;},"File picker follows app mode before explicit audit");
                ui(()->{check(search.getText().toString().equals("pictures")&&search.getSelectionStart()==1&&search.getSelectionEnd()==4,"Picker search survives appearance changes");scan("file-picker/"+dark+"/"+suffix,content);});
            }
            shot("ten18-picker-dark-"+suffix);
        }finally{ui(()->{if(active[0]!=null)active[0].finish();home.getApplication().unregisterActivityLifecycleCallbacks(lifecycle);});waitForIdleSync();}
    }
    Object field(Object object,String name){try{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);}catch(Exception error){throw new RuntimeException(error);}}
    void save()throws IOException{try(Writer out=new OutputStreamWriter(new FileOutputStream(new File(getTargetContext().getFilesDir(),"ten18-dark-audit.tsv")),StandardCharsets.UTF_8)){out.write(report.toString());}}
}
