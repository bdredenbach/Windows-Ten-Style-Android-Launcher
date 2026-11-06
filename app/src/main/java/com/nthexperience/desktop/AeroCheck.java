package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Crisp Vista-era check and radio controls, with native accessibility semantics. */
final class AeroCheck extends Drawable {
    boolean appThemed;private final Context context;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private final boolean radio;
    private boolean previousDark;private int previousAccent;
    void appearance(){boolean dark=TenAppearance.dark(context);int accent=TenAppearance.accent(context);if(!appThemed||dark!=previousDark||accent!=previousAccent){appThemed=true;previousDark=dark;previousAccent=accent;invalidateSelf();}}
    AeroCheck(Context c,boolean radio){context=c;density=c.getResources().getDisplayMetrics().density;this.radio=radio;}
    @Override public int getIntrinsicWidth(){return Math.round(23*density);}
    @Override public int getIntrinsicHeight(){return Math.round(20*density);}
    @Override public boolean isStateful(){return true;}
    @Override protected boolean onStateChange(int[] state){invalidateSelf();return true;}
    @Override public void draw(Canvas canvas){boolean checked=false,enabled=false,pressed=false;for(int s:getState()){checked|=s==android.R.attr.state_checked;enabled|=s==android.R.attr.state_enabled;pressed|=s==android.R.attr.state_pressed;}boolean dark=appThemed&&TenAppearance.dark(context);Rect b=getBounds();float size=15*density,x=b.left+2*density,y=b.exactCenterY()-size/2;RectF box=new RectF(x,y,x+size,y+size);paint.setStyle(Paint.Style.FILL);paint.setShader(new LinearGradient(0,y,0,y+size,new int[]{dark?(pressed?0xff555555:0xff444444):(pressed?0xffb8dcf3:0xffcbd6de),dark?0xff292929:0xfffaffff},null,Shader.TileMode.CLAMP));if(radio)canvas.drawOval(box,paint);else canvas.drawRect(box,paint);paint.setShader(null);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(density);paint.setColor(dark?0xffaaaaaa:enabled?0xff536e83:0xffa4adb4);if(radio)canvas.drawOval(box,paint);else canvas.drawRect(box,paint);paint.setStyle(Paint.Style.FILL);if(checked){paint.setColor(enabled?TenAppearance.accent(context):0xff9ba9b3);if(radio)canvas.drawCircle(box.centerX(),box.centerY(),4*density,paint);else{Path tick=new Path();tick.moveTo(x+3*density,y+7*density);tick.lineTo(x+6*density,y+11*density);tick.lineTo(x+12*density,y+3*density);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.3f*density);paint.setStrokeCap(Paint.Cap.ROUND);canvas.drawPath(tick,paint);paint.setStyle(Paint.Style.FILL);}}}
    @Override public void setAlpha(int alpha){paint.setAlpha(alpha);}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
