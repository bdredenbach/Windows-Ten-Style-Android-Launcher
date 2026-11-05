package com.nthexperience.desktop;
import android.content.Context;
import android.view.*;
import android.widget.*;
import java.util.function.Consumer;
import org.json.*;
/** User-selected RSS feed, cached headlines and routes to the existing weather tool. */
final class TenNews extends LinearLayout {
 final DesktopStore store;final Consumer<String> open;final FeedClient feed;final LinearLayout body;boolean started;
 TenNews(Context c,DesktopStore store,Consumer<String> open){super(c);this.store=store;this.open=open;setOrientation(VERTICAL);ScrollView scroll=new ScrollView(c);body=Aero.column(c);Aero.pad(body,16);scroll.addView(body);addView(scroll,new LayoutParams(-1,-1));feed=new FeedClient(store,()->post(this::render));render();}
 private void render(){body.removeAllViews();TextView heading=Aero.text(getContext(),"News and interests",24,Aero.INK);body.addView(heading,new LayoutParams(-1,Aero.dp(getContext(),56)));TextView info=Aero.text(getContext(),"Choose an HTTPS RSS or Atom feed. Refresh downloads headlines from that feed; article links open in Nth Browser.",13,0xff666666);body.addView(info);EditText address=new EditText(getContext());address.setSingleLine();address.setText(feed.url());address.setHint("https://…/feed.xml");address.setTextSize(13);address.setContentDescription("News feed address");body.addView(address,new LayoutParams(-1,Aero.dp(getContext(),48)));body.addView(Aero.button(getContext(),"Save feed and refresh",()->{String value=address.getText().toString().trim();if(!FeedClient.validUrl(value)){address.setError("Use an HTTPS RSS or Atom feed");return;}started=true;feed.active(true);feed.configure(value);}));body.addView(Aero.button(getContext(),"Refresh headlines",()->{started=true;feed.active(true);feed.refresh(true);}));body.addView(Aero.button(getContext(),"Weather",()->open.accept("@weather")));TextView status=Aero.text(getContext(),feed.status(),12,0xff666666);body.addView(status,new LayoutParams(-1,Aero.dp(getContext(),40)));JSONArray items=FeedClient.items(store);for(int i=0;i<items.length();i++){JSONObject item=items.optJSONObject(i);if(item==null)continue;String link=item.optString("url",item.optString("link"));if(!BrowserUrls.web(link))continue;TextView title=Aero.button(getContext(),item.optString("title","Article"),()->open.accept("favorite-web:"+link));title.setGravity(Gravity.CENTER_VERTICAL);title.setBackground(Aero.selection(getContext()));body.addView(title,new LayoutParams(-1,-2));}}
 @Override public void onVisibilityAggregated(boolean visible){super.onVisibilityAggregated(visible);if(feed!=null)feed.active(visible&&started);}
 @Override protected void onDetachedFromWindow(){feed.close();super.onDetachedFromWindow();}
}
