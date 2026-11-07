package com.nthexperience.desktop;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Aero {
    static final int INK=0xff18232c;
    private static Typeface regular,semibold;private static String fontKey;
    static int dp(Context c,float value){return Math.round(value*c.getResources().getDisplayMetrics().density);}
    static void resetFont(){regular=null;semibold=null;fontKey=null;}
    static Typeface face(Context c){String selected=c.getSharedPreferences("desktop",0).getString(TenFonts.KEY,"selawik");if(regular==null||!selected.equals(fontKey)){fontKey=selected;regular=TenFonts.face(c,selected,false);semibold=null;}return regular;}
    static Typeface strong(Context c){face(c);if(semibold==null)semibold=TenFonts.face(c,c.getSharedPreferences("desktop",0).getString(TenFonts.KEY,"selawik"),true);return semibold;}
    static final class ThemePanel extends GradientDrawable {
        final Context context;final int[] colors;final float radius;final int stroke;boolean appThemed;boolean previousDark,previousAero,previousThemed;int previousAccent;boolean drawn;
        ThemePanel(Context c,int[] colors,float radius,int stroke){super(Orientation.TOP_BOTTOM,new int[]{colors[0],colors[0]});context=c;this.colors=colors.clone();this.radius=radius;this.stroke=stroke;}
        /** Update before rendering so retained hardware display lists are invalidated by palette changes. */
        void appearance(){boolean dark=appThemed&&TenAppearance.dark(context),aero=TenThemes.aero(context.getSharedPreferences("desktop",0));int accent=TenStyle.accent(context);if(!drawn||dark!=previousDark||aero!=previousAero||accent!=previousAccent||appThemed!=previousThemed){int[] mapped=aero?colors.clone():new int[]{colors[0],colors[0]};if(appThemed)for(int i=0;i<mapped.length;i++){int color=mapped[i];if(dark)mapped[i]=TenAppearance.surface(context,color);else if(color==0xffd9e8f8||color==0xffedf8ff||color==0xfff1f9ff||color==0xfff0faff)mapped[i]=TenStyle.mix(android.graphics.Color.WHITE,accent,.10f);}setColors(mapped);setCornerRadius(aero?dp(context,radius):0);if(stroke!=0)setStroke(dp(context,1),dark?TenAppearance.surface(context,stroke):stroke);previousDark=dark;previousAero=aero;previousAccent=accent;previousThemed=appThemed;drawn=true;}}
        @Override public void draw(android.graphics.Canvas canvas){appearance();super.draw(canvas);}
    }
    static final class ThemeStates extends StateListDrawable {final java.util.List<android.graphics.drawable.Drawable> children=new java.util.ArrayList<>();@Override public void addState(int[] states,android.graphics.drawable.Drawable drawable){children.add(drawable);super.addState(states,drawable);}}
    /** A preview or swatch is content: never flatten or recolor its supplied pixels. */
    static final class LiteralPanel extends GradientDrawable {
        LiteralPanel(int[] colors){super(GradientDrawable.Orientation.TOP_BOTTOM,colors);}
    }
    static GradientDrawable literal(Context c,int[] colors,float radius,int stroke){GradientDrawable drawable=new LiteralPanel(colors);drawable.setCornerRadius(dp(c,radius));if(stroke!=0)drawable.setStroke(dp(c,1),stroke);return drawable;}
    static GradientDrawable panel(Context c,int top,int bottom,int radius,int stroke){return new ThemePanel(c,new int[]{top,bottom},radius,stroke);}
    static GradientDrawable gloss(Context c,int[] colors,float radius,int stroke){return new ThemePanel(c,colors,radius,stroke);}
    static StateListDrawable selection(Context c){
        StateListDrawable states=new ThemeStates();
        states.addState(new int[]{android.R.attr.state_pressed},gloss(c,new int[]{0xfff0faff,0xffd6eeff,0xffb6defa,0xffd9f1ff},3,0xff83b4d2));
        states.addState(new int[]{android.R.attr.state_hovered},panel(c,0xfff1f9ff,0xffd9edfb,3,0xffb0cddd));
        states.addState(new int[]{android.R.attr.state_focused},panel(c,0xffedf8ff,0xffd4eaff,3,0xff97bcd3));
        states.addState(new int[]{},panel(c,0x00ffffff,0x00ffffff,3,0));return states;
    }
    static TextView text(Context c,String value,float size,int color){TextView v=new TextView(c);v.setText(value);v.setTextSize(size);v.setTypeface(face(c));v.setTextColor(color);v.setGravity(Gravity.CENTER_VERTICAL);v.setIncludeFontPadding(false);return v;}
    static TextView button(Context c,String label,Runnable click){
        TextView v=text(c,label,13,INK);v.setGravity(Gravity.CENTER);v.setMinHeight(dp(c,48));v.setPadding(dp(c,10),dp(c,5),dp(c,10),dp(c,5));
        v.setBackground(buttonBackground(c));v.setTextColor(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},new int[]{0xff7b858d,INK}));v.setFocusable(true);v.setOnClickListener(view->click.run());return v;
    }
    static StateListDrawable buttonBackground(Context c){StateListDrawable states=new ThemeStates();states.addState(new int[]{-android.R.attr.state_enabled},panel(c,0xffededed,0xffededed,0,0xffcccccc));states.addState(new int[]{android.R.attr.state_pressed},panel(c,0xffbcbcbc,0xffbcbcbc,0,0xff777777));for(int state:new int[]{android.R.attr.state_focused,android.R.attr.state_hovered})states.addState(new int[]{state},panel(c,0xffd9e8f8,0xffd9e8f8,0,0xff0078d7));states.addState(new int[]{},panel(c,0xffe1e1e1,0xffe1e1e1,0,0xffa5a5a5));return states;}
    static boolean motion(Context c){return !c.getSharedPreferences("desktop",0).getBoolean("reduce_motion",false)&&android.animation.ValueAnimator.areAnimatorsEnabled();}
    static android.widget.CheckBox check(Context c,String label,boolean checked){android.widget.CheckBox b=new android.widget.CheckBox(c);b.setText(label);b.setContentDescription(label);b.setTypeface(face(c));b.setTextSize(13);b.setTextColor(INK);b.setButtonDrawable(new AeroCheck(c,false));b.setPadding(dp(c,3),0,dp(c,4),0);b.setMinHeight(dp(c,48));b.setChecked(checked);return b;}
    static android.app.AlertDialog.Builder dialog(Context c){return new VistaDialog(c);}
    static void styleDialog(Context c,android.app.AlertDialog dialog){android.view.Window w=dialog.getWindow();if(w!=null){w.setBackgroundDrawable(panel(c,0xfff8fbfd,0xffe8eef3,5,0xff7e9aac));styleText(w.getDecorView());}for(int which:new int[]{-1,-2,-3}){android.widget.Button b=dialog.getButton(which);if(b!=null){b.setTypeface(face(c));b.setAllCaps(false);b.setTextColor(INK);b.setBackground(buttonBackground(c));}}TenAppearance.bind(w==null?dialog.getListView():w.getDecorView());}
    private static void styleText(View v){if(v instanceof TextView)((TextView)v).setTypeface(face(v.getContext()));if(v instanceof android.view.ViewGroup)for(int i=0;i<((android.view.ViewGroup)v).getChildCount();i++)styleText(((android.view.ViewGroup)v).getChildAt(i));}
    static LinearLayout column(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l;}
    static void bold(TextView v){v.setTypeface(strong(v.getContext()));}
    static void pad(View v,int value){int p=dp(v.getContext(),value);v.setPadding(p,p,p,p);}
    private Aero(){}
}
