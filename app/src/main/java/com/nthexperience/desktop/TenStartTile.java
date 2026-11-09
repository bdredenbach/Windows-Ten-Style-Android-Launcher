package com.nthexperience.desktop;

import android.content.Context;
import android.view.*;
import android.widget.*;
import android.text.TextUtils;

/** A named tile whose live body updates without rebuilding the grid or interrupting a gesture. */
final class TenStartTile extends FrameLayout {
 final DesktopStore store;final StartSearch.Result item;final String size;
 private final View image;private final TextView primary,secondary;private final boolean roomy,wide;private final int ink,top,tileHeight,footer;
 TenStartTile(Context context,DesktopStore store,StartSearch.Result item,String size,int width,int height,View icon,int color){
  super(context);tileHeight=height;this.store=store;this.item=item;this.size=size;image=icon;ink=TenAccent.on(color);roomy=height>=dp(140);wide=width>height*1.5f;top=dp(roomy?12:6);
  setBackground(TenStyle.hover(context,color));setFocusable(true);setClipChildren(true);
  footer=Math.min(dp(34),Math.max(dp(24),height/4));int iconSize=dp(size.equals("Small")?23:roomy?40:26);
  FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(iconSize,iconSize,Gravity.TOP|Gravity.CENTER_HORIZONTAL);ip.topMargin=Math.max(dp(4),(height-footer-iconSize)/2);addView(image,ip);
  primary=Aero.text(context,"",roomy?22:wide?15:12,ink);primary.setGravity(Gravity.CENTER_VERTICAL);primary.setMaxLines(roomy&&height>=dp(180)?4:2);primary.setEllipsize(TextUtils.TruncateAt.END);
  FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(-1,-1);pp.setMargins(dp(8),dp(30),dp(8),footer+dp(4));addView(primary,pp);
  secondary=Aero.text(context,"",roomy?13:11,ink);secondary.setMaxLines(3);secondary.setEllipsize(TextUtils.TruncateAt.END);FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,dp(36),Gravity.BOTTOM);sp.setMargins(dp(8),0,dp(8),footer+dp(6));addView(secondary,sp);
  if(!size.equals("Small")){TextView name=Aero.text(context,item.title,roomy?12:10,ink);name.setMaxLines(2);name.setEllipsize(TextUtils.TruncateAt.END);name.setGravity(Gravity.BOTTOM);name.setPadding(dp(7),0,dp(5),dp(5));addView(name,new FrameLayout.LayoutParams(-1,footer,Gravity.BOTTOM));}
  refresh();
 }
 void refresh(){
  TenLiveTiles.Detail detail=size.equals("Small")?null:TenLiveTiles.detail(getContext(),store,item.id);
  FrameLayout.LayoutParams ip=(FrameLayout.LayoutParams)image.getLayoutParams();int iconSize=dp(detail!=null?20:size.equals("Small")?23:roomy?40:26);ip.width=iconSize;ip.height=iconSize;
  ip.gravity=detail!=null?Gravity.TOP|Gravity.START:Gravity.TOP|Gravity.CENTER_HORIZONTAL;ip.leftMargin=detail!=null?dp(8):0;ip.topMargin=detail!=null?top:Math.max(dp(4),(tileHeight-(size.equals("Small")?0:footer)-iconSize)/2);image.setLayoutParams(ip);
  primary.setVisibility(detail==null?GONE:VISIBLE);secondary.setVisibility(detail!=null&&roomy?VISIBLE:GONE);
  if(detail!=null){if(!detail.primary.contentEquals(primary.getText())){primary.setText(detail.primary);if(isShown()&&Aero.motion(getContext())){primary.setAlpha(.6f);primary.animate().alpha(1).setDuration(160).start();}}primary.setTextSize(roomy&&item.id.equals("@calendar")?(tileHeight>=dp(190)?42:22):roomy?20:wide?14:12);secondary.setText(detail.secondary);FrameLayout.LayoutParams pp=(FrameLayout.LayoutParams)primary.getLayoutParams();pp.bottomMargin=footer+dp(roomy?42:4);primary.setLayoutParams(pp);}
  setContentDescription(item.title+", "+size+" Start tile"+(detail==null?"":", live: "+detail.primary+". "+detail.secondary));
 }
 private int dp(int value){return Aero.dp(getContext(),value);}
}
