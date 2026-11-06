package com.nthexperience.desktop;
import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TenCalculatorTest {
    private void keys(TenCalculatorModel model,String...keys){for(String key:keys)model.key(key);}
    @Test public void decimalDeleteAndRepeatedEquals(){TenCalculatorModel m=new TenCalculatorModel();keys(m,"1","2","3","DEL",".","5","+","7",".","5","=");assertEquals("20",m.display());m.key("=");assertEquals("27.5",m.display());m.key("9");assertEquals("9",m.display());}
    @Test public void sequentialOperationsAndReplacement(){TenCalculatorModel m=new TenCalculatorModel();keys(m,"2","+","3","×","4","=");assertEquals("20",m.display());keys(m,"C","2","+","×","3","=");assertEquals("6",m.display());}
    @Test public void memorySurvivesClearAndSupportsSubtractRecallAndReset(){TenCalculatorModel m=new TenCalculatorModel();keys(m,"1","2","M+","C","3","M−","MR");assertEquals("9",m.display());keys(m,"C","MR");assertEquals("9",m.display());keys(m,"MC","MR");assertEquals("0",m.display());}
    @Test public void errorsRecoverWithoutLosingMemory(){TenCalculatorModel m=new TenCalculatorModel();keys(m,"8","M+","C","2","÷","0","=");assertEquals("Cannot divide by zero",m.display());m.key("7");assertEquals("7",m.display());m.key("MR");assertEquals("8",m.display());}
    @Test public void pendingOperationsAndRepeatStateRestore()throws Exception{TenCalculatorModel m=new TenCalculatorModel();keys(m,"6","×");JSONObject saved=new JSONObject(m.save().toString());TenCalculatorModel.validate(saved);m=new TenCalculatorModel(saved);keys(m,"5","=");assertEquals("30",m.display());m=new TenCalculatorModel(new JSONObject(m.save().toString()));m.key("=");assertEquals("150",m.display());}
    @Test public void legacyResultMigratesAndInvalidStateIsRejected()throws Exception{TenCalculatorModel m=new TenCalculatorModel(new JSONObject().put("result","42"));assertEquals("42",m.display());JSONObject bad=m.save();bad.getJSONObject("ten_state").put("pending","intent://anything");try{TenCalculatorModel.validate(bad);fail("Invalid operation accepted");}catch(JSONException expected){}assertEquals("0",new TenCalculatorModel(bad).display());}
}
