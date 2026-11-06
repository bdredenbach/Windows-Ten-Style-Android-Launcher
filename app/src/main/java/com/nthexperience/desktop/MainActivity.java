package com.nthexperience.desktop;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.*;

@androidx.media3.common.util.UnstableApi
public final class MainActivity extends Activity implements StartMenu.Actions {
    private DesktopStore store;private TenDesktops tenDesktops;
    private LinearLayout tenNotaButton;
    private TenTaskView tenTasks;private TenActionCenter tenActions;private TenNewsFlyout tenNews;private LinearLayout newsButton;private TextView newsText;
    private String layoutKey;
    private JSONObject pausedSession;
    private String pendingFolderWindow="@files";
    private DesktopBackup.Plan restorePlan;
    private String backupMessage="";
    private boolean backupBusy,restoringBackup;
    private FrameLayout restoreCover;
    private StartupExperience startup;
    private static final int REQUEST_HOME = 12;
    static final int REQUEST_USAGE_ACCESS=311;
    static final int REQUEST_USAGE_APP_INFO=312;
    private enum UsageGuide { FIRST_RUN, MANUAL, RETURN }
    private int usageSettingsRequest;
    private boolean usageSettingsLeft,includesExternalApps;
    private AlertDialog usageGuide;
    private static final int IMPORT_THEME=308, EXPORT_THEME=309, EXPORT_STEPS=310;
    private String pendingThemeExport;
    private final StepRecorder steps=new StepRecorder();
    private boolean homeRequestInFlight;
    private StorageSetup storageSetup;private boolean restoreStorageSetup;private boolean storageAllowed;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private final Map<String,AppCatalog.Entry> entries=new LinkedHashMap<>();
    private final Map<String,DesktopWindow> windows=new LinkedHashMap<>();
    private final Map<String,DesktopWindow> desktopHidden=new LinkedHashMap<>();
    private DesktopContextMenu jumpMenu;
    private final Map<AeroGlass,List<DesktopWindow>> taskProgressSurfaces=new LinkedHashMap<>();
    private BrowserPanel downloadBrowser;
    private int browserProgress=-2;
    private boolean queryingDownloads,taskProgressActive;
    private long nextDownloadPoll;
    private final Runnable taskProgressTick=new Runnable(){
        @Override public void run(){
            if(!taskProgressActive)return;
            refreshTaskProgress();
            if(!taskProgressSurfaces.isEmpty())main.postDelayed(this,900);
        }
    };
    private final Map<DesktopWindow,JSONObject> arrangementUndo=new LinkedHashMap<>();
    private boolean recycleFull;private View desktopToggle;private AlertDialog workspaceDialog;
    private FrameLayout root,workspace,videoOverlay;
    private MediaPlayback playback;
    private MediaLibrary mediaLibrary;
    private MediaPanel mediaCenter;
    private static final int PICK_MEDIA = 306, PICK_WALLPAPERS = 305, PICK_WALLPAPER_FOLDER = 307;
    private int previousShellAccessibility;
    private DesktopIconGrid desktop;
    private ScrollView desktopScroll;
    private TenServices services;
    private DesktopContextMenu desktopMenu;
    private int desktopRows,desktopCutoutLeft,desktopCutoutRight,windowTopInset;
    private WeatherLocation weatherLocation;
    private LinearLayout shell,taskItems;private HorizontalScrollView taskStrip;private int taskViewportWidth;
    private StartMenu startMenu;
    private TaskbarReorder taskbarReorder;private final Map<String,Integer> taskColors=new HashMap<>();
    private SystemTray tray;private WindowPreviews previews;private BrightnessControl brightness;
    private TextView clock;private View tenStartButton;
    private AeroWallpaper wallpaper;
    private boolean destroyed,scanPending,scanAgain,receiverRegistered;
    private int scanGeneration,columns;
    private OnBackInvokedCallback backCallback;
    private final BroadcastReceiver packages=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){scanApps();}};
    private final BroadcastReceiver timeChanged=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){updateClock();}};
    private final android.content.SharedPreferences.OnSharedPreferenceChangeListener tenShellPrefs=(prefs,key)->{if(key!=null&&(key.equals("weather_json")||key.equals("weather_city")||key.equals("weather_f")||key.equals("feed_items")||key.equals("ten_news_mode")))main.post(()->{if(!destroyed)updateNewsChip();});};
    private final Runnable taskbarRender=()->{if(!destroyed)renderTaskbar();};
    private final Runnable desktopRender=()->{if(!destroyed)renderDesktop();};
    private final Runnable clockTick=new Runnable(){public void run(){updateClock();main.postDelayed(this,60000-System.currentTimeMillis()%60000);}};

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);TenAppearance.context(this);store=new DesktopStore(this);store.migrateDesktop();if(!store.prefs.contains("ten_apps_dark"))store.prefs.edit().putBoolean("ten_apps_dark",!TenThemes.aero(store.prefs)&&(store.prefs.getInt("wallpaper",11)==13||store.prefs.getInt("wallpaper",11)==14)).apply();TenAppearance.context(this);store.prefs.registerOnSharedPreferenceChangeListener(tenShellPrefs);tenDesktops=new TenDesktops(store,windows,this::desktopChangedTen);
        if(!store.prefs.getBoolean("ten_initialized",false)){store.prefs.edit().putBoolean("ten_initialized",true).putInt("wallpaper",11).apply();store.startPins(Arrays.asList("@nota","@browser","@files","@calendar","@photos","@settings"));}
        if(!store.prefs.getBoolean("ten_v2_defaults",false)){List<String> pins=new ArrayList<>(store.quickLaunch());if(!pins.contains("@nota")&&pins.size()<DesktopStore.MAX_QUICK)pins.add("@nota");store.quickLaunch(pins);store.prefs.edit().putBoolean("ten_v2_defaults",true).apply();}
        if(!store.prefs.getBoolean("ten_v13_taskbar_defaults",false))store.prefs.edit().putBoolean("taskbar_static",false).putBoolean("ten_v13_taskbar_defaults",true).apply();
        if(!store.prefs.getBoolean("seven_taskbar_grouping_set",false))store.prefs.edit().putString("taskbar_grouping","Always").putBoolean("seven_taskbar_grouping_set",true).apply();
        storageSetup=new StorageSetup(this);storageAllowed=StorageAccess.has(this);restoreStorageSetup=saved!=null&&saved.getBoolean("storage_setup_open");layoutKey=getResources().getConfiguration().orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE?"landscape":"portrait";
        if(saved!=null){
            pendingFolderWindow=saved.getString("folder_window","@files");
            homeRequestInFlight=saved.getBoolean("home_request_in_flight");
            usageSettingsRequest=saved.getInt("usage_settings_request");
            usageSettingsLeft=saved.getBoolean("usage_settings_left");
            pendingThemeExport=saved.getString("pending_theme_export");
        }
        includesExternalApps=ExternalTasks.includesExternalApps(store,ExternalTasks.permitted(this));
        getWindow().setStatusBarColor(Color.TRANSPARENT);getWindow().setNavigationBarColor(0xff0a263b);
        if(Build.VERSION.SDK_INT>=28){WindowManager.LayoutParams attributes=getWindow().getAttributes();attributes.layoutInDisplayCutoutMode=Build.VERSION.SDK_INT>=30?WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS:WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;getWindow().setAttributes(attributes);}
        if(Build.VERSION.SDK_INT>=30)getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        brightness=new BrightnessControl(this);brightness.apply();previews=new WindowPreviews(this,new WindowPreviews.Host(){public List<DesktopWindow> windows(){return orderedWindows();}public List<String> external(){List<String> ids=new ArrayList<>();for(String id:ExternalTasks.list())if(entries.containsKey(id))ids.add(id);return ids;}public String label(String id){return MainActivity.this.label(id);}public void open(String id){MainActivity.this.open(id);}public void preparePreview(){if(tenNews!=null)tenNews.dismiss();startMenu.hide();if(tray!=null)tray.dismiss();}public View wallpaper(){return wallpaper;}public void showDesktop(){if(desktopHidden.isEmpty())MainActivity.this.showDesktop();}});
        buildDesktop();weatherLocation=new WeatherLocation(this,store,services.weather,()->true,this::updateWeatherSummary);applySystemChrome();String backupStatus=getIntent().getStringExtra("backup_status");if(backupStatus!=null){getIntent().removeExtra("backup_status");main.post(()->toast(backupStatus));}
        handleRequestedTool();
        if(saved!=null&&saved.containsKey("desktop_session")){
            String session=saved.getString("desktop_session");
            workspace.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){public void onGlobalLayout(){if(workspace.getWidth()<1||workspace.getHeight()<1)return;workspace.getViewTreeObserver().removeOnGlobalLayoutListener(this);restoreSession(session);}});
        }
        if(Build.VERSION.SDK_INT>=33){backCallback=this::handleBack;getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT,backCallback);}
        IntentFilter packageFilter=new IntentFilter();packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);packageFilter.addDataScheme("package");
        // These receivers accept only Android's protected package/time broadcasts.
        if(Build.VERSION.SDK_INT>=33)registerReceiver(packages,packageFilter,Context.RECEIVER_EXPORTED);else registerReceiver(packages,packageFilter);
        IntentFilter timeFilter=new IntentFilter();timeFilter.addAction(Intent.ACTION_TIME_CHANGED);timeFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);timeFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        if(Build.VERSION.SDK_INT>=33)registerReceiver(timeChanged,timeFilter,Context.RECEIVER_EXPORTED);else registerReceiver(timeChanged,timeFilter);
        receiverRegistered=true;scanApps();loadRestorePreview();startup=new StartupExperience(this,root,store,saved!=null,()->open("@welcome"));
    }
    private void buildDesktop(){
        root=new FrameLayout(this);wallpaper=new AeroWallpaper(this,store.prefs.getInt("wallpaper",AeroWallpaper.SEVEN));root.addView(wallpaper,new FrameLayout.LayoutParams(-1,-1));
        shell=Aero.column(this);root.addView(shell,new FrameLayout.LayoutParams(-1,-1));
        shell.setOnApplyWindowInsetsListener((v,insets)->{
            int left,top,right,bottom;
            if(Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());android.graphics.Insets cutout=insets.getInsets(WindowInsets.Type.displayCutout());android.graphics.Insets ime=insets.getInsets(WindowInsets.Type.ime());desktopCutoutLeft=Math.max(0,cutout.left-bars.left);desktopCutoutRight=Math.max(0,cutout.right-bars.right);left=bars.left;top=store.prefs.getBoolean("show_status_bar",false)?Math.max(bars.top,cutout.top):bars.top;windowTopInset=Math.max(0,cutout.top-top);right=bars.right;bottom=Math.max(Math.max(bars.bottom,cutout.bottom),ime.bottom);}
            else{left=insets.getSystemWindowInsetLeft();top=insets.getSystemWindowInsetTop();right=insets.getSystemWindowInsetRight();bottom=insets.getSystemWindowInsetBottom();windowTopInset=0;if(Build.VERSION.SDK_INT>=28&&insets.getDisplayCutout()!=null&&!store.prefs.getBoolean("show_status_bar",false)){int safe=insets.getDisplayCutout().getSafeInsetTop();top=Math.max(0,top-safe);windowTopInset=safe;}}
            shell.setPadding(left,top,right,bottom);if(workspace!=null)workspace.post(this::layoutWorkspace);return insets;
        });
        workspace=new FrameLayout(this);shell.addView(workspace,new LinearLayout.LayoutParams(-1,0,1));
        ScrollView scroll=new ScrollView(this);desktopScroll=scroll;scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setPadding(dp(7),dp(10),dp(7),dp(12));scroll.setVerticalScrollBarEnabled(false);
        // Keep the icon sheet in one cached layer when side insets move during a 180-degree turn.
        desktop=new DesktopIconGrid(this,store,layoutKey);scroll.addView(desktop,new ScrollView.LayoutParams(-1,-2));workspace.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        desktop.setOnDragListener((v,event)->{if(event.getLocalState() instanceof FileDrag){FileDrag drag=(FileDrag)event.getLocalState();if(store.prefs.getBoolean("layout_locked",false)||drag.refs.stream().anyMatch(r->!r.item.directory()))return false;if(event.getAction()==DragEvent.ACTION_DROP){try{for(DocumentOps.Ref ref:drag.refs)FolderLinks.add(store,DocumentFolders.childPage(drag.sourcePage,ref.item),ref.item.name);desktopChanged();toast("Folder shortcut added; original folder kept in place.");}catch(JSONException ex){toast(ex.getMessage());}}return true;}if(!(event.getLocalState() instanceof String)||((String)event.getLocalState()).startsWith("gadget:")||store.prefs.getBoolean("layout_locked",false))return false;if(event.getAction()==DragEvent.ACTION_DROP){desktop.place((String)event.getLocalState(),event.getX()-desktop.cellWidth()/2f,event.getY()-desktop.cellHeight()/2f);return true;}return true;});
        services=new TenServices(this,store,this::readingsChanged);
        GestureDetector gestures=new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            public boolean onDown(MotionEvent e){return true;}
            public boolean onFling(MotionEvent a,MotionEvent b,float vx,float vy){if(a!=null&&a.getY()-b.getY()>dp(80)&&Math.abs(vy)>300){showStart(true);return true;}return false;}
            public void onLongPress(MotionEvent e){desktopOptions(e.getRawX(),e.getRawY());}
            public boolean onSingleTapUp(MotionEvent e){startMenu.hide();return true;}
        });
        desktop.setContentDescription("Desktop workspace");
        desktop.setOnTouchListener((v,event)->{if(event.getActionMasked()==MotionEvent.ACTION_DOWN&&event.isButtonPressed(MotionEvent.BUTTON_SECONDARY)){desktopOptions(event.getRawX(),event.getRawY());return true;}return gestures.onTouchEvent(event);});
        desktop.setOnGenericMotionListener((v,event)->{if(event.getActionMasked()==MotionEvent.ACTION_BUTTON_PRESS&&event.isButtonPressed(MotionEvent.BUTTON_SECONDARY)){desktopOptions(event.getRawX(),event.getRawY());return true;}return false;});
        workspace.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->layoutWorkspace());
        buildTaskbar();
        tenNews=new TenNewsFlyout(this,workspace,store,services.weather,this::open);tenTasks=new TenTaskView(this,workspace,wallpaper,tenDesktops,this::showTenTasks);tenActions=new TenActionCenter(this,workspace,store,this::open);
        startMenu=new StartMenu(this,this,store,wallpaper);startMenu.setFocusableInTouchMode(true);startMenu.setVisibility(View.GONE);
        FrameLayout.LayoutParams menuParams=new FrameLayout.LayoutParams(dp(390),dp(520),Gravity.BOTTOM|Gravity.START);menuParams.setMargins(0,dp(4),0,0);workspace.addView(startMenu,menuParams);
        setContentView(root);shell.requestApplyInsets();renderDesktop();refreshRecycleIcon();
    }
    private void layoutWorkspace(){
        if(destroyed||workspace==null||workspace.getWidth()==0)return;int width=workspace.getWidth(),height=workspace.getHeight();if(tray!=null){tray.fitWidth(width);tray.bounds(desktopCutoutLeft,desktopCutoutRight,windowTopInset);}if(tenTasks!=null)tenTasks.safeInsets(desktopCutoutLeft,desktopCutoutRight,windowTopInset);if(tenActions!=null)tenActions.safeInsets(desktopCutoutLeft,desktopCutoutRight,windowTopInset);if(tenNews!=null)tenNews.bounds(desktopCutoutLeft,desktopCutoutRight,windowTopInset);
            WindowInsets current=root.getRootWindowInsets();java.util.List<android.graphics.Rect> holes=new ArrayList<>();
            if(current!=null&&Build.VERSION.SDK_INT>=28&&current.getDisplayCutout()!=null)holes.addAll(current.getDisplayCutout().getBoundingRects());
            desktop.configure(dp(store.tileWidth()),dp(store.tileHeight()),holes);desktop.viewport(Math.max(1,height-desktopScroll.getPaddingTop()-desktopScroll.getPaddingBottom()));
            for(DesktopWindow tool:windows.values())tool.safeTop(windowTopInset);
            if (startMenu != null) {
                boolean portrait = getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT;
                int available = Math.max(1, height - windowTopInset - dp(8));
                int menuHeight = store.prefs.getBoolean("ten_full_start",false)?available:Math.min(dp(portrait?600:520),available);
                int menuWidth = Math.min(store.prefs.getBoolean("ten_full_start",false)?width:dp(640), Math.max(1,width-desktopCutoutLeft-desktopCutoutRight));
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) startMenu.getLayoutParams();
                params.leftMargin=desktopCutoutLeft;params.rightMargin=desktopCutoutRight;params.topMargin=windowTopInset;params.bottomMargin=0;
                if (params.height != menuHeight || params.width != menuWidth) {
                    params.height = menuHeight;
                    params.width = menuWidth;
                    startMenu.setLayoutParams(params);
                }
            }
    }
    private void buildTaskbar(){
        shell.setClipChildren(false);shell.setClipToPadding(false);
        LinearLayout taskbar=new LinearLayout(this);taskbar.setContentDescription("Desktop taskbar");taskbar.setGravity(Gravity.CENTER_VERTICAL);taskbar.setClipChildren(false);taskbar.setPadding(0,0,0,0);taskbar.setBackground(new AeroGlass(wallpaper,AeroGlass.TASKBAR));taskbar.setElevation(dp(2));
        TenBrandMark orb=new TenBrandMark(this);tenStartButton=orb;orb.setContentDescription("Start menu");orb.setFocusable(true);orb.setBackground(TenStyle.hover(this,Color.TRANSPARENT));orb.setPadding(dp(1),dp(4),dp(1),dp(4));orb.setTooltipText("Start");orb.setOnClickListener(v->{if(startMenu.getVisibility()==View.VISIBLE)startMenu.hide();else showStart(false);});ContextMenus.bind(orb,(x,y)->startOrbMenu(orb,x,y));taskbar.addView(orb,new LinearLayout.LayoutParams(dp(40),dp(48)));
        String searchMode=store.prefs.getString("ten_search_mode","Auto");if(!searchMode.equals("Hidden")){boolean box=searchMode.equals("Auto")&&getResources().getConfiguration().screenWidthDp>=900;View searchButton;if(box){LinearLayout field=new LinearLayout(this);field.setGravity(Gravity.CENTER_VERTICAL);field.setBackgroundColor(0xfff2f2f2);TenStyle.Glyph symbol=new TenStyle.Glyph(this,"search",Aero.INK);field.addView(symbol,new LinearLayout.LayoutParams(dp(36),dp(38)));TextView hint=Aero.text(this,"Type here to search",12,0xff555555);field.addView(hint);searchButton=field;}else searchButton=new TenStyle.Glyph(this,"search",TenStyle.text(this));searchButton.setContentDescription("Search");searchButton.setFocusable(true);if(!box){searchButton.setPadding(dp(6),dp(12),dp(6),dp(12));searchButton.setBackground(TenStyle.hover(this,Color.TRANSPARENT));}searchButton.setTooltipText("Search");searchButton.setOnClickListener(v->open("@search"));taskbar.addView(searchButton,new LinearLayout.LayoutParams(dp(box?220:36),dp(48)));}
        TenStyle.Glyph taskViewButton=new TenStyle.Glyph(this,"taskview",TenStyle.text(this));taskViewButton.setContentDescription("Task View");taskViewButton.setPadding(dp(6),dp(12),dp(6),dp(12));taskViewButton.setTooltipText("Task View");taskViewButton.setFocusable(true);taskViewButton.setBackground(TenStyle.hover(this,Color.TRANSPARENT));taskViewButton.setOnClickListener(v->open("@taskview"));taskbar.addView(taskViewButton,new LinearLayout.LayoutParams(dp(36),dp(48)));


        tenNotaButton=taskButton("@nota","Nth Nota",false);tenNotaButton.setTag(R.id.taskbar_order_key,"@nota");tenNotaButton.setContentDescription("Open Nth Nota");tenNotaButton.setTooltipText("Nth Nota");tenNotaButton.setOnClickListener(v->{boolean overview=(tenTasks!=null&&tenTasks.overlay!=null)||startMenu.getVisibility()==View.VISIBLE;if(tenTasks!=null)tenTasks.dismiss();startMenu.hide();previews.dismiss();if(tray!=null)tray.dismiss();if(tenNews!=null)tenNews.dismiss();DesktopWindow nota=notaTaskWindow();if(!overview&&nota!=null&&nota.active()&&nota.getVisibility()==View.VISIBLE&&!nota.minimizing())nota.minimize();else open("@nota");});taskbar.addView(tenNotaButton,new LinearLayout.LayoutParams(dp(48),dp(48)));
        ContextMenus.bind(tenNotaButton,(x,y)->{DesktopContextMenu menu=new DesktopContextMenu(this,"Nth Nota taskbar menu");menu.item("Open Nth Nota","notes",()->open("@nota"));DesktopWindow nota=notaTaskWindow();if(nota!=null)menu.item("Close Nth Nota","close",nota::closeWindow);ContextMenus.show(menu,tenNotaButton,x,y);});

        HorizontalScrollView strip=new HorizontalScrollView(this){
            private float downX,downY;
            @Override public boolean onInterceptTouchEvent(MotionEvent event){
                if(event.getActionMasked()==MotionEvent.ACTION_DOWN){
                    downX=event.getX();downY=event.getY();
                }else if(event.getActionMasked()==MotionEvent.ACTION_MOVE&&event.getPointerCount()==1
                        &&TaskbarFlick.pointsUp(event.getX()-downX,event.getY()-downY)){
                    return false;
                }
                return super.onInterceptTouchEvent(event);
            }
        };taskStrip=strip;strip.setTag("taskbar-items");strip.setHorizontalScrollBarEnabled(false);strip.setFillViewport(true);strip.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(r-l>0&&taskViewportWidth!=r-l){taskViewportWidth=r-l;main.removeCallbacks(taskbarRender);main.post(taskbarRender);}});taskItems=new LinearLayout(this);taskItems.setGravity(Gravity.CENTER_VERTICAL);strip.addView(taskItems,new HorizontalScrollView.LayoutParams(-2,-1));taskItems.getViewTreeObserver().addOnGlobalLayoutListener(()->{int max=Math.max(0,taskItems.getWidth()-strip.getWidth());if(strip.getScrollX()>max)strip.scrollTo(max,0);});FrameLayout taskClip=new FrameLayout(this);taskClip.setClipChildren(true);taskClip.addView(strip,new FrameLayout.LayoutParams(-1,-1));taskbar.addView(taskClip,new LinearLayout.LayoutParams(0,dp(48),1));
        for(View surface:new View[]{taskbar,taskClip,strip,taskItems})ContextMenus.bind(surface,(x,y)->taskbarMenu(surface,x,y));
        taskbarReorder=new TaskbarReorder(taskItems,strip,order->{
            if(order!=null){List<String> pins=TaskbarOrder.apply(store.quickLaunch(),order);store.quickLaunch(pins);store.taskbarOrder(order);}
            renderTaskbar();
        });
        newsButton=new LinearLayout(this);newsButton.setGravity(Gravity.CENTER_VERTICAL);newsButton.setPadding(dp(6),0,dp(6),0);newsButton.setBackground(TenStyle.hover(this,Color.TRANSPARENT));newsButton.setFocusable(true);newsButton.setContentDescription("News and interests");newsButton.setTooltipText("News and interests");newsButton.addView(new TenStyle.Glyph(this,"news",TenStyle.text(this)),new LinearLayout.LayoutParams(dp(24),dp(32)));newsText=Aero.text(this,"News",12,TenStyle.text(this));newsText.setSingleLine();newsText.setEllipsize(TextUtils.TruncateAt.END);newsButton.addView(newsText,new LinearLayout.LayoutParams(-2,-2));newsButton.setOnClickListener(v->open("@news"));newsButton.setOnHoverListener((v,event)->{if(event.getActionMasked()==MotionEvent.ACTION_HOVER_ENTER&&store.prefs.getBoolean("ten_news_hover",false)){v.postDelayed(()->{if(v.isHovered()&&tenNews!=null&&!tenNews.shown())open("@news");},450);}return false;});ContextMenus.bind(newsButton,(x,y)->newsMenu(newsButton,x,y));taskbar.addView(newsButton,new LinearLayout.LayoutParams(-2,dp(48)));updateNewsChip();
        tray=new SystemTray(this,workspace,wallpaper,new SystemTray.Host(){public void action(String id){vistaAction(id);}public void open(String id){MainActivity.this.open(id);}public void layout(){main.post(()->{layoutWorkspace();renderTaskbar();});}public void opening(){if(tenNews!=null)tenNews.dismiss();if(previews!=null)previews.dismiss();startMenu.hide();((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(),0);}});taskbar.addView(tray,new LinearLayout.LayoutParams(-2,dp(48)));
        clock=Aero.text(this,"",10,TenStyle.text(this));clock.setGravity(Gravity.CENTER);clock.setMaxLines(2);clock.setIncludeFontPadding(false);clock.setPadding(dp(5),0,dp(5),0);clock.setMinWidth(dp(52));clock.setBackground(TenStyle.hover(this,Color.TRANSPARENT));clock.setOnClickListener(v->tray.show("calendar"));clock.setContentDescription("Clock and calendar");taskbar.addView(clock,new LinearLayout.LayoutParams(-2,dp(44)));
        TenStyle.Glyph actionButton=new TenStyle.Glyph(this,"actions",TenStyle.text(this));actionButton.setContentDescription("Action Center");actionButton.setPadding(dp(4),dp(12),dp(4),dp(12));actionButton.setTooltipText("Action Center");actionButton.setFocusable(true);actionButton.setBackground(TenStyle.hover(this,Color.TRANSPARENT));actionButton.setOnClickListener(v->open("@tenactions"));taskbar.addView(actionButton,new LinearLayout.LayoutParams(dp(32),dp(48)));
        FrameLayout peek=new FrameLayout(this);desktopToggle=peek;View peekStrip=new View(this);peekStrip.setBackgroundColor(0xff777777);peek.addView(peekStrip,new FrameLayout.LayoutParams(dp(1),-1,Gravity.END));peek.setContentDescription("Show desktop");peek.setOnClickListener(v->{previews.dismiss();showDesktop();});ContextMenus.bind(peek,(x,y)->taskbarMenu(peek,x,y));previews.bindDesktopPeek(peek);taskbar.addView(peek,new LinearLayout.LayoutParams(dp(6),dp(48)));shell.addView(taskbar,new LinearLayout.LayoutParams(-1,dp(48)));updateClock();renderTaskbar();
    }
    /** Closed windows can remain cached; only attached windows are running tasks. */
    private DesktopWindow notaTaskWindow(){DesktopWindow nota=windows.get("@nota");return nota!=null&&nota.getParent()==workspace?nota:null;}
    private void updateNewsChip(){
        if(newsButton==null)return;String mode=store.prefs.getString("ten_news_mode","Auto");newsButton.setVisibility(mode.equals("Hidden")?View.GONE:View.VISIBLE);boolean wide=getResources().getConfiguration().screenWidthDp>=600;newsText.setVisibility(!mode.equals("Icon")&&wide?View.VISIBLE:View.GONE);String caption="News";try{WeatherClient.Snapshot snapshot=WeatherClient.Snapshot.parse(store.prefs.getString("weather_json",""));boolean f=store.prefs.getBoolean("weather_f",Locale.getDefault().getCountry().equals("US"));caption=snapshot.temperature(f)+"°"+(wide?"  "+snapshot.condition():"");if(!mode.equals("Icon"))newsText.setVisibility(View.VISIBLE);}catch(Exception ignored){JSONArray articles=FeedClient.items(store);if(articles.length()>0)caption="Top stories";}newsText.setText(caption);newsText.setMaxWidth(dp(150));newsButton.setTooltipText("News and interests · "+caption);
    }
    private void newsMenu(View anchor,float x,float y){DesktopContextMenu menu=new DesktopContextMenu(this,"News and interests options");for(String mode:new String[]{"Auto","Icon","Hidden"})menu.item((mode.equals(store.prefs.getString("ten_news_mode","Auto"))?"✓  ":"")+(mode.equals("Auto")?"Show icon and text":mode.equals("Icon")?"Show icon only":"Turn off"),"news",()->{store.prefs.edit().putString("ten_news_mode",mode).apply();updateNewsChip();});menu.separator();menu.item((store.prefs.getBoolean("ten_news_hover",false)?"✓  ":"")+"Open on hover","news",()->store.prefs.edit().putBoolean("ten_news_hover",!store.prefs.getBoolean("ten_news_hover",false)).apply());menu.item("Open news and interests","news",()->open("@news"));ContextMenus.show(menu,anchor,x,y);}
    private android.graphics.PointF taskAnchor(String id){int[] host=new int[2];workspace.getLocationOnScreen(host);if(id.equals("@nota")&&tenNotaButton!=null){int[] p=new int[2];tenNotaButton.getLocationOnScreen(p);return new android.graphics.PointF(p[0]-host[0]+tenNotaButton.getWidth()/2f,p[1]-host[1]+tenNotaButton.getHeight()/2f);}for(int i=0;i<taskItems.getChildCount();i++){View button=taskItems.getChildAt(i);String key=TaskbarReorder.key(button);if(id.equals(key)||(folderTask(id,windows.get(id))&&"@files".equals(key))){int[] point=new int[2];button.getLocationOnScreen(point);if(point[0]>=host[0]&&point[0]<host[0]+workspace.getWidth())return new android.graphics.PointF(point[0]-host[0]+button.getWidth()/2f,point[1]-host[1]+button.getHeight()/2f);}}return new android.graphics.PointF(dp(40),workspace.getHeight()+dp(24));}
    void nota(TenNotaContext context){
        if(context.text.isEmpty()){open("@nota");return;}DesktopWindow existing=windows.get("@nota");Runnable load=()->{open("@nota");DesktopWindow tool=windows.get("@nota");if(tool!=null&&tool.body instanceof TenNotaPanel)((TenNotaPanel)tool.body).context(context);};
        if(existing!=null&&existing.getParent()!=null&&existing.body instanceof TenNotaPanel&&!((TenNotaPanel)existing.body).input.getText().toString().isEmpty())Aero.dialog(this).setTitle("Bring text into Nth Nota?").setMessage("Replace the current composer with the passage from "+context.title+"? Your source is kept.").setPositiveButton("Use passage",(d,w)->load.run()).setNegativeButton("Cancel",null).show();else load.run();
    }
    private void askNota(DesktopWindow source){
        if(source.body instanceof BrowserPanel){((BrowserPanel)source.body).askNota();return;}
        if(source.body instanceof VistaExplorer){((VistaExplorer)source.body).askNota();return;}
        View focus=source.findFocus();EditText editor=focus instanceof EditText?(EditText)focus:findEditor(source.body);
        if(editor!=null&&!(source.body instanceof TenSettings)){nota(TenNotaContext.editor(source.caption(),editor));return;}
        if(source.body instanceof TenSettings){TenSettingsCatalog.Page page=TenSettingsCatalog.page(((TenSettings)source.body).page);nota(new TenNotaContext("Settings",page==null?"Help me organize my Android desktop.":"Explain this Nth Desktop Ten setting in simple terms: "+page.title+". "+page.detail,false));return;}
        nota(new TenNotaContext(source.caption(),"Help me use "+source.caption()+" in Nth Desktop Ten, an Android launcher. Ask me what I want to do if needed.",false));
    }
    private EditText findEditor(View view){if(view instanceof EditText)return (EditText)view;if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){EditText found=findEditor(group.getChildAt(i));if(found!=null)return found;}}return null;}
    private void scanApps(){
        if(destroyed)return;if(scanPending){scanAgain=true;return;}scanPending=true;int generation=++scanGeneration;
        worker.execute(()->{
            List<AppCatalog.Entry> apps;
            try{apps=AppCatalog.load(getApplicationContext());}catch(RuntimeException e){apps=new ArrayList<>();}
            List<AppCatalog.Entry> result=apps;ExternalTasks.refresh(getApplicationContext(),store,result);
            main.post(()->{if(destroyed||restoringBackup||generation!=scanGeneration)return;scanPending=false;taskColors.clear();entries.clear();for(AppCatalog.Entry e:result)entries.put(e.id,e);startMenu.entries(result);renderDesktop();renderTaskbar();for(DesktopWindow tool:windows.values())if(tool.getParent()!=null&&tool.body instanceof VistaShell)((VistaShell)tool.body).refreshApps();if(scanAgain){scanAgain=false;scanApps();}});
        });
    }
    int desktopGridWidth(){return desktop==null?0:desktop.getWidth();}
    int desktopGridHeight(){return desktop==null?0:desktop.viewportHeight();}
    private void renderDesktop(){
        if(desktop==null)return;desktop.removeAllViews();List<String> ids=store.desktop();desktop.configure(dp(store.tileWidth()),dp(store.tileHeight()),java.util.Collections.emptyList());
        for(int index=0;index<ids.size();index++){
            String id=ids.get(index);LinearLayout tile=Aero.column(this);tile.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);tile.setPadding(dp(2+store.spacingX()/2),dp(6+store.spacingY()/2),dp(2+store.spacingX()/2),dp(3+store.spacingY()/2));int iconSize=store.iconSize();View icon=icon(id,iconSize);tile.addView(icon,new LinearLayout.LayoutParams(dp(iconSize),dp(iconSize)));
            TextView label=Aero.text(this,label(id),store.labelSize(),Color.WHITE);label.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);label.setMaxLines(2);label.setEllipsize(TextUtils.TruncateAt.END);label.setShadowLayer(dp(3),0,dp(1),0xff000000);label.setVisibility(store.iconLabels()?View.VISIBLE:View.GONE);tile.addView(label,new LinearLayout.LayoutParams(-1,-2));
            android.graphics.drawable.StateListDrawable background=new android.graphics.drawable.StateListDrawable();background.addState(new int[]{android.R.attr.state_pressed},Aero.panel(this,0x44d0edff,0x2270add0,3,0x779fcde4));background.addState(new int[]{},Aero.panel(this,0x00ffffff,0x00ffffff,3,0));tile.setBackground(background);tile.setContentDescription(label(id)+". Hold and drag onto an icon to group, or near its edge to swap positions.");tile.setOnClickListener(v->open(id));tile.setFocusable(true);ContextMenus.desktopIcon(tile,(x,y)->shortcutMenu(tile,id,"desktop",x,y),()->{if(!id.equals("@recycle")&&!store.prefs.getBoolean("layout_locked",false))tile.startDragAndDrop(ClipData.newPlainText("desktop-shortcut",id),new View.DragShadowBuilder(tile),id,0);});
            if(id.equals("@recycle"))tile.setOnDragListener(this::recycleDrop);
            else tile.setOnDragListener((v,event)->{boolean own=event.getLocalState() instanceof String&&!((String)event.getLocalState()).startsWith("gadget:");switch(event.getAction()){
                case DragEvent.ACTION_DRAG_STARTED:return own;case DragEvent.ACTION_DRAG_ENTERED:if(own)v.setAlpha(.5f);return own;
                case DragEvent.ACTION_DRAG_EXITED:case DragEvent.ACTION_DRAG_ENDED:v.setAlpha(1);return own;
                case DragEvent.ACTION_DROP:if(!own||store.prefs.getBoolean("layout_locked",false))return false;String moving=(String)event.getLocalState();boolean group=event.getX()>v.getWidth()*.18f&&event.getX()<v.getWidth()*.82f&&event.getY()>v.getHeight()*.1f&&event.getY()<v.getHeight()*.8f;try{String folder=store.drop(moving,id,group);if(!group)desktop.swapPositions(moving,id);else if(folder!=null&&!folder.equals(id))desktop.place(folder,v.getX(),v.getY());desktop.post(()->{desktopChanged();if(folder!=null)open(folder);});}catch(IllegalArgumentException error){toast(error.getMessage());}return true;default:return own;}});
            tile.setTag(id);ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams(dp(store.tileWidth()),dp(store.tileHeight()));desktop.addView(tile,lp);
        }
        desktop.post(this::layoutWorkspace);
    }
    private boolean recycleDrop(View target,DragEvent event){
        Object payload=event.getLocalState();
        boolean file=payload instanceof FileDrag;
        boolean shortcut=payload instanceof String&&!payload.equals("@recycle")
                &&!store.prefs.getBoolean("layout_locked",false)&&store.desktop().contains(payload);
        if(event.getAction()==DragEvent.ACTION_DRAG_ENDED||event.getAction()==DragEvent.ACTION_DRAG_EXITED)target.setAlpha(1f);
        if(!file&&!shortcut)return false;
        if(event.getAction()==DragEvent.ACTION_DRAG_ENTERED)target.setAlpha(.6f);
        if(event.getAction()==DragEvent.ACTION_DROP){
            if(file){FileDrag drag=(FileDrag)payload;main.post(()->drag.explorer.dropInRecycleBin(drag));}
            else main.post(()->{String id=(String)payload;RecycleBin.trashShortcut(this,store,"desktop",id,label(id));desktopChanged();});
        }
        return true;
    }

    private void renderTaskbar(){
        if(taskItems==null||(taskbarReorder!=null&&taskbarReorder.dragging()))return;View priorTaskFocus=taskItems.findFocus();String focusedTask=priorTaskFocus==null?null:TaskbarReorder.key(priorTaskFocus);DesktopWindow weatherWindow=windows.get("@weather"),feedWindow=windows.get("@gadget:feeds"),networkWindow=windows.get("@gadget:network"),compassWindow=windows.get("@gadget:compass");if(services!=null)services.details(weatherWindow!=null&&weatherWindow.getParent()!=null&&weatherWindow.getVisibility()==View.VISIBLE,feedWindow!=null&&feedWindow.getParent()!=null&&feedWindow.getVisibility()==View.VISIBLE,networkWindow!=null&&networkWindow.getParent()!=null&&networkWindow.getVisibility()==View.VISIBLE,compassWindow!=null&&compassWindow.getParent()!=null&&compassWindow.getVisibility()==View.VISIBLE);DesktopWindow front=null;for(int i=workspace.getChildCount()-1;i>=0;i--){View child=workspace.getChildAt(i);if(child instanceof DesktopWindow&&child.getVisibility()==View.VISIBLE&&!((DesktopWindow)child).minimizing()){front=(DesktopWindow)child;break;}}for(DesktopWindow window:windows.values())if(window.getParent()!=null){window.active(window==front);}taskProgressSurfaces.clear();taskItems.removeAllViews();
        List<String> taskIds=new ArrayList<>();
        Set<String> explorerIds=new HashSet<>();
        for(Map.Entry<String,DesktopWindow> item:windows.entrySet())if(item.getValue().getParent()!=null){
            if(!item.getKey().equals("@nota"))taskIds.add(item.getKey());
            if(folderTask(item.getKey(),item.getValue()))explorerIds.add(item.getKey());
        }
        List<String> external=new ArrayList<>();
        if(store.prefs.getBoolean("taskbar_apps",true)){
            for(String id:ExternalTasks.list())if(entries.containsKey(id))external.add(id);
        }
        boolean compactTasks=getResources().getConfiguration().screenWidthDp<480&&(!taskIds.isEmpty()||!ExternalTasks.list().isEmpty());
        boolean fit=store.prefs.getBoolean("taskbar_static",false);
        boolean showLabels=store.prefs.getBoolean("taskbar_show_labels",false);
        int buttonWidth=dp(showLabels?152:48), gap=0;
        List<String> pins=new ArrayList<>(store.quickLaunch());pins.remove("@nota");
        Map<String,LinearLayout> buttons=new LinkedHashMap<>();
        int viewport=taskViewportWidth>0?taskViewportWidth:dp(240);
        int taskRoom=viewport;
        int firstTask=taskItems.getChildCount();
        int occupied=external.size();
        for(String pin:pins)if(!taskIds.contains(pin)&&!external.contains(pin))occupied++;
        String grouping=store.prefs.getString("taskbar_grouping","Always");
        List<String> folderPins=new ArrayList<>();
        for(String pin:pins)if(folderTask(pin,windows.get(pin)))folderPins.add(pin);
        String folderSlot=folderPins.contains("@files")?"@files":folderPins.isEmpty()?null:folderPins.get(0);
        boolean familyInPinnedSlot=false;
        boolean mousePreviews=store.prefs.getBoolean("taskbar_previews",true);
        for(List<String> group:TaskbarGrouping.groups(taskIds,explorerIds,grouping,taskRoom-occupied*(buttonWidth+gap),buttonWidth+gap)){
            DesktopWindow first=windows.get(group.get(0));boolean grouped=group.size()>1,isActive=false;List<DesktopWindow> members=new ArrayList<>();for(String id:group){DesktopWindow window=windows.get(id);members.add(window);isActive|=window.active();if(window.active())first=window;}DesktopWindow target=first;
            String title=grouped?"Folders ("+group.size()+")":target.caption();LinearLayout button=taskButton(grouped&&!showLabels?"@files":target.toolId,title,showLabels);AeroGlass surface=new AeroGlass(wallpaper,AeroGlass.TASKBUTTON);surface.active(isActive);surface.stacked(grouped&&!showLabels);button.setBackground(surface);button.setSelected(isActive);button.setContentDescription(grouped?"Folder windows, "+group.size()+" windows":(isActive?"Minimize ":"Restore ")+title);
            if(members.stream().anyMatch(window->window.body instanceof VistaExplorer||window.body instanceof BrowserPanel))
                taskProgressSurfaces.put(surface,members);
            if(target.toolId.equals("@recycle"))button.setOnDragListener(this::recycleDrop);
            boolean pinnedExplorerFamily=folderSlot!=null&&explorerIds.contains(target.toolId)&&explorerIds.size()==group.size();
            ContextMenus.Open commands=(x,y)->{
                previews.dismiss();startMenu.hide();tray.dismiss();if(tenNews!=null)tenNews.dismiss();
                if(!grouped&&!(pinnedExplorerFamily&&folderPins.size()>1)){target.showSystemMenu(button,x,y);return;}
                DesktopContextMenu menu=new DesktopContextMenu(this,"Folder group commands");
                menu.item("Open new Explorer window","files",()->newExplorer("libraries"));
                for(String pin:folderPins)if(!group.contains(pin))menu.item("Open "+label(pin),"files",()->open(pin));
                menu.separator();
                for(DesktopWindow window:members)menu.item(window.caption(),"files",window::restore);
                menu.separator();
                menu.item("Minimize group","empty",()->{for(DesktopWindow window:members)window.minimize();});
                menu.item("Close group","close",()->{for(DesktopWindow window:members)window.closeWindow();});
                ContextMenus.show(menu,button,x,y);
            };
            if(hasJumpList(target.toolId))ContextMenus.bindJumpList(button,(x,y)->showJumpList(button,target.toolId,x,y));
            else ContextMenus.bind(button,commands);
            if(!grouped)button.setOnClickListener(v->{boolean overview=tenTasks!=null&&tenTasks.overlay!=null;if(tenTasks!=null)tenTasks.dismiss();previews.dismiss();startMenu.hide();tray.dismiss();if(tenNews!=null)tenNews.dismiss();if(!overview&&target.getVisibility()==View.VISIBLE&&!target.minimizing()&&target.active())target.minimize();else target.restore();});
            previews.bindTask(button,members,mousePreviews,()->ContextMenus.centered(button,commands));
            LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(showLabels&&grouped?dp(112):buttonWidth,dp(48));
            size.rightMargin=gap;
            button.setLayoutParams(size);
            familyInPinnedSlot|=pinnedExplorerFamily;
            String slot=pinnedExplorerFamily?folderSlot:target.toolId;
            taskbarButton(button,slot,target.toolId);buttons.put(slot,button);
        }
        for(String id:external){
            String title=label(id);
            LinearLayout button=taskButton(id,title,showLabels);button.setContentDescription("Switch to "+title);
            AeroGlass surface=new AeroGlass(wallpaper,AeroGlass.TASKBUTTON);surface.active(false);button.setBackground(surface);
            button.setOnClickListener(v->open(id));
            ContextMenus.Open commands=(x,y)->{
                previews.dismiss();
                DesktopContextMenu menu=new DesktopContextMenu(this,"Android app taskbar menu");
                menu.item("Switch to "+title,"programs",()->open(id));
                menu.item("Remove taskbar button","close",()->{ExternalTasks.remove(id);renderTaskbar();});
                menu.item("Taskbar Properties","control",()->open("setting:page:taskbar"));
                ContextMenus.show(menu,button,x,y);
            };
            ContextMenus.bindJumpList(button,(x,y)->showJumpList(button,id,x,y));
            previews.bindApp(button,title,true,mousePreviews,()->open(id),()->ContextMenus.centered(button,commands));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(buttonWidth,dp(48));
            lp.rightMargin=gap;button.setLayoutParams(lp);taskbarButton(button,id,id);buttons.put(id,button);
        }
        for(String id:pins){
            if(familyInPinnedSlot&&!id.equals(folderSlot)&&folderTask(id,windows.get(id)))continue;
            LinearLayout button=buttons.remove(id);
            if(button==null){
                button=taskButton(id,label(id),showLabels);
                button.setContentDescription("Open "+label(id));
                button.setTooltipText(label(id));
                AeroGlass idle=new AeroGlass(wallpaper,AeroGlass.TASKBUTTON);idle.active(false);idle.idle(true);button.setBackground(idle);
                button.setOnClickListener(v->open(id));
                LinearLayout pinned=button;
                ContextMenus.Open commands=(x,y)->{previews.dismiss();shortcutMenu(pinned,id,"quick",x,y);};
                if(hasJumpList(id))ContextMenus.bindJumpList(pinned,(x,y)->showJumpList(pinned,id,x,y));
                else ContextMenus.bind(pinned,commands);
                previews.bindApp(pinned,label(id),false,false,()->open(id),()->ContextMenus.centered(pinned,commands));
                LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(buttonWidth,dp(48));
                size.rightMargin=gap;
                button.setLayoutParams(size);taskbarButton(button,id,id);
            }
            taskItems.addView(button);
        }
        for(LinearLayout button:buttons.values())taskItems.addView(button);
        Map<String,View> ordered=new LinkedHashMap<>();
        for(int i=firstTask;i<taskItems.getChildCount();i++){View button=taskItems.getChildAt(i);ordered.put(TaskbarReorder.key(button),button);}
        List<String> order=TaskbarOrder.apply(new ArrayList<>(ordered.keySet()),store.taskbarOrder());
        for(int i=taskItems.getChildCount()-1;i>=firstTask;i--)taskItems.removeViewAt(i);
        for(String id:order)taskItems.addView(ordered.get(id));
        if(focusedTask!=null&&ordered.containsKey(focusedTask))ordered.get(focusedTask).requestFocus();
        if(fit){
            fitTaskButtons(firstTask,taskRoom);
            if(taskStrip.getScrollX()!=0)taskStrip.scrollTo(0,0);
        }

        if(tenNotaButton!=null){DesktopWindow nota=notaTaskWindow();boolean active=nota!=null&&nota.active()&&nota.getVisibility()==View.VISIBLE&&!nota.minimizing();AeroGlass surface=new AeroGlass(wallpaper,AeroGlass.TASKBUTTON);surface.active(active);surface.idle(nota==null);tenNotaButton.setBackground(surface);tenNotaButton.setSelected(active);tenNotaButton.setContentDescription(nota==null?"Open Nth Nota":(active?"Minimize ":"Restore ")+"Nth Nota");}
        desktopHidden.entrySet().removeIf(item->item.getValue().getParent()==null||windows.get(item.getKey())!=item.getValue());if(desktopToggle!=null){String hint=desktopHidden.isEmpty()?"Show desktop":"Restore desktop windows";desktopToggle.setContentDescription(hint);desktopToggle.setTooltipText(hint+". Hold to peek.");}
        main.removeCallbacks(taskProgressTick);
        if(!taskProgressSurfaces.isEmpty())main.post(taskProgressTick);
        DesktopWindow manager=windows.get("@taskmanager");if(manager!=null&&manager.getParent()!=null&&manager.body instanceof TaskManagerPanel)((TaskManagerPanel)manager.body).tasksChanged();
    }
    private void refreshTaskProgress(){
        DesktopWindow browser=windows.get("@browser");
        BrowserPanel panel=browser!=null&&browser.getParent()==workspace?(BrowserPanel)browser.body:null;
        if(panel!=downloadBrowser){downloadBrowser=panel;browserProgress=-2;nextDownloadPoll=0;}
        if(panel!=null&&!queryingDownloads&&SystemClock.uptimeMillis()>=nextDownloadPoll&&panel.hasDownloads()){
            queryingDownloads=true;
            worker.execute(()->{
                int progress=panel.downloadProgress();
                main.post(()->{
                    queryingDownloads=false;
                    if(downloadBrowser==panel){
                        browserProgress=progress;
                        nextDownloadPoll=SystemClock.uptimeMillis()+(progress==-2?5000:750);
                    }
                });
            });
        }
        for(Map.Entry<AeroGlass,List<DesktopWindow>> item:taskProgressSurfaces.entrySet()){
            int progress=-2;
            for(DesktopWindow window:item.getValue()){
                if(window.body instanceof VistaExplorer)progress=Math.max(progress,((VistaExplorer)window.body).taskProgress());
                if(window.body==downloadBrowser)progress=Math.max(progress,browserProgress);
            }
            item.getKey().progress(progress);
        }
    }

    private boolean hasJumpList(String id){
        return folderTask(id,windows.get(id))||id.equals("@browser")||id.equals(MediaLibrary.PLAYER)||entries.containsKey(id);
    }
    private void showJumpList(View anchor,String id,float x,float y){
        if(jumpMenu!=null)jumpMenu.dismiss();
        previews.dismiss();
        DesktopWindow active=windows.get(id);
        boolean folders=folderTask(id,active);
        String title=folders?"File Explorer":label(id);
        DesktopContextMenu menu=new DesktopContextMenu(this,title+" Jump List",34,264);
        jumpMenu=menu;
        if(folders){
            JSONArray pinned=jumpFolders();
            if(pinned.length()>0)menu.section("Pinned folders");
            for(int i=0;i<pinned.length();i++){
                JSONObject item=pinned.optJSONObject(i);
                if(item!=null)menu.item(item.optString("name","Folder"),"files",()->newExplorer(item.optString("page")));
            }
            JSONArray recent=RecentFiles.list(store);
            Set<String> seen=new HashSet<>();
            for(int i=0;i<pinned.length();i++){JSONObject item=pinned.optJSONObject(i);if(item!=null)seen.add(item.optString("page"));}
            int shown=0;
            for(int i=0;i<recent.length()&&shown<5;i++){
                JSONObject item=recent.optJSONObject(i);
                if(item==null||!item.optString("kind").equals("folder")||!seen.add(item.optString("page")))continue;
                if(shown++==0)menu.section("Recent folders");
                menu.item(item.optString("name","Folder"),"files",()->newExplorer(item.optString("page")));
            }
            menu.section("Tasks");
            menu.item("Open new Explorer window","files",()->newExplorer("libraries"));
            if(pinned.length()>0)menu.item("Manage pinned folders…","files",()->{
                List<JSONObject> records=new ArrayList<>();
                List<String> names=new ArrayList<>();
                for(int i=0;i<pinned.length();i++){
                    JSONObject item=pinned.optJSONObject(i);
                    if(item!=null){records.add(item);names.add(item.optString("name","Folder"));}
                }
                Aero.dialog(this).setTitle("Choose a folder to unpin")
                        .setItems(names.toArray(new String[0]),(d,i)->{
                            JSONObject item=records.get(i);
                            toggleJumpFolder(item.optString("page"),item.optString("name"));
                        }).setNegativeButton("Cancel",null).show();
            });
            if(active!=null&&active.body instanceof VistaExplorer){
                String page=((VistaExplorer)active.body).page();
                if(page.startsWith("folder:")){
                    boolean alreadyPinned=false;
                    for(int i=0;i<pinned.length();i++){JSONObject item=pinned.optJSONObject(i);if(item!=null&&page.equals(item.optString("page")))alreadyPinned=true;}
                    menu.item(alreadyPinned?"Unpin current folder":"Pin current folder","files",()->toggleJumpFolder(page,active.caption()));
                }
            }
        }else if(id.equals("@browser")){
            JSONArray favorites=new BrowserData(this).list("favorites");
            if(favorites.length()>0)menu.section("Favorites");
            for(int i=0;i<Math.min(5,favorites.length());i++){
                JSONObject item=favorites.optJSONObject(i);
                if(item!=null)menu.item(item.optString("title","Favorite"),"browser",()->open("favorite-web:"+item.optString("url")));
            }
            menu.section("Tasks");
            menu.item("New tab","browser",()->{
                open("@browser");
                DesktopWindow browser=windows.get("@browser");
                if(browser!=null&&browser.body instanceof BrowserPanel)((BrowserPanel)browser.body).addTab("",false);
            });
        }else if(id.equals(MediaLibrary.PLAYER)){
            if(playback!=null&&playback.current()!=null){
                menu.section("Now playing");
                menu.item("Resume "+playback.current().name,"musicfile",()->{
                    mediaPlayer().showNow();
                    if(playback.player.getPlaybackState()==androidx.media3.common.Player.STATE_ENDED)playback.player.seekToDefaultPosition();
                    playback.player.play();
                });
            }
            menu.section("Tasks");
            menu.item("Open media library","mediaplayer",()->open(MediaLibrary.PLAYER));
            List<MediaLibrary.Entry> recent=mediaLibrary().recent();
            if(!recent.isEmpty()){
                menu.section("Recent media");
                for(MediaLibrary.Entry entry:recent.subList(0,Math.min(4,recent.size())))
                    menu.item(entry.name,"musicfile",()->openMedia(entry));
            }
            menu.item("Add media files…","files",this::chooseMedia);
        }else{
            addAndroidShortcuts(menu,id);
        }
        menu.separator();
        menu.item("Open "+title,"programs",()->open(id));
        String pin=folders?"@files":id;
        boolean pinned=store.quickLaunch().contains(pin);
        menu.item(pinned?"Unpin from taskbar":"Pin to taskbar","programs",()->{
            List<String> list=store.quickLaunch();
            if(pinned)list.remove(pin);
            else if(list.size()>=DesktopStore.MAX_QUICK){toast("Remove a taskbar pin before adding another.");return;}
            else list.add(pin);
            store.quickLaunch(list);
            renderTaskbar();
        });
        if(active!=null&&active.getParent()==workspace)menu.item("Close "+active.caption(),"close",active::closeWindow);
        ContextMenus.show(menu,anchor,x,y);
    }
    private void addAndroidShortcuts(DesktopContextMenu menu,String id){
        ComponentName app=ComponentName.unflattenFromString(id);
        android.content.pm.LauncherApps launcher=getSystemService(android.content.pm.LauncherApps.class);
        if(app==null||launcher==null||!launcher.hasShortcutHostPermission())return;
        try{
            android.content.pm.LauncherApps.ShortcutQuery query=new android.content.pm.LauncherApps.ShortcutQuery()
                    .setPackage(app.getPackageName()).setActivity(app)
                    .setQueryFlags(android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                            |android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                            |android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED);
            List<android.content.pm.ShortcutInfo> items=launcher.getShortcuts(query,android.os.Process.myUserHandle());
            if(items==null||items.isEmpty())return;
            menu.section("App shortcuts");
            int shown=0;
            for(android.content.pm.ShortcutInfo item:items){
                if(!item.isEnabled()||item.getShortLabel()==null)continue;
                menu.item(item.getShortLabel().toString(),"programs",()->{
                    try{launcher.startShortcut(item,null,null);}
                    catch(RuntimeException unavailable){toast("This app shortcut is unavailable.");}
                });
                if(++shown==5)break;
            }
        }catch(SecurityException|IllegalStateException unavailable){}
    }
    private JSONArray jumpFolders(){
        try{return new JSONArray(store.prefs.getString("seven_jump_folders","[]"));}
        catch(JSONException invalid){return new JSONArray();}
    }
    private void toggleJumpFolder(String page,String name){
        JSONArray saved=jumpFolders(),next=new JSONArray();
        boolean found=false;
        for(int i=0;i<saved.length();i++){
            JSONObject item=saved.optJSONObject(i);
            if(item==null)continue;
            if(page.equals(item.optString("page")))found=true;else next.put(item);
        }
        if(!found){
            if(next.length()>=8){toast("Unpin a folder before adding another.");return;}
            JSONObject item=new JSONObject();
            GadgetLayout.put(item,"page",page);
            GadgetLayout.put(item,"name",name);
            next.put(item);
        }
        store.prefs.edit().putString("seven_jump_folders",next.toString()).apply();
    }
    private boolean folderTask(String id,DesktopWindow window){
        return window!=null&&(window.body instanceof VistaExplorer||window.body instanceof DesktopOrganizer
                &&(store.isFolder(id)||id.startsWith("@organizer:")))
                ||FolderLinks.id(id)||store.isFolder(id)||Arrays.asList("@files","@computer","@documents","@pictures","@music","@filemanagers").contains(id);
    }
    private int addQuickLaunch(boolean compact,boolean fit,int viewport,boolean hasTasks){
        if(compact)return 0;
        List<String> shortcuts=store.quickLaunch();
        int cell=dp(40),separator=dp(12);
        int slots=shortcuts.size();
        if(fit){
            int budget=hasTasks?Math.min(viewport/2,Math.max(0,viewport-dp(72))):viewport;
            slots=Math.min(slots,Math.max(0,(budget-separator)/cell));
            if(budget<separator)return 0;
        }
        boolean overflow=slots<shortcuts.size();
        int visible=overflow?Math.max(0,slots-1):slots;
        for(int i=0;i<visible;i++){
            String id=shortcuts.get(i);
            FrameLayout quick=new FrameLayout(this);
            quick.addView(icon(id,24),new FrameLayout.LayoutParams(dp(24),dp(24),Gravity.CENTER));
            quick.setContentDescription("Open "+label(id));
            quick.setOnClickListener(v->open(id));
            ContextMenus.Open commands=(x,y)->{previews.dismiss();shortcutMenu(quick,id,"quick",x,y);};
            if(hasJumpList(id))ContextMenus.bindJumpList(quick,(x,y)->showJumpList(quick,id,x,y));
            else ContextMenus.bind(quick,commands);
            previews.bindApp(quick,label(id),false,false,()->open(id),()->ContextMenus.centered(quick,commands));
            taskItems.addView(quick,new LinearLayout.LayoutParams(cell,dp(48)));
        }
        if(overflow&&slots>0){
            TextView more=Aero.text(this,"»",19,Color.WHITE);
            more.setGravity(Gravity.CENTER);
            more.setContentDescription("More Quick Launch shortcuts");
            more.setOnClickListener(v->{
                DesktopContextMenu menu=new DesktopContextMenu(this,"More Quick Launch shortcuts");
                for(String id:shortcuts.subList(visible,shortcuts.size()))menu.item(label(id),"programs",()->open(id));
                ContextMenus.centered(more,(x,y)->ContextMenus.show(menu,more,x,y));
            });
            taskItems.addView(more,new LinearLayout.LayoutParams(cell,dp(48)));
        }
        View divider=new TaskbarSeparator(this);
        divider.setContentDescription("Quick Launch separator");
        ContextMenus.bind(divider,(x,y)->taskbarMenu(divider,x,y));
        taskItems.addView(divider,new LinearLayout.LayoutParams(separator,dp(48)));
        return slots*cell+separator;
    }
    private void fitTaskButtons(int first,int room){
        int count=taskItems.getChildCount()-first;
        if(count==0)return;
        int preferred=0;
        for(int i=first;i<taskItems.getChildCount();i++){
            LinearLayout.LayoutParams size=(LinearLayout.LayoutParams)taskItems.getChildAt(i).getLayoutParams();
            preferred+=size.width+size.rightMargin;
        }
        if(preferred<=room)return;
        int gap=room/count>=dp(32)?dp(3):0;
        int width=Math.max(0,room-count*gap);
        for(int i=0;i<count;i++){
            LinearLayout button=(LinearLayout)taskItems.getChildAt(first+i);
            LinearLayout.LayoutParams size=(LinearLayout.LayoutParams)button.getLayoutParams();
            size.width=width/count+(i<width%count?1:0);
            size.rightMargin=gap;
            button.setLayoutParams(size);
            if(size.width<dp(46)){
                button.setGravity(Gravity.CENTER);
                button.setPadding(0,0,0,0);
                button.getChildAt(1).setVisibility(View.GONE);
                View icon=button.getChildAt(0);
                int side=Math.min(icon.getLayoutParams().width,size.width);
                icon.setLayoutParams(new LinearLayout.LayoutParams(side,side));
            }
        }
    }
    private void taskbarButton(View button,String slot,String id){
        button.setTag(R.id.taskbar_order_key,slot);
        if(button.getBackground() instanceof AeroGlass){
            Integer color=taskColors.get(id);
            if(color==null){
                android.graphics.Bitmap image=android.graphics.Bitmap.createBitmap(32,32,android.graphics.Bitmap.Config.ARGB_8888);
                View sample=icon(id,32);sample.measure(View.MeasureSpec.makeMeasureSpec(32,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(32,View.MeasureSpec.EXACTLY));sample.layout(0,0,32,32);sample.draw(new android.graphics.Canvas(image));
                int[] pixels=new int[1024];image.getPixels(pixels,0,32,0,0,32,32);image.recycle();color=TaskbarColors.dominant(pixels);
                if(taskColors.size()>128)taskColors.clear();taskColors.put(id,color);
            }
            ((AeroGlass)button.getBackground()).accent(color);
        }
        TaskbarTouch.bind(button,taskbarReorder,hasJumpList(id)?(x,y)->showJumpList(button,id,x,y):null);
    }
    private LinearLayout taskButton(String id,String title,boolean showLabel){
        LinearLayout button=new LinearLayout(this);
        button.setGravity(showLabel?Gravity.CENTER_VERTICAL:Gravity.CENTER);
        if(showLabel)button.setPadding(dp(6),0,dp(5),0);
        button.setFocusable(true);
        DesktopWindow window=windows.get(id);
        int side=showLabel?22:24;
        View image=window!=null&&window.body instanceof VistaExplorer&&((VistaExplorer)window.body).page().startsWith("folder:")
                ?new AeroIcon(this,"files"):icon(id,side);
        button.addView(image,new LinearLayout.LayoutParams(dp(side),dp(side)));
        TextView name=Aero.text(this,title,12,TenStyle.text(this));
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        name.setPadding(dp(5),0,0,0);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setVisibility(showLabel?View.VISIBLE:View.GONE);
        button.addView(name,new LinearLayout.LayoutParams(0,-1,1));
        return button;
    }
    View icon(String id,int size){
        if(TenSystemTools.tool(id))return new AeroIcon(this,id.equals("@devices")||id.equals("@management")?"computer":id.equals("@events")?"notes":"run");
        if(id.equals("@gadgets"))return new AeroIcon(MainActivity.this,"feeds");
        if(id.equals("@nota"))return TenStyle.accentGlyph(MainActivity.this,"nota");
        AppCatalog.Entry entry=entries.get(id);
        if(entry!=null){ImageView image=new ImageView(this);image.setImageDrawable(entry.icon);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);return image;}
        return new AeroIcon(this,FolderLinks.id(id)?"files":id.equals("@files")?"file-explorer":id.equals("@recycle")?(recycleFull?"recycle-full":"recycle"):id.startsWith("@gadget:")?GadgetCatalog.icon(id.substring(8)):store.isFolder(id)||id.startsWith("@organizer:")||id.equals("@filemanagers")||id.equals("@organize")?"folderapps":id.equals("@welcome")?"orb":id.equals("@backup")?"drive":id.equals("@quicklaunch")?"programs":id.equals("@photos")?"pictures-app":id.equals(MediaLibrary.PLAYER)?"mediaplayer":id.equals(MediaLibrary.CENTER)?"mediacenter":id.startsWith("@")?id.substring(1):"apps");
    }
    private String label(String id){return store.name(id,defaultLabel(id));}
    private String defaultLabel(String id){
        if(TenSystemTools.tool(id))return TenSystemTools.title(id);
        JSONObject location=FolderLinks.get(store,id);if(location!=null)return location.optString("title","Folder");
        if(id.startsWith("@gadget:")&&GadgetCatalog.extra(id.substring(8)))return GadgetCatalog.title(id.substring(8));
        switch(id){case MediaLibrary.PLAYER:return MediaLibrary.PLAYER_TITLE;case MediaLibrary.CENTER:return MediaLibrary.CENTER_TITLE;case "@run":return "Run";case "@nota":return "Nth Nota";case "@settings":return "Settings";case "@news":return "News and interests";case "@battery":return "Battery";case "@taskview":return "Task View";case "@recycle":return "Recycle Bin";case "@workspaces":return "Saved Workspaces";case "@recent":return "Recent Items";case "@taskmanager":return "Task Manager";case "@actioncenter":return "Action Center";case "@welcome":return "Welcome Center";case "@organize":return "Desktop Organization";case "@quicklaunch":return "Quick Launch";case "@backup":return "Backup and Restore";case "@control":return "Settings";case "@system":return "System";case "@filemanagers":return "File Managers";case "@weather":return "Weather";case "@clock":return "Clock";case "@gadgets":return "News and interests";case "@sticky":return "Notes";case "@calendar":return "Calendar";case "@computer":return "Computer";case "@browser":return BrowserPanel.NAME;case "@photos":return "Photo Gallery";case "@files":return "File Explorer";case "@documents":return "Documents";case "@pictures":return "Pictures";case "@music":return "Music";case "@flip3d":return "Task View";case "@notes":return "Notepad";case "@appearance":return "Personalize";default:AppCatalog.Entry app=entries.get(id);return app==null?"Unavailable app":app.label;}
    }
    @Override public void open(String id){
        if((id.equals("@taskview")||id.equals("@flip3d"))&&tenTasks!=null&&tenTasks.overlay!=null){tenTasks.dismiss();return;}
        if((id.equals("@tenactions")||id.equals("@actioncenter"))&&tenActions!=null&&tenActions.dismiss())return;
        if(tenTasks!=null)tenTasks.dismiss();if(tenActions!=null)tenActions.dismiss();if(tenNews!=null&&!id.equals("@news"))tenNews.dismiss();
        if(id.equals("start-menu:")){showStart(false);return;}
        if(id.startsWith("app-info:")){launchSystem(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.fromParts("package",id.substring(9),null)));return;}
        if(steps.active()&&recordableDesktop()){
            switch(id){
                case "@files":case "@computer":case "@control":case "@appearance":case "@notes":
                case "@calendar":case "@browser":case "@flip3d":case "@switcher":case "@photos":
                case "@taskmanager":case MediaLibrary.PLAYER:case MediaLibrary.CENTER:
                    steps.record("Opened "+defaultLabel(id));break;
            }
        }
        startMenu.hide();if(tray!=null)tray.dismiss();
        TenSystemTools.record(id);
        if(id.startsWith("@")&&DesktopBackup.shortcut(id,new JSONObject(),false)&&!Arrays.asList("@news","@taskview").contains(id))store.launched(id);
        if(id.startsWith("nota-query:")){nota(new TenNotaContext("Start search",Uri.decode(id.substring(11)),false));return;}
        if(id.startsWith("ten-setting:")){open("@settings");DesktopWindow settings=windows.get("@settings");if(settings!=null&&settings.body instanceof TenSettings)((TenSettings)settings.body).navigate(id.substring(12));return;}
        if(FolderLinks.id(id)){JSONObject link=FolderLinks.get(store,id);if(link==null){toast("This folder shortcut is unavailable.");return;}explorer(id,link.optString("page","home"));return;}
        if(validExplorerId(id)){explorer(id,"home");return;}
        if(id.startsWith("setting:")){String destination=id.substring(8);if(destination.equals("page:personalization")||destination.equals("page:colors")){open("ten-setting:"+(destination.endsWith("personalization")?"themes":"colors"));return;}if(destination.startsWith("page:")){open("@legacycontrol");DesktopWindow control=windows.get("@legacycontrol");if(control!=null&&control.body instanceof VistaControlPanel)((VistaControlPanel)control.body).navigate(destination.substring(5));}else if(destination.startsWith("open:"))open(destination.substring(5));else vistaAction(destination);return;}
        if(id.startsWith("start-search:")){explorer("@files","search-all");DesktopWindow folder=windows.get("@files");if(folder!=null&&folder.body instanceof VistaExplorer){VistaExplorer view=(VistaExplorer)folder.body;view.navigate("search-all");view.search.setText(Uri.decode(id.substring(13)));}return;}
        if(id.startsWith("web-search:")){open("@browser");DesktopWindow browser=windows.get("@browser");if(browser!=null&&browser.body instanceof BrowserPanel)((BrowserPanel)browser.body).navigate("https://www.bing.com/search?q="+Uri.encode(Uri.decode(id.substring(11))));return;}
        if(id.startsWith("favorite-web:")){String url=id.substring(13);if(BrowserUrls.web(url)){open("@browser");DesktopWindow browser=windows.get("@browser");if(browser!=null&&browser.body instanceof BrowserPanel)((BrowserPanel)browser.body).navigate(url);}return;}
        if(id.startsWith("recent-file:")){JSONObject file=RecentFiles.get(store,id.substring(12));if(file==null){toast("This recent item was cleared.");return;}if(file.optString("kind").equals("folder"))explorer("@files",file.optString("page"));else viewDocument(Uri.parse(file.optString("uri")),file.optString("mime"),file.optString("name"),false);return;}
        if(id.startsWith("search-file:")){JSONObject file=LinkedFiles.get(store,id.substring(12));if(file==null){toast("This file link is unavailable. Choose it again in Computer.");return;}viewDocument(Uri.parse(file.optString("uri")),file.optString("mime"));return;}
        if(store.isFolder(id)){organizer(id,"contents:"+id);return;}
        switch(id){
            case "@events":case "@devices":case "@management":case "@powershell":case "@powershell-admin":if(!restoreTool(id))window(id,TenSystemTools.title(id),TenSystemTools.content(this,store,id,this::open));return;
            case "@search":showStart(true);return;
            case "@battery":vistaAction("android:battery");return;
            case "@run":TenRun.show(this,store,this::runCommand);return;
            case "@power":Aero.dialog(this).setTitle("Power").setItems(new String[]{"Show desktop","Restart Nth Desktop","Android power settings"},(d,w)->{if(w==0)showDesktop();else if(w==1)vistaAction("restart-desktop");else vistaAction("android:battery");}).show();return;
            case "@settings":if(!restoreTool(id))window(id,"Settings",new TenSettings(this,store,this::open));return;
            case "@nota":if(!restoreTool(id))window(id,"Nth Nota",new TenNotaPanel(this,store));return;
            case "@news":if(previews!=null)previews.dismiss();tenNews.show();return;
            case "@tenactions":tenActions.show();return;
            case "@taskview":case "@flip3d":showTenTasks();return;
            case "@switcher":previews.show();return;
            case "@brightness":tray.show("brightness");return;
            case "@recycle":if(!restoreTool(id))window(id,"Recycle Bin",new RecyclePanel(this,store,vistaActions,this::filesChanged));windows.get(id).captionIcon(recycleFull?"recycle-full":"recycle");return;
            case "@recent":if(!restoreTool(id))window(id,"Recent Items",new RecentItemsPanel(this,store,this::open));else if(windows.get(id).body instanceof RecentItemsPanel)((RecentItemsPanel)windows.get(id).body).refresh();return;
            case "@workspaces":showWorkspaces();return;
            case "@taskmanager":taskManager();return;
            case "@actioncenter":tenActions.show();return;
            case "@welcome":if(!restoreTool(id))window(id,"Welcome Center",new WelcomeCenter(this,store,vistaActions));return;
            case "@organize":organizer("@organize","desktop");return;
            case "@quicklaunch":organizer("@quicklaunch","quick");return;
            case "@backup":backupPanel(false);return;
            case "@control":open("@settings");return;
            case "@legacycontrol":controlPanel("@legacycontrol","Advanced Desktop Settings","home");return;
            case "@system":open("ten-setting:about");return;
            case "@filemanagers":explorer("@filemanagers","managers");return;
            case "@gadgets":gadgetGallery();return;
            case "@clock":clockOptions();return;
            case "@weather":weatherSettings();return;
            case "@sticky":stickyNote();return;
            case "@calendar":calendar();return;
            case "@documents":case "@pictures":case "@music":
                if(!restoreTool(id)){
                    explorer(id,"home");
                    DesktopWindow folder=windows.get(id);
                    if(folder!=null)((VistaExplorer)folder.body).openFavorite(id.substring(1));
                }
                return;
            case "@computer":computer();return;
            case "@notes":notes();return;
            case "@appearance":open("ten-setting:themes");return;
            case "@photos":photoGallery();return;
            case MediaLibrary.PLAYER:mediaPlayer();return;
            case MediaLibrary.CENTER:openMediaCenter();return;
            case "@browser":if(!restoreTool(id))try{window(id,BrowserPanel.NAME,new BrowserPanel(this));}catch(RuntimeException|LinkageError ex){Aero.dialog(this).setTitle("Browser engine unavailable").setMessage("Install or enable Android System WebView, then reopen Nth Browser.").setPositiveButton("OK",null).show();}return;
            case "@files":explorer("@files","home");return;
        }
        if(id.startsWith("@gadget:")&&GadgetCatalog.extra(id.substring(8))){gadgetPanel(id.substring(8));return;}
        ComponentName component=ComponentName.unflattenFromString(id);
        if(component==null){toast("This shortcut is unavailable. Hold its Start menu entry to remove it.");return;}
        launchInstalledApp(component);
    }
    private boolean launchInstalledApp(ComponentName component){
        if(tenTasks!=null)tenTasks.dismiss();if(tenActions!=null)tenActions.dismiss();if(tenNews!=null)tenNews.dismiss();startMenu.hide();if(tray!=null)tray.dismiss();
        try{
            Intent launch=Intent.makeMainActivity(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            if(Aero.motion(this))startActivity(launch,android.app.ActivityOptions.makeCustomAnimation(this,R.anim.ten_app_open,R.anim.ten_shell_exit).toBundle());else startActivity(launch);ExternalTasks.opened(component.flattenToString());store.launched(component.flattenToString());renderTaskbar();return true;
        }catch(ActivityNotFoundException|SecurityException e){
            Aero.dialog(this).setTitle("App unavailable").setMessage("This app may have been removed or disabled.").setPositiveButton("Remove shortcut",(d,w)->{List<String> ids=store.desktop();ids.remove(component.flattenToString());store.desktop(ids);renderDesktop();}).setNegativeButton("Keep",null).show();scanApps();return false;
        }
    }
    View notaActionIcon(TenNotaActions.Action action){
        switch(action.kind){case OPEN:return action.value.startsWith("setting:")||action.value.startsWith("ten-setting:")?new AeroIcon(this,"settings"):icon(action.value,32);case WEB_SEARCH:case WEBSITE:return new AeroIcon(this,"browser");case FILE_SEARCH:return new AeroIcon(this,"files");default:return new TenStyle.Glyph(this,"taskview",TenAppearance.ink(this));}
    }
    List<TenNotaActions.Target> notaAppTargets(){List<TenNotaActions.Target> apps=new ArrayList<>();for(AppCatalog.Entry app:entries.values())apps.add(new TenNotaActions.Target(store.name(app.id,app.label),app.id,app.label));return apps;}
    String performNotaAction(TenNotaActions.Action action){
        switch(action.kind){
            case WEB_SEARCH:open("web-search:"+Uri.encode(action.value));return "Opening web search";
            case FILE_SEARCH:open("start-search:"+Uri.encode(action.value));return "Searching linked files";
            case WEBSITE:if(BrowserUrls.web(action.value)){open("favorite-web:"+action.value);return "Opening website";}return "That website address is unavailable";
            case DESKTOP:showDesktop();return "Desktop shown; restore windows from the taskbar";
            case NEW_DESKTOP:String previous=tenDesktops.current();String id=tenDesktops.create();if(id==null)return "Desktop limit reached. Use Task View to manage desktops.";tenDesktops.select(id);showTenTasks();tenTasks.createdGuide(previous,id);return "Created "+tenDesktops.name()+". Task View shows how to return to your previous desktop.";
            case NEXT_DESKTOP:case PREVIOUS_DESKTOP:String before=tenDesktops.current();tenDesktops.adjacent(action.kind==TenNotaActions.Kind.NEXT_DESKTOP?1:-1);return before.equals(tenDesktops.current())?"No desktop in that direction. Open Task View to create one.":"Switched to "+tenDesktops.name();
            case OPEN:String title=action.label.replaceFirst("^Open ","");ComponentName component=ComponentName.unflattenFromString(action.value);if(component!=null){if(!launchInstalledApp(component))return "Could not open "+title+". The app may have been removed or disabled.";}else open(action.value);return "Opening "+title;
            default:return "This action is unavailable";
        }
    }
    boolean runCommand(String value){String command=value.trim(),key=command.toLowerCase(Locale.ROOT);Map<String,String> aliases=new HashMap<>();aliases.put("notepad","@notes");aliases.put("calc","@gadget:calculator");aliases.put("calculator","@gadget:calculator");aliases.put("explorer","@files");aliases.put("control","@settings");aliases.put("settings","@settings");aliases.put("ms-settings:","@settings");aliases.put("taskmgr","@taskmanager");aliases.put("winver","ten-setting:about");aliases.put("nth nota","@nota");aliases.put("nota","@nota");aliases.put("news","@news");String target=aliases.get(key);if(target!=null){open(target);return true;}if(BrowserUrls.web(command)){open("favorite-web:"+command);return true;}if(key.startsWith("ms-settings:")&&key.substring(12).matches("[a-z-]{1,40}")){String page=key.substring(12);if(TenSettingsCatalog.page(page)!=null||!TenSettingsCatalog.first(page).equals("home")){open("ten-setting:"+page);return true;}}List<AppCatalog.Entry> matches=new ArrayList<>();for(AppCatalog.Entry app:entries.values())if(app.id.equals(command)||store.name(app.id,app.label).equalsIgnoreCase(command))matches.add(app);if(matches.size()==1){open(matches.get(0).id);return true;}if(matches.size()>1){String[] names=matches.stream().map(a->a.label+" · "+a.id).toArray(String[]::new);Aero.dialog(this).setTitle("Choose an app").setItems(names,(d,w)->open(matches.get(w).id)).show();return true;}return false;}
    @Override public void options(View anchor,String id){ContextMenus.centered(anchor,(x,y)->shortcutMenu(anchor,id,"desktop",x,y));}
    private void shortcutMenu(View anchor,String id,String source,float x,float y){
        if(id.equals("@recycle")&&source.equals("desktop")){
            DesktopContextMenu menu=new DesktopContextMenu(this,"Recycle Bin desktop menu");
            menu.item("Open","recycle",()->open(id));
            menu.item("Empty Recycle Bin","delete",recycleFull,this::emptyRecycleBin);
            menu.separator();
            menu.item("Properties","control",()->recycleWindow().properties());
            ContextMenus.show(menu,anchor,x,y);
            return;
        }
        ShortcutMenus.show(this,anchor,x,y,store,id,source,label(id),new ShortcutMenus.Host(){
            public void open(String value){MainActivity.this.open(value);}
            public void rename(String value){organizer("@organize","rename:"+value);}
            public void changed(){desktopChanged();}
        });
    }
    private void emptyRecycleBin(){
        RecyclePanel.confirmEmpty(this,()->worker.execute(()->{
            String result;
            try{RecycleBin.empty(getApplicationContext());result="Recycle Bin emptied";}
            catch(Exception ex){result=ex.getMessage()==null?"Recycle Bin could not be emptied.":ex.getMessage();}
            String message=result;
            main.post(()->{if(!destroyed){toast(message);filesChanged();}});
        }));
    }
    private RecyclePanel recycleWindow(){open("@recycle");return (RecyclePanel)windows.get("@recycle").body;}
    private void desktopOptions(float x,float y){
        if(desktopMenu!=null)desktopMenu.dismiss();startMenu.hide();previews.dismiss();
        desktopMenu=new DesktopContextMenu(this,"Desktop context menu");
        desktopMenu.submenu("View","appearance",()->desktopViewMenu(x,y));
        desktopMenu.submenu("Sort by","programs",()->desktopSortMenu(x,y));
        desktopMenu.item("Refresh","refresh",()->{scanApps();services.refresh();});
        desktopMenu.separator();
        desktopMenu.item("Paste","paste",!ExplorerClipboard.shortcuts.isEmpty(),()->{
            try{ExplorerClipboard.paste(store,"desktop");desktopChanged();}
            catch(IllegalArgumentException ex){toast(ex.getMessage());}
        });
        desktopMenu.item("New folder","folderapps",()->organizer("@organize","new"));
        desktopMenu.submenu("Desktop tools","computer",()->desktopTools(x,y));
        desktopMenu.separator();
        desktopMenu.item("Display settings","computer",()->open("ten-setting:display"));
        desktopMenu.submenu("Desktop background","pictures",()->backgroundMenu(x,y));
        if(wallpaper.hasSlideshow())desktopMenu.item("Next desktop background","pictures",wallpaper::next);
        desktopMenu.item("Personalize","appearance",this::appearance);
        if(!isHome()){desktopMenu.separator();desktopMenu.item("Make default home","orb",this::home);}
        showDesktopMenu(x,y);
    }
    private void desktopTools(float x,float y){
        desktopMenu=new DesktopContextMenu(this,"Desktop tools");
        desktopMenu.item("All apps","programs",()->showStart(false));
        desktopMenu.item("Computer","computer",this::computer);
        desktopMenu.item("Settings","control",()->open("@settings"));
        desktopMenu.item("Recycle Bin","delete",()->open("@recycle"));
        desktopMenu.item("Saved Workspaces…","computer",this::showWorkspaces);
        desktopMenu.item("Task View","appearance",()->open("@taskview"));
        desktopMenu.item("Manage desktop shortcuts","computer",this::manageShortcuts);
        showDesktopMenu(x,y);
    }
    private void backgroundMenu(float x,float y){
        desktopMenu=new DesktopContextMenu(this,"Desktop backgrounds");
        List<String> current=DesktopBackgrounds.current(store.prefs);
        for(int i=0;i<DesktopBackgrounds.NAMES.length;i++){
            int theme=i;String key=DesktopBackgrounds.stock(theme);
            desktopMenu.checked(DesktopBackgrounds.NAMES[i],current.size()==1&&current.contains(key),()->{
                android.content.SharedPreferences.Editor edit=store.prefs.edit();
                DesktopBackgrounds.choose(edit,Collections.singletonList(key),AeroWallpaper.SEVEN);edit.apply();
                vistaAction("wallpaper");
            });
        }
        desktopMenu.separator();
        desktopMenu.item("Choose pictures or a slide show…","pictures",()->open("setting:page:background"));
        showDesktopMenu(x,y);
    }
    private void showDesktopMenu(float x,float y){
        desktopMenu.show(root,x,y,shell.getPaddingLeft(),shell.getPaddingTop()+windowTopInset,shell.getPaddingRight(),shell.getPaddingBottom());
    }
    private void manageShortcuts(){organizer("@organize","desktop");}
    private void filesChanged(){refreshRecycleIcon();if(destroyed)return;for(DesktopWindow tool:windows.values())if(tool.getParent()!=null){if(tool.body instanceof VistaExplorer)((VistaExplorer)tool.body).refreshFiles();else if(tool.body instanceof DesktopOrganizer)((DesktopOrganizer)tool.body).refreshApps();else if(tool.body instanceof RecyclePanel)((RecyclePanel)tool.body).refresh();}renderDesktop();renderTaskbar();}
    private void desktopChanged(){
        layoutWorkspace();renderDesktop();renderTaskbar();
        for(Map.Entry<String,DesktopWindow> entry:new ArrayList<>(windows.entrySet())){DesktopWindow window=entry.getValue();if(window.getParent()==null)continue;if(window.body instanceof DesktopOrganizer)((DesktopOrganizer)window.body).refreshApps();else if(window.body instanceof RecyclePanel)((RecyclePanel)window.body).refresh();}
        refreshRecycleIcon();renderTaskbar();
    }
    private void quickOptions(View anchor,String id){ContextMenus.centered(anchor,(x,y)->shortcutMenu(anchor,id,"quick",x,y));}
    private void desktopViewMenu(float x,float y){desktopMenu=new DesktopContextMenu(this);String[] names={"Small icons","Medium icons","Large icons"};for(int i=0;i<names.length;i++){final int value=i;desktopMenu.item((store.prefs.getInt("desktop_icon_size",1)==i?"✓  ":"")+names[i],"computer",()->{store.prefs.edit().putInt("desktop_icon_size",value).remove("desktop_icon_percent").apply();desktopChanged();});}showDesktopMenu(x,y);}
    private void desktopSortMenu(float x,float y){desktopMenu=new DesktopContextMenu(this);for(String mode:Arrays.asList("Name","Type"))desktopMenu.item(mode,"programs",()->{List<String> ids=store.desktop();java.text.Collator collator=java.text.Collator.getInstance();ids.sort((a,b)->{if(mode.equals("Type")&&store.isFolder(a)!=store.isFolder(b))return store.isFolder(a)?-1:1;return collator.compare(label(a),label(b));});store.desktop(ids);desktop.clearPositions();desktopChanged();});desktopMenu.show(root,x,y,shell.getPaddingLeft(),shell.getPaddingTop()+windowTopInset,shell.getPaddingRight(),shell.getPaddingBottom());}
    private void startOrbMenu(View anchor,float x,float y){
        if(desktopMenu!=null)desktopMenu.dismiss();startMenu.hide();if(tenNews!=null)tenNews.dismiss();if(tray!=null)tray.dismiss();
        desktopMenu=new DesktopContextMenu(this,"Start button context menu",34,286);
        for(String[] row:new String[][]{{"Apps and Features","ten-setting:installed"},{"Power Options","ten-setting:power"},{"Event Viewer","@events"},{"System","ten-setting:about"},{"Device Manager","@devices"},{"Network Connections","ten-setting:network"},{"Disk Management","ten-setting:storage"},{"Computer Management","@management"},{"Windows PowerShell","@powershell"},{"Windows PowerShell (Admin)","@powershell-admin"}})desktopMenu.item(row[0],"empty",()->open(row[1]));
        desktopMenu.separator();for(String[] row:new String[][]{{"Task Manager","@taskmanager"},{"Settings","@settings"},{"File Explorer","@files"},{"Search","@search"},{"Run","@run"}})desktopMenu.item(row[0],"empty",()->open(row[1]));desktopMenu.separator();desktopMenu.submenu("Shut down or sign out","empty",()->open("@power"));desktopMenu.item("Desktop","empty",this::showDesktop);ContextMenus.show(desktopMenu,anchor,x,y);
    }

    private void showStart(boolean search){if(tenTasks!=null)tenTasks.dismiss();if(tenNews!=null)tenNews.dismiss();if(previews!=null)previews.dismiss();if(tray!=null)tray.dismiss();startMenu.show(search);}
    private void showDesktop(){if(tenNews!=null)tenNews.dismiss();
        if(tray!=null)tray.dismiss();((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(workspace.getWindowToken(),0);startMenu.hide();
        if(!desktopHidden.isEmpty()){for(Map.Entry<String,DesktopWindow> item:new ArrayList<>(desktopHidden.entrySet())){DesktopWindow window=item.getValue();if(windows.get(item.getKey())==window&&window.getParent()!=null)window.restore();}desktopHidden.clear();}
        else for(DesktopWindow window:orderedWindows()){if(window.getVisibility()==View.VISIBLE&&!window.minimizing()){desktopHidden.put(window.toolId,window);window.dismissSystemMenu();window.settle();window.setVisibility(View.GONE);}}
        renderTaskbar();
    }
    private void shake(DesktopWindow owner){
        if(recordableDesktop())steps.record("Toggled Aero Shake");
        previews.dismiss();
        List<DesktopWindow> others=new ArrayList<>();boolean visible=false;
        for(DesktopWindow window:orderedWindows())if(window!=owner){others.add(window);visible|=window.getVisibility()==View.VISIBLE&&!window.minimizing();}
        if(visible){for(DesktopWindow window:others){window.dismissSystemMenu();window.settle();window.setVisibility(View.GONE);}}
        else for(DesktopWindow window:others)window.restore();
        owner.restore();
    }
    private List<DesktopWindow> orderedWindows(){List<DesktopWindow> result=new ArrayList<>();for(int i=0;i<workspace.getChildCount();i++)if(workspace.getChildAt(i) instanceof DesktopWindow&&windows.containsValue(workspace.getChildAt(i)))result.add((DesktopWindow)workspace.getChildAt(i));return result;}
    private void taskbarMenu(View anchor,float x,float y){
        previews.dismiss();
        if(desktopMenu!=null)desktopMenu.dismiss();startMenu.hide();if(tray!=null)tray.dismiss();desktopMenu=new DesktopContextMenu(this,"Taskbar context menu");
        desktopMenu.item("News and interests","news",()->open("@news"));desktopMenu.item("Nth Nota","notes",()->open("@nota"));desktopMenu.item("Pinned taskbar apps…","programs",()->open("@quicklaunch"));desktopMenu.separator();
        boolean fit=store.prefs.getBoolean("taskbar_static",false);
        desktopMenu.checked("Scrollable",!fit,()->setTaskbarStatic(false));
        desktopMenu.checked("Static",fit,()->setTaskbarStatic(true));
        desktopMenu.separator();boolean visible=false;for(DesktopWindow window:orderedWindows())if(window.getVisibility()==View.VISIBLE&&!window.minimizing())visible=true;
        for(String[] mode:new String[][]{{"Cascade Windows","cascade"},{"Show Windows Stacked","stacked"},{"Show Windows Side by Side","side"}})desktopMenu.item(mode[0],"empty",visible,()->arrangeWindows(mode[1]));
        if(!arrangementUndo.isEmpty())desktopMenu.item("Undo Window Arrangement","empty",this::undoArrangement);
        desktopMenu.item(desktopHidden.isEmpty()?"Show the Desktop":"Show Open Windows","computer",this::showDesktop);desktopMenu.separator();desktopMenu.item("Switch Windows","computer",()->previews.show());desktopMenu.item("Task View","computer",this::showTenTasks);desktopMenu.item("Saved Workspaces…","computer",this::showWorkspaces);desktopMenu.item("Start Task Manager","taskmanager",()->open("@taskmanager"));desktopMenu.separator();desktopMenu.item("Taskbar settings","control",()->open("ten-setting:taskbar"));ContextMenus.show(desktopMenu,anchor,x,y);
    }
    private void setTaskbarStatic(boolean fit){
        store.prefs.edit().putBoolean("taskbar_static",fit).apply();
        renderTaskbar();
    }
    private void arrangeWindows(String mode){
        List<DesktopWindow> visible=new ArrayList<>();for(DesktopWindow window:orderedWindows())if(window.getVisibility()==View.VISIBLE&&!window.minimizing())visible.add(window);if(visible.isEmpty())return;
        desktopHidden.clear();arrangementUndo.clear();int width=Math.max(1,workspace.getWidth()),height=Math.max(1,workspace.getHeight()-windowTopInset);
        int[][] rects=WindowArrangement.rectangles(mode,visible.size(),width,height,dp(WindowArrangement.MIN_SIDE_WIDTH_DP),dp(WindowArrangement.MIN_STACK_HEIGHT_DP),dp(30));for(int i=0;i<visible.size();i++){DesktopWindow window=visible.get(i);arrangementUndo.put(window,window.geometry());int[] r=rects[i];window.arrange(r[0],r[1],r[2],r[3]);}renderTaskbar();
    }
    private void undoArrangement(){for(Map.Entry<DesktopWindow,JSONObject> item:arrangementUndo.entrySet())if(item.getKey().getParent()!=null){item.getKey().restoreGeometry(item.getValue());item.getKey().saveGeometry();}arrangementUndo.clear();renderTaskbar();}
    private void showWorkspaces(){
        if(workspaceDialog!=null)workspaceDialog.dismiss();((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(),0);startMenu.hide();tray.dismiss();LinearLayout body=Aero.column(this);Aero.pad(body,12);paragraph(body,"Save up to six named Nth window layouts. Reopening a layout minimizes other windows, keeps current note contents, and uses current folder permissions. Android apps and private browser sessions are not saved.");
        addAction(body,"Save current workspace…",()->{EditText name=new EditText(this);name.setSingleLine();name.setContentDescription("Workspace name");name.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(60)});AlertDialog dialog=Aero.dialog(this).setTitle("Save workspace").setView(name).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();dialog.setOnShowListener(d->{Aero.styleDialog(this,dialog);dialog.getButton(-1).setOnClickListener(v->{try{WorkspacePresets.save(store,name.getText().toString(),captureCurrentSession(),false,layoutKey);dialog.dismiss();toast("Workspace saved");DesktopSounds.play(this,"notice");}catch(JSONException ex){name.setError(ex.getMessage());}});});dialog.show();});
        JSONArray items=WorkspacePresets.list(store);for(int i=0;i<items.length();i++){JSONObject item=items.optJSONObject(i);if(item==null)continue;String name=item.optString("name");paragraph(body,name+" · "+item.optString("orientation"));addAction(body,"Open "+name,()->applyWorkspace(item));addAction(body,"Delete "+name+"…",()->Aero.dialog(this).setTitle("Delete saved workspace?").setMessage(name+". Your open windows and documents are kept.").setPositiveButton("Delete",(d,w)->{WorkspacePresets.remove(store,name);toast("Saved workspace removed");}).setNegativeButton("Cancel",null).show());}
        ScrollView scroll=new ScrollView(this);scroll.addView(body);workspaceDialog=Aero.dialog(this).setTitle("Saved Workspaces").setView(scroll).setNegativeButton("Close",null).show();
    }
    private void applyWorkspace(JSONObject item){
        if(workspaceDialog!=null)workspaceDialog.dismiss();
        for(DesktopWindow window:orderedWindows()){window.settle();window.setVisibility(View.GONE);}desktopHidden.clear();arrangementUndo.clear();layoutWorkspace();
        JSONObject session=item.optJSONObject("session");if(session!=null){restoreSession(session.toString());JSONArray tools=session.optJSONArray("tools");if(tools!=null)for(int i=0;i<tools.length();i++){JSONObject saved=tools.optJSONObject(i);DesktopWindow window=saved==null?null:windows.get(saved.optString("id"));if(window!=null&&window.getParent()!=null&&saved.optJSONObject("geometry")!=null)window.restoreGeometry(saved.optJSONObject("geometry"));}}renderTaskbar();toast("Workspace opened");
    }
    private static boolean validExplorerId(String id){if(id==null||!id.startsWith("@explorer:"))return false;try{UUID.fromString(id.substring(10));return true;}catch(IllegalArgumentException ex){return false;}}
    private void newExplorer(String page){int count=0;for(DesktopWindow window:orderedWindows())if(window.body instanceof VistaExplorer)count++;if(count>=12){toast("Close a folder window before opening another (12 maximum).");return;}explorer("@explorer:"+UUID.randomUUID(),page);}
    private void refreshRecycleIcon(){if(destroyed)return;worker.execute(()->{boolean full=false;try{full=!RecycleBin.list(getApplicationContext()).isEmpty();}catch(Exception ignored){}boolean value=full;main.post(()->{if(destroyed)return;if(recycleFull!=value){recycleFull=value;renderDesktop();renderTaskbar();}DesktopWindow bin=windows.get("@recycle");if(bin!=null)bin.captionIcon(value?"recycle-full":"recycle");});});}
    private void taskManager(){
        if(restoreTool("@taskmanager"))return;window("@taskmanager","Task Manager",new TaskManagerPanel(this,new TaskManagerPanel.Host(){
            public List<TaskManagerPanel.Task> tasks(){List<TaskManagerPanel.Task> result=new ArrayList<>();for(DesktopWindow window:orderedWindows())if(!window.toolId.equals("@taskmanager"))result.add(new TaskManagerPanel.Task(window.toolId,window.caption(),window.getVisibility()!=View.VISIBLE||window.minimizing()?"Minimized":"Running",false));for(String id:ExternalTasks.list())if(entries.containsKey(id))result.add(new TaskManagerPanel.Task(id,label(id),"Recently opened",true));return result;}
            public View icon(String id){return MainActivity.this.icon(id,22);}
            public void switchTo(String id){DesktopWindow manager=windows.get("@taskmanager");if(manager!=null)manager.minimize();DesktopWindow window=windows.get(id);if(window!=null&&window.getParent()!=null)window.restore();else open(id);}
            public void end(String id){DesktopWindow window=windows.get(id);if(window!=null)window.closeWindow();}
            public void appInfo(String id){ComponentName component=ComponentName.unflattenFromString(id);if(component!=null)launchSystem(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+component.getPackageName())));}
            public void restart(){vistaAction("restart-desktop");}public void newTask(){showStart(true);}
            public void close(){DesktopWindow window=windows.get("@taskmanager");if(window!=null)window.closeWindow();}
        }));
    }
    private void window(String id,String title,View body){
        DesktopWindow current=windows.get(id);if(current!=null&&current.getParent()!=null){if(body instanceof VistaShell)((VistaShell)body).close();current.restore();return;}
        DesktopWindow next=new DesktopWindow(this,workspace,title,body,wallpaper,this::renderTaskbar,store,id,layoutKey);next.desktopScope(tenDesktops.current());next.safeTop(windowTopInset);next.nota(()->askNota(next));next.taskAnchor(()->taskAnchor(next.toolId));next.onShake(()->shake(next));next.onPlacement(value->{if(recordableDesktop())steps.record("Window placement: "+value);});windows.put(id,next);tenDesktops.register(next);
        if(workspace.getWidth()>0){next.open();renderTaskbar();}else workspace.post(()->{if(!destroyed&&next.getParent()==null){next.open();renderTaskbar();}});
    }
    private void notes(){
        if(restoreTool("@notes"))return;
        LinearLayout body=Aero.column(this);
        TextView hint=Aero.text(this,"  Saved automatically on this device",12,0xff567082);hint.setPadding(dp(4),dp(8),dp(4),dp(8));body.addView(hint);
        EditText editor=new SharedNoteEditor(this,store);editor.setTypeface(Aero.face(this));editor.setTextSize(15);editor.setTextColor(Aero.INK);editor.setGravity(Gravity.TOP|Gravity.START);editor.setBackgroundColor(0xfffcfdf7);editor.setPadding(dp(14),dp(12),dp(14),dp(12));editor.setHint("A thought worth keeping…");editor.setContentDescription("Notepad text");editor.setText(store.prefs.getString("note",""));

        body.addView(editor,new LinearLayout.LayoutParams(-1,0,1));window("@notes","Notepad",body);
    }
    private final VistaShell.Actions vistaActions=new VistaShell.Actions(){public void open(String id){MainActivity.this.open(id);}public void action(String id){vistaAction(id);}};
    private final DesktopOrganizer.Host organizerHost=new DesktopOrganizer.Host(){public String label(String id){return MainActivity.this.label(id);}public View icon(String id,int size){
        if(id.equals("@gadgets"))return new AeroIcon(MainActivity.this,"feeds");
        if(id.equals("@nota"))return TenStyle.accentGlyph(MainActivity.this,"nota");return MainActivity.this.icon(id,size);}public void changed(){desktopChanged();}public void open(String id){MainActivity.this.open(id);}public void openFolderWindow(String id){
        if(!store.isFolder(id))return;
        int count=0;for(DesktopWindow window:orderedWindows())if(window.body instanceof DesktopOrganizer)count++;
        if(count>=12){toast("Close a shortcut folder window before opening another (12 maximum).");return;}
        organizer("@organizer:"+UUID.randomUUID(),"contents:"+id);
    }};
    private void organizer(String id,String page){
        DesktopWindow existing=windows.get(id);if(existing!=null&&existing.getParent()!=null&&existing.body instanceof DesktopOrganizer){existing.restore();((DesktopOrganizer)existing.body).navigate(page);((DesktopOrganizer)existing.body).refreshApps();return;}
        window(id,page.startsWith("contents:")?label(page.substring(9)):label(id),new DesktopOrganizer(this,store,vistaActions,organizerHost,()->new ArrayList<>(entries.values()),page));
    }
    private final BackupPanel.Host backupHost=new BackupPanel.Host(){
        public DesktopBackup.Plan pending(){return restorePlan;}public String message(){return backupMessage;}public boolean busy(){return backupBusy;}
        public void exportBackup(){MainActivity.this.exportBackup();}public void importBackup(){MainActivity.this.importBackup();}public void restoreBackup(boolean undo){MainActivity.this.restoreBackup(undo);}public void discardPreview(){restorePlan=null;DesktopBackup.discard(MainActivity.this,DesktopBackup.PENDING);}
    };
    private void backupPanel(boolean preview){
        if(!restoreTool("@backup"))window("@backup","Backup and Restore",new BackupPanel(this,store,vistaActions,backupHost));
        DesktopWindow current=windows.get("@backup");if(current!=null&&current.body instanceof BackupPanel){if(preview)((BackupPanel)current.body).preview();else ((BackupPanel)current.body).refreshApps();}
    }
    private void backupStatus(String message){
        if(message.startsWith("Backup could not")||message.startsWith("Backup was not opened")||message.startsWith("The backup could not"))
            ActionCenter.notice(this,"backup","A desktop backup could not be saved or opened.");
        backupMessage=message;DesktopWindow current=windows.get("@backup");if(current!=null&&current.body instanceof BackupPanel)((BackupPanel)current.body).refreshApps();}
    private void loadRestorePreview(){
        if(!DesktopBackup.file(this,DesktopBackup.PENDING).exists())return;
        worker.execute(()->{try{DesktopBackup.Plan plan=DesktopBackup.staged(this,DesktopBackup.PENDING);main.post(()->{if(destroyed||restoringBackup)return;restorePlan=plan;backupStatus("A selected backup is ready to review.");});}catch(Exception error){DesktopBackup.discard(this,DesktopBackup.PENDING);}});
    }
    private void exportBackup(){
        if(backupBusy)return;backupBusy=true;backupStatus("Preparing your desktop backup…");
        final JSONObject snapshot;try{for(DesktopWindow tool:windows.values())if(tool.getParent()!=null)tool.saveGeometry();snapshot=DesktopBackup.snapshot(store);}catch(JSONException error){backupBusy=false;backupStatus("The backup could not be prepared.");return;}
        worker.execute(()->{try{String text=snapshot.toString(2);DesktopBackup.parse(text);DesktopBackup.stage(this,DesktopBackup.EXPORT,text);main.post(()->{if(destroyed)return;backupBusy=false;backupStatus("Choose where to save your backup.");Intent create=new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("application/json").putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI,android.provider.DocumentsContract.buildDocumentUri("com.android.externalstorage.documents","primary:Documents")).putExtra(Intent.EXTRA_TITLE,"Nth-Desktop-"+new SimpleDateFormat("yyyy-MM-dd-HHmmss",Locale.US).format(new Date())+".nthdesktop.json");try{startActivityForResult(create,20);}catch(ActivityNotFoundException|SecurityException error){DesktopBackup.discard(this,DesktopBackup.EXPORT);backupStatus("No app can save the backup on this device.");}});}catch(Exception error){main.post(()->{if(destroyed)return;backupBusy=false;backupStatus("Backup could not be prepared: "+error.getMessage());});}});
    }
    private void importBackup(){
        if(backupBusy)return;restorePlan=null;DesktopBackup.discard(this,DesktopBackup.PENDING);Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*").putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI,android.provider.DocumentsContract.buildDocumentUri("com.android.externalstorage.documents","primary:Documents")).putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/json","text/plain","application/octet-stream"}).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try{startActivityForResult(pick,21);}catch(ActivityNotFoundException|SecurityException error){backupStatus("No app can open a backup on this device.");}
    }
    private void backupResult(int request,int result,Intent data){
        if(result!=RESULT_OK||data==null||data.getData()==null){if(request==20)DesktopBackup.discard(this,DesktopBackup.EXPORT);return;}Uri uri=data.getData();backupBusy=true;backupStatus(request==20?"Saving backup…":"Reading backup…");
        worker.execute(()->{try{
            if(request==20){DesktopBackup.Plan plan=DesktopBackup.staged(this,DesktopBackup.EXPORT);try(java.io.OutputStream output=getContentResolver().openOutputStream(uri,"wt")){if(output==null)throw new java.io.IOException("The location is unavailable");output.write(plan.text.getBytes(java.nio.charset.StandardCharsets.UTF_8));output.flush();}DesktopBackup.discard(this,DesktopBackup.EXPORT);main.post(()->{if(destroyed)return;backupBusy=false;backupStatus("Backup saved. Your desktop is unchanged.");});}
            else{DesktopBackup.Plan plan=DesktopBackup.parse(DesktopBackup.read(getContentResolver().openInputStream(uri)));DesktopBackup.stage(this,DesktopBackup.PENDING,plan.text);main.post(()->{if(destroyed)return;backupBusy=false;restorePlan=plan;backupMessage="Review the contents before restoring.";backupPanel(true);});}
        }catch(Exception error){if(request==20)DesktopBackup.discard(this,DesktopBackup.EXPORT);main.post(()->{if(destroyed)return;backupBusy=false;backupStatus((request==20?"Backup could not be saved: ":"Backup was not opened: ")+error.getMessage());backupPanel(false);});}});
    }
    private void restoreBackup(boolean undo){
        if(backupBusy||(!undo&&restorePlan==null))return;backupBusy=true;restoringBackup=true;scanGeneration++;if(weatherLocation!=null)weatherLocation.pause();if(services!=null)services.close();
        restoreCover=new FrameLayout(this);restoreCover.setBackgroundColor(0xee0a263b);restoreCover.setClickable(true);restoreCover.setElevation(dp(100));TextView waiting=Aero.text(this,"Restoring your desktop…",18,Color.WHITE);waiting.setGravity(Gravity.CENTER);restoreCover.addView(waiting,new FrameLayout.LayoutParams(-1,-1));root.addView(restoreCover,new FrameLayout.LayoutParams(-1,-1));DesktopBackup.Plan selected=restorePlan;
        worker.execute(()->{String result;try{DesktopBackup.Plan plan=undo?DesktopBackup.staged(this,DesktopBackup.UNDO):selected;DesktopBackup.apply(this,store,plan,undo);result=undo?"Your previous desktop was recovered.":"Desktop restored. Undo last restore is available in Backup and Restore.";}catch(Exception error){result="Restore did not finish: "+error.getMessage();}String message=result;main.post(()->{if(destroyed)return;getIntent().putExtra("backup_status",message);recreate();});});
    }
    private void computer(){explorer("@computer","computer");}
    private void appearance(){open("ten-setting:themes");}
    private void controlPanel(String id,String title,String page){if(restoreTool(id))return;window(id,title,new VistaControlPanel(this,store,wallpaper,vistaActions,page));}
    private void explorer(String id,String page){
        if(restoreTool(id))return;int opened=0;for(DesktopWindow existing:orderedWindows())if(existing.body instanceof VistaExplorer)opened++;if(opened>=12){toast("Close a folder window before opening another (12 maximum).");return;}
        VistaExplorer.Files files=new VistaExplorer.Files(){
            public void caption(String title){DesktopWindow window=windows.get(id);if(window!=null&&window.getParent()!=null){VistaExplorer view=(VistaExplorer)window.body;String kind=view.page().startsWith("folder:")?"files":view.page().equals("search-all")?"search":id.equals("@computer")?"computer":id.equals("@filemanagers")?"folderapps":id.equals("@files")?"file-explorer":"files";boolean changed=window.captionIcon(kind);if(!window.caption().equals(title)){window.caption(title);changed=true;}if(changed){main.removeCallbacks(taskbarRender);main.post(taskbarRender);}}}public void chooseFolder(){chooseLinkedFolder(id);}public void requestFolder(Uri initial){chooseLinkedFolder(id,initial);}public void requestDeviceAccess(){StorageAccess.request(MainActivity.this);}public void changed(){filesChanged();}
            public void operationFailed(){ActionCenter.notice(MainActivity.this,"files","An Explorer file operation could not complete.");}
            public void operationStarted(){if(recordableDesktop())steps.record("Started an Explorer file operation");}
            public void linkFile(){chooseSearchFile();}public void pickFile(String kind){pickDocument(kind);}public void openDocument(Uri uri,String mime){viewDocument(uri,mime);}public void openDocument(Uri uri,String mime,String name){viewDocument(uri,mime,name,false);}public void openPicture(Uri uri,String mime,String name,JSONObject context){openPhoto(uri,name,mime,context,id);}public void openWith(Uri uri,String mime,String name){viewDocument(uri,mime,name,true);}public void openFolder(String page){newExplorer(page);}public void desktopFolder(String page,String name){try{FolderLinks.add(store,page,name);desktopChanged();}catch(JSONException ex){toast(ex.getMessage());}}public void app(String app){MainActivity.this.open(app);}
        };
        window(id,id.equals("@computer")?"Computer":id.equals("@filemanagers")?"File Managers":"Files",new VistaExplorer(this,store,vistaActions,files,()->new ArrayList<>(entries.values()),page));
    }
    private void rebuildTenTaskbar(){if(tenNews!=null)tenNews.dismiss();if(tray!=null)tray.stop();if(taskbarReorder!=null)taskbarReorder.close();if(previews!=null)previews.dismiss();if(shell.getChildCount()>1)shell.removeViewAt(shell.getChildCount()-1);taskViewportWidth=0;buildTaskbar();if(tray!=null)tray.start();}
    android.graphics.drawable.Drawable tenShellSurface(){return new AeroGlass(wallpaper,AeroGlass.START);}
    void applyTenFont(){applyTenAppearance();TenFonts.apply(root);}
    void applyTenAppearance(){if(root==null)return;TenAppearance.context(this);wallpaper.theme(store.prefs.getInt("wallpaper",11));wallpaper.appearanceChanged();rebuildTenTaskbar();startMenu.appearanceChanged();for(DesktopWindow tool:tenDesktops.all()){if(tool.body instanceof TenSettings)((TenSettings)tool.body).refreshAppearance();TenAppearance.apply(tool.body);tool.active(tool.active());}renderDesktop();renderTaskbar();}
    void refreshTen(){if(root==null)return;layoutWorkspace();renderDesktop();renderTaskbar();}
    private void desktopChangedTen(){if(root==null)return;renderDesktop();desktopScroll.scrollTo(0,0);desktopHidden.clear();arrangementUndo.clear();if(previews!=null)previews.dismiss();for(DesktopWindow w:windows.values())if(w.getParent()!=null){w.orientation(layoutKey);w.safeTop(windowTopInset);}renderTaskbar();}
    private void showTenTasks(){if(tenNews!=null)tenNews.dismiss();startMenu.hide();previews.dismiss();tray.dismiss();List<String> apps=new ArrayList<>();for(String id:ExternalTasks.list())if(entries.containsKey(id))apps.add(id);tenTasks.show(orderedWindows(),apps,this::label,this::open);}
    private void actionCenter(){
        if(restoreTool("@actioncenter")){
            ((ActionCenter)windows.get("@actioncenter").body).refresh();
            return;
        }
        window("@actioncenter","Action Center",new ActionCenter(this,store,steps,new ActionCenter.Host(){
            public boolean isHome(){return MainActivity.this.isHome();}
            public void home(){MainActivity.this.home();}
            public void fileSetup(){storageSetup.show(true);}
            public void open(String id){MainActivity.this.open(id);}
            public boolean capture(){return captureDesktopStep();}
            public void export(){exportSteps();}
        }));
    }
    private boolean recordableDesktop(){
        DesktopWindow browser=windows.get("@browser");
        return videoOverlay==null&&mediaCenter==null&&(browser==null||browser.getParent()==null||WindowPreviews.allowed(browser));
    }
    private boolean captureDesktopStep(){
        if(!recordableDesktop())return false;
        DesktopWindow center=windows.get("@actioncenter");
        if(center!=null)center.setVisibility(View.INVISIBLE);
        try{return steps.capture(workspace);}
        catch(OutOfMemoryError unavailable){return false;}
        finally{if(center!=null)center.setVisibility(View.VISIBLE);}
    }
    private void exportSteps(){
        if(steps.empty()||steps.active())return;
        android.util.AtomicFile staged=new android.util.AtomicFile(StepRecorder.exportFile(this));
        try{
            java.io.FileOutputStream stream=staged.startWrite();
            try{stream.write(steps.export());staged.finishWrite(stream);}
            catch(java.io.IOException failed){staged.failWrite(stream);throw failed;}
            pickSupportDocument(Intent.ACTION_CREATE_DOCUMENT,"text/html","Nth-Desktop-Ten-Steps.html",EXPORT_STEPS);
        }catch(Exception unavailable){staged.delete();toast("The report could not be prepared.");}
    }
    private void stepsResult(int result,Intent data){
        android.util.AtomicFile staged=new android.util.AtomicFile(StepRecorder.exportFile(this));
        if(result!=RESULT_OK||data==null||data.getData()==null){staged.delete();return;}
        Uri target=data.getData();
        worker.execute(()->{
            try(java.io.InputStream input=staged.openRead();java.io.OutputStream output=getContentResolver().openOutputStream(target,"wt")){
                if(output==null)throw new java.io.IOException();
                byte[] bytes=new byte[8192];
                int count;
                while((count=input.read(bytes))!=-1)output.write(bytes,0,count);
                main.post(()->{if(!destroyed)toast("Steps report exported");});
            }catch(Exception failure){main.post(()->{if(!destroyed)toast("The steps report could not be saved.");});}
            finally{staged.delete();}
        });
    }
    private void refreshPersonalization(){
        for(DesktopWindow window:tenDesktops.all())if(window.body instanceof TenSettings)((TenSettings)window.body).refreshAppearance();
        for(DesktopWindow window:orderedWindows())if(window.body instanceof VistaControlPanel
                &&((VistaControlPanel)window.body).page().equals("personalization"))((VistaControlPanel)window.body).render();
    }
    private void saveTheme(){
        EditText name=new EditText(this);name.setSingleLine(true);name.setHint("Theme name");name.setContentDescription("Theme name");
        name.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(60)});
        AlertDialog dialog=Aero.dialog(this).setTitle("Save current theme").setView(name)
                .setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(v->{
            Aero.styleDialog(this,dialog);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button->{
                try{
                    JSONObject profile=ThemeProfiles.capture(store,name.getText().toString());
                    ThemeProfiles.add(store,profile);
                    dialog.dismiss();refreshPersonalization();toast("Theme saved");
                }catch(JSONException invalid){name.setError(invalid.getMessage());}
            });
        });
        dialog.show();
    }
    private void themePreview(JSONObject profile,int index){
        if(profile==null)return;
        LinearLayout content=Aero.column(this);Aero.pad(content,12);
        JSONArray pictures=profile.optJSONArray("backgrounds");
        if(pictures==null||pictures.length()==0)return;
        String first=pictures.optString(0);
        int stock=DesktopBackgrounds.stockId(first);
        AeroWallpaper sample=new AeroWallpaper(this,stock>=0?stock:AeroWallpaper.SEVEN,false);
        content.addView(sample,new LinearLayout.LayoutParams(-1,dp(140)));
        int tint=profile.optInt("aero_color");
        paragraph(content,pictures.length()+" picture"+(pictures.length()==1?"":"s")+" · "+profile.optString("position")
                +" · "+AeroGlass.COLOR_NAMES[Math.max(0,Math.min(AeroGlass.COLOR_NAMES.length-1,tint))]
                +(profile.optBoolean("aero_transparency")?" glass":" solid"));
        AlertDialog dialog=Aero.dialog(this).setTitle(profile.optString("name","Theme preview")).setView(content)
                .setNegativeButton("Cancel",null)
                .setPositiveButton(index<0?"Save theme":"Apply",(d,which)->{
                    try{
                        if(index<0){ThemeProfiles.add(store,profile);toast("Theme added to My Themes");}
                        else{ThemeProfiles.apply(store,profile);applyTenAppearance();toast("Theme applied");}
                        refreshPersonalization();
                    }catch(JSONException invalid){toast(invalid.getMessage());}
                }).create();
        if(index>=0){
            addAction(content,"Export theme…",()->{
                dialog.dismiss();
                pendingThemeExport=profile.toString();
                try{pickSupportDocument(Intent.ACTION_CREATE_DOCUMENT,"application/json",profile.optString("name","Desktop")+".nththeme",EXPORT_THEME);}
                catch(RuntimeException unavailable){pendingThemeExport=null;toast("No document location is available.");}
            });
            addAction(content,"Remove saved theme…",()->{
                dialog.dismiss();
                Aero.dialog(this).setTitle("Remove saved theme?").setMessage(profile.optString("name"))
                        .setPositiveButton("Remove",(confirmation,which)->{ThemeProfiles.remove(store,index);refreshPersonalization();})
                        .setNegativeButton("Cancel",null).show();
            });
        }
        android.os.CancellationSignal cancel=new android.os.CancellationSignal();
        android.graphics.Bitmap[] picture={null};
        dialog.setOnDismissListener(d->{
            cancel.cancel();
            sample.draft(AeroWallpaper.SEVEN,null,0,0,"Fill",BackgroundPicker.COLORS[0]);
            if(picture[0]!=null){picture[0].recycle();picture[0]=null;}
            sample.close();
        });
        dialog.show();
        if(stock<0)worker.execute(()->{
            try{
                PhotoImage preview=PhotoImage.read(this,Uri.parse(first),cancel,640,640L*640);
                main.post(()->{
                    if(destroyed||cancel.isCanceled()||!dialog.isShowing()){preview.bitmap.recycle();return;}
                    picture[0]=preview.bitmap;
                    int background=Math.max(0,Math.min(BackgroundPicker.COLORS.length-1,profile.optInt("wallpaper_bg")));
                    sample.draft(AeroWallpaper.SEVEN,preview.bitmap,preview.width,preview.height,profile.optString("position","Fill"),BackgroundPicker.COLORS[background]);
                });
            }catch(Exception unavailable){}
        });
    }
    private void pickSupportDocument(String action,String mime,String title,int request){
        Intent intent=new Intent(action).addCategory(Intent.CATEGORY_OPENABLE).setType(mime);
        if(title!=null)intent.putExtra(Intent.EXTRA_TITLE,title);
        startActivityForResult(intent,request);
    }
    private void writeSupportDocument(Uri uri,byte[] bytes)throws java.io.IOException{
        try(java.io.OutputStream output=getContentResolver().openOutputStream(uri,"wt")){
            if(output==null)throw new java.io.IOException("The destination could not be opened.");
            output.write(bytes);
        }
    }
    private void themeResult(int request,int result,Intent data){
        String export=pendingThemeExport;
        if(request==EXPORT_THEME)pendingThemeExport=null;
        if(result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        worker.execute(()->{
            try{
                if(request==IMPORT_THEME){
                    JSONObject profile;
                    try(java.io.InputStream input=getContentResolver().openInputStream(uri)){profile=ThemeProfiles.unpack(this,input);}
                    main.post(()->{if(!destroyed)themePreview(profile,-1);});
                }else if(export!=null){
                    writeSupportDocument(uri,ThemeProfiles.pack(this,new JSONObject(export)));
                    main.post(()->{if(!destroyed)toast("Theme exported with its pictures.");});
                }
            }catch(Exception failure){main.post(()->{if(!destroyed)toast(failure.getMessage()==null?"Theme transfer failed.":failure.getMessage());});}
        });
    }
    private void vistaAction(String id){
        if(id.startsWith("theme-preview:")){
            try{int index=Integer.parseInt(id.substring(14));themePreview(ThemeProfiles.saved(store).optJSONObject(index),index);}
            catch(NumberFormatException invalid){}
            return;
        }
        if(id.equals("theme-save")){saveTheme();return;}
        if(id.equals("theme-import")){
            try{pickSupportDocument(Intent.ACTION_OPEN_DOCUMENT,"*/*",null,IMPORT_THEME);}
            catch(RuntimeException unavailable){toast("No document browser is available.");}
            return;
        }
        if(id.startsWith("rename-shortcut:")){organizer("@organize","rename:"+id.substring(16));return;}
        if(id.startsWith("gadget:")&&GadgetCatalog.valid(id.substring(7))){gadgetAction(id.substring(7));return;}
        switch(id){
            case "sidebar-show":case "sidebar-hide":case "sidebar-apply":open("@news");return;
            case "taskbar-refresh":layoutWorkspace();renderTaskbar();return;
            case "taskbar-usage-welcome":
                if(ExternalTasks.permitted(this))open("setting:page:taskbar");
                else showTaskbarUsageGuide(UsageGuide.MANUAL);
                return;
            case "taskbar-usage-enable":
                if(ExternalTasks.permitted(this))setTaskbarUsage(true);
                else showTaskbarUsageGuide(UsageGuide.MANUAL);
                return;
            case "taskbar-usage-disable":setTaskbarUsage(false);return;
            case "taskbar-usage-app-info":openUsageAppInfo();return;
            case "restart-desktop":startMenu.hide();main.post(this::recreate);return;
            case "show-desktop":for(DesktopWindow w:windows.values())if(w.getParent()!=null&&w.getVisibility()==View.VISIBLE)w.minimize();startMenu.hide();return;
            case "storage-access":storageSetup.show(true);return;case "glass":applyTenAppearance();return;case "startup-preview":if(startup!=null)startup.preview();return;
            case "chrome":applySystemChrome();return;case "desktop":renderDesktop();return;case "gadgets":services.setActive(true);services.refresh();return;case "wallpaper":applyTenAppearance();return;
            case "wallpaper-browse":chooseWallpapers(false);return;
            case "wallpaper-folder":chooseWallpapers(true);return;
            case "profile":startMenu.refreshName();return;case "profile-pick":chooseProfilePicture();return;case "home":home();return;case "shortcuts":manageShortcuts();return;case "new-folder":organizer("@organize","new");return;
            case "reset-icons":Aero.dialog(this).setTitle("Restore desktop icons?").setMessage("Restore the original desktop icons and their order? Your notes and apps are kept.").setPositiveButton("Restore",(d,w)->{store.prefs.edit().remove("shortcuts").apply();renderDesktop();}).setNegativeButton("Cancel",null).show();return;
        }
        String action;
        switch(id){
            case "android:wifi":action=Settings.ACTION_WIFI_SETTINGS;break;case "android:network":action=Settings.ACTION_WIRELESS_SETTINGS;break;case "android:bluetooth":action=Settings.ACTION_BLUETOOTH_SETTINGS;break;
            case "android:sound":action=Settings.ACTION_SOUND_SETTINGS;break;case "android:display":action=Settings.ACTION_DISPLAY_SETTINGS;break;case "android:apps":action=Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS;break;
            case "android:defaults":action=Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS;break;case "android:security":action=Settings.ACTION_SECURITY_SETTINGS;break;case "android:privacy":action=Settings.ACTION_PRIVACY_SETTINGS;break;
            case "android:storage":action=Settings.ACTION_INTERNAL_STORAGE_SETTINGS;break;case "android:battery":action=Settings.ACTION_BATTERY_SAVER_SETTINGS;break;case "android:accounts":action=Settings.ACTION_SYNC_SETTINGS;break;
            case "android:accessibility":action=Settings.ACTION_ACCESSIBILITY_SETTINGS;break;case "android:date":action=Settings.ACTION_DATE_SETTINGS;break;case "android:language":action=Settings.ACTION_LOCALE_SETTINGS;break;case "android:device":action=Settings.ACTION_DEVICE_INFO_SETTINGS;break;
            case "android:print":action=Settings.ACTION_PRINT_SETTINGS;break;case "android:input":action=Settings.ACTION_INPUT_METHOD_SETTINGS;break;case "android:cast":action=Settings.ACTION_CAST_SETTINGS;break;case "android:wallpaper":action=Intent.ACTION_SET_WALLPAPER;break;case "android:connected":action=Settings.ACTION_BLUETOOTH_SETTINGS;break;case "android:airplane":action=Settings.ACTION_AIRPLANE_MODE_SETTINGS;break;case "android:vpn":action=Settings.ACTION_VPN_SETTINGS;break;case "android:speech":action="com.android.settings.TTS_SETTINGS";break;case "android:notifications":launchSystem(new Intent(Build.VERSION.SDK_INT>=33?Settings.ACTION_ALL_APPS_NOTIFICATION_SETTINGS:Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));return;case "android:permissions":launchSystem(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.fromParts("package",getPackageName(),null)));return;
            default:action=Settings.ACTION_SETTINGS;
        }
        try{startActivity(new Intent(action));}catch(ActivityNotFoundException|SecurityException ex){launchSystem(new Intent(Settings.ACTION_SETTINGS));}
    }
    private void showTaskbarUsageGuide(UsageGuide mode){
        if(usageGuide!=null||destroyed||restoringBackup||isFinishing())return;
        if(mode!=UsageGuide.FIRST_RUN)startup.takeUsageSetupOffer();

        String appName=getString(R.string.app_name);
        boolean returning=mode==UsageGuide.RETURN;
        boolean showAppInfo=Build.VERSION.SDK_INT>=33;
        String message=returning
                ?"Usage Access is still off. "+(showAppInfo
                        ?"If Android showed ‘App was denied access’, tap Open App info below."
                        :"Tap OK to try again, or Cancel to leave it off.")
                :"Usage Access is optional. It lets "+appName+" show apps you open elsewhere on the taskbar. "
                        +"Tap OK, turn on Usage Access for "+appName+", then press Back. The checkbox turns on when you return.";
        LinearLayout content=Aero.column(this);
        Aero.pad(content,16);
        content.addView(Aero.text(this,message,14,Aero.INK));
        if(showAppInfo){
            String steps="In App info, tap the three dots (⋮) and choose Allow restricted settings if shown. "
                    +"Then press Back to return to the Usage Access switch.";
            if(!returning)steps="If Android says ‘App was denied access’, tap Close, then press Back to "+appName+". "
                    +"This guide will reopen. Tap Open App info below. "+steps;
            TextView recovery=Aero.text(this,steps,14,Aero.INK);
            recovery.setPadding(0,dp(12),0,dp(10));
            content.addView(recovery);

            content.addView(new UsageAccessExample(this),new LinearLayout.LayoutParams(-1,-2));
        }

        ScrollView scroll=new ScrollView(this);
        scroll.addView(content);
        AlertDialog.Builder guide=Aero.dialog(this).setTitle("Set up Usage Access").setView(scroll)
                .setPositiveButton("OK",(dialog,which)->requestTaskbarUsage())
                .setNegativeButton(mode==UsageGuide.FIRST_RUN?"Later":"Cancel",null);
        if(showAppInfo)guide.setNeutralButton("Open App info",(dialog,which)->openUsageAppInfo());
        usageGuide=guide.create();
        usageGuide.setOnDismissListener(dialog->{
            usageGuide=null;
            if(!destroyed)refreshUsageAccess();
        });
        usageGuide.show();
    }

    private void requestTaskbarUsage(){
        if(ExternalTasks.permitted(this)){
            setTaskbarUsage(true);
            return;
        }
        Uri ownApp=Uri.fromParts("package",getPackageName(),null);
        if(!launchUsageSettings(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,ownApp),REQUEST_USAGE_ACCESS)){
            if(!launchUsageSettings(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),REQUEST_USAGE_ACCESS)){
                toast("Usage Access settings are unavailable on this device.");
                return;
            }
            toast("Select "+getString(R.string.app_name)+" and turn on usage access.");
        }
        // A new setup attempt follows Android's grant again once Settings opens.
        store.usagePrefs.edit().remove(DesktopStore.TASKBAR_USAGE).apply();
        ExternalTasks.resetObservation();
    }

    private void openUsageAppInfo(){
        startup.takeUsageSetupOffer();
        Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.fromParts("package",getPackageName(),null));
        if(!launchUsageSettings(intent,REQUEST_USAGE_APP_INFO))toast("App info is unavailable on this device.");
    }

    private boolean launchUsageSettings(Intent intent,int request){
        usageSettingsRequest=request;
        usageSettingsLeft=false;
        try{
            startActivityForResult(intent,request);
            return true;
        }catch(ActivityNotFoundException|SecurityException unavailable){
            usageSettingsRequest=0;
            return false;
        }
    }

    private void resumeUsageAccessGuide(){
        if(destroyed||restoringBackup||isFinishing()||!hasWindowFocus())return;
        if(refreshUsageAccess())scanApps();
        // Android may deliver a Settings result before its activity has taken focus.
        if(!usageSettingsLeft)return;
        int request=usageSettingsRequest;
        usageSettingsRequest=0;
        usageSettingsLeft=false;
        if(request==REQUEST_USAGE_APP_INFO)requestTaskbarUsage();
        else if(request==REQUEST_USAGE_ACCESS&&!ExternalTasks.permitted(this))showTaskbarUsageGuide(UsageGuide.RETURN);
    }

    private void setTaskbarUsage(boolean enabled){
        store.usagePrefs.edit().putBoolean(DesktopStore.TASKBAR_USAGE,enabled).apply();
        ExternalTasks.resetObservation();
        refreshUsageAccess();
        scanApps();
    }

    private boolean refreshUsageAccess(){
        boolean permitted=ExternalTasks.permitted(this);
        if(permitted&&usageGuide!=null)usageGuide.dismiss();
        for(DesktopWindow window:windows.values()){
            if(window.getParent()!=null&&window.body instanceof VistaControlPanel)
                ((VistaControlPanel)window.body).refreshUsageAccess(permitted);
        }
        boolean included=ExternalTasks.includesExternalApps(store,permitted);
        if(includesExternalApps==included)return false;
        includesExternalApps=included;
        if(!included)ExternalTasks.resetObservation();
        renderTaskbar();
        return true;
    }

    private void chooseWallpapers(boolean folder){
        Intent pick=new Intent(this,ExplorerPickerActivity.class).setAction(PickerSpec.ACTION)
                .setType("image/*").putExtra(Intent.EXTRA_ALLOW_MULTIPLE,!folder)
                .putExtra(ExplorerPickerActivity.EXTRA_WALLPAPER_FOLDER,folder);
        try{startActivityForResult(pick,folder?PICK_WALLPAPER_FOLDER:PICK_WALLPAPERS);}
        catch(ActivityNotFoundException ex){toast("Nth Explorer could not be opened.");}
    }
    private void addWallpaper(Set<String> photos,Uri uri){
        if(uri!=null&&photos.size()<DesktopBackgrounds.LIMIT&&"content".equals(uri.getScheme())&&DesktopBackgrounds.valid(uri.toString()))
            photos.add(uri.toString());
    }
    private void importWallpapers(Intent data,boolean folder){
        Uri chosen=data.getData();
        // Explorer uses our own storage provider or a linked folder with retained access.
        if(folder){
            if(chosen==null||!"content".equals(chosen.getScheme())||!DocumentsContract.isTreeUri(chosen)){
                toast("Choose an accessible folder in Nth Explorer.");return;
            }
            worker.execute(()->{
                Set<String> photos=new LinkedHashSet<>();
                try{
                    List<DocumentOps.Ref> items=DocumentOps.children(this,chosen,new DocumentOps.Cancel());
                    items.sort(Comparator.comparing(ref->ref.item.name,String.CASE_INSENSITIVE_ORDER));
                    for(DocumentOps.Ref ref:items){
                        if(photos.size()==DesktopBackgrounds.LIMIT)break;
                        if(!ref.item.directory()&&(ref.item.flags&DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT)==0
                                &&PhotoImage.picture(ref.item.name,ref.item.mime))addWallpaper(photos,ref.uri);
                    }
                }catch(Exception ignored){}
                main.post(()->afterDesktopReady(()->applyWallpaperImport(new ArrayList<>(photos))));
            });
            return;
        }
        Set<String> photos=new LinkedHashSet<>();
        ClipData clip=data.getClipData();
        if(clip!=null)for(int i=0;i<Math.min(DesktopBackgrounds.LIMIT,clip.getItemCount());i++)
            addWallpaper(photos,clip.getItemAt(i).getUri());
        addWallpaper(photos,chosen);
        afterDesktopReady(()->applyWallpaperImport(new ArrayList<>(photos)));
    }
    private void applyWallpaperImport(List<String> photos){
        if(destroyed||restoringBackup)return;
        if(photos.isEmpty()){toast("No accessible pictures were found.");return;}
        DesktopWindow tenSettings=windows.get("@settings");if(tenSettings!=null&&tenSettings.getParent()!=null&&tenSettings.body instanceof TenSettings&&((TenSettings)tenSettings.body).page.equals("background")){android.content.SharedPreferences.Editor edit=store.prefs.edit();DesktopBackgrounds.choose(edit,photos,11);edit.apply();wallpaper.reload();((TenSettings)tenSettings.body).navigate("background");return;}
        List<DesktopWindow> ordered=orderedWindows();Collections.reverse(ordered);
        for(DesktopWindow window:ordered)if(window.getVisibility()==View.VISIBLE&&window.body instanceof VistaControlPanel){
            VistaControlPanel panel=(VistaControlPanel)window.body;
            if(panel.page().equals("background")){panel.wallpaperChosen(photos);return;}
        }
        open("setting:page:background");
        DesktopWindow panel=windows.get("@legacycontrol");
        if(panel!=null&&panel.body instanceof VistaControlPanel)((VistaControlPanel)panel.body).wallpaperChosen(photos);
    }
    private void chooseProfilePicture(){chooseDocument("image/*",15);}
    private void chooseSearchFile(){if(LinkedFiles.list(store).length()>=LinkedFiles.MAX){toast("50 files are already linked. Remove a file link in Computer first.");return;}chooseDocument("*/*",16);}
    private void chooseDocument(String mime,int request){try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(mime).putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI,android.provider.DocumentsContract.buildDocumentUri("com.android.externalstorage.documents",request==15?"primary:Pictures":"primary:Documents")).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),request);}catch(ActivityNotFoundException|SecurityException ex){toast("No file picker is available on this device.");}}
    private void pickDocument(String kind){String mime=kind.equals("pictures")?"image/*":kind.equals("music")?"audio/*":kind.equals("videos")?"video/*":"*/*";launchSystem(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),true);}
    MediaPlayback mediaPlayback(){
        if(playback==null){
            playback=new MediaPlayback(this);
            playback.player.addListener(new androidx.media3.common.Player.Listener(){
                @Override public void onMediaItemTransition(androidx.media3.common.MediaItem item,int reason){
                    if(item!=null)mediaLibrary().played(playback.current());
                }
            });
        }
        return playback;
    }
    MediaLibrary mediaLibrary(){
        if(mediaLibrary==null)mediaLibrary=new MediaLibrary(store);
        return mediaLibrary;
    }
    private final MediaPanel.Host mediaHost=new MediaPanel.Host(){
        public void addFiles(){chooseMedia();}
        public void switchView(boolean center){
            if(center)open(MediaLibrary.CENTER);
            else{closeMediaCenter();mediaPlayer().showNow();}
        }
        public void closeCenter(){closeMediaCenter();}
        public void libraryChanged(){
            MediaPanel player=playerPanel();
            if(player!=null)player.libraryChanged();
            if(mediaCenter!=null)mediaCenter.libraryChanged();
        }
    };
    private MediaPanel playerPanel(){
        DesktopWindow existing=windows.get(MediaLibrary.PLAYER);
        return existing!=null&&existing.getParent()!=null&&existing.body instanceof MediaPanel
                ?(MediaPanel)existing.body:null;
    }
    private MediaPanel mediaPlayer(){
        MediaPanel panel=playerPanel();
        if(panel==null){
            panel=new MediaPanel(this,mediaPlayback(),mediaLibrary(),mediaHost,false);
            window(MediaLibrary.PLAYER,MediaLibrary.PLAYER_TITLE,panel);
        }else restoreTool(MediaLibrary.PLAYER);
        if(mediaCenter==null)mediaPlayback().surface(panel.videoSurface());
        return panel;
    }
    void openMedia(MediaLibrary.Entry entry){
        if(mediaCenter!=null)closeMediaCenter();
        MediaPanel panel=mediaPlayer();
        mediaPlayback().play(Collections.singletonList(entry),0);
        panel.showNow();
    }
    private void openMediaCenter(){
        if(mediaCenter!=null)return;
        DesktopWindow browser=windows.get("@browser");
        if(browser!=null&&browser.body instanceof BrowserPanel)((BrowserPanel)browser.body).exitFullScreen();
        if(videoOverlay!=null)return;
        if(previews!=null)previews.dismiss();
        mediaCenter=new MediaPanel(this,mediaPlayback(),mediaLibrary(),mediaHost,true);
        TenAppearance.bind(mediaCenter);enterFullscreen(mediaCenter,MediaLibrary.CENTER_TITLE);
        mediaPlayback().surface(mediaCenter.videoSurface());
    }
    private void closeMediaCenter(){
        if(mediaCenter==null)return;
        MediaPanel center=mediaCenter;
        mediaCenter=null;
        MediaPanel player=playerPanel();
        mediaPlayback().surface(player==null?null:player.videoSurface());
        if(player==null)mediaPlayback().player.pause();
        exitBrowserFullscreen(center);
    }
    private void chooseMedia(){
        if(mediaLibrary().remaining()==0){toast("The media library is full. Remove an entry first.");return;}
        Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                .setType("*/*").putExtra(Intent.EXTRA_MIME_TYPES,MediaLibrary.TYPES)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try{startActivityForResult(pick,PICK_MEDIA);}
        catch(ActivityNotFoundException|SecurityException unavailable){toast("No Android document picker is available.");}
    }
    private void importMedia(Intent data){
        Set<Uri> selected=new LinkedHashSet<>();
        ClipData clip=data.getClipData();
        if(clip!=null)for(int i=0;i<Math.min(clip.getItemCount(),MediaLibrary.LIMIT);i++)selected.add(clip.getItemAt(i).getUri());
        else if(data.getData()!=null)selected.add(data.getData());
        int room=mediaLibrary().remaining();
        selected.removeIf(mediaLibrary()::contains);
        worker.execute(()->{
            List<MediaLibrary.Entry> files=new ArrayList<>();
            for(Uri uri:selected){
                if(files.size()>=room)break;
                MediaLibrary.Entry entry=MediaLibrary.describe(this,uri);
                if(entry==null)continue;
                try{
                    getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    files.add(entry);
                }catch(SecurityException unavailable){}
            }
            main.post(()->{
                if(destroyed||restoringBackup)return;
                int added=mediaLibrary().add(files);
                mediaHost.libraryChanged();
                toast(added>0?added+" added to the media library"
                        :selected.isEmpty()?"These files are already in the library":"No supported files with lasting access were added.");
            });
        });
    }
    private PhotoGallery photoGallery(){
        DesktopWindow existing=windows.get("@photos");if(existing!=null&&existing.getParent()!=null&&existing.body instanceof PhotoGallery){existing.restore();return (PhotoGallery)existing.body;}
        PhotoGallery gallery=new PhotoGallery(this,new PhotoGallery.Host(){public void openPicture(){try{startActivityForResult(new Intent(MainActivity.this,ExplorerPickerActivity.class).setAction(PickerSpec.ACTION).setType("image/*"),304);}catch(RuntimeException ex){toast("The picture picker could not be opened.");}}public void pictures(){explorer("@files","home");DesktopWindow files=windows.get("@files");if(files!=null&&files.body instanceof VistaExplorer)((VistaExplorer)files.body).openDevice("pictures");}public void caption(String title){DesktopWindow window=windows.get("@photos");if(window!=null&&!window.caption().equals(title)){window.caption(title);main.removeCallbacks(taskbarRender);main.post(taskbarRender);}}public void back(JSONObject context,String origin){DesktopWindow photo=windows.get("@photos");if(photo!=null)photo.minimize();String page=context.optString("page");if(!page.startsWith("folder:"))return;String target=Arrays.asList("@computer","@filemanagers","@files").contains(origin)||validExplorerId(origin)||FolderLinks.id(origin)?origin:"@files";DesktopWindow previous=windows.get(target);if(previous==null||previous.getParent()==null)explorer(target,page);else{previous.restore();if(previous.body instanceof VistaExplorer&&!((VistaExplorer)previous.body).page().equals(page))((VistaExplorer)previous.body).navigate(page);}}});window("@photos","Photo Gallery",gallery);return gallery;
    }
    void openPhoto(Uri uri,String name,String mime,JSONObject source,String origin){RecentFiles.opened(store,uri,mime,name);photoGallery().open(uri,name,mime,source,origin);}
    private void viewDocument(Uri uri,String mime){viewDocument(uri,mime,null,false);}
    private void viewDocument(Uri uri,String mime,String name,boolean chooser){
        worker.execute(()->{String display=name;if(display==null||display.isEmpty())try(android.database.Cursor cursor=getContentResolver().query(uri,new String[]{android.provider.OpenableColumns.DISPLAY_NAME},null,null,null)){if(cursor!=null&&cursor.moveToFirst())display=cursor.getString(0);}catch(RuntimeException ignored){}String label=display;main.post(()->{if(!destroyed&&!isFinishing()){RecentFiles.opened(store,uri,mime,label);FileLaunch.open(this,uri,label,mime,chooser);}});});
    }
    private void chooseLinkedFolder(String id){chooseLinkedFolder(id,null);}
    private void chooseLinkedFolder(String id,Uri initial){pendingFolderWindow=id;try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).putExtra(DocumentsContract.EXTRA_INITIAL_URI,initial).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),14);}catch(ActivityNotFoundException|SecurityException ex){toast("No folder picker is available on this device.");}}
    private void afterDesktopReady(Runnable action){if(workspace.isLaidOut()&&workspace.getWidth()>0){action.run();return;}workspace.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){public void onGlobalLayout(){workspace.getViewTreeObserver().removeOnGlobalLayoutListener(this);if(!destroyed)action.run();}});}
    private void calendar(){
        startMenu.hide();if(restoreTool("@calendar"))return;LinearLayout content=Aero.column(this);Aero.pad(content,12);
        TextView monthLabel=Aero.text(this,"",17,0xff173a65);monthLabel.setGravity(Gravity.CENTER);TextView date=Aero.text(this,"",12,0xff4a6071);date.setGravity(Gravity.CENTER);
        MonthCalendarView[] ref=new MonthCalendarView[1];MonthCalendarView calendar=new MonthCalendarView(this,()->{monthLabel.setText(ref[0].title());date.setText(java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL,Locale.getDefault()).format(ref[0].selected()));});ref[0]=calendar;
        LinearLayout navigation=new LinearLayout(this);navigation.addView(Aero.button(this,"‹",()->calendar.offset(-1)),new LinearLayout.LayoutParams(dp(44),dp(42)));navigation.addView(monthLabel,new LinearLayout.LayoutParams(0,dp(42),1));navigation.addView(Aero.button(this,"›",()->calendar.offset(1)),new LinearLayout.LayoutParams(dp(44),dp(42)));content.addView(navigation);
        calendar.setMinimumHeight(dp(240));content.addView(calendar,new LinearLayout.LayoutParams(-1,dp(240)));content.addView(date,new LinearLayout.LayoutParams(-1,dp(37)));addAction(content,"Today",calendar::today);monthLabel.setText(calendar.title());date.setText(java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL,Locale.getDefault()).format(calendar.selected()));ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.addView(content);window("@calendar","Calendar",scroll);
    }
    @Override public void home(){
        startMenu.hide();
        if(!requestHome())toast("Android Home settings are unavailable on this device.");
    }
    boolean offerInitialHome(SharedPreferences session){
        if(homeRequestInFlight)return true;
        if(restoringBackup||isFinishing()||destroyed||!hasWindowFocus())return false;
        if(session.getBoolean(StartupExperience.HOME_OFFERED,false))return false;
        // Record the offer before leaving for Android so canceling it never causes a prompt loop.
        session.edit().putBoolean(StartupExperience.HOME_OFFERED,true).commit();
        return !isHome()&&requestHome();
    }
    private boolean requestHome(){
        if(homeRequestInFlight)return true;
        if(Build.VERSION.SDK_INT>=29){
            RoleManager roles=getSystemService(RoleManager.class);
            if(roles!=null&&roles.isRoleAvailable(RoleManager.ROLE_HOME)&&!roles.isRoleHeld(RoleManager.ROLE_HOME)){
                if(launchHomeRequest(roles.createRequestRoleIntent(RoleManager.ROLE_HOME)))return true;
            }
        }
        return launchHomeRequest(new Intent(Settings.ACTION_HOME_SETTINGS));
    }
    private boolean launchHomeRequest(Intent intent){
        homeRequestInFlight=true;
        try{
            startActivityForResult(intent,REQUEST_HOME);
            return true;
        }catch(ActivityNotFoundException|SecurityException unavailable){
            homeRequestInFlight=false;
            return false;
        }
    }
    @Override public boolean isHome(){
        if(Build.VERSION.SDK_INT>=29){RoleManager roles=getSystemService(RoleManager.class);if(roles!=null&&roles.isRoleAvailable(RoleManager.ROLE_HOME))return roles.isRoleHeld(RoleManager.ROLE_HOME);}
        Intent home=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);android.content.pm.ResolveInfo result=getPackageManager().resolveActivity(home,PackageManager.MATCH_DEFAULT_ONLY);return result!=null&&getPackageName().equals(result.activityInfo.packageName);
    }
    @Override public void settings(){open("@control");}
    private void launchSystem(Intent intent){launchSystem(intent,false);}
    private void launchSystem(Intent intent,boolean result){
        try{if(result)startActivityForResult(intent,11);else startActivity(intent);}catch(ActivityNotFoundException|SecurityException ex){toast("No app is available for that action on this device.");}
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==REQUEST_HOME){
            homeRequestInFlight=false;
            if(startMenu!=null)startMenu.refreshHome();
            if(startup!=null)root.post(startup::maybeShow);
            return;
        }
        if(request==REQUEST_USAGE_ACCESS||request==REQUEST_USAGE_APP_INFO){
            refreshUsageAccess();
            root.post(this::resumeUsageAccessGuide);
            return;
        }
        if(request==EXPORT_STEPS){stepsResult(result,data);return;}
        if(request==IMPORT_THEME||request==EXPORT_THEME){themeResult(request,result,data);return;}
        if(request==PICK_MEDIA){if(result==RESULT_OK&&data!=null)importMedia(data);return;}
        if(request==PICK_WALLPAPERS||request==PICK_WALLPAPER_FOLDER){
            if(result==RESULT_OK&&data!=null)importWallpapers(data,request==PICK_WALLPAPER_FOLDER);
            return;
        }
        if(request==304){if(result==RESULT_OK&&data!=null&&data.getData()!=null)openPhoto(data.getData(),null,"image/*",null,"@files");return;}
        DesktopWindow browser=windows.get("@browser");if(browser!=null&&browser.getParent()!=null&&browser.body instanceof BrowserPanel&&((BrowserPanel)browser.body).result(request,result,data))return;
        if((request==15||request==16)&&result==RESULT_OK&&data!=null&&data.getData()!=null){
            Uri uri=data.getData();if(!"content".equals(uri.getScheme())){toast("Choose a file from the document picker.");return;}
            if(request==16)try{int flags=data.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION;if(flags==0)throw new SecurityException();getContentResolver().takePersistableUriPermission(uri,flags);}catch(SecurityException ex){toast("This provider cannot keep file access. Choose another file.");return;}
            worker.execute(()->{try{if(request==15){String portrait=ProfilePortrait.importPicture(getApplicationContext(),uri);store.prefs.edit().putString("profile_portrait",portrait).apply();}else{JSONObject file=LinkedFiles.describe(getApplicationContext(),uri);if(!LinkedFiles.add(store,file))throw new IllegalStateException("50 files are already linked");}main.post(()->{if(destroyed||restoringBackup)return;startMenu.refreshName();for(DesktopWindow tool:windows.values())if(tool.getParent()!=null&&tool.body instanceof VistaControlPanel)((VistaControlPanel)tool.body).render();else if(tool.getParent()!=null&&tool.body instanceof VistaExplorer)((VistaExplorer)tool.body).render();toast(request==15?"Profile picture updated":"File added to Start search");});}catch(Exception ex){main.post(()->{if(!destroyed)toast(request==15?"This picture could not be opened. Choose another picture.":"This file could not be linked. Try choosing it again.");});}});return;
        }
        if(request==20||request==21){backupResult(request,result,data);return;}
        if(request==14&&result==RESULT_OK&&data!=null&&data.getData()!=null){
            Uri tree=data.getData();String target=pendingFolderWindow;int flags=data.getFlags()&(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try{if((flags&Intent.FLAG_GRANT_READ_URI_PERMISSION)==0||!android.provider.DocumentsContract.isTreeUri(tree))throw new SecurityException();getContentResolver().takePersistableUriPermission(tree,flags);}catch(SecurityException ex){toast("This provider couldn't keep folder access. Please choose another folder.");return;}
            worker.execute(()->{String label;try{label=DocumentFolders.rootName(this,tree);}catch(RuntimeException ex){label="Linked folder";}String name=label;main.post(()->{if(destroyed||restoringBackup)return;if(!DocumentFolders.add(store,tree,name)){try{getContentResolver().releasePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(SecurityException ignored){}toast("16 folders are already linked. Remove a folder link before adding another.");return;}afterDesktopReady(()->{explorer(target,target.equals("@computer")?"computer":target.equals("@filemanagers")?"managers":"home");DesktopWindow tool=windows.get(target);if(tool!=null&&tool.body instanceof VistaExplorer)((VistaExplorer)tool.body).folderAdded(tree.toString(),name);});});});return;
        }
        if(request==13&&result==RESULT_OK&&data!=null){
            List<String> selected=new ArrayList<>();if(data.getClipData()!=null){for(int i=0;i<Math.min(12,data.getClipData().getItemCount());i++)rememberPhoto(data.getClipData().getItemAt(i).getUri(),selected);}else if(data.getData()!=null)rememberPhoto(data.getData(),selected);
            if(!selected.isEmpty()){store.photos(selected);List<String> widgets=store.gadgets();if(!widgets.contains("photo")){widgets.add("photo");store.gadgets(widgets);}toast(selected.size()==1?"Photo frame updated":"Slideshow updated");}return;
        }
        if(request==11&&result==RESULT_OK&&data!=null&&data.getData()!=null){
            Uri uri=data.getData();String mime;try{mime=getContentResolver().getType(uri);}catch(RuntimeException ex){mime=null;}viewDocument(uri,mime);
        }
    }
    private void paragraph(LinearLayout parent,String text){TextView p=Aero.text(this,text,14,0xff476071);p.setLineSpacing(dp(3),1);p.setPadding(0,dp(12),0,dp(12));parent.addView(p);}
    private void addAction(LinearLayout parent,String label,Runnable action){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(8);parent.addView(Aero.button(this,label,action),lp);}
    private int dp(int value){return Aero.dp(this,value);}
    private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_SHORT).show();}
    private void updateClock(){
        if(tray!=null)tray.clockNow();
        if(clock!=null){
            Date now=new Date();
            clock.setText(DateFormat.getTimeFormat(this).format(now)+"\n"+new SimpleDateFormat("M/d/yyyy",Locale.getDefault()).format(now));
        }
    }
    private void handleBack(){if(tenNews!=null&&tenNews.dismiss())return;
        if(tenTasks!=null&&tenTasks.dismiss())return;if(tenActions!=null&&tenActions.dismiss())return;
        if(restoringBackup)return;
        if(jumpMenu!=null&&jumpMenu.showing()){jumpMenu.dismiss();return;}
        if(previews!=null&&previews.dismiss())return;
        if(mediaCenter!=null){closeMediaCenter();return;}
        if(videoOverlay!=null){DesktopWindow browser=windows.get("@browser");if(browser!=null&&browser.body instanceof BrowserPanel){((BrowserPanel)browser.body).exitFullScreen();return;}}
        if(startup!=null&&startup.skip())return;
        if(tray!=null&&tray.dismiss())return;
        if(desktopMenu!=null&&desktopMenu.showing()){desktopMenu.dismiss();return;}
        if(startMenu.getVisibility()==View.VISIBLE){startMenu.hide();return;}
        for(int i=workspace.getChildCount()-1;i>=0;i--){View v=workspace.getChildAt(i);if(v instanceof DesktopWindow&&v.getVisibility()==View.VISIBLE){DesktopWindow tool=(DesktopWindow)v;if(tool.body instanceof PhotoGallery&&((PhotoGallery)tool.body).back())return;if(tool.body instanceof BrowserPanel&&((BrowserPanel)tool.body).goBack())return;if(tool.body instanceof VistaShell&&((VistaShell)tool.body).goBack())return;tool.minimize();((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(),0);renderTaskbar();return;}}
    }
    @Override public boolean dispatchTouchEvent(MotionEvent event){
        if(steps.active()&&event.getActionMasked()==MotionEvent.ACTION_UP&&recordableDesktop()&&workspace!=null){
            int[] origin=new int[2];workspace.getLocationOnScreen(origin);
            float x=event.getRawX()-origin[0],y=event.getRawY()-origin[1];
            if(x>=0&&y>=0&&x<workspace.getWidth()&&y<workspace.getHeight()){
                int across=Math.round(x*100/workspace.getWidth()),down=Math.round(y*100/workspace.getHeight());
                steps.record("Desktop touch at "+across+"% across, "+down+"% down");
            }
        }
        return super.dispatchTouchEvent(event);
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event){
        if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0&&videoOverlay==null&&!restoringBackup){
            int key=event.getKeyCode();if(event.isMetaPressed()&&key==KeyEvent.KEYCODE_R){open("@run");return true;}if(event.isMetaPressed()&&key==KeyEvent.KEYCODE_X){ContextMenus.centered(tenStartButton,(x,y)->startOrbMenu(tenStartButton,x,y));return true;}if(event.isMetaPressed()&&event.isCtrlPressed()){if(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT){tenDesktops.adjacent(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);return true;}if(key==KeyEvent.KEYCODE_D){String created=tenDesktops.create();if(created!=null)tenDesktops.select(created);return true;}}if(event.isMetaPressed()&&key==KeyEvent.KEYCODE_N){open("@nota");return true;}if(event.isMetaPressed()&&key==KeyEvent.KEYCODE_W){open("@news");return true;}if(key==KeyEvent.KEYCODE_ESCAPE&&tenNews!=null&&tenNews.dismiss())return true;if(event.isMetaPressed()&&key==KeyEvent.KEYCODE_A){open("@tenactions");return true;}if(event.isMetaPressed()&&key==KeyEvent.KEYCODE_I){open("@settings");return true;}if(event.isMetaPressed()&&key==KeyEvent.KEYCODE_TAB){showTenTasks();return true;}if(event.isAltPressed()&&key==KeyEvent.KEYCODE_TAB){previews.show();return true;}if(event.isCtrlPressed()&&event.isShiftPressed()&&key==KeyEvent.KEYCODE_ESCAPE){open("@taskmanager");return true;}
            if(event.isMetaPressed()&&key==KeyEvent.KEYCODE_D){showDesktop();return true;}
            if(event.isMetaPressed()&&key==KeyEvent.KEYCODE_E){open("@files");return true;}
            DesktopWindow front=null;for(DesktopWindow window:orderedWindows())if(window.getVisibility()==View.VISIBLE&&!window.minimizing())front=window;
            if(front!=null&&event.isMetaPressed()){
                switch(key){
                    case KeyEvent.KEYCODE_DPAD_LEFT: return front.snap(AeroSnap.Placement.LEFT);
                    case KeyEvent.KEYCODE_DPAD_RIGHT: return front.snap(AeroSnap.Placement.RIGHT);
                    case KeyEvent.KEYCODE_DPAD_UP: front.maximize();return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN: if(front.snapped()||front.maximized())front.restoreDown();else front.minimize();return true;
                    case KeyEvent.KEYCODE_MOVE_HOME: shake(front);return true;
                }
            }
            if(front!=null&&event.isAltPressed()&&key==KeyEvent.KEYCODE_F4){front.closeWindow();return true;}
            if(front!=null&&event.isAltPressed()&&key==KeyEvent.KEYCODE_SPACE){DesktopWindow target=front;ContextMenus.centered(front,(x,y)->target.showSystemMenu(target,x,y));return true;}
        }
        return super.dispatchKeyEvent(event);
    }
    @Override public void onBackPressed(){handleBack();}
    private void handleRequestedTool(){String imported=getIntent().getStringExtra("nota_import");if(imported!=null){getIntent().removeExtra("nota_import");String passage=imported.substring(0,Math.min(imported.length(),TenWritingPrompts.MAX_COMPOSER));afterDesktopReady(()->nota(new TenNotaContext("Android text selection or share",passage,false)));return;}String tool=getIntent().getStringExtra("open_tool");if(tool==null)return;getIntent().removeExtra("open_tool");if(Arrays.asList("@nota","@news","@settings","@files","@computer","@control","@appearance","@system","@filemanagers","@notes","@gadgets","@backup","@browser","@photos","@taskmanager","@recycle","@recent","@actioncenter",MediaLibrary.PLAYER,MediaLibrary.CENTER).contains(tool))afterDesktopReady(()->open(tool));}
    @Override public void onWindowFocusChanged(boolean focused){
        super.onWindowFocusChanged(focused);
        if(!focused||store==null||destroyed)return;
        applySystemChrome();
        root.post(this::resumeUsageAccessGuide);
        if(startup!=null)root.post(startup::maybeShow);
    }
    void startupSetupReady(){
        root.post(()->{
            if(destroyed||restoringBackup||isFinishing()||!hasWindowFocus()||usageGuide!=null||usageSettingsRequest!=0)return;
            boolean force=restoreStorageSetup;
            restoreStorageSetup=false;
            storageSetup.show(force);
            if(!storageSetup.showing()&&startup.takeUsageSetupOffer()
                    &&!ExternalTasks.permitted(this)&&!store.usagePrefs.contains(DesktopStore.TASKBAR_USAGE)){
                showTaskbarUsageGuide(UsageGuide.FIRST_RUN);
            }
        });
    }
    void storageFolderSetup(){afterDesktopReady(()->{explorer("@computer","computer");chooseLinkedFolder("@computer");});}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);closeMediaCenter();DesktopWindow currentBrowser=windows.get("@browser");if(currentBrowser!=null&&currentBrowser.body instanceof BrowserPanel)((BrowserPanel)currentBrowser.body).exitFullScreen();handleRequestedTool();if(desktopMenu!=null)desktopMenu.dismiss();if(tray!=null)tray.dismiss();if(startMenu!=null){startMenu.hide();startMenu.refreshHome();}}
    @Override protected void onResume(){
        for(DesktopWindow w:windows.values())if(w.body instanceof TenNotaPanel)((TenNotaPanel)w.body).foreground(w.getVisibility()==View.VISIBLE);super.onResume();taskProgressActive=true;nextDownloadPoll=0;main.removeCallbacks(taskProgressTick);if(!taskProgressSurfaces.isEmpty())main.post(taskProgressTick);if(wallpaper!=null)wallpaper.resumeSlides();if(brightness!=null)brightness.apply();if(restoringBackup)return;refreshUsageAccess();boolean access=StorageAccess.has(this);if(storageAllowed!=access){storageAllowed=access;filesChanged();}pausedSession=null;if(startMenu!=null)startMenu.refreshHome();main.removeCallbacks(clockTick);main.post(clockTick);if(store!=null)scanApps();if(services!=null)services.setActive(true);if(startup!=null)startup.resume();if(tray!=null)tray.start();if(weatherLocation!=null)weatherLocation.resume();for(DesktopWindow tool:windows.values()){if(tool.body instanceof TaskManagerPanel)((TaskManagerPanel)tool.body).active(true);if(tool.body instanceof GadgetPanel)((GadgetPanel)tool.body).active(true);if(tool.body instanceof BrowserPanel)((BrowserPanel)tool.body).active(true);if(tool.body instanceof PhotoGallery)((PhotoGallery)tool.body).active(true);}}
    @Override protected void onStop(){
        if(playback!=null)playback.player.pause();
        super.onStop();
    }
    @Override protected void onPause(){if(tenNews!=null)tenNews.dismiss();
        if(tenTasks!=null)tenTasks.dismiss();if(tenActions!=null)tenActions.dismiss();if(tenNews!=null)tenNews.dismiss();for(DesktopWindow w:windows.values())if(w.body instanceof TenNotaPanel)((TenNotaPanel)w.body).foreground(false);
        if(usageSettingsRequest!=0)usageSettingsLeft=true;
        if(taskbarReorder!=null)taskbarReorder.cancel();taskProgressActive=false;main.removeCallbacks(taskProgressTick);if(jumpMenu!=null)jumpMenu.dismiss();if(wallpaper!=null)wallpaper.pauseSlides();if(workspaceDialog!=null)workspaceDialog.dismiss();DesktopSounds.stop();if(previews!=null)previews.dismiss();for(DesktopWindow tool:windows.values()){if(tool.body instanceof PhotoGallery)((PhotoGallery)tool.body).active(false);if(tool.body instanceof BrowserPanel){((BrowserPanel)tool.body).save();((BrowserPanel)tool.body).active(false);}if(tool.body instanceof TaskManagerPanel)((TaskManagerPanel)tool.body).active(false);tool.dismissSystemMenu();tool.cancelCaptionDrag();tool.cancelResize();if(tool.body instanceof GadgetPanel)((GadgetPanel)tool.body).active(false);if(tool.body instanceof VistaExplorer)((VistaExplorer)tool.body).dismissMenus();}if(tray!=null)tray.stop();if(startup!=null)startup.pause();pausedSession=restoringBackup?null:captureSession();if(desktopMenu!=null)desktopMenu.dismiss();super.onPause();main.removeCallbacks(clockTick);if(services!=null)services.setActive(false);if(weatherLocation!=null)weatherLocation.pause();if(startMenu!=null)startMenu.hide();}
    @Override public void onConfigurationChanged(android.content.res.Configuration config){
        super.onConfigurationChanged(config);if(tenTasks!=null)tenTasks.dismiss();if(tenActions!=null)tenActions.dismiss();if(tenNews!=null)tenNews.dismiss();if(previews!=null)previews.dismiss();String next=config.orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE?"landscape":"portrait";
        if(!next.equals(layoutKey)){arrangementUndo.clear();for(DesktopWindow tool:windows.values())if(tool.getParent()!=null){tool.dismissSystemMenu();tool.settle();tool.orientation(next);}layoutKey=next;desktop.orientation(next);}
        rebuildTenTaskbar();if(startup!=null){startup.pause();startup.resume();}if(desktopMenu!=null)desktopMenu.dismiss();if(tray!=null)tray.dismiss();pausedSession=null;applySystemChrome();workspace.post(()->{layoutWorkspace();renderTaskbar();});
    }
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putBoolean("storage_setup_open",storageSetup!=null&&storageSetup.showing());state.putBoolean("home_request_in_flight",homeRequestInFlight);state.putInt("usage_settings_request",usageSettingsRequest);state.putBoolean("usage_settings_left",usageSettingsLeft);state.putString("pending_theme_export",pendingThemeExport);state.putString("folder_window",pendingFolderWindow);if(!restoringBackup)state.putString("desktop_session",(pausedSession==null?captureSession():pausedSession).toString());}
    private JSONObject captureSession(){return captureSession(true);}
    private JSONObject captureCurrentSession(){return captureSession(false);}
    private JSONObject captureSession(boolean allDesktops){
        JSONObject session=new JSONObject();JSONArray tools=new JSONArray();
        if(workspace==null)return session;
        if(allDesktops)GadgetLayout.put(session,"desktops",tenDesktops.metadata());
        for(DesktopWindow tool:allDesktops?tenDesktops.all():orderedWindows()){tool.saveGeometry();JSONObject item=new JSONObject();GadgetLayout.put(item,"id",tool.toolId);GadgetLayout.put(item,"desktop",tenDesktops.owner(tool));GadgetLayout.put(item,"visible",tenDesktops.visibility(tool));GadgetLayout.put(item,"geometry",tool.geometry());if(tool.body instanceof TenSettings)GadgetLayout.put(item,"ten_page",((TenSettings)tool.body).page);GadgetLayout.put(item,"ui",ToolViewState.capture(tool));tools.put(item);
        }
        GadgetLayout.put(session,"desktopHidden",new JSONArray(desktopHidden.keySet()));GadgetLayout.put(session,"tools",tools);GadgetLayout.put(session,"start",startMenu.state());GadgetLayout.put(session,"desktopScroll",desktopScroll.getScrollY());return session;
    }
    private void restoreSession(String saved){
        if(destroyed||saved==null)return;
        try{
            JSONObject session=new JSONObject(saved);JSONObject desktopMetadata=session.optJSONObject("desktops");if(desktopMetadata!=null&&tenDesktops.all().isEmpty())tenDesktops.load(desktopMetadata);String activeDesktop=desktopMetadata==null?tenDesktops.current():desktopMetadata.optString("current",tenDesktops.current());JSONArray tools=session.optJSONArray("tools");
            if(tools!=null)for(int i=0;i<tools.length();i++){
                JSONObject item=tools.optJSONObject(i);if(item==null)continue;String targetDesktop=item.optString("desktop",tenDesktops.current());if(!tenDesktops.desks.containsKey(targetDesktop))continue;tenDesktops.select(targetDesktop);String id=item.optString("id");
                if(!id.startsWith("@organizer:")&&!id.startsWith("folder:")&&!validExplorerId(id)&&!FolderLinks.id(id)&&!(id.startsWith("@gadget:")&&GadgetCatalog.extra(id.substring(8)))&&!TenSystemTools.tool(id)&&!Arrays.asList("@legacycontrol","@news","@nota","@settings","@notes","@sticky","@calendar","@gadgets","@clock","@weather","@appearance","@computer","@control","@system","@files","@documents","@pictures","@music","@filemanagers","@organize","@quicklaunch","@backup","@welcome","@browser","@photos","@taskmanager","@recycle","@recent","@actioncenter",MediaLibrary.PLAYER).contains(id)&&!store.isFolder(id))continue;
                if(id.startsWith("@organizer:")||(id.startsWith("folder:")&&!store.isFolder(id)))organizer(id,"desktop");else open(id);DesktopWindow tool=windows.get(id);if(tool==null)continue;if(tool.body instanceof TenSettings)((TenSettings)tool.body).navigate(item.optString("ten_page","home"));tool.readGeometry(item.optJSONObject("geometry"));tool.setVisibility(item.optBoolean("visible",true)?View.VISIBLE:View.GONE);ToolViewState.restore(tool,item.optJSONObject("ui"));
            }
            tenDesktops.select(activeDesktop);
            JSONArray hidden=session.optJSONArray("desktopHidden");if(hidden!=null)for(int i=0;i<hidden.length();i++){String id=hidden.optString(i);DesktopWindow tool=windows.get(id);if(tool!=null&&tool.getVisibility()!=View.VISIBLE)desktopHidden.put(id,tool);}
            desktopScroll.post(()->desktopScroll.scrollTo(0,session.optInt("desktopScroll")));startMenu.restore(session.optJSONObject("start"));renderTaskbar();
        }catch(JSONException ignored){}
    }
    @Override protected void onDestroy(){destroyed=true;if(store!=null)store.prefs.unregisterOnSharedPreferenceChangeListener(tenShellPrefs);if(tenNews!=null)tenNews.dismiss();if(taskbarReorder!=null)taskbarReorder.close();if(usageGuide!=null){usageGuide.dismiss();usageGuide=null;}if(playback!=null)playback.release();if(wallpaper!=null)wallpaper.close();if(previews!=null)previews.dismiss();DesktopSounds.stop();if(storageSetup!=null)storageSetup.close();if(weatherLocation!=null)weatherLocation.close();if(tray!=null)tray.stop();if(startup!=null)startup.close();main.removeCallbacksAndMessages(null);worker.shutdownNow();for(DesktopWindow tool:tenDesktops.all()){if(tool.body instanceof VistaShell)((VistaShell)tool.body).close();if(tool.body instanceof WeatherPanel)((WeatherPanel)tool.body).close();if(tool.body instanceof BrowserPanel)((BrowserPanel)tool.body).close();if(tool.body instanceof PhotoGallery)((PhotoGallery)tool.body).close();if(tool.body instanceof TaskManagerPanel)((TaskManagerPanel)tool.body).close();if(tool.body instanceof RecyclePanel)((RecyclePanel)tool.body).close();}if(services!=null)services.close();if(weatherLocation!=null)weatherLocation.pause();if(receiverRegistered){unregisterReceiver(packages);unregisterReceiver(timeChanged);}if(Build.VERSION.SDK_INT>=33&&backCallback!=null)getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backCallback);super.onDestroy();}

    void enterBrowserFullscreen(View video){enterFullscreen(video,"Fullscreen video");}
    private void enterFullscreen(View video,String description){
        startMenu.hide();if(tray!=null)tray.dismiss();if(desktopMenu!=null)desktopMenu.dismiss();((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(),0);
        if(video.getParent() instanceof ViewGroup)((ViewGroup)video.getParent()).removeView(video);videoOverlay=new FrameLayout(this);videoOverlay.setBackgroundColor(Color.BLACK);videoOverlay.setContentDescription(description);videoOverlay.setElevation(dp(64));videoOverlay.setFocusableInTouchMode(true);videoOverlay.setClickable(true);
        videoOverlay.setOnApplyWindowInsetsListener((v,insets)->{int l=0,t=0,r=0,b=0;if(Build.VERSION.SDK_INT>=28&&insets.getDisplayCutout()!=null){android.view.DisplayCutout cutout=insets.getDisplayCutout();l=cutout.getSafeInsetLeft();t=cutout.getSafeInsetTop();r=cutout.getSafeInsetRight();b=cutout.getSafeInsetBottom();}v.setPadding(l,t,r,b);return insets;});
        videoOverlay.addView(video,new FrameLayout.LayoutParams(-1,-1));previousShellAccessibility=shell.getImportantForAccessibility();shell.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);root.addView(videoOverlay,new FrameLayout.LayoutParams(-1,-1));applySystemChrome();videoOverlay.requestApplyInsets();video.requestFocus();
    }
    void exitBrowserFullscreen(View video){if(video.getParent() instanceof ViewGroup)((ViewGroup)video.getParent()).removeView(video);if(videoOverlay!=null){root.removeView(videoOverlay);videoOverlay=null;shell.setImportantForAccessibility(previousShellAccessibility);}applySystemChrome();}
    private void applySystemChrome(){
        boolean full=videoOverlay!=null,show=!full&&store.prefs.getBoolean("show_status_bar",false);
        if(Build.VERSION.SDK_INT>=30){WindowInsetsController controller=getWindow().getInsetsController();if(controller!=null){controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);if(full)controller.hide(WindowInsets.Type.systemBars());else{controller.show(WindowInsets.Type.navigationBars());if(show)controller.show(WindowInsets.Type.statusBars());else controller.hide(WindowInsets.Type.statusBars());}}}
        else{int flags=View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;if(!show)flags|=View.SYSTEM_UI_FLAG_FULLSCREEN;if(full)flags|=View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;getWindow().getDecorView().setSystemUiVisibility(flags);}
        if(shell!=null)shell.requestApplyInsets();
    }
        /** Kept for inherited test-driver source compatibility; Ten has no gadget surface. */
    @Deprecated public void sidebarChanged(){refreshTen();}
    public void readingsChanged(){updateWeatherSummary();for(DesktopWindow tool:windows.values())if(tool.body instanceof GadgetPanel&&tool.getParent()!=null)((GadgetPanel)tool.body).readings();}
    public void gadgetAction(String id){
        startMenu.hide();
        switch(id){
            case "sidebar-properties":open("@news");break;
            case "clock":clockOptions();break;case "calendar":calendar();break;case "weather":weatherSettings();break;
            default:if(GadgetCatalog.extra(id))gadgetPanel(id);break;
            case "notes":stickyNote();break;case "photo":choosePhotos();break;
            case "meters":Aero.dialog(this).setTitle("System Meter").setMessage("The left dial shows the device's battery level. The right dial shows the percentage of physical memory currently in use. These are live Android readings.").setPositiveButton("OK",null).show();break;
        }
    }
    private boolean restoreTool(String id){DesktopWindow current=windows.get(id);if(current!=null&&current.getParent()!=null){current.restore();return true;}return false;}
    private void gadgetGallery(){open("@news");}

    private void gadgetPanel(String kind){if(!GadgetCatalog.extra(kind))return;String id="@gadget:"+kind;if(restoreTool(id))return;if(kind.equals("calculator")){window(id,"Calculator",new TenCalculator(this,store));return;}window(id,GadgetCatalog.title(kind),new GadgetPanel(this,store,kind,services.feeds,services.hardware,new GadgetPanel.Host(){public void open(String id){MainActivity.this.open(id);}public void action(String id){vistaAction(id);}public void changed(){services.refresh();}public void feed(String url){services.feeds.configure(url);}public void refreshFeed(){services.feeds.refresh(true);}public void article(String url){Uri uri=Uri.parse(url);if(Arrays.asList("http","https").contains(uri.getScheme()))launchSystem(new Intent(Intent.ACTION_VIEW,uri));}}));}
    private void clockOptions(){controlPanel("@clock","Clock settings","clock");}
    private void stickyNote(){
        if(restoreTool("@sticky"))return;LinearLayout body=Aero.column(this);EditText editor=new EditText(this);editor.setTypeface(Aero.face(this));editor.setTextSize(16);editor.setTextColor(0xff5c4a1f);editor.setGravity(Gravity.TOP|Gravity.START);editor.setBackgroundColor(0xffffe889);Aero.pad(editor,14);editor.setHint("A little note…");TenAppearance.preserve(editor);editor.setContentDescription("Sticky note text");editor.setText(store.prefs.getString("gadget_note",""));editor.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){store.prefs.edit().putString("gadget_note",s.toString()).apply();services.refresh();}public void afterTextChanged(Editable e){}});body.addView(editor,new LinearLayout.LayoutParams(-1,-1));window("@sticky","Notes",body);
    }
    private void choosePhotos(){
        startMenu.hide();Intent choose=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("image/*").putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);try{startActivityForResult(choose,13);}catch(ActivityNotFoundException e){toast("No photo picker is available on this device.");}
    }
    private void rememberPhoto(Uri uri,List<String> selected){try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);if(!selected.contains(uri.toString()))selected.add(uri.toString());}catch(SecurityException ignored){}}
    private void weatherSettings(){
        if(restoreTool("@weather"))return;
        WeatherPanel panel=new WeatherPanel(this,store,services.weather,weatherLocation,new WeatherPanel.Host(){public void locate(){locateWeather();}public void settings(){launchSystem(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));}public void follow(boolean enabled){store.prefs.edit().putBoolean("weather_follow",enabled).apply();if(enabled)locateWeather();else weatherLocation.stopFollowing();updateWeatherSummary();}public void refresh(){services.weather.refresh(true);services.refresh();updateWeatherSummary();}});window("@weather","Weather settings",panel);
    }
    private void updateWeatherSummary(){DesktopWindow current=windows.get("@weather");if(current!=null&&current.body instanceof WeatherPanel)((WeatherPanel)current.body).refresh();}
    private void locateWeather(){if(weatherLocation==null)return;if(!weatherLocation.permitted()){requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},42);return;}weatherLocation.locate();}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){super.onRequestPermissionsResult(request,permissions,grants);DesktopWindow browser=windows.get("@browser");if(browser!=null&&browser.getParent()!=null&&browser.body instanceof BrowserPanel&&((BrowserPanel)browser.body).permissionResult(request))return;if(request==StorageAccess.REQUEST){boolean access=StorageAccess.has(this);storageAllowed=access;filesChanged();if(!access)toast("File access was not enabled. You can still connect individual folders.");return;}if(request==42){if(grants.length>0&&grants[0]==android.content.pm.PackageManager.PERMISSION_GRANTED)weatherLocation.locate();else weatherLocation.denied();}}
}
