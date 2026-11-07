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
    static int accent(Context c){return TenAccent.readable(TenStyle.accent(c),dark(c));}
    static double contrast(int a,int b){return TenAccent.contrast(a,b);}
    static void accentText(TextView text){text.setTag(R.id.ten_appearance_accent,Boolean.TRUE);}
    static double luma(int color){return .2126*Color.red(color)+.7152*Color.green(color)+.0722*Color.blue(color);}
    private static boolean neutral(int color){int max=Math.max(Color.red(color),Math.max(Color.green(color),Color.blue(color))),min=Math.min(Color.red(color),Math.min(Color.green(color),Color.blue(color)));return max-min<46;}
    static int foreground(Context c,int color){if(!dark(c)||Color.alpha(color)==0)return color;int a=color&0xff000000;if(neutral(color)){double l=luma(color);return a|(l>200?0x00f2f2f2:l>90?0x00cccccc:0x00f2f2f2);}if(contrast(color,0xff333333)<4.5)return a|(TenAccent.readable(color,true)&0xffffff);return color;}
    static int surface(Context c,int color){if(!dark(c)||Color.alpha(color)==0)return color;int a=color&0xff000000;if(neutral(color)){double l=luma(color);if(l>245)return a|0x001e1e1e;if(l>225)return a|0x00262626;if(l>195)return a|0x00333333;if(l>140)return a|0x00505050;return color;}if(luma(color)>175&&Color.blue(color)>=Color.red(color))return a|(TenStyle.mix(0xff272727,TenStyle.accent(c),.22f)&0xffffff);return color;}
    private static final java.util.Map<Drawable,Integer> originalColors=new java.util.WeakHashMap<>();
    private static final java.util.Map<GradientDrawable,GradientColors> gradientColors=new java.util.WeakHashMap<>();
    private static final class GradientColors {
        int[] source,applied;
        GradientColors(int[] colors){source=colors.clone();applied=colors.clone();}
    }
    static void context(Context c){c.getTheme().applyStyle(dark(c)?R.style.TenDarkControls:R.style.TenLightControls,true);}
    static void preserve(View view){view.setTag(R.id.ten_appearance_preserve,Boolean.TRUE);}
    static void bind(View root){if(root==null)return;if(root.getTag(R.id.ten_appearance_binding)==null){Binding binding=new Binding(root);root.setTag(R.id.ten_appearance_binding,binding);root.addOnAttachStateChangeListener(binding);if(root.isAttachedToWindow())binding.onViewAttachedToWindow(root);}apply(root);}
    private static final class Binding implements View.OnAttachStateChangeListener,ViewTreeObserver.OnPreDrawListener {
        final View root;ViewTreeObserver observer;Binding(View root){this.root=root;}
        public void onViewAttachedToWindow(View view){observer=view.getViewTreeObserver();observer.addOnPreDrawListener(this);apply(root);}
        public void onViewDetachedFromWindow(View view){if(observer!=null&&observer.isAlive())observer.removeOnPreDrawListener(this);observer=null;}
        public boolean onPreDraw(){apply(root);return true;}
    }
    private static final class Original {Drawable source,applied;ColorStateList text,appliedText,hint,appliedHint;boolean mapped,wasDark,wasAccent,controls;int accent,controlAccent,surfaceKey;boolean controlDark;}
    static void apply(View view){
        if(Boolean.TRUE.equals(view.getTag(R.id.ten_appearance_preserve))||view instanceof WebView||view instanceof ImageView||view instanceof SurfaceView||view instanceof TextureView)return;
        Context c=view.getContext();Original original=(Original)view.getTag(R.id.ten_appearance_original);if(original==null){original=new Original();view.setTag(R.id.ten_appearance_original,original);}
        Drawable current=view.getBackground();if(current!=original.applied){original.source=current;original.applied=current;}
        if(original.source instanceof ColorDrawable){int color=((ColorDrawable)original.source).getColor(),mapped=surface(c,color);Drawable next=dark(c)&&mapped!=color?new ColorDrawable(mapped):original.source;if(view.getBackground()!=next&&!(view.getBackground() instanceof ColorDrawable&&((ColorDrawable)view.getBackground()).getColor()==mapped)){view.setBackground(next);}original.applied=view.getBackground();}
        else enable(c,original.source);
        if(view instanceof TextView){TextView text=(TextView)view;boolean sourceChanged=text.getTextColors()!=original.appliedText,hintChanged=text.getHintTextColors()!=original.appliedHint,role=Boolean.TRUE.equals(text.getTag(R.id.ten_appearance_accent));if(sourceChanged)original.text=text.getTextColors();if(hintChanged)original.hint=text.getHintTextColors();int currentAccent=TenStyle.accent(c);int[] surfaces=accentSurfaces(view);int surfaceKey=java.util.Arrays.hashCode(surfaces);if(!original.mapped||sourceChanged||hintChanged||original.wasDark!=dark(c)||original.accent!=currentAccent||original.wasAccent!=role||original.surfaceKey!=surfaceKey){ColorStateList colors=mapped(c,original.text,role,surfaces),hint=view instanceof EditText?mappedHint(c,original.hint,surfaces):mapped(c,original.hint,false,null);if(!sourceChanged)colors=retainedPalette(colors,original.appliedText);if(!hintChanged)hint=retainedPalette(hint,original.appliedHint);if(text.getTextColors()!=colors)text.setTextColor(colors);if(hint!=null&&text.getHintTextColors()!=hint)text.setHintTextColor(hint);text.setHighlightColor(TenStyle.mix(background(c),accent(c),.35f));original.appliedText=text.getTextColors();original.appliedHint=text.getHintTextColors();original.mapped=true;original.wasDark=dark(c);original.accent=currentAccent;original.wasAccent=role;original.surfaceKey=surfaceKey;}
            if(view instanceof EditText&&(!original.controls||original.controlAccent!=currentAccent||original.controlDark!=dark(c))){if(current!=null&&!(current instanceof GradientDrawable)&&!(current instanceof ColorDrawable))view.setBackgroundTintList(ColorStateList.valueOf(dark(c)?0xffaaaaaa:0xff777777));if(android.os.Build.VERSION.SDK_INT>=29&&(!original.controls||original.controlAccent!=currentAccent||original.controlDark!=dark(c))){Drawable cursor=text.getTextCursorDrawable();if(cursor!=null)cursor.setTint(accent(c));}}
            if(view instanceof CompoundButton){CompoundButton button=(CompoundButton)view;enable(c,button.getButtonDrawable());}
            if(view instanceof Switch&&(!original.controls||original.controlAccent!=currentAccent||original.controlDark!=dark(c))){Switch toggle=(Switch)view;toggle.setThumbTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{accent(c),dark(c)?0xffaaaaaa:0xffeeeeee}));toggle.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{TenStyle.mix(accent(c),background(c),.5f),dark(c)?0xff555555:0xffbbbbbb}));}
        }
        if(view instanceof SeekBar&&(!original.controls||original.controlAccent!=TenStyle.accent(c)||original.controlDark!=dark(c))){SeekBar seek=(SeekBar)view;seek.setProgressTintList(ColorStateList.valueOf(accent(c)));seek.setThumbTintList(ColorStateList.valueOf(accent(c)));}
        if(view instanceof TenStyle.Glyph){TenStyle.Glyph glyph=(TenStyle.Glyph)view;if(!glyph.appThemed||original.controlAccent!=TenStyle.accent(c)||original.controlDark!=dark(c)){glyph.appThemed=true;view.invalidate();}}
        if(view instanceof TenCalculator)((TenCalculator)view).appearance();
        if((view instanceof BrowserGlyph||view instanceof PhotoGlyph||view instanceof MonthCalendarView)
                &&(!original.controls||original.controlDark!=dark(c)||original.controlAccent!=TenStyle.accent(c)))view.invalidate();
        original.controls=true;original.controlAccent=TenStyle.accent(c);original.controlDark=dark(c);
        if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)apply(group.getChildAt(i));}
    }
    /** A newly attached row may inherit a new surface without changing either mapped color. */
    private static ColorStateList retainedPalette(ColorStateList next,ColorStateList previous){
        if(next==null||previous==null)return next;
        int[] disabled={-android.R.attr.state_enabled};
        return next.getDefaultColor()==previous.getDefaultColor()
                &&next.getColorForState(disabled,next.getDefaultColor())==previous.getColorForState(disabled,previous.getDefaultColor())
                ?previous:next;
    }
    private static ColorStateList mapped(Context c,ColorStateList original,boolean accentRole,int[] surfaces){if(original==null)return null;int normal=accentRole?TenAccent.readableOn(TenStyle.accent(c),surfaces):readableForeground(c,foreground(c,original.getDefaultColor()),surfaces),disabled=accentRole?TenAccent.readableOn(TenStyle.accent(c),surfaces):foreground(c,original.getColorForState(new int[]{-android.R.attr.state_enabled},original.getDefaultColor()));return new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},new int[]{disabled,normal});}
    /** Controls created in dark mode must also remain readable when that same view returns to light. */
    private static int readableForeground(Context c,int color,int[] surfaces){
        if(surfaces==null||Color.alpha(color)==0)return color;
        boolean readable=true;for(int fill:surfaces){int visible=TenStyle.mix(fill,color,Color.alpha(color)/255f);if(contrast(visible,fill)<4.5){readable=false;break;}}
        if(readable)return color;
        return TenAccent.readableOn(neutral(color)?ink(c):color|0xff000000,surfaces);
    }
    private static ColorStateList mappedHint(Context c,ColorStateList original,int[] surfaces){
        if(original==null)return null;
        int normal=readableHint(c,foreground(c,original.getDefaultColor()),surfaces);
        int disabled=foreground(c,original.getColorForState(new int[]{-android.R.attr.state_enabled},original.getDefaultColor()));
        return new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},new int[]{disabled,normal});
    }
    private static int readableHint(Context c,int color,int[] surfaces){
        boolean readable=true;for(int fill:surfaces){int visible=TenStyle.mix(fill,color,Color.alpha(color)/255f);if(contrast(visible,fill)<4.5){readable=false;break;}}
        if(readable)return color;
        int base=neutral(color)?dark(c)?0xffb5b5b5:0xff666666:color|0xff000000;
        return TenAccent.readableOn(base,surfaces);
    }
    /** Include gradient stops and interaction states; transparent controls inherit their parent surface. */
    private static int[] accentSurfaces(View view){java.util.ArrayList<Integer> colors=new java.util.ArrayList<>();View current=view;while(current!=null){if(surfaces(current.getBackground(),colors))break;ViewParent parent=current.getParent();current=parent instanceof View?(View)parent:null;}if(colors.isEmpty())colors.add(background(view.getContext()));int[] result=new int[colors.size()];for(int i=0;i<result.length;i++)result[i]=colors.get(i);return result;}
    private static boolean surfaces(Drawable drawable,java.util.List<Integer> colors){
        if(drawable instanceof Aero.ThemeStates){boolean opaque=true;for(Drawable child:((Aero.ThemeStates)drawable).children)opaque=surfaces(child,colors)&&opaque;return opaque;}
        if(drawable instanceof StateListDrawable)return surfaces(drawable.getCurrent(),colors);
        if(drawable instanceof InsetDrawable)return surfaces(((InsetDrawable)drawable).getDrawable(),colors);
        // The first layer is the text surface; later layers include the narrow Settings selection stripe.
        if(drawable instanceof LayerDrawable){LayerDrawable layers=(LayerDrawable)drawable;return layers.getNumberOfLayers()>0&&surfaces(layers.getDrawable(0),colors);}
        if(drawable instanceof GradientDrawable){GradientDrawable gradient=(GradientDrawable)drawable;int[] values=gradient.getColors();if(values==null&&gradient.getColor()!=null)values=new int[]{gradient.getColor().getDefaultColor()};if(values==null)return false;boolean opaque=true;for(int color:values){if(Color.alpha(color)==255)colors.add(color);else opaque=false;}return opaque;}
        if(drawable instanceof ColorDrawable){int color=((ColorDrawable)drawable).getColor();if(Color.alpha(color)==255){colors.add(color);return true;}}
        return false;
    }
    static void enable(Context c,Drawable drawable){
        if(drawable instanceof ColorDrawable){int color=originalColors.computeIfAbsent(drawable,key->((ColorDrawable)key).getColor());int mapped=surface(c,color);if(((ColorDrawable)drawable).getColor()!=mapped)((ColorDrawable)drawable).setColor(mapped);}
        else if(drawable instanceof Aero.ThemePanel){Aero.ThemePanel panel=(Aero.ThemePanel)drawable;panel.appThemed=true;panel.appearance();}
        else if(drawable instanceof Aero.LiteralPanel)return;
        else if(drawable instanceof GradientDrawable)gradient(c,(GradientDrawable)drawable);
        else if(drawable instanceof Aero.ThemeStates)for(Drawable child:((Aero.ThemeStates)drawable).children)enable(c,child);
        else if(drawable instanceof StateListDrawable){Drawable child=drawable.getCurrent();if(child!=drawable)enable(c,child);}
        else if(drawable instanceof AeroCheck){AeroCheck check=(AeroCheck)drawable;check.appearance();}
        else if(drawable instanceof LayerDrawable){LayerDrawable layers=(LayerDrawable)drawable;for(int i=0;i<layers.getNumberOfLayers();i++)enable(c,layers.getDrawable(i));}
        else if(drawable instanceof InsetDrawable)enable(c,((InsetDrawable)drawable).getDrawable());
    }
    /** Theme fill colors only; keep geometry, borders and the original light palette intact. */
    private static void gradient(Context c,GradientDrawable drawable){
        int[] current=drawable.getColors();
        if(current==null){ColorStateList color=drawable.getColor();if(color==null||color.isStateful())return;current=new int[]{color.getDefaultColor()};}
        GradientColors original=gradientColors.get(drawable);
        if(original==null||!java.util.Arrays.equals(current,original.applied)){original=new GradientColors(current);gradientColors.put(drawable,original);}
        int[] mapped=original.source.clone();for(int i=0;i<mapped.length;i++)mapped[i]=surface(c,mapped[i]);
        if(!java.util.Arrays.equals(current,mapped)){if(mapped.length==1)drawable.setColor(mapped[0]);else drawable.setColors(mapped);}
        original.applied=mapped;
    }
    private TenAppearance(){}
}
