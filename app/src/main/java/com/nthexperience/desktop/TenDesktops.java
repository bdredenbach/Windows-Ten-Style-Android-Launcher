package com.nthexperience.desktop;
import android.view.View;
import java.util.*;
import org.json.*;

/** Built-in windows remain attached while inactive desktops preserve their visibility and state. */
final class TenDesktops {
 static final String KEY="ten_desktops";static final int LIMIT=8;
 static final class Desk {final String id;String name;final LinkedHashMap<String,DesktopWindow> windows=new LinkedHashMap<>();final Map<DesktopWindow,Boolean> visible=new IdentityHashMap<>();Desk(String id,String name){this.id=id;this.name=name;}}
 final DesktopStore store;final Map<String,DesktopWindow> active;final Runnable changed;final LinkedHashMap<String,Desk> desks=new LinkedHashMap<>();String current;
 TenDesktops(DesktopStore store,Map<String,DesktopWindow> active,Runnable changed){this.store=store;this.active=active;this.changed=changed;try{load(new JSONObject(store.prefs.getString(KEY,"{}")));}catch(Exception e){reset();}save();TenDesktopItems.initialize(store,new ArrayList<>(desks.keySet()));}
 private void reset(){desks.clear();current="desktop-1";desks.put(current,new Desk(current,"Desktop 1"));}
 static void validate(JSONObject data)throws JSONException {JSONArray rows=data.getJSONArray("desktops");if(rows.length()<1||rows.length()>LIMIT)throw new JSONException("Choose one to eight desktops");Set<String> ids=new HashSet<>();for(int i=0;i<rows.length();i++){JSONObject d=rows.getJSONObject(i);if(!d.getString("id").matches("[a-z0-9-]{1,40}")||!ids.add(d.getString("id"))||d.getString("name").trim().isEmpty()||d.getString("name").length()>60)throw new JSONException("Invalid desktop");}if(!ids.contains(data.getString("current")))throw new JSONException("Invalid current desktop");}
 void load(JSONObject data){try{validate(data);desks.clear();JSONArray rows=data.getJSONArray("desktops");for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);desks.put(row.getString("id"),new Desk(row.getString("id"),row.getString("name")));}current=data.getString("current");}catch(JSONException e){reset();}}
 JSONObject metadata(){JSONObject data=new JSONObject();JSONArray rows=new JSONArray();for(Desk d:desks.values()){JSONObject row=new JSONObject();GadgetLayout.put(row,"id",d.id);GadgetLayout.put(row,"name",d.name);rows.put(row);}GadgetLayout.put(data,"desktops",rows);GadgetLayout.put(data,"current",current);return data;}
 void save(){store.prefs.edit().putString(KEY,metadata().toString()).apply();}
 List<Desk> list(){return new ArrayList<>(desks.values());}String current(){return current;}String name(){return desks.get(current).name;}
 void register(DesktopWindow w){Desk d=desks.get(current);d.windows.put(w.toolId,w);d.visible.put(w,true);}
 void sync(){Desk d=desks.get(current);d.windows.clear();for(Map.Entry<String,DesktopWindow> e:active.entrySet())if(e.getValue().getParent()!=null){d.windows.put(e.getKey(),e.getValue());d.visible.put(e.getValue(),e.getValue().getVisibility()==View.VISIBLE&&!e.getValue().minimizing());}d.visible.keySet().removeIf(w->!d.windows.containsValue(w));}
 boolean select(String id){if(!desks.containsKey(id)||id.equals(current))return false;sync();for(DesktopWindow w:active.values())if(w.getParent()!=null){w.settle();w.setVisibility(View.GONE);}active.clear();current=id;Desk next=desks.get(id);next.windows.values().removeIf(w->w.getParent()==null);active.putAll(next.windows);for(DesktopWindow w:active.values())w.setVisibility(next.visible.getOrDefault(w,true)?View.VISIBLE:View.GONE);save();changed.run();return true;}
 String create(){if(desks.size()>=LIMIT)return null;String id=UUID.randomUUID().toString();desks.put(id,new Desk(id,"Desktop "+(desks.size()+1)));TenDesktopItems.create(store,id);save();return id;}
 boolean rename(String id,String name){name=name.trim();if(name.isEmpty()||name.length()>60||!desks.containsKey(id))return false;desks.get(id).name=name;save();changed.run();return true;}
 boolean remove(String id){sync();Desk d=desks.get(id);if(d==null||desks.size()==1)return false;String target=desks.keySet().stream().filter(k->!k.equals(id)).findFirst().get();if(!TenDesktopItems.canMerge(store,id,target))return false;List<DesktopWindow> live=new ArrayList<>();for(DesktopWindow w:d.windows.values())if(w.getParent()!=null)live.add(w);for(DesktopWindow w:live)if(!canMove(w,target))return false;for(DesktopWindow w:live)move(w,target);TenDesktopItems.merge(store,id,target);if(id.equals(current))select(target);desks.remove(id);save();changed.run();return true;}

 String owner(DesktopWindow window){for(Desk d:desks.values())if(d.windows.containsValue(window))return d.id;return current;}
 boolean visibility(DesktopWindow w){return owner(w).equals(current)?w.getVisibility()==View.VISIBLE&&!w.minimizing():desks.get(owner(w)).visible.getOrDefault(w,true);}
 boolean canMove(DesktopWindow w,String target){Desk d=desks.get(target);return d!=null&&!target.equals(owner(w))&&(!d.windows.containsKey(w.toolId)||d.windows.get(w.toolId).getParent()==null);}
 boolean move(DesktopWindow w,String target){sync();if(!canMove(w,target))return false;Desk from=desks.get(owner(w)),to=desks.get(target);boolean visible=visibility(w);from.windows.remove(w.toolId);from.visible.remove(w);if(from.id.equals(current))active.remove(w.toolId);to.windows.put(w.toolId,w);to.visible.put(w,visible);w.desktopScope(target);w.saveGeometry();if(target.equals(current))active.put(w.toolId,w);w.setVisibility(target.equals(current)&&visible?View.VISIBLE:View.GONE);save();changed.run();return true;}
 List<DesktopWindow> all(){sync();List<DesktopWindow> result=new ArrayList<>();for(Desk d:desks.values())for(DesktopWindow w:d.windows.values())if(w.getParent()!=null)result.add(w);return result;}
 void adjacent(int delta){List<String> ids=new ArrayList<>(desks.keySet());int at=ids.indexOf(current)+delta;if(at>=0&&at<ids.size())select(ids.get(at));}
}
