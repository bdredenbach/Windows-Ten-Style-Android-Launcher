package com.nthexperience.desktop;

// Adapted from Nth Desktop 3D b7926709ae2584527933f5246bbd5fdb499cadcf.

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;

/** Ten reading surfaces and the shared N/spark mark. */
final class NotaDesign {
    static final int CANVAS=0xfffafafa, SURFACE=0xffeeeeee, TEXT=0xff202020, MUTED=0xff555555, LINE=0xffcccccc;
    static GradientDrawable surface(Context c,int color){
        GradientDrawable fill=new GradientDrawable();
        fill.setColor(color);

        fill.setStroke(Aero.dp(c,1),LINE);
        return fill;
    }
    static Button action(Context c,String label,Runnable run){
        Button button = new Button(c, null, android.R.attr.borderlessButtonStyle);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(MUTED);
        button.setTextSize(14);
        button.setPadding(Aero.dp(c,8),0,Aero.dp(c,8),0);
        button.setMinimumWidth(0);
        button.setMinWidth(0);
        button.setMinHeight(Aero.dp(c,44));
        button.setOnClickListener(v -> run.run());
        return button;
    }
    static final class Mark extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        Mark(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int save=canvas.save();
            float scale=Math.min(getWidth()-getPaddingLeft()-getPaddingRight(),getHeight()-getPaddingTop()-getPaddingBottom())/64f;
            canvas.translate((getWidth()-64*scale)/2,(getHeight()-64*scale)/2);
            canvas.scale(scale,scale);
            drawMark(canvas,paint);
            canvas.restoreToCount(save);
        }
    }
    static void drawMark(Canvas canvas, Paint paint) {
            paint.setStyle(Paint.Style.FILL);paint.setShader(new LinearGradient(4,4,60,60,new int[]{0xFF7462AB,0xFF333B56,0xFF426A78},null,Shader.TileMode.CLAMP));canvas.drawRoundRect(2,2,62,62,19,19,paint);paint.setShader(null);
            paint.setColor(0x66DDD2FF);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1);canvas.drawRoundRect(2.5f,2.5f,61.5f,61.5f,19,19,paint);
            paint.setColor(0xfff3f2f7);paint.setStrokeWidth(3);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);Path n=new Path();n.moveTo(20,43);n.lineTo(20,22);n.lineTo(39,43);n.lineTo(39,22);canvas.drawPath(n,paint);paint.setColor(0xFFB6F1ED);paint.setStrokeWidth(2);canvas.drawLine(45,16,45,24,paint);canvas.drawLine(41,20,49,20,paint);
    }
}
