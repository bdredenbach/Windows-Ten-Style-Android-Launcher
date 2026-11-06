package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.*;

/** Named, literal accent swatches; black and white samples retain their real colors. */
final class TenAccentPicker extends GridLayout {
    TenAccentPicker(Context c,DesktopStore store,Runnable changed){
        super(c);setColumnCount(4);setContentDescription("Accent color palette");
        for(int i=0;i<TenAccent.COLORS.length;i++){final int color=TenAccent.COLORS[i];LinearLayout cell=Aero.column(c);cell.setGravity(Gravity.CENTER);cell.setFocusable(true);cell.setSelected(color==TenStyle.accent(c));cell.setContentDescription("Accent color "+Integer.toHexString(color));cell.setOnClickListener(v->{store.prefs.edit().putInt("ten_accent",color).apply();changed.run();});
            FrameLayout sample=new FrameLayout(c);sample.setBackgroundColor(color);sample.setContentDescription("Accent sample "+Integer.toHexString(color));TenAppearance.preserve(sample);TextView mark=Aero.text(c,cell.isSelected()?"✓":"",19,TenAccent.on(color));mark.setGravity(Gravity.CENTER);sample.addView(mark,new FrameLayout.LayoutParams(-1,-1));cell.addView(sample,new LinearLayout.LayoutParams(-1,Aero.dp(c,40)));
            TextView name=Aero.text(c,TenAccent.NAMES[i],11,Aero.INK);name.setGravity(Gravity.CENTER);name.setMaxLines(2);cell.addView(name,new LinearLayout.LayoutParams(-1,Aero.dp(c,28)));GridLayout.LayoutParams p=new GridLayout.LayoutParams(GridLayout.spec(i/4),GridLayout.spec(i%4,1f));p.width=0;p.setMargins(Aero.dp(c,3),Aero.dp(c,3),Aero.dp(c,3),Aero.dp(c,3));addView(cell,p);
        }
    }
}
