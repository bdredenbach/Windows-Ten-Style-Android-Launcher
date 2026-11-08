package com.nthexperience.desktop;

import org.json.*;
import java.util.*;

/** Versioned data only: never restores an editor, action card, or running request. */
final class TenNotaState {
    static final int MAX_REPLY=100000;
    static JSONObject ordinary(String draft,int task,boolean imported,String reply,String source,
            boolean guidance,boolean command)throws JSONException {
        return new JSONObject().put("version",1).put("draft",draft).put("task",task)
            .put("imported",imported).put("reply",reply).put("source",source)
            .put("guidance",guidance).put("command",command);
    }
    static JSONObject empty(){try{return ordinary("",0,false,"","",true,false);}catch(JSONException e){throw new IllegalStateException(e);}}
    static JSONObject redacted(){try{return new JSONObject().put("version",1).put("private",true);}catch(JSONException e){throw new IllegalStateException(e);}}
    static JSONObject checked(JSONObject data)throws JSONException {
        if(data==null||!Integer.valueOf(1).equals(data.opt("version")))throw new JSONException("Invalid Nota state version");
        if(data.has("private")){
            if(!Boolean.TRUE.equals(data.get("private"))||data.length()!=2)throw new JSONException("Private Nota state must be empty");
            return redacted();
        }
        Set<String> fields=new HashSet<>(Arrays.asList("version","draft","task","imported","reply","source","guidance","command"));
        Iterator<String> keys=data.keys();while(keys.hasNext())if(!fields.contains(keys.next()))throw new JSONException("Unknown Nota state field");
        Object task=data.get("task");if(!(task instanceof Integer)||(Integer)task<0||(Integer)task>5)throw new JSONException("Invalid Nota task");
        for(String key:new String[]{"imported","guidance","command"})if(!(data.get(key) instanceof Boolean))throw new JSONException("Invalid Nota flag");
        for(String key:new String[]{"draft","reply","source"})if(!(data.get(key) instanceof String))throw new JSONException("Invalid Nota text");
        if(data.getString("draft").length()>TenWritingPrompts.MAX_COMPOSER||data.getString("reply").length()>MAX_REPLY||data.getString("source").length()>512)throw new JSONException("Nota text too long");
        // Source metadata never grants permission to run commands, even in a hand-edited backup.
        return ordinary(data.getString("draft"),(Integer)task,data.getBoolean("imported")||!data.getString("source").isEmpty(),data.getString("reply"),data.getString("source"),data.getBoolean("guidance"),data.getBoolean("command"));
    }
    static void validateBanks(String text,JSONObject desktops)throws JSONException {
        JSONObject banks=new JSONObject(text);JSONArray rows=desktops.getJSONArray("desktops");Set<String> ids=new HashSet<>();
        for(int i=0;i<rows.length();i++)ids.add(rows.getJSONObject(i).getString("id"));
        if(banks.length()>8)throw new JSONException("Too many Nota windows");
        Iterator<String> keys=banks.keys();while(keys.hasNext()){String id=keys.next();if(!ids.contains(id))throw new JSONException("Unknown Nota desktop");JSONObject state=checked(banks.getJSONObject(id));if(state.optBoolean("private"))throw new JSONException("Private Nota state is session-only");}
    }
}
