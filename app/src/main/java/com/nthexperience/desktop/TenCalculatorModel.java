package com.nthexperience.desktop;

import org.json.*;
import java.util.*;

/** Four-function calculator state, including pending operations, repeat equals and memory. */
final class TenCalculatorModel {
    String entry="0",history="",pending="",last="",error="";double accumulator,operand,memory;boolean waiting,fresh;
    TenCalculatorModel(){}
    TenCalculatorModel(JSONObject data){
        JSONObject state=data.optJSONObject("ten_state");try{if(state!=null){validate(data);entry=state.getString("entry");history=state.optString("history");pending=state.optString("pending");last=state.optString("last");error=state.optString("error");accumulator=state.optDouble("accumulator");operand=state.optDouble("operand");memory=state.optDouble("memory");waiting=state.optBoolean("waiting");fresh=state.optBoolean("fresh");}else{String previous=data.optString("result","0");if(Double.isFinite(Double.parseDouble(previous)))entry=GadgetMath.number(Double.parseDouble(previous));}}catch(Exception invalid){entry="0";history=pending=last=error="";accumulator=operand=memory=0;waiting=fresh=false;}
    }
    String display(){return error.isEmpty()?entry:error;}
    void key(String key){try{
        if(key.equals("MC")){memory=0;return;}if(key.equals("C")){entry="0";history=pending=last=error="";accumulator=operand=0;waiting=fresh=false;return;}
        if(key.equals("MR")){entry=GadgetMath.number(memory);error="";waiting=fresh=false;return;}
        if(!error.isEmpty()){if(key.matches("[0-9.]")||key.equals("DEL")){entry="0";history=pending=last=error="";waiting=fresh=false;}else return;}
        if(key.equals("M+")||key.equals("M−")||key.equals("M-")){memory=finite(memory+(key.equals("M+")?1:-1)*number());return;}
        if(key.matches("[0-9.]")){if(waiting||fresh){entry="0";waiting=fresh=false;}if(pending.isEmpty())history="";last="";if(key.equals(".")){if(!entry.contains(".")&&!entry.toLowerCase(Locale.ROOT).contains("e"))entry+=".";}else if(entry.equals("0"))entry=key;else if(entry.equals("-0"))entry="-"+key;else if(entry.length()<18)entry+=key;return;}
        if(key.equals("DEL")){waiting=fresh=false;last="";history="";entry=entry.length()>1?entry.substring(0,entry.length()-1):"0";if(entry.equals("-")||entry.toLowerCase(Locale.ROOT).endsWith("e")||entry.endsWith("+"))entry="0";number();return;}
        if(key.equals("±")){entry=entry.startsWith("-")?entry.substring(1):entry.equals("0")?"0":"-"+entry;waiting=fresh=false;return;}
        if(key.equals("%")){entry=GadgetMath.number(number()/100);waiting=false;return;}
        String op=key.equals("×")?"*":key.equals("÷")?"/":key.equals("−")?"-":key;
        if(Arrays.asList("+","-","*","/").contains(op)){if(!pending.isEmpty()&&!waiting)entry=GadgetMath.number(calculate(accumulator,number(),pending));accumulator=number();pending=op;waiting=true;fresh=false;last="";history=entry+" "+symbol(op);return;}
        if(key.equals("=")){if(!pending.isEmpty()){double left=accumulator;operand=number();last=pending;entry=GadgetMath.number(calculate(left,operand,pending));history=GadgetMath.number(left)+" "+symbol(pending)+" "+GadgetMath.number(operand)+" =";pending="";}else if(fresh&&!last.isEmpty()){double left=number();entry=GadgetMath.number(calculate(left,operand,last));history=GadgetMath.number(left)+" "+symbol(last)+" "+GadgetMath.number(operand)+" =";}waiting=false;fresh=true;}
    }catch(IllegalArgumentException ex){error=ex.getMessage();pending=last="";waiting=false;fresh=true;entry="0";}}
    private double number(){try{return finite(Double.parseDouble(entry));}catch(NumberFormatException bad){throw new IllegalArgumentException("Check the number");}}
    private static double finite(double value){if(!Double.isFinite(value))throw new IllegalArgumentException("Number is too large");return value;}
    private static double calculate(double a,double b,String op){switch(op){case "+":return finite(a+b);case "-":return finite(a-b);case "*":return finite(a*b);case "/":if(b==0)throw new IllegalArgumentException("Cannot divide by zero");return finite(a/b);default:return b;}}
    private static String symbol(String op){return op.equals("*")?"×":op.equals("/")?"÷":op.equals("-")?"−":op;}
    JSONObject save(){JSONObject out=new JSONObject(),state=new JSONObject();try{state.put("entry",entry).put("history",history).put("pending",pending).put("last",last).put("error",error).put("accumulator",accumulator).put("operand",operand).put("memory",memory).put("waiting",waiting).put("fresh",fresh);out.put("ten_state",state).put("result",display()).put("expression",history);}catch(JSONException impossible){throw new IllegalStateException(impossible);}return out;}
    static void validate(JSONObject data)throws JSONException{JSONObject state=data.optJSONObject("ten_state");if(data.has("ten_state")&&state==null)throw new JSONException("Invalid calculator state");if(state==null)return;if(state.length()>10)throw new JSONException("Too many calculator fields");String entry=state.getString("entry");if(entry.length()>80||!entry.matches("-?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?"))throw new JSONException("Invalid calculator number");try{if(!Double.isFinite(Double.parseDouble(entry)))throw new JSONException("Invalid calculator number");}catch(NumberFormatException invalid){throw new JSONException("Invalid calculator number");}for(String key:new String[]{"accumulator","operand","memory"})if(!(state.get(key) instanceof Number)||!Double.isFinite(state.getDouble(key)))throw new JSONException("Invalid calculator value");for(String key:new String[]{"waiting","fresh"})if(!(state.get(key) instanceof Boolean))throw new JSONException("Invalid calculator flag");for(String key:new String[]{"pending","last"})if(!Arrays.asList("","+","-","*","/").contains(state.getString(key)))throw new JSONException("Invalid calculator operation");if(state.getString("history").length()>120||state.getString("error").length()>80)throw new JSONException("Calculator text too long");}
}
