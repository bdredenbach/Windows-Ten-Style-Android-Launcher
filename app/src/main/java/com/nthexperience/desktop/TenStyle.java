package com.nthexperience.desktop;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.TextView;

/** Shared flat surfaces and original Nth line artwork. */
final class TenStyle {
    static final int ACCENT=0xff0078d7,DARK=0xff202020;
    static int accent(Context c){return c.getSharedPreferences("desktop",0).getInt("ten_accent",ACCENT);}
    static boolean light(Context c){return c.getSharedPreferences("desktop",0).getBoolean("ten_light",false);}
    static int surface(Context c){return light(c)?0xfff2f2f2:DARK;}
    static int text(Context c){return light(c)?0xff171717:Color.WHITE;}
    static StateListDrawable hover(Context c,int base){StateListDrawable d=new StateListDrawable();for(int s:new int[]{android.R.attr.state_pressed,android.R.attr.state_hovered,android.R.attr.state_focused})d.addState(new int[]{s},new ColorDrawable(light(c)?0xffd6d6d6:0xff454545));d.addState(new int[]{},new ColorDrawable(base));return d;}
    static TextView command(Context c,String label,Runnable action){TextView v=Aero.text(c,label,13,text(c));v.setGravity(Gravity.CENTER);v.setFocusable(true);v.setMinHeight(Aero.dp(c,44));v.setBackground(hover(c,Color.TRANSPARENT));v.setContentDescription(label);v.setOnClickListener(w->action.run());return v;}
    static final class Glyph extends View {
        final String kind;final Paint p=new Paint(3);final Path path=new Path();final int color;
        Glyph(Context c,String kind,int color){super(c);this.kind=kind;this.color=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);int save=canvas.save();float s=Math.min(getWidth(),getHeight())/32f;canvas.translate((getWidth()-32*s)/2,(getHeight()-32*s)/2);canvas.scale(s,s);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.6f);
            switch(kind){
                case "start":p.setStyle(Paint.Style.FILL);path.reset();path.moveTo(7,25);path.lineTo(7,7);path.lineTo(11,7);path.lineTo(21,21);path.lineTo(21,7);path.lineTo(25,7);path.lineTo(25,25);path.lineTo(21,25);path.lineTo(11,11);path.lineTo(11,25);path.close();canvas.drawPath(path,p);break;
                case "search":canvas.drawCircle(14,13,7,p);canvas.drawLine(19,19,26,26,p);break;
                case "taskview":canvas.drawRect(8,7,24,25,p);canvas.drawLine(3,11,3,21,p);canvas.drawLine(29,11,29,21,p);break;
                case "nota":canvas.drawRoundRect(5,5,27,25,2,2,p);canvas.drawLine(10,25,10,29,p);canvas.drawLine(10,29,16,25,p);canvas.drawLine(10,12,22,12,p);canvas.drawLine(10,18,18,18,p);break;
                case "actions":canvas.drawRect(6,6,26,24,p);canvas.drawLine(6,24,6,28,p);canvas.drawLine(6,28,12,24,p);canvas.drawLine(10,11,22,11,p);canvas.drawLine(10,16,22,16,p);break;
                default:canvas.drawRect(6,7,26,23,p);canvas.drawLine(16,23,16,27,p);canvas.drawLine(11,27,21,27,p);
            }canvas.restoreToCount(save);
        }
    }
}
