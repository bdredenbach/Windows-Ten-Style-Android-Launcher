package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Popup rows are separate windows: paint them before their first frame, including recycled rows. */
final class TenChoiceAdapter extends ArrayAdapter<String> {
    private final boolean suggestions;
    private TenChoiceAdapter(Context c,List<String> values,boolean suggestions){
        super(c,suggestions?android.R.layout.simple_dropdown_item_1line:android.R.layout.simple_spinner_item,values);
        this.suggestions=suggestions;setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
    static void attach(Spinner spinner,String[] values){
        spinner.setAdapter(new TenChoiceAdapter(spinner.getContext(),Arrays.asList(values),false));
        spinner.setPopupBackgroundDrawable(popupBackground(spinner.getContext()));
    }
    static void attach(AutoCompleteTextView entry,List<String> values){
        entry.setAdapter(new TenChoiceAdapter(entry.getContext(),values,true));
        entry.setDropDownBackgroundDrawable(popupBackground(entry.getContext()));
    }
    private static Drawable popupBackground(Context c){Drawable background=Aero.panel(c,0xfffafafa,0xfff0f0f0,1,0xff777777);TenAppearance.enable(c,background);return background;}
    @Override public View getView(int position,View recycled,ViewGroup parent){return paint(super.getView(position,recycled,parent),suggestions);}
    @Override public View getDropDownView(int position,View recycled,ViewGroup parent){return paint(super.getDropDownView(position,recycled,parent),true);}
    private View paint(View view,boolean popup){
        Context c=getContext();TextView row=(TextView)view;row.setTypeface(Aero.face(c));row.setTextSize(14);row.setTextColor(TenAppearance.ink(c));row.setSingleLine(true);row.setEllipsize(TextUtils.TruncateAt.END);
        row.setPadding(Aero.dp(c,popup?12:8),Aero.dp(c,popup?10:3),Aero.dp(c,12),Aero.dp(c,popup?10:3));row.setMinHeight(popup?Aero.dp(c,44):0);
        if(popup){
            Aero.ThemeStates states=new Aero.ThemeStates();for(int state:new int[]{android.R.attr.state_pressed,android.R.attr.state_hovered,android.R.attr.state_focused,android.R.attr.state_checked})states.addState(new int[]{state},Aero.panel(c,0xffd9e8f8,0xffd9e8f8,0,0));states.addState(new int[]{},Aero.panel(c,0xfffafafa,0xfffafafa,0,0));row.setBackground(states);
            if(row instanceof CheckedTextView){AeroCheck mark=new AeroCheck(c,true);mark.appearance();((CheckedTextView)row).setCheckMarkDrawable(mark);}
        }
        TenAppearance.bind(row);return row;
    }
}
