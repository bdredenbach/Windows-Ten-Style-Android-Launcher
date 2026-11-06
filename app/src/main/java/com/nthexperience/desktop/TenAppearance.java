package com.nthexperience.desktop;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.view.*;
import android.webkit.WebView;
import android.widget.*;

/** Recolors native application surfaces in place, preserving documents, selection, media and web content. */
final class TenAppearance {
    static boolean dark(Context c){return c.getSharedPreferences("desktop",0).getBoolean("ten_apps_dark",false);}
    static int background(Context c){return dark(c)?0xff202020:0xfffafafa;}
    static int ink(Context c){return dark(c)?0xfff2f2f2:Aero.INK;}
    static int accent(Context c){int color=TenStyle.accent(c);if(!dark(c))return color;return luma(color)<135?TenStyle.mix(color,Color.WHITE,.35f):color;}
    static double luma(int color){return .2126*Color.red(color)+.7152*Color.green(color)+.0722*Color.blue(color);}
    private static boolean neutral(int color){int max=Math.max(Color.red(color),Math.max(Color.green(color),Color.blue(color))),min=Math.min(Color.red(color),Math.min(Color.green(color),Color.blue(color)));return max-min<46;}
    static int foreground(Context c,int color){if(color==0xff0078d7||color==0xff0066aa||color==0xff1673a5)return accent(c);if(!dark(c)||Color.alpha(color)==0)return color;int a=color&0xff000000;if(neutral(color)){double l=luma(color);return a|(l>200?0x00f2f2f2:l>90?0x00cccccc:0x00f2f2f2);}if(Color.blue(color)>Color.red(color)*1.3&&Color.blue(color)>Color.green(color)*1.1)return a|(accent(c)&0xffffff);return color;}
    static int surface(Context c,int color){if(!dark(c)||Color.alpha(color)==0)return color;int a=color&0xff000000;if(neutral(color)){double l=luma(color);if(l>245)return a|0x001e1e1e;if(l>225)return a|0x00262626;if(l>195)return a|0x00333333;if(l>140)return a|0x00505050;return color;}if(luma(color)>175&&Color.blue(color)>=Color.red(color))return a|(TenStyle.mix(0xff272727,TenStyle.accent(c),.22f)&0xffffff);return color;}
    private static final java.util.Map<Drawable,Integer> originalColors=new java.util.WeakHashMap<>();
    static void context(Context c){c.getTheme().applyStyle(dark(c)?R.style.TenDarkControls:R.style.TenLightControls,true);}
    static void preserve(View view){view.setTag(R.id.ten_appearance_preserve,Boolean.TRUE);}
    static void bind(View root){if(root==null)return;if(root.getTag(R.id.ten_appearance_binding)==null){Binding binding=new Binding(root);root.setTag(R.id.ten_appearance_binding,binding);root.addOnAttachStateChangeListener(binding);if(root.isAttachedToWindow())binding.onViewAttachedToWindow(root);}apply(root);}
    private static final class Binding implements View.OnAttachStateChangeListener,ViewTreeObserver.OnGlobalLayoutListener {
        final View root;ViewTreeObserver observer;Binding(View root){this.root=root;}
        public void onViewAttachedToWindow(View view){observer=view.getViewTreeObserver();observer.addOnGlobalLayoutListener(this);apply(root);}
        public void onViewDetachedFromWindow(View view){if(observer!=null&&observer.isAlive())observer.removeOnGlobalLayoutListener(this);observer=null;}
        public void onGlobalLayout(){apply(root);}
    }
    private static final class Original {Drawable source,applied;ColorStateList text,appliedText,hint,appliedHint;}
    static void apply(View view){
        if(Boolean.TRUE.equals(view.getTag(R.id.ten_appearance_preserve))||view instanceof WebView||view instanceof ImageView||view instanceof SurfaceView||view instanceof TextureView)return;
        Context c=view.getContext();Original original=(Original)view.getTag(R.id.ten_appearance_original);if(original==null){original=new Original();view.setTag(R.id.ten_appearance_original,original);}
        Drawable current=view.getBackground();if(current!=original.applied){original.source=current;original.applied=current;}
        if(original.source instanceof ColorDrawable){int color=((ColorDrawable)original.source).getColor(),mapped=surface(c,color);Drawable next=dark(c)&&mapped!=color?new ColorDrawable(mapped):original.source;if(view.getBackground()!=next&&!(view.getBackground() instanceof ColorDrawable&&((ColorDrawable)view.getBackground()).getColor()==mapped)){view.setBackground(next);}original.applied=view.getBackground();}
        else enable(c,original.source);
        if(view instanceof TextView){TextView text=(TextView)view;if(text.getTextColors()!=original.appliedText)original.text=text.getTextColors();if(text.getHintTextColors()!=original.appliedHint)original.hint=text.getHintTextColors();ColorStateList colors=mapped(c,original.text),hint=mapped(c,original.hint);if(!colors.equals(text.getTextColors()))text.setTextColor(colors);if(hint!=null&&!hint.equals(text.getHintTextColors()))text.setHintTextColor(hint);text.setHighlightColor(TenStyle.mix(background(c),accent(c),.35f));original.appliedText=text.getTextColors();original.appliedHint=text.getHintTextColors();
            if(view instanceof EditText){if(current!=null&&!(current instanceof Aero.ThemePanel)&&!(current instanceof ColorDrawable))view.setBackgroundTintList(ColorStateList.valueOf(dark(c)?0xffaaaaaa:0xff777777));if(android.os.Build.VERSION.SDK_INT>=29){Drawable cursor=text.getTextCursorDrawable();if(cursor!=null)cursor.setTint(accent(c));}}
            if(view instanceof CompoundButton){CompoundButton button=(CompoundButton)view;enable(c,button.getButtonDrawable());}
            if(view instanceof Switch){Switch toggle=(Switch)view;toggle.setThumbTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{accent(c),dark(c)?0xffaaaaaa:0xffeeeeee}));toggle.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{TenStyle.mix(accent(c),background(c),.5f),dark(c)?0xff555555:0xffbbbbbb}));}
        }
        if(view instanceof SeekBar){SeekBar seek=(SeekBar)view;seek.setProgressTintList(ColorStateList.valueOf(accent(c)));seek.setThumbTintList(ColorStateList.valueOf(accent(c)));}
        if(view instanceof TenStyle.Glyph){((TenStyle.Glyph)view).appThemed=true;view.invalidate();}
        if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)apply(group.getChildAt(i));}
        view.invalidate();
    }
    private static ColorStateList mapped(Context c,ColorStateList original){if(original==null)return null;int normal=foreground(c,original.getDefaultColor()),disabled=foreground(c,original.getColorForState(new int[]{-android.R.attr.state_enabled},original.getDefaultColor()));return new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},new int[]{disabled,normal});}
    static void enable(Context c,Drawable drawable){if(drawable instanceof ColorDrawable){int color=originalColors.computeIfAbsent(drawable,key->((ColorDrawable)key).getColor());((ColorDrawable)drawable).setColor(surface(c,color));}else if(drawable instanceof Aero.ThemePanel)((Aero.ThemePanel)drawable).appThemed=true;else if(drawable instanceof Aero.ThemeStates)for(Drawable child:((Aero.ThemeStates)drawable).children)enable(c,child);else if(drawable instanceof AeroCheck)((AeroCheck)drawable).appThemed=true;else if(drawable instanceof LayerDrawable){LayerDrawable layers=(LayerDrawable)drawable;for(int i=0;i<layers.getNumberOfLayers();i++)enable(c,layers.getDrawable(i));}}
    private TenAppearance(){}
}
