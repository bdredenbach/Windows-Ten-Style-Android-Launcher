package com.nthexperience.desktop;
import java.util.*;
import org.json.*;
final class TenStartData {
 static final String KEY="ten_start_groups";final DesktopStore store;
 TenStartData(DesktopStore s){store=s;}
 static List<String> parse(String text)throws JSONException{JSONArray data=new JSONArray(text);if(data.length()<1||data.length()>12)throw new JSONException("Use one to twelve tile groups");List<String> names=new ArrayList<>();for(int i=0;i<data.length();i++){String s=data.getString(i);if(s.trim().isEmpty()||s.length()>40||names.contains(s))throw new JSONException("Invalid tile group name");names.add(s);}return names;}
 List<String> groups(){try{return parse(store.prefs.getString(KEY,"[\"Productivity\",\"Explore\"]"));}catch(JSONException e){return new ArrayList<>(Arrays.asList("Productivity","Explore"));}}
 String group(String id){List<String> names=groups();String value=store.prefs.getString("ten_group_"+id,names.get(0));return names.contains(value)?value:names.get(0);}
 boolean rename(String old,String value){value=value.trim();List<String> groups=groups();if(value.isEmpty()||value.length()>40||!groups.contains(old)||(!old.equals(value)&&groups.contains(value)))return false;groups.set(groups.indexOf(old),value);android.content.SharedPreferences.Editor edit=store.prefs.edit();for(Map.Entry<String,?> e:store.prefs.getAll().entrySet())if(e.getKey().startsWith("ten_group_")&&old.equals(e.getValue()))edit.putString(e.getKey(),value);for(String pin:store.startPins())if(group(pin).equals(old))edit.putString("ten_group_"+pin,value);edit.putString(KEY,new JSONArray(groups).toString()).apply();return true;}
 boolean add(String value){value=value.trim();List<String> groups=groups();if(value.isEmpty()||value.length()>40||groups.contains(value)||groups.size()>=12)return false;groups.add(value);store.prefs.edit().putString(KEY,new JSONArray(groups).toString()).apply();return true;}
 void reorder(String id,int delta){List<String> pins=new ArrayList<>(store.startPins());int at=pins.indexOf(id);if(at<0||delta==0)return;int step=delta<0?-1:1,next=at+step;String group=group(id);while(next>=0&&next<pins.size()&&!group(pins.get(next)).equals(group))next+=step;if(next<0||next>=pins.size())return;Collections.swap(pins,at,next);store.startPins(pins);}
}
