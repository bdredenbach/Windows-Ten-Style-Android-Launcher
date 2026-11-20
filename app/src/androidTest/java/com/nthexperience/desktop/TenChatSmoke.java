package com.nthexperience.desktop;

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.*;

/** Exercises the shipped window and host routes. Real service checks are explicitly selected. */
public final class TenChatSmoke extends TenThemeSmoke {
    TenNotaPanel panel(){return (TenNotaPanel) windows().get("@nota").body;}
    TenDesktops desks() {
        try {
            java.lang.reflect.Field field=MainActivity.class.getDeclaredField("tenDesktops");
            field.setAccessible(true);
            return (TenDesktops) field.get(home);
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    boolean live;
    final AtomicInteger probes=new AtomicInteger(), posts=new AtomicInteger(), cancelled=new AtomicInteger();
    volatile JSONObject last;
    volatile boolean hold;
    private final CountDownLatch release=new CountDownLatch(1);

    @Override public void onCreate(Bundle args){live="yes".equals(args==null?null:args.getString("live"));super.onCreate(args);}
    TenNotaChat chat(){return panel().chat;}
    @Override public void onStart(){
        Bundle out=new Bundle();
        try {
            Context app=getTargetContext();
            store=new DesktopStore(app);
            store.prefs.edit().putInt("startup_mode",2).putBoolean("reduce_motion",true).putBoolean("welcome_on_start",false)
                .putBoolean("ten_taskbar_extended",false).putBoolean("ten_apps_dark",false).putBoolean("ten_transparency",true)
                .remove("window_portrait_@nota").remove("window_landscape_@nota").remove(TenNotaDrafts.KEY).remove("ten_nota_draft").commit();
            TenChatConsent.preferences(app).edit().clear().commit();
            app.getSharedPreferences("ten-nota-conversations",0).edit().clear().commit();
            app.getSharedPreferences(StorageAccess.PREFS,0).edit().putBoolean("seen",true).commit();
            app.getSharedPreferences(StartupExperience.SESSION,0).edit().putBoolean("seen",true)
                .putBoolean("welcome_seen",true).putBoolean(StartupExperience.HOME_OFFERED,true).commit();
            ShellTestSetup.complete(app);
            home=(MainActivity)startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(app,MainActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            root=home.getWindow().getDecorView();
            await(()->TenSmoke.description(root,"Start menu")!=null,"shell ready");
            ui(()->{home.open("@nota");if(!live)fixtureWindow();});
            await(()->chat().cloud.isShown()&&chat().getHeight()>0,"Chat opens on a fresh desktop");
            if(live) realQuestions(); else fixtures();
            out.putString("stream","PASS: "+(live?"live sourced question, follow-up and source open":"Ten chat drafts, independent consent, local commands, request counts, context, source cards, cancellation, taskbar placement, move and restore, IME and per-desktop restore")+"\n");
            finish(Activity.RESULT_OK,out);
        }catch(Throwable e){try{shot("ten-chat-failure");}catch(Exception ignored){}
            out.putString("stream","FAIL: "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,out);}
        finally{release.countDown();}
    }
    @Override void shot(String name)throws Exception {
        waitForIdleSync();
        android.graphics.Bitmap image=getUiAutomation().takeScreenshot();
        boolean usable=false;
        if(image!=null){
            for(int y=image.getHeight()/12;y<image.getHeight()&&!usable;y+=Math.max(1,image.getHeight()/12)){
                for(int x=image.getWidth()/12;x<image.getWidth();x+=Math.max(1,image.getWidth()/12)){
                    if((image.getPixel(x,y)&0xffffff)!=0){usable=true;break;}
                }
            }
        }
        if(!usable){
            if(image!=null) image.recycle();
            android.graphics.Bitmap appView=android.graphics.Bitmap.createBitmap(root.getWidth(),root.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);
            ui(()->root.draw(new android.graphics.Canvas(appView)));
            image=appView;
            name+="-app-view";
        }
        try(java.io.OutputStream out=new java.io.FileOutputStream(new java.io.File(getTargetContext().getFilesDir(),name+".png"))){
            image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);
        }finally{image.recycle();}
    }
    void fixtureWindow(){
        windows().get("@nota").closeWindow();
        TenNotaPanel p=new TenNotaPanel(home,store,desks().current(),null,
            allowed->new LiveQuestions.Transport(){
                public String get(String url){probes.incrementAndGet();return "{\"chatVersion\":1}";}
                public String post(String url,String json)throws Exception{
                    last=new JSONObject(json);posts.incrementAndGet();
                    if(hold)release.await(30,TimeUnit.SECONDS);
                    return new JSONObject().put("text","The taskbar lets you launch apps and switch between open windows.")
                        .put("searched",true).put("sources",new JSONArray().put(new JSONObject()
                            .put("title","Windows 10 taskbar").put("url","https://learn.microsoft.com/en-us/windows/apps/design/design-principles")))
                        .toString();
                }
                public void cancel(){cancelled.incrementAndGet();}
            });
        try {
            Method method=MainActivity.class.getDeclaredMethod("window",String.class,String.class,View.class);
            method.setAccessible(true);method.invoke(home,"@nota","Nth Nota AI",p);
        }catch(Exception e){throw new IllegalStateException(e);}
    }
    void sendChat(String question)throws Exception {ui(()->{chat().question.setText(question);chat().send.performClick();});}
    void enable()throws Exception {ui(()->chat().cloud.performClick());systemClick("Enable cloud");await(()->chat().cloud.isChecked(),"Cloud switch reflects opt-in");}
    void fixtures()throws Exception {
        waitForIdleSync();
        assertPlacement();
        shot("ten-chat-welcome");
        ui(()->{store.prefs.edit().putBoolean("ten_taskbar_extended",true).apply();home.applyTenAppearance();});
        waitForIdleSync();
        assertPlacement();
        shot("ten-chat-extended");
        ui(()->TenSmoke.description(chat(),"Explore an idea").performClick());
        check(probes.get()==0&&posts.get()==0,"Opening and starter card are local");
        sendChat("Explain why rainbows appear.");
        ui(()->check(chat().store.conversations.current().turns.get(0).answer.text.contains("Cloud questions are off"),"Offline response points to switch"));
        sendChat("open Calculator");
        await(()->windows().get("@gadget:calculator")!=null,"Explicit command reaches local Calculator");
        ui(()->{home.open("@nota");chat().cloud.performClick();});systemClick("Keep off");
        ui(()->check(!chat().cloud.isChecked(),"Declining leaves cloud off"));
        enable();check(probes.get()==0&&posts.get()==0,"Consent alone sends nothing");
        ui(()->{chat().newChat();chat().web.performClick();});
        sendChat("What can I do from the Windows 10 taskbar?");
        await(()->TenSmoke.description(chat(),"Open source: Windows 10 taskbar, learn.microsoft.com")!=null,"Source title and domain");
        check(probes.get()==1&&posts.get()==1&&"search".equals(last.getString("mode")),"One probe and search request");
        ui(()->{home.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);});
        await(()->root.getWidth()>root.getHeight(),"Landscape available");
        ui(()->{
            DesktopWindow w=windows().get("@nota");
            w.arrange(45,35,Math.min(root.getWidth()-55,Aero.dp(home,400)),Math.min(root.getHeight()-90,Aero.dp(home,350)));
            float x=w.getX(),y=w.getY();
            w.minimize();home.open("@nota");
            check(w==windows().get("@nota")&&w.getX()==x&&w.getY()==y,"Restore retains user positioning");
        });
        ui(()->{store.prefs.edit().putBoolean("ten_apps_dark",true).apply();home.applyTenAppearance();});
        shot("ten-chat-dark");
        ui(()->{chat().web.performClick();chat().question.requestFocus();home.getSystemService(InputMethodManager.class).showSoftInput(chat().question,InputMethodManager.SHOW_IMPLICIT);});
        await(()->root.getRootWindowInsets().isVisible(WindowInsets.Type.ime()),"Keyboard open");
        await(()->{
            for(View control:new View[]{chat().send,chat().cloud,chat().web}){
                Rect visible=new Rect();
                if(!control.getGlobalVisibleRect(visible)||visible.height()!=control.getHeight()) return false;
            }
            return true;
        },"Send, Cloud questions and web mode above the keyboard");
        shot("ten-chat-keyboard");
        ui(()->home.getSystemService(InputMethodManager.class).hideSoftInputFromWindow(chat().question.getWindowToken(),0));
        sendChat("Explain that more simply.");
        await(()->posts.get()==2 && chat().store.conversations.current().turns.size()==2,"Follow-up rendered");
        check("auto".equals(last.getString("mode"))&&last.getJSONArray("messages").length()==2,"Only eligible turns sent");
        ui(()->{chat().newChat();hold=true;});
        sendChat("Pending question");
        await(()->posts.get()==3,"Pending request began");
        check(last.getJSONArray("messages").length()==0,"New conversation resets context");
        int previous=cancelled.get();
        ui(()->{home.open("ten-setting:assistant");TenSmoke.description(windows().get("@settings"),"Cloud questions for Nth Nota AI").performClick();});
        release.countDown();
        await(()->cancelled.get()>previous && !chat().cloud.isChecked() && "Stopped".equals(chat().store.conversations.current().turns.get(0).answer.kind),"Settings revocation cancels and ignores late completion");
        ui(()->{home.open("@nota");chat().question.setText("First desktop draft");String first=desks().current();String other=desks().create();
            desks().select(other);home.open("@nota");check(chat().question.length()==0,"Second desktop has its own draft");
            panel().context(new TenNotaContext("Private source","open Calculator PRIVATE-CHECK",true));
            String bank=home.getSharedPreferences("ten-nota-conversations",0).getAll().toString();
            check(!bank.contains("PRIVATE-CHECK")&&!store.prefs.getAll().toString().contains("PRIVATE-CHECK"),"Private passage excluded from both stores");
            desks().select(first);check("First desktop draft".contentEquals(chat().question.getText()),"Original draft and mode restored");});
    }
    void assertPlacement() throws Exception {
        ui(()->{
            DesktopWindow w=windows().get("@nota");
            View taskbar=TenSmoke.description(root,"Desktop taskbar");
            int[] windowPos=new int[2], taskPos=new int[2];
            w.getLocationOnScreen(windowPos);taskbar.getLocationOnScreen(taskPos);
            check(windowPos[0]==taskPos[0] && Math.abs(windowPos[1]+w.getHeight()-taskPos[1])<=1,
                "First-open panel sits at the left edge above the actual taskbar");
            int workspaceWidth=((View)w.getParent()).getWidth();
            if(workspaceWidth>=Aero.dp(home,600)) check(w.getWidth()<workspaceWidth,"Wide desktop stays visible");
            else check(w.getWidth()==workspaceWidth,"Phone panel uses available width");
        });
    }
    void realQuestions()throws Exception {
        enable();
        ui(()->chat().web.performClick());
        sendChat("According to NASA, on what date did Apollo 11 land on the Moon? Cite NASA.");
        waitReply(1);
        LiveAnswer reply=chat().store.conversations.current().turns.get(0).answer;
        check(!reply.searchFallback && !reply.sources.isEmpty(),"Shared service returned a cited answer");
        sendChat("Which lunar module made that landing? Cite a source.");
        waitReply(2);
        LiveAnswer follow=chat().store.conversations.current().turns.get(1).answer;
        check(!follow.searchFallback && !follow.sources.isEmpty(),"Follow-up returned sources");
        LiveAnswer.Source source=follow.sources.get(0);
        ui(()->TenSmoke.description(chat(),"Open source: "+source.title+", "+android.net.Uri.parse(source.url).getHost()).performClick());
        await(()->windows().get("@browser")!=null,"Source opens in Nth Browser");
    }
    void waitReply(int count)throws Exception {
        for(int i=0;i<90;i++){final boolean[] done={false};ui(()->done[0]=chat().store.conversations.current().turns.size()>=count);if(done[0])return;Thread.sleep(1000);}
        throw new AssertionError("Timed out waiting for live reply");
    }
}
