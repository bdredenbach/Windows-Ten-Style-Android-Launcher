package com.nthexperience.desktop;

import android.app.Activity;
import android.os.Looper;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** A late successful reply must never cross a desktop, chat or consent boundary. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=35,qualifiers="mdpi")
public class TenChatWindowTest {
    Activity activity;
    TenNotaPanel panel;
    final CountDownLatch started=new CountDownLatch(1), resume=new CountDownLatch(1), returned=new CountDownLatch(1);
    final AtomicBoolean cancelled=new AtomicBoolean();
    final AtomicBoolean startedTransport=new AtomicBoolean();

    @Before public void setup(){
        activity=Robolectric.buildActivity(Activity.class).setup().get();
        panel=new TenNotaPanel(activity,new DesktopStore(activity),"desktop-1",null,
                allowed->new LiveQuestions.Transport(){
                    public String get(String url){startedTransport.set(true);return "{\"chatVersion\":1}";}
                    public String post(String url,String json)throws Exception{
                        started.countDown();resume.await(10,TimeUnit.SECONDS);returned.countDown();
                        return "{\"text\":\"UNEXPECTED_LATE_REPLY\"}";
                    }
                    public void cancel(){cancelled.set(true);}
                });
        activity.setContentView(panel);
    }
    @After public void cleanup(){resume.countDown();panel.close();}
    void start() throws Exception{
        TenChatConsent.preferences(activity).edit().putBoolean(TenChatConsent.KEY,true).commit();
        assertFalse(startedTransport.get());
        panel.chat.question.setText("Explain a rainbow");panel.chat.send.performClick();
        assertTrue(started.await(5,TimeUnit.SECONDS));
    }
    void checkLate() throws Exception{
        assertTrue(cancelled.get());
        resume.countDown();assertTrue(returned.await(5,TimeUnit.SECONDS));
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertFalse(panel.chat.store.conversations.save().contains("UNEXPECTED_LATE_REPLY"));
        assertEquals(0,panel.chat.store.conversations.current().context().length());
    }
    @Test public void stopRejectsLateReply()throws Exception{
        start();panel.chat.send.performClick();checkLate();
        assertEquals("Stopped",panel.chat.store.conversations.current().turns.get(0).answer.kind);
    }
    @Test public void backgroundRejectsLateReply()throws Exception{
        start();panel.foreground(false);checkLate();
    }
    @Test public void newChatRejectsLateReply()throws Exception{
        start();panel.chat.newChat();checkLate();assertTrue(panel.chat.store.conversations.current().turns.isEmpty());
    }
    @Test public void closeRejectsLateReply()throws Exception{
        start();panel.close();resume.countDown();assertTrue(cancelled.get());assertEquals(0,panel.chat.store.conversations.current().context().length());
    }
    @Test public void revocationRejectsLateReply()throws Exception{
        start();TenChatConsent.preferences(activity).edit().putBoolean(TenChatConsent.KEY,false).commit();
        Shadows.shadowOf(Looper.getMainLooper()).idle();checkLate();
    }
    @Test public void writingPermissionCannotEnableChatOrSendAStarter() {
        panel.cloud.allow(true);
        assertFalse(panel.chat.cloud.isChecked());
        panel.chat.offerDraft("Show me today's technology headlines",true);
        assertTrue(panel.chat.store.conversations.current().turns.isEmpty());
        panel.chat.send.performClick();
        assertTrue(panel.chat.store.conversations.current().turns.get(0).answer.text.contains("Cloud questions are off"));
        assertFalse(startedTransport.get());
    }
    @Test public void localSettingQuestionWorksWithoutNetwork() {
        panel.chat.question.setText("Show my extended taskbar");
        panel.chat.send.performClick();
        assertEquals("Extended taskbar: Off",panel.chat.store.conversations.current().turns.get(0).answer.text);
        assertFalse(startedTransport.get());
    }
    @Test public void longWritingImportSurvivesAlongsideItsChatDraft() {
        String text="open Calculator "+"sample ".repeat(750);
        panel.chat.question.setText("Unfinished chat");
        panel.context(new TenNotaContext("Editor",text,false));
        assertEquals(text,TenNotaDrafts.read(panel.store,panel.desktopId).optString("draft"));
        assertEquals("Unfinished chat",panel.chat.store.draft());
        assertTrue(panel.chat.store.conversations.current().turns.isEmpty());
        assertFalse(startedTransport.get());
    }
    @Test public void backupCannotCarryCloudConsentOrChatHistory() throws Exception {
        TenChatConsent.preferences(activity).edit().putBoolean(TenChatConsent.KEY,true).commit();
        panel.chat.store.conversations.append("TRANSCRIPT_MARKER",LiveAnswer.plain("Answer","AI answer"),true);
        panel.chat.store.save();
        String backup=DesktopBackup.snapshot(panel.store).toString();
        assertFalse(backup.contains("TRANSCRIPT_MARKER"));
        assertFalse(backup.contains(TenChatConsent.KEY));
        assertNotNull(DesktopBackup.parse(backup));
    }
    @Test public void privateWritingAndCommandLookingImportNeverEnterChatHistory() {
        panel.input.setText("Ordinary writing");
        panel.context(new TenNotaContext("InPrivate","open Calculator PRIVATE_PASSAGE",true));
        assertEquals("Ordinary writing",TenNotaDrafts.read(panel.store,panel.desktopId).optString("draft"));
        assertFalse(panel.state().toString().contains("PRIVATE_PASSAGE"));
        assertFalse(panel.store.prefs.getAll().toString().contains("PRIVATE_PASSAGE"));
        panel.showWriting(false);
        panel.chat.offerDraft("open Calculator",true);panel.chat.send.performClick();
        assertTrue(panel.chat.store.conversations.current().turns.get(0).answer.text.contains("Cloud questions are off"));
        assertFalse(panel.chat.store.conversations.save().contains("PRIVATE_PASSAGE"));assertFalse(startedTransport.get());
    }
}
