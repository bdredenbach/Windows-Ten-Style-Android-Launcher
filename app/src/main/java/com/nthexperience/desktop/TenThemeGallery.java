package com.nthexperience.desktop;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import java.util.function.Consumer;

/** Personalization theme cards are visible directly in Ten Settings. */
final class TenThemeGallery extends LinearLayout {
    final DesktopStore store;final Consumer<String> open;final Runnable changed;final List<AeroWallpaper> previews=new ArrayList<>();final int columns;
    TenThemeGallery(Context c,DesktopStore store,int width,Consumer<String> open,Runnable changed){
        super(c);this.store=store;this.open=open;this.changed=changed;columns=width>=Aero.dp(c,760)?3:width>=Aero.dp(c,420)?2:1;setOrientation(VERTICAL);setContentDescription("Ten theme gallery");
        TextView name=Aero.text(c,"Current theme: "+TenThemes.currentName(store),18,Aero.INK);addView(name,new LayoutParams(-1,Aero.dp(c,40)));
        section("Windows 10 inspired",TenThemes.TEN);section("Aero from Nth Desktop Seven",TenThemes.AERO);
        heading("My themes");addView(button("Save current theme…",()->open.accept("setting:theme-save")));addView(button("Import an Nth theme…",()->open.accept("setting:theme-import")));
        JSONArray saved=ThemeProfiles.saved(store);for(int i=0;i<saved.length();i++){JSONObject row=saved.optJSONObject(i);if(row==null)continue;final int index=i;TextView entry=button(row.optString("name","Saved theme"),()->open.accept("setting:theme-preview:"+index));entry.setContentDescription("Saved theme: "+row.optString("name"));addView(entry);}
    }
    private TextView button(String label,Runnable run){TextView view=Aero.text(getContext(),label,13,0xff0078d7);view.setMinHeight(Aero.dp(getContext(),44));view.setGravity(Gravity.CENTER_VERTICAL);view.setFocusable(true);view.setBackground(Aero.selection(getContext()));view.setOnClickListener(v->run.run());return view;}
    private void heading(String label){TextView title=Aero.text(getContext(),label,17,Aero.INK);title.setPadding(0,Aero.dp(getContext(),14),0,Aero.dp(getContext(),7));addView(title);}
    private void section(String label,List<TenThemes.Preset> presets){heading(label);GridLayout grid=new GridLayout(getContext());grid.setColumnCount(columns);int i=0;for(TenThemes.Preset theme:presets){boolean selected=theme.matches(store);LinearLayout card=Aero.column(getContext());card.setPadding(Aero.dp(getContext(),5),Aero.dp(getContext(),5),Aero.dp(getContext(),5),Aero.dp(getContext(),5));card.setBackground(Aero.panel(getContext(),selected?0xffe8f3fc:0xfffafafa,selected?0xffe8f3fc:0xfffafafa,0,selected?TenStyle.accent(getContext()):0xffd5d5d5));card.setFocusable(true);card.setSelected(selected);card.setContentDescription("Apply theme: "+theme.name);
        FrameLayout sample=new FrameLayout(getContext());TenAppearance.preserve(sample);sample.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);AeroWallpaper art=new AeroWallpaper(getContext(),theme.wallpaper,false);previews.add(art);sample.addView(art,new FrameLayout.LayoutParams(-1,-1));
        View taskbar=new View(getContext());taskbar.setBackgroundColor(theme.aero?0xff314e66:theme.light?0xffeeeeee:0xff202020);sample.addView(taskbar,new FrameLayout.LayoutParams(-1,Aero.dp(getContext(),13),Gravity.BOTTOM));LinearLayout window=Aero.column(getContext());window.setBackground(Aero.panel(getContext(),theme.appsDark()?0xff202020:0xfffafafa,theme.appsDark()?0xff202020:0xfffafafa,theme.aero?3:0,theme.accent));View caption=new View(getContext());caption.setBackground(theme.aero?Aero.gloss(getContext(),new int[]{0xffedf9ff,AeroGlass.TINTS[theme.aeroColor],0xffb9ccdc},2,0xffd4e9f5):new android.graphics.drawable.ColorDrawable(theme.appsDark()?0xff292929:0xffeeeeee));window.addView(caption,new LayoutParams(-1,Aero.dp(getContext(),10)));FrameLayout.LayoutParams w=new FrameLayout.LayoutParams(Aero.dp(getContext(),65),Aero.dp(getContext(),48),Gravity.CENTER);sample.addView(window,w);card.addView(sample,new LayoutParams(-1,Aero.dp(getContext(),105)));
        TextView title=Aero.text(getContext(),(selected?"✓  ":"")+theme.name,13,Aero.INK);title.setMinHeight(Aero.dp(getContext(),36));title.setGravity(Gravity.CENTER_VERTICAL);card.addView(title);card.setOnClickListener(v->{theme.apply(store);changed.run();});GridLayout.LayoutParams p=new GridLayout.LayoutParams(GridLayout.spec(i/columns),GridLayout.spec(i%columns,1f));p.width=0;p.setMargins(0,0,Aero.dp(getContext(),8),Aero.dp(getContext(),8));grid.addView(card,p);i++;}addView(grid,new LayoutParams(-1,-2));}
    @Override protected void onDetachedFromWindow(){for(AeroWallpaper preview:previews)preview.close();previews.clear();super.onDetachedFromWindow();}
}
