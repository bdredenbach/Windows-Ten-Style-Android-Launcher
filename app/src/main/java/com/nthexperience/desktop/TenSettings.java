package com.nthexperience.desktop;
import android.content.*;
import android.graphics.Color;
import android.os.Build;
import android.view.*;
import android.widget.*;
import java.util.function.Consumer;

/** Ten Settings home and real launcher personalization; system pages delegate to Android. */
final class TenSettings extends LinearLayout {
 final DesktopStore store;final Consumer<String> open;final LinearLayout body;String page="home";
 TenSettings(Context c,DesktopStore store,Consumer<String> open){super(c);this.store=store;this.open=open;setOrientation(VERTICAL);setBackgroundColor(Color.WHITE);setContentDescription("Windows 10 style Settings");ScrollView scroll=new ScrollView(c);body=Aero.column(c);Aero.pad(body,22);scroll.addView(body);addView(scroll,new LayoutParams(-1,-1));navigate("home");}
 void navigate(String destination){page=destination;body.removeAllViews();if(!page.equals("home"))button("‹  Settings home",()->navigate("home"));heading(page.equals("home")?"Windows Settings":page.equals("personalization")?"Personalization":page.equals("system")?"System":"About");
  switch(page){case "home":
   detail("Nth Desktop Ten",store.prefs.getString("desktop_name","Your desktop"));
   button("▣  System",()->navigate("system"));detail("","Display, sound, storage and device information");
   button("◈  Personalization",()->navigate("personalization"));detail("","Background, colors, Start and taskbar");
   button("▦  Apps",()->open.accept("setting:android:apps"));button("○  Accounts",()->open.accept("setting:page:accounts"));button("⌨  Ease of Access",()->open.accept("setting:android:accessibility"));button("↶  Backup and restore",()->open.accept("@backup"));button("✎  Nth Nota",()->open.accept("@nota"));button("ⓘ  About",()->navigate("about"));break;
  case "system":button("Display",()->open.accept("setting:android:display"));button("Sound",()->open.accept("setting:android:sound"));button("Storage",()->open.accept("setting:android:storage"));button("Battery",()->open.accept("setting:android:battery"));button("Network & Internet",()->open.accept("setting:android:network"));button("Multitasking",()->open.accept("setting:page:taskbar"));button("About",()->navigate("about"));break;
  case "personalization":
   button("Background",()->open.accept("setting:page:background"));detail("Colors","Choose your launcher mode");Switch light=new Switch(getContext());light.setText("Light Start menu and taskbar");light.setChecked(TenStyle.light(getContext()));light.setMinHeight(Aero.dp(getContext(),48));body.addView(light);light.setOnCheckedChangeListener((v,value)->{store.prefs.edit().putBoolean("ten_light",value).apply();((android.app.Activity)getContext()).recreate();});
   detail("Accent color","Used for active taskbar indicators and tiles");LinearLayout palette=new LinearLayout(getContext());for(int color:new int[]{0xff0078d7,0xff008272,0xff744da9,0xffc239b3,0xffd83b01}){TextView sample=Aero.text(getContext(),"✓",18,color==TenStyle.accent(getContext())?Color.WHITE:color);sample.setGravity(Gravity.CENTER);sample.setBackgroundColor(color);sample.setContentDescription("Accent color "+Integer.toHexString(color));sample.setFocusable(true);sample.setOnClickListener(v->{store.prefs.edit().putInt("ten_accent",color).apply();((android.app.Activity)getContext()).recreate();});LayoutParams p=new LayoutParams(0,Aero.dp(getContext(),48),1);p.setMargins(3,3,3,3);palette.addView(sample,p);}body.addView(palette);
   button("Desktop icons and spacing",()->open.accept("setting:page:desktop"));button("Start and taskbar options",()->open.accept("setting:page:taskbar"));break;
  case "about":detail("Nth Desktop Ten","Version "+BuildConfig.VERSION_NAME);detail("Device",Build.MANUFACTURER+" "+Build.MODEL);detail("Android",Build.VERSION.RELEASE+" · API "+Build.VERSION.SDK_INT);detail("An Nth Experience","Windows 10 inspired Android launcher. Built-in tools use Nth windows; Android apps open through Android.");button("Guided setup",()->open.accept("@welcome"));break;
  }
 }
 private void heading(String s){TextView t=Aero.text(getContext(),s,25,Aero.INK);body.addView(t,new LayoutParams(-1,Aero.dp(getContext(),60)));}
 private void detail(String title,String text){if(!title.isEmpty()){TextView h=Aero.text(getContext(),title,16,Aero.INK);body.addView(h,new LayoutParams(-1,Aero.dp(getContext(),38)));}TextView t=Aero.text(getContext(),text,13,0xff666666);t.setPadding(0,0,0,Aero.dp(getContext(),12));body.addView(t);}
 private void button(String label,Runnable run){TextView t=Aero.button(getContext(),label,run);t.setGravity(Gravity.CENTER_VERTICAL);t.setTextSize(15);t.setBackground(Aero.selection(getContext()));body.addView(t,new LayoutParams(-1,Aero.dp(getContext(),52)));}
}
