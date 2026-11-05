package com.nthexperience.desktop;
import com.google.mlkit.genai.common.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TenLocalAITest {
 static final class Fake implements LocalAI.Backend {
  volatile int feature=FeatureStatus.DOWNLOADABLE;AtomicInteger downloads=new AtomicInteger(),generations=new AtomicInteger();CompletableFuture<String> reply=CompletableFuture.completedFuture("On-device fixture reply");
  public CompletableFuture<Integer> status(){return CompletableFuture.completedFuture(feature);}public CompletableFuture<Void> download(DownloadCallback callback){downloads.incrementAndGet();feature=FeatureStatus.AVAILABLE;return CompletableFuture.completedFuture(null);}public CompletableFuture<Integer> tokens(String p,String task){return CompletableFuture.completedFuture(15);}public CompletableFuture<String> generate(String p,String task){generations.incrementAndGet();return reply;}public void close(){}
 }
 static JSONObject terminal(BlockingQueue<JSONObject> queue)throws Exception{long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(4);while(System.nanoTime()<end){JSONObject e=queue.poll(200,TimeUnit.MILLISECONDS);if(e!=null&&e.optString("event").equals("result"))return e;}throw new AssertionError("No terminal event");}
 static void idle(LocalAI ai)throws Exception{long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(2);while(ai.isBusy()&&System.nanoTime()<end)Thread.sleep(5);assertFalse(ai.isBusy());}
 @Test public void modelDownloadNeedsExplicitPrepare()throws Exception {Fake backend=new Fake();BlockingQueue<JSONObject> events=new LinkedBlockingQueue<>();LocalAI ai=new LocalAI(events::add,backend);try{String input=new JSONObject().put("prompt","Write a thank-you note").put("task","generate").toString();ai.request("first","generate",input);assertEquals("MODEL_NEEDED",terminal(events).getString("error"));assertEquals(0,backend.downloads.get());idle(ai);ai.request("prepare","prepare","{}");assertEquals("ready",terminal(events).getString("status"));assertEquals(1,backend.downloads.get());idle(ai);ai.request("reply","generate",input);JSONObject reply=terminal(events);assertTrue(reply.getBoolean("local"));assertEquals("On-device fixture reply",reply.getString("text"));assertEquals(1,backend.generations.get());}finally{ai.close();}}
 @Test public void stopDoesNotPublishLateModelReply()throws Exception {Fake backend=new Fake();backend.feature=FeatureStatus.AVAILABLE;backend.reply=new CompletableFuture<>();BlockingQueue<JSONObject> events=new LinkedBlockingQueue<>();LocalAI ai=new LocalAI(events::add,backend);try{ai.request("stopme","generate",new JSONObject().put("prompt","A local fixture").put("task","generate").toString());long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(2);while(backend.generations.get()==0&&System.nanoTime()<end)Thread.sleep(5);assertEquals(1,backend.generations.get());ai.cancel("stopme");assertEquals("CANCELLED",terminal(events).getString("error"));backend.reply.complete("Late reply");idle(ai);assertFalse(events.stream().anyMatch(e->e.has("text")));}finally{ai.close();}}
}
