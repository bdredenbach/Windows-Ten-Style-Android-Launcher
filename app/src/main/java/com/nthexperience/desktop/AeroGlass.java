package com.nthexperience.desktop;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.View;

/** Samples the softened wallpaper beneath the chrome, keeping foreground text sharp. */
final class AeroGlass extends Drawable {
    static final int WINDOW=0,START=1,TASKBAR=2,SIDEBAR=3,TASKBUTTON=4;
    private final AeroWallpaper wallpaper;
    private final int style;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final RectF rect=new RectF();
    private final Path clip=new Path();
    private boolean active=true;
    private boolean stacked,idle,hot,pressed,peek,focused;
    private int accent=0xff79c9f5;private float pointerX=-1,pointerY=-1;
    private int insetTop,progress=-2;
    private int drawableAlpha=255;
    static final int DEFAULT_OPACITY=85;
    private final android.content.SharedPreferences prefs;
    static final String[] COLOR_NAMES={"Frost","Graphite","Seafoam","Sage","Lavender","Gold","Ruby","Clear"};
    static final int[] TINTS={0xff599cbd,0xff58636e,0xff3e9ca0,0xff6d986c,0xff8b78b0,0xffb69a5c,0xffa56872,0xffb4cad7};
    AeroGlass(AeroWallpaper wallpaper,int style){this.wallpaper=wallpaper;this.style=style;prefs=wallpaper.getContext().getSharedPreferences("desktop",0);wallpaper.register(this);}
    void active(boolean value){if(active!=value){active=value;invalidateSelf();}}
    void stacked(boolean value){stacked=value;invalidateSelf();}
    void progress(int value){if(progress!=value){progress=value;invalidateSelf();}}
    void idle(boolean value){idle=value;invalidateSelf();}
    void accent(int color){accent=color;invalidateSelf();}
    void hotTrack(boolean value,float x,float y){hot=value;pointerX=x;pointerY=y;invalidateSelf();}
    void press(boolean value,float x,float y){pressed=value;pointerX=x;pointerY=y;invalidateSelf();}
    void peek(boolean value){peek=value;invalidateSelf();}
    void insetTop(int pixels){insetTop=pixels;invalidateSelf();}
    @Override public boolean isStateful(){return style==TASKBUTTON;}
    @Override protected boolean onStateChange(int[] state){
        boolean next=false;for(int value:state)if(value==android.R.attr.state_focused)next=true;
        if(next==focused)return false;focused=next;invalidateSelf();return true;
    }
    @Override public void draw(Canvas c){
        rect.set(getBounds());rect.top+=insetTop;if(rect.isEmpty())return;float d=getCallback() instanceof View?((View)getCallback()).getResources().getDisplayMetrics().density:1;
        boolean light=prefs.getBoolean("ten_light",false);int color=style==WINDOW?0xfff7f7f7:light?0xffededed:0xff202020;
        boolean over=hot||pressed||focused;
        if(style==TASKBUTTON){if(idle&&!over&&progress<-1)return;color=over?(light?0xffcccccc:0xff4d4d4d):active?(light?0xffd6d6d6:0xff3a3a3a):(light?0xffededed:0xff202020);}
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setAlpha(drawableAlpha);c.drawRect(rect,p);
        if(style==WINDOW){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d);p.setColor(active?prefs.getInt("ten_accent",TenStyle.ACCENT):0xffb7b7b7);c.drawRect(rect.left+d/2,rect.top+d/2,rect.right-d/2,rect.bottom-d/2,p);p.setStyle(Paint.Style.FILL);}
        if(style==TASKBUTTON&&!idle){p.setColor(prefs.getInt("ten_accent",TenStyle.ACCENT));c.drawRect(rect.left+(active?0:8*d),rect.bottom-2*d,rect.right-(active?0:8*d),rect.bottom,p);}
        if(style==TASKBUTTON&&stacked){p.setColor(light?0xff999999:0xff777777);c.drawRect(rect.right-3*d,rect.top+5*d,rect.right-d,rect.bottom-5*d,p);}
        if(style==TASKBUTTON&&progress>=-1){p.setColor(0xff60b947);float part=progress<0?1:Math.max(.04f,Math.min(1,progress/100f));c.drawRect(rect.left,rect.bottom-5*d,rect.left+rect.width()*part,rect.bottom,p);}
        if(style==WINDOW&&peek){p.setColor(0xfff7f7f7);p.setAlpha(120);c.drawRect(rect,p);}
    }
    private static int mix(int a,int b,float t){return Color.argb(Color.alpha(a),Math.round(Color.red(a)*(1-t)+Color.red(b)*t),Math.round(Color.green(a)*(1-t)+Color.green(b)*t),Math.round(Color.blue(a)*(1-t)+Color.blue(b)*t));}
    @Override public void setAlpha(int alpha){drawableAlpha=Math.max(0,Math.min(255,alpha));invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){p.setColorFilter(filter);}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
