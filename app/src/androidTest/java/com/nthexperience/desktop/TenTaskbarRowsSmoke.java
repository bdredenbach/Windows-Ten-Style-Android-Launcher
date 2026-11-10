package com.nthexperience.desktop;

import android.app.*;import android.content.*;import android.content.pm.ActivityInfo;import android.graphics.*;import android.os.*;import android.view.*;import android.widget.*;import org.json.*;import java.util.*;

/** Real swipe/drag regression for the owner's independently scrolling taskbar rows. */
public final class TenTaskbarRowsSmoke extends TenTaskbarSizeSmoke {
    int checks;
    @Override public void onStart(){Bundle out=new Bundle();try{
        store=new DesktopStore(getTargetContext());store.prefs.edit().clear().commit();
        store.prefs.edit().putInt("startup_mode",2).putBoolean("welcome_on_start",false).putBoolean("reduce_motion",true).putBoolean("taskbar_static",false).putBoolean("ten_taskbar_extended",true).putBoolean("taskbar_show_labels",false).putString("ten_search_mode","Icon").remove(TaskbarRows.KEY).commit();
        ShellTestSetup.complete(getTargetContext());
        getTargetContext().getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true).putBoolean("welcome_seen",true).commit();
        home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(getTargetContext(),MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();settle();
        for(String alignment:new String[]{"dark","light","aero"}){
            progress("independent rows in "+alignment);
            ui(()->{(alignment.equals("aero")?TenThemes.AERO.get(0):TenThemes.TEN.get(alignment.equals("light")?1:0)).apply(store);closeAll();for(String id:ExternalTasks.list())ExternalTasks.remove(id);store.prefs.edit().remove(TaskbarRows.KEY).putString("ten_news_mode","Hidden").putBoolean("taskbar_static",false).putBoolean("taskbar_show_labels",true).putBoolean("ten_taskbar_extended",true).commit();store.quickLaunch(Arrays.asList("@photos","@browser","@recycle","@calendar","@welcome","@clock"));home.applyTenAppearance();for(String id:new String[]{"@notes","@settings","@gadget:calculator","@welcome","@taskmanager"})home.open(id);});settle();
            ui(()->{TenTaskbarItems items=find(TenTaskbarItems.class);items.scrollRowTo(0,0);items.scrollRowTo(1,0);check(items.rowRange(0)>Aero.dp(home,60)&&items.rowRange(1)>Aero.dp(home,60),"Both rows actually overflow");});settle();
            Map<String,String> fixed=new LinkedHashMap<>();for(Map.Entry<String,View> e:fixedViews().entrySet())fixed.put(e.getKey(),bounds(e.getValue()));
            List<String> top=geometryRow(0),bottom=geometryRow(1);swipeRow(0);waitFling();
            check(find(TenTaskbarItems.class).rowScroll(0)>0,"Upper swipe moves upper row");check(geometryRow(1).equals(bottom),"Upper swipe leaves every bottom app stationary");
            top=geometryRow(0);swipeRow(1);waitFling();check(find(TenTaskbarItems.class).rowScroll(1)>0,"Lower swipe moves lower row");check(geometryRow(0).equals(top),"Lower swipe leaves every upper app stationary");
            for(Map.Entry<String,View> e:fixedViews().entrySet())check(fixed.get(e.getKey()).equals(bounds(e.getValue())),"Fixed controls never scroll: "+e.getKey());
            int[] offsets=find(TenTaskbarItems.class).scrollPositions();ui(home::applyTenAppearance);settle();check(Arrays.equals(offsets,find(TenTaskbarItems.class).scrollPositions()),"Both offsets survive an appearance rebuild after measured reservations");
            ui(()->{TenTaskbarItems items=find(TenTaskbarItems.class);items.stopScrolling();items.scrollRowTo(0,0);items.scrollRowTo(1,0);store.prefs.edit().putBoolean("taskbar_show_labels",false).commit();home.refreshTen();});settle();
            dragAcross("@photos",0);checkRow("@photos",0,"Idle pin dragged to top");
            dragAcross("@notes",1);checkRow("@notes",1,"Running app dragged to bottom");
            tapSlot("@photos");ui(()->check(windows().get("@photos")!=null&&windows().get("@photos").getParent()!=null,"Moved pin opens its actual app with native touch"));
            ui(()->windows().get("@notes").minimize());settle();tapSlot("@notes");ui(()->check(windows().get("@notes").isShown(),"Moved running task restores with native touch"));
            String saved=store.prefs.getString(TaskbarRows.KEY,"");
            ui(()->{home.open("@photos");try{java.lang.reflect.Field f=MainActivity.class.getDeclaredField("windows");f.setAccessible(true);Map<?,?> windows=(Map<?,?>)f.get(home);((DesktopWindow)windows.get("@notes")).closeWindow();}catch(Exception e){throw new RuntimeException(e);}});settle();
            checkRow("@photos",0,"Pin retains chosen row after launch");ui(()->home.open("@notes"));settle();checkRow("@notes",1,"Running app retains chosen bottom row after reopen");
            ui(this::toggleMenu);settle();check(!find(TenTaskbarItems.class).expanded,"Collapsed mode remains one strip");ui(this::toggleMenu);settle();checkRow("@photos",0,"Expand restores saved top choice");checkRow("@notes",1,"Expand restores saved bottom choice");
            ui(()->home.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));await(()->root.getWidth()>root.getHeight(),"Landscape rotation completes");settle();checkRow("@photos",0,"Top choice survives rotation");ui(()->home.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));await(()->root.getHeight()>root.getWidth(),"Portrait rotation completes");settle();checkRow("@notes",1,"Bottom choice survives rotation");
            DesktopBackup.Plan plan=DesktopBackup.parse(DesktopBackup.snapshot(store).toString());check(saved.equals(plan.values.get(TaskbarRows.KEY)),"Portable backup retains row choices");
            ui(()->{try{store.prefs.edit().remove(TaskbarRows.KEY).commit();DesktopBackup.apply(home,store,plan,false);home.applyTenAppearance();}catch(Exception e){throw new RuntimeException(e);}});settle();checkRow("@photos",0,"Actual backup restore recovers top choice");checkRow("@notes",1,"Actual backup restore recovers bottom choice");
            ui(()->{store.prefs.edit().putBoolean("taskbar_static",true).putBoolean("taskbar_show_labels",true).commit();home.refreshTen();});settle();
            ui(()->{TenTaskbarItems items=find(TenTaskbarItems.class);for(int i=0;i<items.getChildCount();i++){View child=items.getChildAt(i);int row=TenTaskbarItems.row(child);check(child.getLeft()>=items.rowStart(row)&&child.getRight()<=items.rowEnd(row),"Static fit respects each manually assigned row");}check(items.rowScroll(0)==0&&items.rowScroll(1)==0,"Static mode resets both scroll offsets");});
            shot("ten29-rows-"+alignment.toLowerCase(Locale.ROOT));
        }
        ui(()->home.finish());waitForIdleSync();home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(getTargetContext(),MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));root=home.getWindow().getDecorView();store=new DesktopStore(home);settle();checkRow("@photos",0,"Fresh Activity retains top choice");ui(()->home.open("@notes"));settle();checkRow("@notes",1,"Fresh Activity retains bottom choice");
        ui(()->{View notes=slot("@notes");check(notes.performAccessibilityAction(R.id.taskbar_move_top,null),"Accessible move-to-top action accepted");});settle();checkRow("@notes",0,"Accessibility row move persisted");
        ui(()->{View notes=slot("@notes");check(notes.requestFocusFromTouch(),"Task button receives keyboard focus");check(notes.dispatchKeyEvent(new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN,0,KeyEvent.META_CTRL_ON|KeyEvent.META_SHIFT_ON)),"Keyboard row move accepted");});settle();checkRow("@notes",1,"Keyboard row move persisted");
        edgeCases();
        out.putString("stream","PASS: "+checks+" independent-row assertions; real upper/lower swipes leave all apps in the opposite row stationary, native drag/drop in both directions, default and saved placement through launch/close/reopen, collapse/expand, rotation, portable backup, static fitting, fixed controls and accessible/keyboard row moves.\n");finish(Activity.RESULT_OK,out);
    }catch(Throwable e){out.putString("stream","FAIL: "+android.util.Log.getStackTraceString(e));try{shot("ten29-rows-failure");}catch(Exception ignored){}finish(Activity.RESULT_CANCELED,out);}}
    @Override void check(boolean ok,String message){checks++;super.check(ok,message);}
    List<String> geometryRow(int row){List<String> result=new ArrayList<>();TenTaskbarItems items=find(TenTaskbarItems.class);for(int i=0;i<items.getChildCount();i++){View c=items.getChildAt(i);if(TenTaskbarItems.row(c)==row)result.add(TaskbarReorder.key(c)+":"+bounds(c));}return result;}
    void checkRow(String id,int row,String message)throws Exception{
        long deadline=SystemClock.uptimeMillis()+10000;final boolean[] matches={false};
        while(SystemClock.uptimeMillis()<deadline){ui(()->{View child=slot(id);matches[0]=child!=null&&TenTaskbarItems.row(child)==row&&child.getTop()==row*Aero.dp(home,48);});if(matches[0])break;Thread.sleep(80);}
        ui(()->{View child=slot(id);check(child!=null&&TenTaskbarItems.row(child)==row&&child.getTop()==row*Aero.dp(home,48),message+"; saved="+store.prefs.getString(TaskbarRows.KEY,"{}")+"; actual="+(child==null?"missing":TenTaskbarItems.row(child)+"/"+child.getTop()));});
    }
    void touch(int action,float x,float y,long down){MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);check(getUiAutomation().injectInputEvent(event,false),"Native row input accepted");event.recycle();}
    void timedTouch(int action,float x,float y,long down,long time){MotionEvent event=MotionEvent.obtain(down,time,action,x,y,0);event.setSource(InputDevice.SOURCE_TOUCHSCREEN);check(getUiAutomation().injectInputEvent(event,true),"Timed native row swipe accepted");event.recycle();}
    void swipeRow(int row)throws Exception{TenTaskbarItems items=find(TenTaskbarItems.class);int[] at=new int[2];items.getLocationOnScreen(at);float start=at[0]+items.rowEnd(row)-Aero.dp(home,8),end=at[0]+items.rowStart(row)+Aero.dp(home,8),y=at[1]+Aero.dp(home,row*48+24);long now=SystemClock.uptimeMillis();timedTouch(MotionEvent.ACTION_DOWN,start,y,now,now);for(int i=1;i<=6;i++)timedTouch(MotionEvent.ACTION_MOVE,start+(end-start)*i/6,y,now,now+i*14);timedTouch(MotionEvent.ACTION_UP,end,y,now,now+98);}

    void waitFling()throws Exception{Thread.sleep(700);ui(()->find(TenTaskbarItems.class).stopScrolling());settle();}
    void dragAcross(String id,int row)throws Exception{
        ui(()->{TenTaskbarItems items=find(TenTaskbarItems.class);items.stopScrolling();items.scrollRowTo(0,0);items.scrollRowTo(1,0);});settle();
        View source=slot(id);int[] at=new int[2],origin=new int[2];source.getLocationOnScreen(origin);TenTaskbarItems items=find(TenTaskbarItems.class);items.getLocationOnScreen(at);float x=origin[0]+source.getWidth()/2f,y=origin[1]+source.getHeight()/2f,targetX=at[0]+items.rowStart(row)+Aero.dp(home,16),targetY=at[1]+Aero.dp(home,row*48+24);long down=SystemClock.uptimeMillis();touch(MotionEvent.ACTION_DOWN,x,y,down);Thread.sleep(185);touch(MotionEvent.ACTION_MOVE,x+Aero.dp(home,14),y,down);Thread.sleep(120);
        for(int i=1;i<=5;i++){touch(MotionEvent.ACTION_MOVE,x+(targetX-x)*i/5,y+(targetY-y)*i/5,down);Thread.sleep(35);}touch(MotionEvent.ACTION_UP,targetX,targetY,down);Thread.sleep(150);settle();
    }
    <T extends View>T find(Class<T> type){return TenSmoke.find(root,type);}
    View slot(String id){TenTaskbarItems items=find(TenTaskbarItems.class);for(int i=0;i<items.getChildCount();i++)if(id.equals(TaskbarReorder.key(items.getChildAt(i))))return items.getChildAt(i);return null;}
    String bounds(View view){return rect(view).toShortString();}
    Map<String,View> fixedViews(){Map<String,View> result=new LinkedHashMap<>();for(String name:new String[]{"Start menu","Search","Task View","Device controls","Clock and calendar","Action Center","Show desktop"})result.put(name,TenSmoke.description(bar(),name));ViewGroup controls=(ViewGroup)TenSmoke.description(bar(),"Taskbar controls");for(int i=0;i<controls.getChildCount();i++)if("@nota".equals(TaskbarReorder.key(controls.getChildAt(i))))result.put("Nota",controls.getChildAt(i));return result;}
    @SuppressWarnings("unchecked") Map<String,DesktopWindow> windows(){try{java.lang.reflect.Field f=MainActivity.class.getDeclaredField("windows");f.setAccessible(true);return (Map<String,DesktopWindow>)f.get(home);}catch(Exception e){throw new RuntimeException(e);}}
    void closeAll(){for(DesktopWindow w:new ArrayList<>(windows().values()))if(w.getParent()!=null)w.closeWindow();}
    void toggleMenu(){home.open("ten-setting:taskbar");((Switch)TenSmoke.text(TenSmoke.find(root,TenSettings.class),"Extended taskbar")).performClick();}

    void edgeCases()throws Exception{
        progress("single slot, rejected/interrupted drops, mouse and row edge scrolling");
        ui(()->{closeAll();for(String id:ExternalTasks.list())ExternalTasks.remove(id);store.quickLaunch(Collections.singletonList("@photos"));store.prefs.edit().remove(TaskbarRows.KEY).remove(TaskbarOrder.KEY).putBoolean("taskbar_static",false).putBoolean("taskbar_show_labels",false).putString("ten_news_mode","Hidden").commit();home.applyTenAppearance();});settle();
        check(find(TenTaskbarItems.class).getChildCount()==1,"A single movable slot is present");
        dragAcross("@photos",0);checkRow("@photos",0,"Single pin can move into an empty upper row");
        dragAcross("@photos",1);checkRow("@photos",1,"Single pin can move into an empty lower row");
        String rows=store.prefs.getString(TaskbarRows.KEY,"{}"),order=store.prefs.getString(TaskbarOrder.KEY,"");
        View old=slot("@photos");float[] start=touchPoint(old);long down=SystemClock.uptimeMillis();
        touch(MotionEvent.ACTION_DOWN,start[0],start[1],down);Thread.sleep(185);touch(MotionEvent.ACTION_MOVE,start[0]+dp(14),start[1],down);Thread.sleep(100);
        ui(home::applyTenAppearance);touch(MotionEvent.ACTION_UP,start[0]+dp(14),start[1],down);settle();
        check(old.getAlpha()==1f&&rows.equals(store.prefs.getString(TaskbarRows.KEY,"{}"))&&order.equals(store.prefs.getString(TaskbarOrder.KEY,"")),"Interrupted native drag restores alpha and leaves saved placement unchanged");
        nativeDrop("@photos",0,false,true);checkRow("@photos",1,"Dropping outside the taskbar leaves the row unchanged");
        check(rows.equals(store.prefs.getString(TaskbarRows.KEY,"{}"))&&order.equals(store.prefs.getString(TaskbarOrder.KEY,"")),"Rejected drop does not save order or row");
        overflowFixture();
        for(int row=0;row<2;row++){
            final int current=row;progress("native touch edge drag in row "+row);ui(()->{find(TenTaskbarItems.class).scrollRowTo(0,0);find(TenTaskbarItems.class).scrollRowTo(1,0);});settle();
            List<String> opposite=geometryRow(1-row);View source=null;TenTaskbarItems items=find(TenTaskbarItems.class);for(int i=0;i<items.getChildCount();i++)if(TenTaskbarItems.row(items.getChildAt(i))==row){source=items.getChildAt(i);break;}
            float[] point=touchPoint(source);int[] at=new int[2];items.getLocationOnScreen(at);long now=SystemClock.uptimeMillis();
            touch(MotionEvent.ACTION_DOWN,point[0],point[1],now);Thread.sleep(185);touch(MotionEvent.ACTION_MOVE,point[0]+dp(14),point[1],now);Thread.sleep(100);await(this::dragging,"The native gesture enters drag mode before an edge hover: row "+row);
            float y=at[1]+dp(row*48+24),edgeX=at[0]+items.rowEnd(row)-dp(3);
            for(int step=1;step<=6;step++){touch(MotionEvent.ACTION_MOVE,point[0]+(edgeX-point[0])*step/6,y,now);Thread.sleep(40);}
            touch(MotionEvent.ACTION_MOVE,edgeX,y,now);progress("right edge delivery row "+row+": "+dragTrace()+"; range="+items.rowRange(row)+"; target="+edgeX+","+y);
            await(()->items.rowScroll(current)>0,"Native drag edge scroll advances the hovered row "+row+"; "+dragTrace());
            int right=items.rowScroll(row);check(right>0,"Native drag edge scroll advances the hovered row "+row);check(opposite.equals(geometryRow(1-row)),"Drag edge scroll leaves opposite row unchanged");
            for(int step=1;step<=6;step++){touch(MotionEvent.ACTION_MOVE,edgeX+(at[0]+items.rowStart(row)+dp(3)-edgeX)*step/6,y,now);Thread.sleep(40);}touch(MotionEvent.ACTION_MOVE,at[0]+items.rowStart(row)+dp(3),y,now);await(()->items.rowScroll(current)<right,"Left edge scroll returns toward the beginning in row "+row);check(items.rowScroll(row)<right,"Left edge scroll returns toward the beginning");
            touch(MotionEvent.ACTION_UP,at[0]+items.rowStart(row)+dp(3),y,now);settle();
        }
        // Keep all native touch cases ahead of mouse injection: API 30's virtual source transition can lose drag locations.
        ui(()->{closeAll();store.quickLaunch(Collections.singletonList("@photos"));store.prefs.edit().remove(TaskbarRows.KEY).remove(TaskbarOrder.KEY).putBoolean("taskbar_show_labels",false).commit();home.applyTenAppearance();});settle();
        nativeDrop("@photos",0,true,false);checkRow("@photos",0,"Native mouse drag moves the single pin to the upper row");
        overflowFixture();
        for(int row=0;row<2;row++){
            progress("native mouse wheel in row "+row);ui(()->{find(TenTaskbarItems.class).scrollRowTo(0,0);find(TenTaskbarItems.class).scrollRowTo(1,0);});settle();
            List<String> opposite=geometryRow(1-row);wheel(row);settle();
            check(find(TenTaskbarItems.class).rowScroll(row)>0,"Native mouse wheel scrolls the selected row");check(opposite.equals(geometryRow(1-row)),"Mouse wheel leaves opposite row geometry unchanged");
        }
    }
    void overflowFixture()throws Exception{
        ui(()->{closeAll();store.quickLaunch(Arrays.asList("@photos","@browser","@recycle","@calendar","@clock","@weather"));store.prefs.edit().remove(TaskbarRows.KEY).putBoolean("taskbar_show_labels",true).commit();home.applyTenAppearance();for(String id:new String[]{"@notes","@settings","@gadget:calculator","@welcome","@taskmanager"})home.open(id);});settle();
    }
    float[] touchPoint(View source){TenTaskbarItems items=find(TenTaskbarItems.class);int[] origin=new int[2],at=new int[2];source.getLocationOnScreen(origin);items.getLocationOnScreen(at);int row=TenTaskbarItems.row(source);float left=Math.max(origin[0],at[0]+items.rowStart(row)),right=Math.min(origin[0]+source.getWidth(),at[0]+items.rowEnd(row));check(right>left,"Drag source has visible pixels in its own row");return new float[]{(left+right)/2,origin[1]+source.getHeight()/2f};}
    void nativeDrop(String id,int targetRow,boolean mouse,boolean outside)throws Exception{
        View source=slot(id);float[] point=touchPoint(source);TenTaskbarItems items=find(TenTaskbarItems.class);int[] at=new int[2];items.getLocationOnScreen(at);float x=at[0]+items.rowStart(targetRow)+dp(16),y=outside?at[1]-dp(40):at[1]+dp(targetRow*48+24);long now=SystemClock.uptimeMillis();
        pointer(MotionEvent.ACTION_DOWN,point[0],point[1],now,mouse);Thread.sleep(mouse?30:185);pointer(MotionEvent.ACTION_MOVE,point[0]+dp(14),point[1],now,mouse);Thread.sleep(120);
        for(int i=1;i<=6;i++){pointer(MotionEvent.ACTION_MOVE,point[0]+(x-point[0])*i/6,point[1]+(y-point[1])*i/6,now,mouse);Thread.sleep(35);}pointer(MotionEvent.ACTION_UP,x,y,now,mouse);settle();
    }
    void pointer(int action,float x,float y,long down,boolean mouse){
        MotionEvent.PointerProperties prop=new MotionEvent.PointerProperties();prop.id=0;prop.toolType=mouse?MotionEvent.TOOL_TYPE_MOUSE:MotionEvent.TOOL_TYPE_FINGER;MotionEvent.PointerCoords coords=new MotionEvent.PointerCoords();coords.x=x;coords.y=y;coords.pressure=action==MotionEvent.ACTION_UP?0:1;coords.size=1;
        MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,1,new MotionEvent.PointerProperties[]{prop},new MotionEvent.PointerCoords[]{coords},0,mouse&&action!=MotionEvent.ACTION_UP?MotionEvent.BUTTON_PRIMARY:0,1,1,0,0,mouse?InputDevice.SOURCE_MOUSE:InputDevice.SOURCE_TOUCHSCREEN,0);check(getUiAutomation().injectInputEvent(event,false),"Native pointer input accepted");event.recycle();
    }
    void wheel(int row){TenTaskbarItems items=find(TenTaskbarItems.class);int[] at=new int[2];items.getLocationOnScreen(at);MotionEvent.PointerProperties p=new MotionEvent.PointerProperties();p.id=0;p.toolType=MotionEvent.TOOL_TYPE_MOUSE;MotionEvent.PointerCoords c=new MotionEvent.PointerCoords();c.x=at[0]+(items.rowStart(row)+items.rowEnd(row))/2f;c.y=at[1]+dp(row*48+24);c.setAxisValue(MotionEvent.AXIS_VSCROLL,-4);long now=SystemClock.uptimeMillis();MotionEvent event=MotionEvent.obtain(now,now,MotionEvent.ACTION_SCROLL,1,new MotionEvent.PointerProperties[]{p},new MotionEvent.PointerCoords[]{c},0,0,1,1,0,0,InputDevice.SOURCE_MOUSE,0);check(getUiAutomation().injectInputEvent(event,true),"Native mouse wheel accepted");event.recycle();}

    void tapSlot(String id)throws Exception{ui(()->{TenTaskbarItems items=find(TenTaskbarItems.class);items.stopScrolling();items.scrollRowTo(TenTaskbarItems.row(slot(id)),0);});settle();float[] at=touchPoint(slot(id));long now=SystemClock.uptimeMillis();timedTouch(MotionEvent.ACTION_DOWN,at[0],at[1],now,now);timedTouch(MotionEvent.ACTION_UP,at[0],at[1],now,now+60);settle();}

    void progress(String label){Bundle out=new Bundle();out.putString("stream","CHECK: "+label+"\n");sendStatus(1,out);}

    boolean dragging(){try{java.lang.reflect.Field f=MainActivity.class.getDeclaredField("taskbarReorder");f.setAccessible(true);return ((TaskbarReorder)f.get(home)).dragging();}catch(Exception e){throw new RuntimeException(e);}}

    String dragTrace(){try{java.lang.reflect.Field f=MainActivity.class.getDeclaredField("taskbarReorder");f.setAccessible(true);Object reorder=f.get(home);StringBuilder out=new StringBuilder();for(String name:new String[]{"rawX","rawY","closed","active"}){java.lang.reflect.Field value=TaskbarReorder.class.getDeclaredField(name);value.setAccessible(true);out.append(name).append('=').append(value.get(reorder)).append(' ');}return out.toString();}catch(Exception e){return e.toString();}}

}
