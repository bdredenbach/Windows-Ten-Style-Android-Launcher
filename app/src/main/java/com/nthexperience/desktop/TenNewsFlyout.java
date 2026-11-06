package com.nthexperience.desktop;
import android.content.Context;
import android.view.*;
import android.widget.*;

/** Taskbar anchored dashboard; does not create a spurious running-app button. */
final class TenNewsFlyout {
    final Context context;final FrameLayout host;final DesktopStore store;final WeatherClient weather;final java.util.function.Consumer<String> open;
    FrameLayout cover;LinearLayout frame;TenNews news;int left,right,top;
    TenNewsFlyout(Context c,FrameLayout host,DesktopStore store,WeatherClient weather,java.util.function.Consumer<String> open){context=c;this.host=host;this.store=store;this.weather=weather;this.open=open;}
    boolean shown(){return cover!=null;}
    void show(){if(shown()){dismiss();return;}cover=new FrameLayout(context);cover.setElevation(Aero.dp(context,52));cover.setContentDescription("News and interests flyout");cover.setOnClickListener(v->dismiss());host.addView(cover,new FrameLayout.LayoutParams(-1,-1));frame=Aero.column(context);frame.setClickable(true);frame.setBackgroundColor(0xff202020);news=new TenNews(context,store,open,weather);news.onClose=this::dismiss;frame.addView(news,new LinearLayout.LayoutParams(-1,0,1));FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(1,1,Gravity.BOTTOM|Gravity.END);cover.addView(frame,p);bounds(left,right,top);if(Aero.motion(context)){frame.setAlpha(0);frame.setTranslationY(Aero.dp(context,12));frame.animate().alpha(1).translationY(0).setDuration(160).start();}}
    void bounds(int left,int right,int top){this.left=left;this.right=right;this.top=top;if(frame==null)return;FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)frame.getLayoutParams();int w=Math.min(Aero.dp(context,1000),Math.max(1,host.getWidth()-left-right-Aero.dp(context,8))),h=Math.min(Aero.dp(context,720),Math.max(1,host.getHeight()-top-Aero.dp(context,8)));if(p.width!=w||p.height!=h||p.rightMargin!=right+Aero.dp(context,4)){p.width=w;p.height=h;p.rightMargin=right+Aero.dp(context,4);p.leftMargin=left+Aero.dp(context,4);p.bottomMargin=Aero.dp(context,4);frame.setLayoutParams(p);}}
    boolean dismiss(){if(cover==null)return false;frame.animate().cancel();host.removeView(cover);cover=null;frame=null;news=null;return true;}
}
