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
    static int surface(Context c){android.content.SharedPreferences prefs=c.getSharedPreferences("desktop",0);if(light(c))return 0xfff2f2f2;if(TenThemes.aero(prefs))return mix(0xff102030,AeroGlass.TINTS[Math.max(0,Math.min(7,prefs.getInt("aero_color",0)))],.28f);if(!prefs.getBoolean("ten_accent_shell",false))return DARK;float amount=.64f;int fill=mix(DARK,accent(c),amount);while(TenAccent.contrast(Color.WHITE,fill)<5&&amount>0){amount=Math.max(0,amount-.02f);fill=mix(DARK,accent(c),amount);}return fill;}
    static int mix(int a,int b,float t){return Color.rgb(Math.round(Color.red(a)*(1-t)+Color.red(b)*t),Math.round(Color.green(a)*(1-t)+Color.green(b)*t),Math.round(Color.blue(a)*(1-t)+Color.blue(b)*t));}
    static int titleText(Context c,boolean active){android.content.SharedPreferences prefs=c.getSharedPreferences("desktop",0);if(active&&!TenThemes.aero(prefs)&&prefs.getBoolean("ten_accent_titles",false))return TenAccent.on(accent(c));if(!TenThemes.aero(prefs)&&TenAppearance.dark(c))return active?0xfff2f2f2:0xffbbbbbb;return active?0xff111b22:0xff465563;}
    static int text(Context c){return TenAccent.on(surface(c));}
    static StateListDrawable hover(Context c,int base){StateListDrawable d=new Aero.ThemeStates();int surface=Color.alpha(base)==0?surface(c):base;for(int s:new int[]{android.R.attr.state_pressed,android.R.attr.state_hovered,android.R.attr.state_focused})d.addState(new int[]{s},new ColorDrawable(TenAccent.on(surface)==0xff000000?0xffd6d6d6:0xff454545));d.addState(new int[]{},new ColorDrawable(base));return d;}
    static TextView command(Context c,String label,Runnable action){TextView v=Aero.text(c,label,13,text(c));v.setGravity(Gravity.CENTER);v.setFocusable(true);v.setMinHeight(Aero.dp(c,44));v.setBackground(hover(c,Color.TRANSPARENT));v.setContentDescription(label);v.setOnClickListener(w->action.run());return v;}
    static Glyph accentGlyph(Context c,String kind){return new Glyph(c,kind,accent(c),true);}
    static final class Glyph extends View {
        boolean appThemed;final boolean accentRole;final String kind;final Paint p=new Paint(3);final Path path=new Path();final int color;
        Glyph(Context c,String kind,int color){this(c,kind,color,false);}
        Glyph(Context c,String kind,int color,boolean accentRole){super(c);this.kind=kind;this.color=color;this.accentRole=accentRole;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);int save=canvas.save();float s=Math.min(getWidth()-getPaddingLeft()-getPaddingRight(),getHeight()-getPaddingTop()-getPaddingBottom())/32f;canvas.translate(getPaddingLeft()+(getWidth()-getPaddingLeft()-getPaddingRight()-32*s)/2,getPaddingTop()+(getHeight()-getPaddingTop()-getPaddingBottom()-32*s)/2);canvas.scale(s,s);p.setColor(appThemed?(accentRole?TenAppearance.accent(getContext()):TenAppearance.foreground(getContext(),color)):accentRole?TenAccent.readableOn(TenStyle.accent(getContext()),TenStyle.surface(getContext())):color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.6f);
            switch(kind){
                case "start":android.graphics.drawable.Drawable logo=getContext().getDrawable(R.drawable.ic_launcher);logo.setBounds(0,0,32,32);logo.draw(canvas);break;
                case "search":canvas.drawCircle(14,13,7,p);canvas.drawLine(19,19,26,26,p);break;
                case "taskview":canvas.drawRect(8,7,24,25,p);canvas.drawLine(3,11,3,21,p);canvas.drawLine(29,11,29,21,p);break;
                case "nota":canvas.drawRoundRect(5,5,27,25,2,2,p);canvas.drawLine(10,25,10,29,p);canvas.drawLine(10,29,16,25,p);canvas.drawLine(10,12,22,12,p);canvas.drawLine(10,18,18,18,p);break;
                case "actions":canvas.drawRect(6,6,26,24,p);canvas.drawLine(6,24,6,28,p);canvas.drawLine(6,28,12,24,p);canvas.drawLine(10,11,22,11,p);canvas.drawLine(10,16,22,16,p);break;
                case "news":canvas.drawRoundRect(3,5,29,27,2,2,p);canvas.drawRect(7,10,15,17,p);canvas.drawLine(19,10,25,10,p);canvas.drawLine(19,15,25,15,p);canvas.drawLine(7,22,25,22,p);break;
                case "menu":for(int y=9;y<=23;y+=7)canvas.drawLine(7,y,25,y,p);break;
                case "apps":for(int y=8;y<24;y+=10)for(int x=8;x<24;x+=10)canvas.drawRect(x,y,x+6,y+6,p);break;
                case "account":case "accounts":canvas.drawCircle(16,10,5,p);canvas.drawArc(6,16,26,31,180,180,false,p);break;
                case "document":case "clipboard":canvas.drawRect(8,5,24,27,p);for(int y=11;y<25;y+=5)canvas.drawLine(12,y,21,y,p);break;
                case "picture":case "personalization":canvas.drawRect(5,7,27,25,p);canvas.drawCircle(22,12,2,p);path.reset();path.moveTo(6,24);path.lineTo(13,14);path.lineTo(19,21);path.lineTo(23,17);path.lineTo(27,23);canvas.drawPath(path,p);break;
                case "sound":case "volume":path.reset();path.moveTo(4,12);path.lineTo(10,12);path.lineTo(16,7);path.lineTo(16,25);path.lineTo(10,20);path.lineTo(4,20);path.close();canvas.drawPath(path,p);canvas.drawArc(15,8,28,24,-65,130,false,p);canvas.drawArc(16,12,22,20,-65,130,false,p);break;
                case "power":canvas.drawArc(6,6,26,27,-60,300,false,p);canvas.drawLine(16,3,16,16,p);break;
                case "settings":canvas.drawCircle(16,16,7,p);canvas.drawCircle(16,16,2,p);for(int n=0;n<8;n++){double a=n*Math.PI/4;canvas.drawLine(16+8*(float)Math.cos(a),16+8*(float)Math.sin(a),16+12*(float)Math.cos(a),16+12*(float)Math.sin(a),p);}break;
                case "network":canvas.drawArc(3,7,29,33,220,100,false,p);canvas.drawArc(8,12,24,28,220,100,false,p);canvas.drawArc(13,17,19,23,220,100,false,p);break;
                case "bluetooth":case "devices":path.reset();path.moveTo(16,3);path.lineTo(16,29);path.lineTo(24,22);path.lineTo(8,10);path.moveTo(8,22);path.lineTo(24,10);path.lineTo(16,3);canvas.drawPath(path,p);break;
                case "focus":path.reset();path.moveTo(21,4);path.cubicTo(3,5,2,26,20,28);path.cubicTo(10,21,11,11,21,4);canvas.drawPath(path,p);break;
                case "sun":canvas.drawCircle(16,16,5,p);for(int n=0;n<8;n++){double a=n*Math.PI/4;canvas.drawLine(16+9*(float)Math.cos(a),16+9*(float)Math.sin(a),16+13*(float)Math.cos(a),16+13*(float)Math.sin(a),p);}break;
                case "mobile":case "tablet":canvas.drawRoundRect(8,3,24,29,2,2,p);canvas.drawLine(14,25,18,25,p);break;
                case "time":canvas.drawCircle(16,16,11,p);canvas.drawLine(16,7,16,16,p);canvas.drawLine(16,16,22,20,p);break;
                case "privacy":canvas.drawRect(7,14,25,28,p);canvas.drawArc(10,2,22,22,180,180,false,p);break;
                case "update":canvas.drawArc(5,5,27,27,25,285,false,p);canvas.drawLine(26,7,26,15,p);canvas.drawLine(26,15,19,12,p);break;
                case "gaming":canvas.drawRoundRect(3,8,29,25,5,5,p);canvas.drawLine(8,13,8,21,p);canvas.drawLine(4,17,12,17,p);canvas.drawCircle(22,14,1,p);canvas.drawCircle(25,19,1,p);break;
                case "accessibility":canvas.drawCircle(16,5,3,p);canvas.drawLine(4,11,28,11,p);canvas.drawLine(16,9,16,20,p);canvas.drawLine(16,20,8,29,p);canvas.drawLine(16,20,24,29,p);break;
                case "copy":canvas.drawRect(6,6,20,23,p);canvas.drawRect(12,11,26,28,p);break;
                case "cut":canvas.drawCircle(9,23,4,p);canvas.drawCircle(23,23,4,p);canvas.drawLine(11,20,24,4,p);canvas.drawLine(21,20,8,4,p);break;
                case "paste":canvas.drawRect(7,7,25,28,p);canvas.drawRect(12,4,20,10,p);break;
                case "folder":path.reset();path.moveTo(4,10);path.lineTo(13,10);path.lineTo(16,14);path.lineTo(28,14);path.lineTo(28,27);path.lineTo(4,27);path.close();canvas.drawPath(path,p);break;
                case "delete":canvas.drawRect(9,9,24,27,p);canvas.drawLine(6,7,27,7,p);canvas.drawLine(13,4,20,4,p);canvas.drawLine(14,13,14,23,p);canvas.drawLine(19,13,19,23,p);break;
                case "share":canvas.drawCircle(7,16,3,p);canvas.drawCircle(25,6,3,p);canvas.drawCircle(25,26,3,p);canvas.drawLine(10,14,22,7,p);canvas.drawLine(10,18,22,25,p);break;
                default:canvas.drawRect(6,7,26,23,p);canvas.drawLine(16,23,16,27,p);canvas.drawLine(11,27,21,27,p);
            }canvas.restoreToCount(save);
        }
    }
}
