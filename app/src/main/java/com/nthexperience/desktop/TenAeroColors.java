package com.nthexperience.desktop;

import android.content.Context;
import android.view.*;
import android.widget.*;

/** Literal color samples must remain truthful in either app mode and either shell finish. */
final class TenAeroColors extends LinearLayout {
    TenAeroColors(Context c,DesktopStore store,Runnable changed){
        super(c);setOrientation(VERTICAL);setContentDescription("Aero glass controls");
        addView(Aero.text(c,"Aero glass color",16,Aero.INK),new LayoutParams(-1,Aero.dp(c,40)));
        GridLayout grid=new GridLayout(c);grid.setColumnCount(4);int selected=store.prefs.getInt("aero_color",0);
        for(int i=0;i<AeroGlass.TINTS.length;i++){final int value=i;LinearLayout tile=Aero.column(c);tile.setGravity(Gravity.CENTER);tile.setFocusable(true);tile.setContentDescription("Aero glass tint: "+AeroGlass.COLOR_NAMES[i]);tile.setBackground(Aero.selection(c));tile.setOnClickListener(v->{store.prefs.edit().putString("ten_shell_style","Aero").putInt("aero_color",value).putInt("ten_accent",AeroGlass.TINTS[value]).apply();changed.run();});
            View swatch=new View(c);TenAppearance.preserve(swatch);swatch.setContentDescription("Aero tint sample: "+AeroGlass.COLOR_NAMES[i]);swatch.setBackground(Aero.literal(c,new int[]{0xfff0f8ff,AeroGlass.TINTS[i],AeroGlass.TINTS[i],0xffd9edf8},4,selected==i?TenStyle.accent(c):0xff888888));tile.addView(swatch,new LayoutParams(Aero.dp(c,43),Aero.dp(c,34)));
            TextView name=Aero.text(c,(selected==i?"✓  ":"")+AeroGlass.COLOR_NAMES[i],11,Aero.INK);name.setGravity(Gravity.CENTER);tile.addView(name,new LayoutParams(-1,Aero.dp(c,27)));GridLayout.LayoutParams p=new GridLayout.LayoutParams(GridLayout.spec(i/4),GridLayout.spec(i%4,1f));p.width=0;p.setMargins(0,Aero.dp(c,4),Aero.dp(c,4),Aero.dp(c,4));grid.addView(tile,p);
        }
        addView(grid,new LayoutParams(-1,-2));slider(c,store,"Aero glass opacity","aero_opacity",85,changed);slider(c,store,"Aero color intensity","aero_intensity",45,changed);
    }
    private void slider(Context c,DesktopStore store,String title,String key,int fallback,Runnable changed){TextView label=Aero.text(c,title+": "+store.prefs.getInt(key,fallback)+"%",13,Aero.INK);addView(label,new LayoutParams(-1,Aero.dp(c,34)));SeekBar slider=new SeekBar(c);slider.setMax(100);slider.setProgress(store.prefs.getInt(key,fallback));slider.setContentDescription(title);slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar view){}public void onProgressChanged(SeekBar view,int value,boolean user){if(user){store.prefs.edit().putInt(key,value).apply();label.setText(title+": "+value+"%");}}public void onStopTrackingTouch(SeekBar view){changed.run();}});addView(slider,new LayoutParams(-1,Aero.dp(c,44)));}
}
