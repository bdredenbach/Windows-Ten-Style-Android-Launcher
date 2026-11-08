package com.nthexperience.desktop;

import org.json.*;

/** Ordinary drafts belong to their desktop, including when a window is closed. */
final class TenNotaDrafts {
    static final String KEY="ten_nota_windows";
    static JSONObject banks(DesktopStore store){try{return new JSONObject(store.prefs.getString(KEY,"{}"));}catch(JSONException e){return new JSONObject();}}
    static void migrate(DesktopStore store){
        if(store.prefs.contains(KEY))return;
        JSONObject banks=new JSONObject();String draft=store.prefs.getString("ten_nota_draft","");
        try{banks.put(TenDesktopItems.first(store),TenNotaState.ordinary(draft.substring(0,Math.min(draft.length(),TenWritingPrompts.MAX_COMPOSER)),Math.max(0,Math.min(5,store.prefs.getInt("ten_nota_task",0))),store.prefs.getBoolean("ten_nota_imported",!draft.isEmpty()),store.prefs.getString("ten_nota_reply",""),"",true,false));}catch(JSONException ignored){}
        store.prefs.edit().putString(KEY,banks.toString()).remove("ten_nota_draft").remove("ten_nota_reply").remove("ten_nota_task").remove("ten_nota_imported").apply();
    }
    static JSONObject read(DesktopStore store,String desktop){migrate(store);try{return TenNotaState.checked(banks(store).optJSONObject(desktop));}catch(JSONException e){return TenNotaState.empty();}}
    static void write(DesktopStore store,String desktop,JSONObject state){JSONObject banks=banks(store);GadgetLayout.put(banks,desktop,state);store.prefs.edit().putString(KEY,banks.toString()).apply();}
    static void move(DesktopStore store,String from,String to,JSONObject state){JSONObject banks=banks(store);banks.remove(from);GadgetLayout.put(banks,to,state);store.prefs.edit().putString(KEY,banks.toString()).apply();}
    static void remove(DesktopStore store,String desktop){JSONObject banks=banks(store);banks.remove(desktop);store.prefs.edit().putString(KEY,banks.toString()).apply();}
}
