package com.nthexperience.desktop;
import org.junit.Test;
import org.json.*;
import java.util.*;
import static org.junit.Assert.*;
public class TaskbarRowsTest {
 @Test public void explicitChoicesAcceptBothRunningAndIdleIds()throws Exception{Map<String,Integer> rows=TaskbarRows.parse("{\"@notes\":1,\"package/.App\":0}");assertEquals(Integer.valueOf(1),rows.get("@notes"));assertEquals(Integer.valueOf(0),rows.get("package/.App"));}
 @Test public void malformedPortableRowsAreRejected()throws Exception{for(String value:Arrays.asList("{\"@notes\":2}","{\"@notes\":true}","{\"@notes\":\"0\"}","{\"\":0}","{\"@notes\":0.5}")){try{TaskbarRows.parse(value);fail(value);}catch(JSONException expected){}}}
 @Test public void boundedPortableRowsRejectOversizedMaps()throws Exception{JSONObject rows=new JSONObject();for(int i=0;i<65;i++)rows.put("package"+i+"/.App",i%2);try{TaskbarRows.parse(rows.toString());fail();}catch(JSONException expected){}}
}
