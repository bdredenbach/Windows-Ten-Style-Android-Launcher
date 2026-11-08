package com.nthexperience.desktop;
import android.content.Context;
import org.json.*;
import java.util.function.Consumer;

/** Automatic local-first routing. Downloading a model is a separate user action. */
final class TenAssistant {
 final LocalAI local;final CloudAI cloud;final Consumer<JSONObject> emit;String active="",task,prompt;boolean preparing,cloudStage,closed;
 TenAssistant(Context context,Consumer<JSONObject> emit){this(context,emit,null);}
 TenAssistant(Context context,Consumer<JSONObject> emit,LocalAI.Backend backend){this.emit=emit;local=backend==null?new LocalAI(e->dispatch(false,e)):new LocalAI(e->dispatch(false,e),backend);cloud=new CloudAI(context,e->dispatch(true,e));}
 synchronized void request(String id,String task,String prompt){if(closed)return;if(!active.isEmpty()){send(id,"BUSY","The assistant is still finishing a request.");return;}active=id;this.task=task;this.prompt=TenWritingPrompts.build(task,prompt);preparing=false;cloudStage=false;try{local.request(id+"_local","generate",new JSONObject().put("task",task).put("prompt",this.prompt).toString());}catch(Exception e){send(id,"UNAVAILABLE","The request could not be prepared.");active="";}}
 synchronized void prepare(String id){if(closed||!active.isEmpty())return;active=id;preparing=true;cloudStage=false;local.request(id+"_local","prepare","{}");}
 private synchronized void dispatch(boolean fromCloud,JSONObject event){if(closed||active.isEmpty()||!event.optString("id").equals(active+(fromCloud?"_cloud":"_local")))return;if(cloudStage&&!fromCloud)return;
  if(event.optString("event").equals("progress")){publish(event);return;}
  String error=event.optString("error");if(!fromCloud&&!preparing&&TenAiPolicy.fallback(error)){
   boolean allowed=false;try{allowed=new JSONObject(cloud.state()).optBoolean("allowed");}catch(JSONException ignored){}
   if(allowed){cloudStage=true;try{JSONObject progress=new JSONObject().put("event","progress").put("message","On-device AI is unavailable. Trying free cloud assistance…");publish(progress);cloud.request(active+"_cloud",new JSONObject().put("task",task).put("prompt",prompt).toString());return;}catch(JSONException ignored){}}
   try{event.put("needsCloud",true).put("message",error.equals("MODEL_NEEDED")?"Prepare on-device AI, or allow optional free cloud assistance.":"On-device AI is unavailable on this phone. Optional free cloud assistance is off.");}catch(JSONException ignored){}
  }
  publish(event);active="";
 }
 private void publish(JSONObject event){try{emit.accept(new JSONObject(event.toString()).put("id",active));}catch(JSONException ignored){}}
 private void send(String id,String code,String message){try{emit.accept(new JSONObject().put("id",id).put("event","result").put("error",code).put("message",message));}catch(JSONException ignored){}}
 synchronized void cancel(String id){if(!id.equals(active))return;String previous=active;active="";local.cancel(previous+"_local");cloud.cancel(previous+"_cloud");send(previous,"CANCELLED","Reply stopped.");}
 synchronized void foreground(boolean value){if(!value&&!active.isEmpty())cancel(active);local.foreground(value);cloud.foreground(value);}
 synchronized void close(){if(closed)return;if(!active.isEmpty())cancel(active);closed=true;local.close();cloud.close();}
}
