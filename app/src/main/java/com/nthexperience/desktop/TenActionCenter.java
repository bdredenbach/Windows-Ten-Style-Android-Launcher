package com.nthexperience.desktop;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.function.Consumer;

/** Right-edge Ten quick actions and real Nth alerts. */
final class TenActionCenter {
 final Context context;final FrameLayout host;final DesktopStore store;final Consumer<String> open;FrameLayout overlay;
 TenActionCenter(Context c,FrameLayout host,DesktopStore store,Consumer<String> open){context=c;this.host=host;this.store=store;this.open=open;}
 boolean dismiss(){if(overlay==null)return false;host.removeView(overlay);overlay=null;return true;}
 void show(){dismiss();overlay=new FrameLayout(context);overlay.setBackgroundColor(0x44000000);overlay.setOnClickListener(v->dismiss());LinearLayout panel=Aero.column(context);Aero.pad(panel,16);panel.setBackgroundColor(TenStyle.surface(context));panel.setOnClickListener(v->{});panel.setContentDescription("Ten Action Center");FrameLayout.LayoutParams bounds=new FrameLayout.LayoutParams(Math.min(Aero.dp(context,360),Math.max(1,host.getWidth())),Math.max(1,host.getHeight()),Gravity.END);overlay.addView(panel,bounds);
  TextView title=Aero.text(context,"Action Center",20,TenStyle.text(context));panel.addView(title,new LinearLayout.LayoutParams(-1,Aero.dp(context,48)));panel.addView(TenStyle.command(context,"Close",this::dismiss));ScrollView scroll=new ScrollView(context);LinearLayout notices=Aero.column(context);scroll.addView(notices);panel.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));java.util.List<ActionCenter.Alert> alerts=ActionCenter.alerts(context,store);
  if(alerts.isEmpty()){TextView empty=Aero.text(context,"No new Nth notifications",14,TenStyle.text(context));Aero.pad(empty,10);notices.addView(empty);}
  for(ActionCenter.Alert alert:alerts){TextView item=TenStyle.command(context,alert.title+"\n"+alert.message,()->{dismiss();open.accept(alert.target);});item.setGravity(Gravity.CENTER_VERTICAL);item.setPadding(10,10,10,10);notices.addView(item,new LinearLayout.LayoutParams(-1,-2));}
  TextView caption=Aero.text(context,"Quick actions",13,TenStyle.text(context));panel.addView(caption,new LinearLayout.LayoutParams(-1,Aero.dp(context,40)));for(String[][] row:new String[][][]{{{"Wi-Fi","setting:android:network"},{"Bluetooth","setting:android:bluetooth"}},{{"Battery","setting:android:battery"},{"All settings","@settings"}},{{"Brightness","@brightness"},{"Task Manager","@taskmanager"}}}){LinearLayout line=new LinearLayout(context);for(String[] action:row){TextView tile=TenStyle.command(context,action[0],()->{dismiss();open.accept(action[1]);});tile.setBackground(TenStyle.hover(context,TenStyle.light(context)?0xffdddddd:0xff3b3b3b));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Aero.dp(context,64),1);p.setMargins(2,2,2,2);line.addView(tile,p);}panel.addView(line);}
  TextView note=Aero.text(context,"System controls open Android settings.",11,TenStyle.text(context));panel.addView(note,new LinearLayout.LayoutParams(-1,Aero.dp(context,38)));host.addView(overlay,new FrameLayout.LayoutParams(-1,-1));overlay.bringToFront();
 }
}
