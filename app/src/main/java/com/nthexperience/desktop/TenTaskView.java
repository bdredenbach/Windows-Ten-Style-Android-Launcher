package com.nthexperience.desktop;
import android.app.Activity;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Overview of Nth windows and tracked Android apps, with private previews excluded. */
final class TenTaskView {
 final Activity activity;final FrameLayout host;FrameLayout overlay;final List<Bitmap> images=new ArrayList<>();
 TenTaskView(Activity a,FrameLayout host){activity=a;this.host=host;}
 boolean dismiss(){if(overlay==null)return false;host.removeView(overlay);overlay=null;for(Bitmap b:images)b.recycle();images.clear();return true;}
 void show(List<DesktopWindow> windows,List<String> external,java.util.function.Function<String,String> label,java.util.function.Consumer<String> open){
  dismiss();overlay=new FrameLayout(activity);overlay.setContentDescription("Task View");overlay.setBackgroundColor(0xe51a1a1a);overlay.setClickable(true);
  LinearLayout body=Aero.column(activity);Aero.pad(body,16);overlay.addView(body,new FrameLayout.LayoutParams(-1,-1));LinearLayout header=new LinearLayout(activity);TextView title=Aero.text(activity,"Task View",22,Color.WHITE);header.addView(title,new LinearLayout.LayoutParams(0,Aero.dp(activity,48),1));header.addView(TenStyle.command(activity,"Close",this::dismiss));body.addView(header);
  TextView info=Aero.text(activity,"Open windows",13,0xffd0d0d0);body.addView(info,new LinearLayout.LayoutParams(-1,Aero.dp(activity,32)));ScrollView scroll=new ScrollView(activity);GridLayout cards=new GridLayout(activity);int cols=Math.max(1,host.getWidth()/Aero.dp(activity,280));cards.setColumnCount(cols);scroll.addView(cards);body.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  for(DesktopWindow window:windows){if(window.getParent()==null)continue;LinearLayout card=Aero.column(activity);card.setBackground(TenStyle.hover(activity,0xff353535));LinearLayout bar=new LinearLayout(activity);TextView caption=Aero.text(activity,window.caption(),14,Color.WHITE);Aero.pad(caption,10);bar.addView(caption,new LinearLayout.LayoutParams(0,Aero.dp(activity,44),1));TextView close=TenStyle.command(activity,"×",()->{window.closeWindow();show(windows,external,label,open);});close.setContentDescription("Close "+window.caption());bar.addView(close,new LinearLayout.LayoutParams(Aero.dp(activity,48),-1));card.addView(bar);
   View preview;if(WindowPreviews.allowed(window)&&window.getWidth()>0&&window.getHeight()>0){try{float scale=Math.min(420f/window.getWidth(),210f/window.getHeight());Bitmap bitmap=Bitmap.createBitmap(Math.max(1,(int)(window.getWidth()*scale)),Math.max(1,(int)(window.getHeight()*scale)),Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bitmap);c.scale(scale,scale);window.drawPreview(c);images.add(bitmap);ImageView image=new ImageView(activity);image.setImageBitmap(bitmap);image.setScaleType(ImageView.ScaleType.FIT_CENTER);preview=image;}catch(RuntimeException|OutOfMemoryError ex){preview=Aero.text(activity,"Preview unavailable",13,Color.WHITE);}}else preview=Aero.text(activity,"Private preview hidden",13,Color.WHITE);
   card.addView(preview,new LinearLayout.LayoutParams(-1,Aero.dp(activity,150)));card.setContentDescription("Switch to "+window.caption());card.setFocusable(true);card.setOnClickListener(v->{dismiss();window.restore();});GridLayout.LayoutParams p=cell(cards,cols);cards.addView(card,p);
  }
  for(String id:external){TextView app=TenStyle.command(activity,"Open Android app: "+label.apply(id),()->{dismiss();open.accept(id);});cards.addView(app,cell(cards,cols));}
  if(cards.getChildCount()==0)cards.addView(Aero.text(activity,"Your open windows will appear here.",16,Color.WHITE));
  host.addView(overlay,new FrameLayout.LayoutParams(-1,-1));overlay.bringToFront();overlay.setFocusableInTouchMode(true);overlay.requestFocus();overlay.setOnKeyListener((v,key,event)->{if(key==KeyEvent.KEYCODE_ESCAPE&&event.getAction()==KeyEvent.ACTION_UP){dismiss();return true;}return false;});
 }
 private GridLayout.LayoutParams cell(GridLayout grid,int cols){int index=grid.getChildCount();GridLayout.LayoutParams p=new GridLayout.LayoutParams(GridLayout.spec(index/cols),GridLayout.spec(index%cols));p.width=Math.max(1,(host.getWidth()-Aero.dp(activity,32))/cols-Aero.dp(activity,12));p.height=Aero.dp(activity,194);p.setMargins(Aero.dp(activity,6),0,Aero.dp(activity,6),Aero.dp(activity,14));return p;}
}
