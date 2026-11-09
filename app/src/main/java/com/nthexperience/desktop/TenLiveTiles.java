package com.nthexperience.desktop;

import android.content.*;
import android.os.BatteryManager;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;

/** Read-only tile details from the app's local state and already-cached feeds. */
final class TenLiveTiles {
 static final class Detail {final String primary,secondary;Detail(String a,String b){primary=a;secondary=b;}}
 static Detail detail(Context context,DesktopStore store,String id){
  TenStartCatalog.App app=TenStartCatalog.app(id);
  if(app==null||!app.live||!store.prefs.getBoolean("ten_live_tiles",true)||!store.prefs.getBoolean("ten_tile_live_"+id,true))return null;
  Date now=new Date();
  switch(id){
   case "@calendar":return new Detail(new SimpleDateFormat("d",Locale.getDefault()).format(now),new SimpleDateFormat("EEEE\nMMMM yyyy",Locale.getDefault()).format(now));
   case "@clock":return new Detail(android.text.format.DateFormat.getTimeFormat(context).format(now),new SimpleDateFormat("EEEE, MMM d",Locale.getDefault()).format(now));
   case "@notes":return note(store.prefs.getString("note",""),"Write your first note");
   case "@sticky":return note(store.prefs.getString("gadget_note",""),"Capture a quick thought");
   case "@weather":
    String city=store.prefs.getString("weather_city","");
    try{WeatherClient.Snapshot weather=WeatherClient.Snapshot.parse(store.prefs.getString("weather_json","{}"));boolean f=store.prefs.getBoolean("weather_f",Locale.getDefault().getCountry().equals("US"));return new Detail(weather.temperature(f)+(f?"°F":"°C"),weather.condition()+"\n"+(city.isEmpty()?"Saved forecast":city)+" · Cached");}catch(JSONException e){return new Detail(city.isEmpty()?"Choose a place":city,"Open Weather to get a forecast");}
   case "@news":case "@gadget:feeds":
    JSONArray stories=FeedClient.items(store);JSONObject story=stories.optJSONObject(stories.length()==0?0:(int)((System.currentTimeMillis()/15000)%stories.length()));return story==null?new Detail("Your headlines","Connect a feed in News and interests"):new Detail(story.optString("title","Headlines"),"Saved headline · Open to read");
   case "@battery":
    Intent battery=context.registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));int level=battery==null?-1:battery.getIntExtra(BatteryManager.EXTRA_LEVEL,-1),scale=battery==null?100:battery.getIntExtra(BatteryManager.EXTRA_SCALE,100);
    return new Detail(level<0?"Battery":Math.round(level*100f/Math.max(1,scale))+"%",battery!=null&&battery.getIntExtra(BatteryManager.EXTRA_PLUGGED,0)!=0?"Charging":"Device battery");
   case "@gadget:checklist":
    JSONArray tasks=GadgetData.get(store,"checklist").optJSONArray("tasks");int total=tasks==null?0:tasks.length(),done=0;String next="Add a task to get started";
    if(tasks!=null)for(int i=tasks.length()-1;i>=0;i--){JSONObject t=tasks.optJSONObject(i);if(t==null)continue;if(t.optBoolean("done"))done++;else next=t.optString("text");}
    return new Detail(total==0?"Your tasks":done+" of "+total+" done",next);
   default:return null;
  }
 }
 private static Detail note(String text,String empty){text=text.trim().replaceAll("\\s+"," ");return new Detail(text.isEmpty()?empty:text.substring(0,Math.min(100,text.length())),text.isEmpty()?"Saved on this device":"Latest saved note");}
}
