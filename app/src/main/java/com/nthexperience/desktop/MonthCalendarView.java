package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** A compact, locale-aware month grid in the desktop's native visual style. */
final class MonthCalendarView extends View {
    private boolean shell;
    void shellStyle(){shell=true;invalidate();}
    interface Changed{void changed();}
    private final Calendar month=Calendar.getInstance(),selected=Calendar.getInstance();
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Changed listener;
    MonthCalendarView(Context c,Changed listener){super(c);this.listener=listener;month.set(Calendar.DAY_OF_MONTH,1);setFocusable(true);updateDescription();}
    String title(){return new SimpleDateFormat("MMMM yyyy",Locale.getDefault()).format(month.getTime());}
    long monthTime(){return month.getTimeInMillis();}
    void restore(long monthTime,long selectedTime){month.setTimeInMillis(monthTime);selected.setTimeInMillis(selectedTime);updateDescription();invalidate();listener.changed();}
    Date selected(){return selected.getTime();}
    void offset(int direction){month.add(Calendar.MONTH,direction);updateDescription();invalidate();listener.changed();}
    void today(){Calendar now=Calendar.getInstance();month.setTime(now.getTime());month.set(Calendar.DAY_OF_MONTH,1);selected.setTime(now.getTime());updateDescription();invalidate();listener.changed();}
    private int offset(){return Math.floorMod(month.get(Calendar.DAY_OF_WEEK)-month.getFirstDayOfWeek(),7);}
    @Override protected void onDraw(Canvas c){
        float density=getResources().getDisplayMetrics().density,w=getWidth()/7f,top=34*density,row=(getHeight()-top)/6f;Calendar today=Calendar.getInstance();boolean dark=!shell&&TenAppearance.dark(getContext());
        p.setTypeface(Aero.face(getContext()));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(12*density);
        String[] names=new java.text.DateFormatSymbols(Locale.getDefault()).getShortWeekdays();
        for(int col=0;col<7;col++){int day=(month.getFirstDayOfWeek()-1+col)%7+1;p.setColor(shell?TenStyle.text(getContext()):TenAppearance.foreground(getContext(),0xff657382));c.drawText(names[day],w*(col+.5f),22*density,p);}
        p.setColor(dark?0xff495057:0xffd4dce2);p.setStrokeWidth(density);c.drawLine(0,top-density,getWidth(),top-density,p);
        for(int day=1;day<=month.getActualMaximum(Calendar.DAY_OF_MONTH);day++){
            int index=offset()+day-1,col=index%7,line=index/7;float x=col*w,y=top+line*row;
            boolean picked=selected.get(Calendar.YEAR)==month.get(Calendar.YEAR)&&selected.get(Calendar.MONTH)==month.get(Calendar.MONTH)&&selected.get(Calendar.DAY_OF_MONTH)==day;
            boolean current=today.get(Calendar.YEAR)==month.get(Calendar.YEAR)&&today.get(Calendar.MONTH)==month.get(Calendar.MONTH)&&today.get(Calendar.DAY_OF_MONTH)==day;
            int selectedFill=TenStyle.mix(TenAppearance.background(getContext()),TenStyle.accent(getContext()),.30f);
            if(picked){p.setShader(new LinearGradient(0,y,0,y+row,shell?TenStyle.accent(getContext()):dark?selectedFill:0xffe8f6ff,shell?TenStyle.accent(getContext()):dark?selectedFill:0xffb6d9f1,Shader.TileMode.CLAMP));c.drawRoundRect(x+3*density,y+3*density,x+w-3*density,y+row-3*density,3*density,3*density,p);p.setShader(null);}
            if(current){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(density);p.setColor(shell?TenStyle.accent(getContext()):dark?TenAppearance.accent(getContext()):0xff9b4935);c.drawRoundRect(x+4*density,y+4*density,x+w-4*density,y+row-4*density,3*density,3*density,p);p.setStyle(Paint.Style.FILL);}
            p.setColor(shell?(picked?TenAccent.on(TenStyle.accent(getContext())):TenStyle.text(getContext())):dark&&picked?TenAccent.on(selectedFill):TenAppearance.foreground(getContext(),0xff263c4f));p.setTypeface(current?Aero.strong(getContext()):Aero.face(getContext()));p.setTextSize(14*density);c.drawText(String.valueOf(day),x+w/2,y+row/2+5*density,p);
        }
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        if(event.getActionMasked()==MotionEvent.ACTION_DOWN)return true;
        if(event.getActionMasked()==MotionEvent.ACTION_UP){float top=Aero.dp(getContext(),34),row=(getHeight()-top)/6f;if(event.getY()>=top&&row>0){int col=Math.min(6,Math.max(0,(int)(event.getX()/(getWidth()/7f)))),line=(int)((event.getY()-top)/row),day=line*7+col-offset()+1;if(day>=1&&day<=month.getActualMaximum(Calendar.DAY_OF_MONTH)){selected.setTime(month.getTime());selected.set(Calendar.DAY_OF_MONTH,day);updateDescription();invalidate();listener.changed();}}performClick();return true;}return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    private void updateDescription(){setContentDescription("Calendar, "+title()+". Selected "+new SimpleDateFormat("EEEE, MMMM d, yyyy",Locale.getDefault()).format(selected.getTime()));}
}
