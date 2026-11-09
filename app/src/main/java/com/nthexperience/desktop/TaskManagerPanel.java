package com.nthexperience.desktop;

import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.*;
import android.net.TrafficStats;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.util.*;

/** Windows 10 compact/detailed monitor. Every plotted value is a measured sample. */
final class TaskManagerPanel extends LinearLayout {
    static final class Task {final String id,title,status;final boolean external;Task(String id,String title,String status,boolean external){this.id=id;this.title=title;this.status=status;this.external=external;}}
    interface Host {List<Task> tasks();View icon(String id);void switchTo(String id);void end(String id);void appInfo(String id);void newTask();void close();default void restart(){newTask();}default void open(String id){newTask();}}
    private final Host host;private final LinearLayout content,tabs;private final TextView status,selection,switchTo,end,detailsToggle;private final LinearLayout menuBar;private final HorizontalScrollView tabScroll;
    private List<Task> tasks=new ArrayList<>();private String selected="",tab="Processes",sort="Name";private boolean active=true,paused,descending,detailed;private int metric;private final TextView[] metricLabels=new TextView[5];private TextView mainMetric;
    private int interval=2000,samples;private long lastWall,lastCpu,lastRx=-1,lastTx=-1;private float cpu=Float.NaN,memory,heap,rx=Float.NaN,tx=Float.NaN;
    private ActivityManager.MemoryInfo memoryInfo=new ActivityManager.MemoryInfo();private DesktopContextMenu menu;
    private final float[][] history=new float[5][60];private final List<HistoryGraph> graphs=new ArrayList<>();
    private TextView cpuText,memoryText,heapText,detail,rxText,txText;
    private final Runnable tick=new Runnable(){public void run(){if(!sampling())return;sample();if(tab.equals("Processes"))refreshTasks();else if(tab.equals("Details"))renderProcesses();else if(tab.equals("App history")){content.removeAllViews();renderHistory();}postDelayed(this,interval);}};
    TaskManagerPanel(Context c,Host host){
        super(c);this.host=host;setOrientation(VERTICAL);setContentDescription("Task Manager content");for(float[] series:history)Arrays.fill(series,Float.NaN);
        detailed=c.getSharedPreferences("desktop",0).getBoolean("ten_taskmanager_details",false);setBackgroundColor(Color.WHITE);
        LinearLayout bar=new LinearLayout(c);menuBar=bar;bar.setBackgroundColor(0xfff3f4f5);
        for(String label:new String[]{"File","Options","View","Help"}){TextView link=text(label,12);link.setGravity(Gravity.CENTER);link.setFocusable(true);link.setBackground(Aero.selection(c));link.setOnClickListener(v->menu(link,label));bar.addView(link,new LayoutParams(0,dp(32),1));}addView(bar);
        tabScroll=new HorizontalScrollView(c);tabScroll.setHorizontalScrollBarEnabled(false);tabs=new LinearLayout(c);tabs.setPadding(dp(5),dp(5),dp(5),0);tabScroll.addView(tabs);addView(tabScroll,new LayoutParams(-1,dp(42)));
        content=Aero.column(c);Aero.pad(content,7);addView(content,new LayoutParams(-1,0,1));
        selection=text("Select a task",10);addView(selection,new LayoutParams(-1,dp(27)));
        LinearLayout buttons=new LinearLayout(c);buttons.setPadding(dp(5),0,dp(5),dp(5));
        detailsToggle=Aero.button(c,"More details",()->{detailed=!detailed;getContext().getSharedPreferences("desktop",0).edit().putBoolean("ten_taskmanager_details",detailed).apply();if(!detailed)tab="Processes";render();});
        switchTo=Aero.button(c,"Switch to",this::switchSelected);end=Aero.button(c,"End task",this::endSelected);
        for(TextView button:new TextView[]{detailsToggle,switchTo,end}){button.setTextSize(12);button.setMinWidth(0);LayoutParams size=new LayoutParams(0,dp(40),1);size.setMargins(dp(2),0,dp(2),0);buttons.addView(button,size);}addView(buttons);
        status=text("",10);status.setBackground(Aero.panel(c,0xfff5f6f7,0xffe4e8ed,0,0xffb7c1ca));addView(status,new LayoutParams(-1,dp(29)));render();
    }
    private void menu(View anchor,String label){dismissMenu();menu=new DesktopContextMenu(getContext(),"Task Manager "+label+" menu");
        if(label.equals("File")){menu.item("Run new task","programs",host::newTask);menu.item("Restart Nth Desktop","refresh",host::restart);menu.separator();menu.item("Exit Task Manager","close",host::close);}
        else if(label.equals("View")){menu.item("Refresh Now","refresh",this::refresh);for(int speed:new int[]{1000,2000,5000})menu.checked(speed==1000?"Update speed: High":speed==2000?"Update speed: Normal":"Update speed: Low",interval==speed&&!paused,()->{interval=speed;paused=false;sync();});menu.checked("Pause updates",paused,()->{paused=!paused;sync();});}
        else if(label.equals("Options")){menu.checked("Animated graphs",getContext().getSharedPreferences("desktop",0).getBoolean("taskmanager_animate",true),()->{android.content.SharedPreferences p=getContext().getSharedPreferences("desktop",0);p.edit().putBoolean("taskmanager_animate",!p.getBoolean("taskmanager_animate",true)).apply();stopAnimations();});}
        else menu.item("About this Task Manager","taskmanager",()->Aero.dialog(getContext()).setTitle("Nth Task Manager").setMessage("Processes manages real Nth app sessions. Details lists only processes owned by Nth. Performance shows Nth CPU, device RAM and Nth Java heap. Network performance measures device-wide interface counters; it does not inspect connections or browsing content. Unsupported counters are marked unavailable. Animation interpolates real samples, follows reduced motion, and stops when hidden or in the background.").setPositiveButton("OK",null).show());
        ContextMenus.centered(anchor,(x,y)->ContextMenus.show(menu,anchor,x,y));
    }
    private void render(){stopAnimations();graphs.clear();cpuText=memoryText=heapText=detail=rxText=txText=null;tabs.removeAllViews();for(String name:new String[]{"Processes","Performance","App history","Startup","Users","Details","Services"}){TextView button=text(name,12);button.setGravity(Gravity.CENTER);button.setFocusable(true);button.setBackground(Aero.panel(getContext(),name.equals(tab)?0xffffffff:0xfff1f1f1,name.equals(tab)?0xffffffff:0xfff1f1f1,0,0xffb8b8b8));button.setContentDescription("Task Manager "+name);button.setOnClickListener(v->{detailed=true;tab=name;render();});tabs.addView(button,new LayoutParams(dp(name.equals("Processes")?106:98),dp(36)));}
        menuBar.setVisibility(detailed?VISIBLE:GONE);tabScroll.setVisibility(detailed?VISIBLE:GONE);status.setVisibility(detailed?VISIBLE:GONE);selection.setVisibility(detailed?VISIBLE:GONE);
        detailsToggle.setText(detailed?"Fewer details":"More details");detailsToggle.setContentDescription(detailed?"Fewer details":"More details");
        Arrays.fill(metricLabels,null);mainMetric=null;
        content.removeAllViews();if(tab.equals("Processes"))renderTasks();else if(tab.equals("Details"))renderProcesses();else if(tab.equals("App history"))renderHistory();else if(Arrays.asList("Startup","Users","Services").contains(tab))renderSystemTab();else renderGraphs();updateSelection();updateLabels();
    }
    private void renderHistory(){LinearLayout panel=Aero.column(getContext());panel.addView(text("Resource usage during this device session",17));panel.addView(text("Nth Desktop Ten",15));panel.addView(text("CPU time: "+android.os.Process.getElapsedCpuTime()+" ms",13));panel.addView(text("Samples observed: "+samples,13));panel.addView(text("Network counters are device-wide. Per-app traffic for other Android apps is unavailable.",12));content.addView(panel,new LayoutParams(-1,-1));}
    private void renderTasks(){tasks=new ArrayList<>(host.tasks());if(tasks.stream().noneMatch(t->t.id.equals(selected)))selected="";tasks.sort((a,b)->{int compare=(sort.equals("Status")?a.status:a.title).compareToIgnoreCase(sort.equals("Status")?b.status:b.title);return descending?-compare:compare;});
        if(detailed){TextView group=text("Apps ("+tasks.size()+")",16);group.setTextColor(0xff246d9f);content.addView(group);}
        LinearLayout header=new LinearLayout(getContext());header.setBackgroundColor(0xffedf1f5);for(String name:new String[]{"Name","Status"}){TextView label=text(name+(sort.equals(name)?descending?" ▾":" ▴":""),12);label.setContentDescription("Sort tasks by "+name);label.setOnClickListener(v->{descending=sort.equals(name)&&!descending;sort=name;content.removeAllViews();renderTasks();});header.addView(label,name.equals("Name")?new LayoutParams(0,dp(28),1):new LayoutParams(dp(101),dp(28)));}if(detailed)content.addView(header);
        ScrollView scroll=new ScrollView(getContext());LinearLayout rows=Aero.column(getContext());rows.setBackgroundColor(0xffffffff);scroll.addView(rows);content.addView(scroll,new LayoutParams(-1,0,1));
        for(Task task:tasks){LinearLayout row=new LinearLayout(getContext());row.setGravity(Gravity.CENTER_VERTICAL);row.setFocusable(true);row.setContentDescription("Task: "+task.title);row.setBackground(task.id.equals(selected)?Aero.panel(getContext(),0xffddecfa,0xffc6e1f5,1,0xff7fabca):Aero.selection(getContext()));row.setSelected(task.id.equals(selected));row.addView(host.icon(task.id),new LayoutParams(dp(22),dp(22)));TextView title=text(task.title,12);title.setSingleLine();title.setEllipsize(android.text.TextUtils.TruncateAt.END);row.addView(title,new LayoutParams(0,-1,1));TextView state=text(task.status,10);state.setMaxLines(2);row.addView(state,new LayoutParams(dp(101),-1));row.setOnClickListener(v->{selected=task.id;render();});ContextMenus.bind(row,(x,y)->{selected=task.id;updateSelection();dismissMenu();menu=new DesktopContextMenu(getContext(),"Task commands");menu.item("Switch to","programs",this::switchSelected);menu.item(task.external?"App Info…":"End task",task.external?"control":"close",this::endSelected);ContextMenus.show(menu,row,x,y);});if(!detailed)state.setVisibility(GONE);else state.setBackgroundColor(0xfffff8d9);rows.addView(row,new LayoutParams(-1,dp(42)));}
        if(tasks.isEmpty())rows.addView(text("No other Nth windows are open.",12),new LayoutParams(-1,dp(54)));TextView hint=text("Android apps are recently opened entries. Use App Info to manage them.",10);hint.setTypeface(Aero.face(getContext()),Typeface.ITALIC);if(detailed)content.addView(hint,new LayoutParams(-1,-2));
    }
    private void renderProcesses(){if(!tab.equals("Details"))return;content.removeAllViews();LinearLayout body=Aero.column(getContext());body.addView(text("Processes owned by Nth Desktop",14));body.addView(text("Android keeps other apps' process details private. Restart Desktop refreshes Nth while keeping saved notes and its window session.",11));
        ActivityManager manager=getContext().getSystemService(ActivityManager.class);List<ActivityManager.RunningAppProcessInfo> running=manager==null?null:manager.getRunningAppProcesses();int count=0;
        if(running!=null)for(ActivityManager.RunningAppProcessInfo process:running){if(process.uid!=android.os.Process.myUid())continue;count++;long bytes=-1;try{Debug.MemoryInfo[] values=manager.getProcessMemoryInfo(new int[]{process.pid});if(values.length>0)bytes=values[0].getTotalPss()*1024L;}catch(RuntimeException ignored){}body.addView(text(process.processName,13));body.addView(text("PID "+process.pid+"  •  "+(bytes>=0?mb(bytes)+" PSS":"Memory unavailable")+(process.pid==android.os.Process.myPid()?"\nCPU: "+percent(cpu)+" (one core)  •  Java threads: "+Thread.getAllStackTraces().size():""),11));}
        if(count==0)body.addView(text("Process details are unavailable on this device.",12));ScrollView scroll=new ScrollView(getContext());scroll.addView(body);content.addView(scroll,new LayoutParams(-1,-1));updateSelection();
    }
    private void renderSystemTab(){
        LinearLayout body=Aero.column(getContext());ScrollView scroll=new ScrollView(getContext());scroll.addView(body);content.addView(scroll,new LayoutParams(-1,-1));
        android.content.SharedPreferences prefs=getContext().getSharedPreferences("desktop",0);
        if(tab.equals("Startup")){
            body.addView(text("Nth Desktop startup",18));
            CheckBox sound=new CheckBox(getContext());sound.setText("Play startup sound");sound.setTextColor(Color.BLACK);sound.setChecked(prefs.getBoolean("startup_sound",true));sound.setOnCheckedChangeListener((v,on)->prefs.edit().putBoolean("startup_sound",on).apply());body.addView(sound);
            body.addView(text("Android manages startup for other apps. Startup impact is unavailable.",12));
        }else if(tab.equals("Users")){
            body.addView(text(prefs.getString("desktop_name","Nth Experience"),18));body.addView(text("Current Android user • Active",13));body.addView(text("Other Android users and their processes are private.",12));
        }else{
            body.addView(text("Services",18));body.addView(text("Android manages system services. Nth Desktop cannot enumerate or stop services belonging to other apps.",13));
            body.addView(Aero.button(getContext(),"Open Android app settings",()->host.open("setting:android:settings")));
        }
    }
    private static final String[] METRICS={"CPU","Memory","Java heap","Received","Sent"};
    private static final int[] GRAPH_COLORS={0xff3c92b1,0xff995bb0,0xff688f39,0xffb86b27,0xffb86b27};
    private String metricValue(int n){return n==0?percent(cpu):n==1?percent(memory):n==2?percent(heap):rate(n==3?rx:tx);}
    private void renderGraphs(){
        boolean wide=getResources().getConfiguration().screenWidthDp>=600;
        LinearLayout layout=new LinearLayout(getContext());layout.setOrientation(wide?HORIZONTAL:VERTICAL);content.addView(layout,new LayoutParams(-1,-1));
        LinearLayout choices=new LinearLayout(getContext());choices.setOrientation(wide?VERTICAL:HORIZONTAL);
        for(int n=0;n<5;n++){
            final int index=n;LinearLayout choice=Aero.column(getContext());choice.setPadding(dp(5),dp(5),dp(5),dp(5));choice.setBackgroundColor(metric==n?0xffe8f3fa:Color.WHITE);choice.setContentDescription("Performance metric "+METRICS[n]);choice.setFocusable(true);choice.setOnClickListener(v->{metric=index;render();});
            HistoryGraph mini=new HistoryGraph(getContext(),history[n],n<3?100:0,GRAPH_COLORS[n]);graphs.add(mini);choice.addView(mini,new LayoutParams(dp(90),dp(42)));TextView label=text(METRICS[n]+"\n"+metricValue(n),11);metricLabels[n]=label;choice.addView(label);choices.addView(choice,new LayoutParams(dp(112),-2));
        }
        if(wide){ScrollView scroll=new ScrollView(getContext());scroll.addView(choices);layout.addView(scroll,new LayoutParams(dp(116),-1));}
        else{HorizontalScrollView scroll=new HorizontalScrollView(getContext());scroll.addView(choices);layout.addView(scroll,new LayoutParams(-1,dp(105)));scroll.post(()->scroll.smoothScrollTo(metric*dp(112),0));}
        ScrollView scroll=new ScrollView(getContext());LinearLayout body=Aero.column(getContext());scroll.addView(body);layout.addView(scroll,wide?new LayoutParams(0,-1,1):new LayoutParams(-1,0,1));
        mainMetric=text(METRICS[metric],24);body.addView(mainMetric);
        HistoryGraph graph=new HistoryGraph(getContext(),history[metric],metric<3?100:0,GRAPH_COLORS[metric]);graph.setContentDescription("Task Manager detailed "+METRICS[metric]+" graph");graphs.add(graph);body.addView(graph,new LayoutParams(-1,dp(180)));body.addView(text("60 samples   •   "+(metric<3?"0–100%":"Automatically scaled"),11));detail=text("",12);body.addView(detail);
        if(samples==0)sample();updateLabels();
    }
    private TextView text(String value,int size){TextView t=Aero.text(getContext(),value,size,Aero.INK);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(6),dp(3),dp(4),dp(3));return t;}
    private Task selected(){for(Task task:tasks)if(task.id.equals(selected))return task;return null;}
    private void updateSelection(){Task task=selected();boolean enabled=tab.equals("Processes")&&task!=null;switchTo.setVisibility(detailed?VISIBLE:GONE);switchTo.setEnabled(enabled);end.setEnabled(enabled||tab.equals("Details"));switchTo.setAlpha(enabled?1:.4f);end.setAlpha(end.isEnabled()?1:.4f);end.setText(tab.equals("Details")?"Restart Desktop":task!=null&&task.external?"App Info…":"End task");selection.setText(tab.equals("Performance")?"CPU measures Nth Desktop; memory covers the device.":tab.equals("Networking")?"Device-wide network counters; no connection inspection.":tab.equals("Details")?"Only Nth-owned processes are listed.":task==null?"Select a task":task.title);}
    private void switchSelected(){Task task=selected();if(task!=null&&tab.equals("Processes"))host.switchTo(task.id);}
    private void endSelected(){if(tab.equals("Details")){host.restart();return;}Task task=selected();if(task==null)return;if(task.external)host.appInfo(task.id);else host.end(task.id);refresh();}
    void tasksChanged(){if(tab.equals("Processes"))refreshTasks();}
    private void refreshTasks(){List<Task> next=host.tasks();boolean same=next.size()==tasks.size();if(same)for(Task a:next){Task b=tasks.stream().filter(t->t.id.equals(a.id)).findFirst().orElse(null);if(b==null||!a.title.equals(b.title)||!a.status.equals(b.status)){same=false;break;}}if(!same){content.removeAllViews();renderTasks();updateSelection();}}
    void refresh(){if(tab.equals("Processes"))refreshTasks();else{sample();if(tab.equals("Details"))renderProcesses();}}
    private void sample(){long now=SystemClock.elapsedRealtime(),elapsed=lastWall==0?0:now-lastWall,currentCpu=android.os.Process.getElapsedCpuTime(),currentRx=TrafficStats.getTotalRxBytes(),currentTx=TrafficStats.getTotalTxBytes();cpu=elapsed>0?Math.max(0,100f*(currentCpu-lastCpu)/elapsed):Float.NaN;rx=elapsed>0&&lastRx>=0&&currentRx>=lastRx?(currentRx-lastRx)*1000f/elapsed:Float.NaN;tx=elapsed>0&&lastTx>=0&&currentTx>=lastTx?(currentTx-lastTx)*1000f/elapsed:Float.NaN;lastWall=now;lastCpu=currentCpu;lastRx=currentRx;lastTx=currentTx;
        ActivityManager manager=getContext().getSystemService(ActivityManager.class);if(manager!=null)manager.getMemoryInfo(memoryInfo);memory=memoryInfo.totalMem>0?100f*(memoryInfo.totalMem-memoryInfo.availMem)/memoryInfo.totalMem:Float.NaN;Runtime runtime=Runtime.getRuntime();heap=100f*(runtime.totalMemory()-runtime.freeMemory())/runtime.maxMemory();float[] values={Float.isNaN(cpu)?cpu:Math.min(100,cpu),memory,heap,rx,tx};for(int i=0;i<history.length;i++){System.arraycopy(history[i],1,history[i],0,59);history[i][59]=values[i];}samples++;updateLabels();for(HistoryGraph graph:graphs)graph.changed(sampling()&&!paused&&Aero.motion(getContext())&&getContext().getSharedPreferences("desktop",0).getBoolean("taskmanager_animate",true));
    }
    private void updateLabels(){
        for(int n=0;n<5;n++)if(metricLabels[n]!=null)metricLabels[n].setText(METRICS[n]+"\n"+metricValue(n));
        if(mainMetric!=null)mainMetric.setText(METRICS[metric]+"  "+metricValue(metric));
        if(detail!=null){Runtime runtime=Runtime.getRuntime();detail.setText(metric==0?"Nth Desktop CPU, measured against one logical core.\nUptime: "+SystemClock.elapsedRealtime()/3600000+"h "+SystemClock.elapsedRealtime()/60000%60+"m":metric==1?"Device RAM\nIn use: "+mb(memoryInfo.totalMem-memoryInfo.availMem)+"\nAvailable: "+mb(memoryInfo.availMem)+"\nTotal: "+mb(memoryInfo.totalMem):metric==2?"Nth Java heap\nUsed: "+mb(runtime.totalMemory()-runtime.freeMemory())+"\nLimit: "+mb(runtime.maxMemory()):"Device-wide network interface counters.\nOther apps' individual connections are private. VPN traffic may be counted more than once.");}
        if(status!=null)status.setText((paused?"Updates paused":interval==1000?"Live • 1 second":interval==5000?"Live • 5 seconds":"Live • 2 seconds")+"  •  Nth CPU "+percent(cpu)+"  •  RAM "+percent(memory));
    }
    private String percent(float n){return Float.isNaN(n)?"sampling…":String.format(Locale.getDefault(),"%.1f%%",n);}
    private String rate(float value){return Float.isNaN(value)?"unavailable / awaiting interval":mb((long)value)+"/s";}
    private String mb(long bytes){return android.text.format.Formatter.formatShortFileSize(getContext(),Math.max(0,bytes));}
    private boolean sampling(){return active&&!paused&&isAttachedToWindow()&&isShown()&&getWindowVisibility()==VISIBLE;}
    private void sync(){removeCallbacks(tick);lastWall=0;lastCpu=0;lastRx=lastTx=-1;if(!sampling())stopAnimations();else post(tick);updateLabels();}
    private void stopAnimations(){for(HistoryGraph graph:graphs)graph.stop();}
    void active(boolean value){active=value;if(!value)dismissMenu();sync();}
    int sampleCount(){return samples;}int animatedGraphCount(){int n=0;for(HistoryGraph graph:graphs)if(graph.animating())n++;return n;}
    JSONObject state(){JSONObject value=new JSONObject();GadgetLayout.put(value,"version",2);GadgetLayout.put(value,"tab",tab);GadgetLayout.put(value,"detailed",detailed);GadgetLayout.put(value,"metric",metric);GadgetLayout.put(value,"selected",selected);GadgetLayout.put(value,"paused",paused);GadgetLayout.put(value,"interval",interval);GadgetLayout.put(value,"sort",sort);GadgetLayout.put(value,"descending",descending);return value;}
    void restore(JSONObject value){
        if(value==null)return;
        boolean legacy=value.optInt("version",1)<2;String name=value.optString("tab");
        metric=Math.max(0,Math.min(4,value.optInt("metric")));
        if(legacy){if(name.equals("Applications"))name="Processes";else if(name.equals("Processes"))name="Details";else if(name.equals("Networking")){name="Performance";metric=3;}}
        tab=Arrays.asList("Processes","Details","Performance","App history","Startup","Users","Services").contains(name)?name:"Processes";
        detailed=value.optBoolean("detailed",legacy||!tab.equals("Processes")||detailed);
        selected=value.optString("selected");paused=value.optBoolean("paused");interval=value.optInt("interval",2000);
        if(interval!=1000&&interval!=2000&&interval!=5000)interval=2000;
        sort=value.optString("sort").equals("Status")?"Status":"Name";descending=value.optBoolean("descending");render();sync();
    }
    void close(){active=false;removeCallbacks(tick);stopAnimations();dismissMenu();}
    private void dismissMenu(){if(menu!=null){menu.dismiss();menu=null;}}
    @Override protected void onConfigurationChanged(android.content.res.Configuration config){super.onConfigurationChanged(config);if(content!=null)render();}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();sync();}
    @Override protected void onVisibilityChanged(View v,int visibility){super.onVisibilityChanged(v,visibility);if(content!=null)sync();}
    @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(content!=null)sync();}
    @Override protected void onDetachedFromWindow(){removeCallbacks(tick);stopAnimations();dismissMenu();super.onDetachedFromWindow();}
    private int dp(int value){return Aero.dp(getContext(),value);}
    private static final class HistoryGraph extends View {
        private final float[] values;private float[] before;private float fraction=1,maximum;private final int color;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private ValueAnimator animator;
        HistoryGraph(Context context,float[] values,float maximum,int color){super(context);this.color=color;this.values=values;this.maximum=maximum;before=values.clone();}
        void changed(boolean animate){stop();if(animate){fraction=0;animator=ValueAnimator.ofFloat(0,1);animator.setDuration(500);animator.addUpdateListener(a->{fraction=(float)a.getAnimatedValue();invalidate();});animator.start();}else{fraction=1;before=values.clone();invalidate();}}
        boolean animating(){return animator!=null&&animator.isRunning();}
        void stop(){if(animator!=null){animator.cancel();animator=null;}fraction=1;}
        @Override protected void onDraw(Canvas canvas){boolean classic=TenThemes.aero(getContext().getSharedPreferences("desktop",0)),dark=TenAppearance.dark(getContext());canvas.drawColor(classic?0xff071a0b:dark?0xff1e1e1e:0xfffbfdff);paint.setColor(classic?0xff174627:dark?0xff334755:(color&0xffffff)|0x30000000);paint.setStrokeWidth(1);for(int x=0;x<getWidth();x+=Math.max(1,getWidth()/12))canvas.drawLine(x,0,x,getHeight(),paint);for(int y=0;y<getHeight();y+=Math.max(1,getHeight()/5))canvas.drawLine(0,y,getWidth(),y,paint);float ceiling=maximum;if(ceiling<=0){ceiling=1024;for(float n:values)if(Float.isFinite(n))ceiling=Math.max(ceiling,n*1.15f);}paint.setColor(classic?0xff57e86c:color);paint.setStrokeWidth(Aero.dp(getContext(),1));for(int i=1;i<60;i++){float a=value(i-1),b=value(i);if(Float.isFinite(a)&&Float.isFinite(b))canvas.drawLine((i-1)*getWidth()/59f,getHeight()*(1-a/ceiling),i*getWidth()/59f,getHeight()*(1-b/ceiling),paint);}if(fraction>=1)before=values.clone();}
        private float value(int i){return Float.isFinite(before[i])&&Float.isFinite(values[i])?before[i]*(1-fraction)+values[i]*fraction:values[i];}
    }
}
