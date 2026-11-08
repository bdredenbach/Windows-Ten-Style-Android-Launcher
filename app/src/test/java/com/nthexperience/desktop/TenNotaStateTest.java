package com.nthexperience.desktop;

import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TenNotaStateTest {
    JSONObject desktops()throws Exception{return new JSONObject("{\"desktops\":[{\"id\":\"desktop-1\"},{\"id\":\"desktop-2\"}]}");}
    @Test public void independentFullDraftsAndReplies()throws Exception {
        String a="A".repeat(15000),b="B".repeat(15000);
        JSONObject banks=new JSONObject().put("desktop-1",TenNotaState.ordinary(a,2,true,"reply A","Notepad",true,false))
            .put("desktop-2",TenNotaState.ordinary(b,5,false,"Opening fixture","",false,true));
        TenNotaState.validateBanks(banks.toString(),desktops());
        JSONObject restored=new JSONObject(banks.toString());
        assertEquals(a,TenNotaState.checked(restored.getJSONObject("desktop-1")).getString("draft"));
        assertEquals(b,TenNotaState.checked(restored.getJSONObject("desktop-2")).getString("draft"));
        assertEquals("reply A",restored.getJSONObject("desktop-1").getString("reply"));
        assertEquals(5,restored.getJSONObject("desktop-2").getInt("task"));
        assertFalse(restored.getJSONObject("desktop-2").getBoolean("imported"));
    }
    @Test public void privateStateHasNoPassageOrReply()throws Exception {
        JSONObject data=TenNotaState.checked(TenNotaState.redacted());assertEquals(2,data.length());
        assertTrue(data.getBoolean("private"));assertFalse(data.has("draft"));assertFalse(data.has("reply"));
        try{TenNotaState.checked(TenNotaState.redacted().put("draft","private"));fail();}catch(JSONException expected){}
        try{TenNotaState.validateBanks(new JSONObject().put("desktop-1",data).toString(),desktops());fail();}catch(JSONException expected){}
    }
    @Test public void rejectsBadTaskTextOwnerAndExecutableFields()throws Exception {
        for(Object task:new Object[]{-1,6,"5",1.5})try{TenNotaState.checked(TenNotaState.empty().put("task",task));fail();}catch(JSONException expected){}
        try{TenNotaState.checked(TenNotaState.empty().put("draft","A".repeat(15001)));fail();}catch(JSONException expected){}
        try{TenNotaState.checked(TenNotaState.empty().put("actions",new JSONArray()));fail();}catch(JSONException expected){}
        try{TenNotaState.validateBanks(new JSONObject().put("unowned",TenNotaState.empty()).toString(),desktops());fail();}catch(JSONException expected){}
    }
    @Test public void sourceAlwaysKeepsWritingOrigin()throws Exception {
        JSONObject data=TenNotaState.checked(TenNotaState.ordinary("open Audible",5,false,"open Audible","Browser",true,false));
        assertTrue(data.getBoolean("imported"));
    }
}
