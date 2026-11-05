package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.function.Consumer;

/** Untitled41's dashboard: real feed stories, saved stories, cached weather and Nth games. */
final class TenNews extends LinearLayout {
    final DesktopStore store; final Consumer<String> open; final FeedClient feed; final WeatherClient weather;
    final TenNewsImages images=new TenNewsImages();final LinearLayout cards; final TextView status; boolean savedOnly,started; int columns=1;
    TenNews(Context c,DesktopStore store,Consumer<String> open,WeatherClient weather){
        super(c);this.store=store;this.open=open;this.weather=new WeatherClient(store,()->post(this::render));feed=new FeedClient(store,()->post(this::render));started=!feed.url().isEmpty();setOrientation(VERTICAL);setBackgroundColor(0xff202020);setContentDescription("News and interests dashboard");
        LinearLayout toolbar=new LinearLayout(c);toolbar.setGravity(Gravity.CENTER_VERTICAL);toolbar.setPadding(dp(12),0,dp(8),0);
        TextView brand=text("N  /  News and interests",16,Color.WHITE);toolbar.addView(brand,new LayoutParams(0,dp(48),1));
        toolbar.addView(command("↻","Refresh news",()->{started=true;feed.active(true);feed.refresh(true);weather.refresh(true);}),new LayoutParams(dp(44),dp(44)));
        toolbar.addView(command("⚙","Manage interests",this::configure),new LayoutParams(dp(44),dp(44)));addView(toolbar);
        LinearLayout tabs=new LinearLayout(c);tabs.setPadding(dp(12),0,dp(12),0);
        tabs.addView(command("My feed","My news feed",()->{savedOnly=false;render();}),new LayoutParams(0,dp(40),1));
        tabs.addView(command("Saved","Saved news stories",()->{savedOnly=true;render();}),new LayoutParams(0,dp(40),1));addView(tabs);
        status=text("",11,0xffbdbdbd);status.setPadding(dp(16),dp(4),dp(12),dp(8));status.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);addView(status);
        ScrollView scroll=new ScrollView(c);scroll.setFillViewport(true);cards=new LinearLayout(c);cards.setOrientation(HORIZONTAL);cards.setPadding(dp(8),0,dp(8),dp(12));scroll.addView(cards);addView(scroll,new LayoutParams(-1,0,1));
        addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{int next=r-l>=dp(750)?3:r-l>=dp(510)?2:1;if(next!=columns){columns=next;render();}});render();
    }
    private int dp(int n){return Aero.dp(getContext(),n);}
    private TextView text(String s,int size,int color){return Aero.text(getContext(),s,size,color);}
    private TextView command(String label,String description,Runnable run){TextView v=TenStyle.command(getContext(),label,run);v.setTextColor(Color.WHITE);v.setContentDescription(description);return v;}
    private LinearLayout card(LinearLayout column,String description){LinearLayout card=Aero.column(getContext());card.setPadding(dp(16),dp(14),dp(16),dp(14));GradientDrawable bg=new GradientDrawable();bg.setColor(0xff303030);bg.setCornerRadius(dp(7));card.setBackground(bg);card.setContentDescription(description);LayoutParams lp=new LayoutParams(-1,-2);lp.setMargins(dp(5),dp(5),dp(5),dp(6));column.addView(card,lp);return card;}
    void render(){
        if(feed==null)return;cards.removeAllViews();status.setText(feed.status());
        LinearLayout[] col=new LinearLayout[columns];for(int i=0;i<columns;i++){col[i]=Aero.column(getContext());cards.addView(col[i],new LayoutParams(0,-2,1));}
        JSONArray source=savedOnly?saved():FeedClient.items(store);int n=0;
        if(source.length()==0){LinearLayout welcome=card(col[0],"News feed setup");welcome.addView(text(savedOnly?"Your saved stories":"Your interests, at a glance",23,Color.WHITE));TextView hint=text(savedOnly?"Use Save on any story to keep its title and link here.":"Connect a news feed to see real headlines here. Weather uses your selected city. You can also add your own HTTPS RSS or Atom feed.",13,0xffcccccc);hint.setPadding(0,dp(12),0,dp(12));welcome.addView(hint);if(!savedOnly){welcome.addView(command("Connect a news feed","Choose news feed",this::configure));}}
        for(int i=0;i<source.length();i++){
            JSONObject item=source.optJSONObject(i);if(item==null||!BrowserUrls.web(item.optString("url")))continue;
            String title=item.optString("title"),url=item.optString("url");LinearLayout story=card(col[n%Math.max(1,columns==3?2:columns)],"News story: "+title);
            String thumbnail=item.optString("image");if(store.prefs.getBoolean("ten_news_images",false)&&FeedClient.validUrl(thumbnail)){ImageView image=new ImageView(getContext());image.setScaleType(ImageView.ScaleType.CENTER_CROP);image.setBackgroundColor(0xff3a4c59);image.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);story.addView(image,new LayoutParams(-1,dp(n==0?190:140)));images.load(thumbnail,image);}
            String host=android.net.Uri.parse(url).getHost();TextView byline=text((host!=null&&(host.endsWith("bbc.co.uk")||host.endsWith("bbc.com"))?"BBC News":host==null?"News":host.replaceFirst("^www\\.",""))+"  ·  "+(savedOnly?"Saved story":"Latest feed"),11,0xffb9b9b9);story.addView(byline);
            TextView headline=text(title,n==0?23:18,Color.WHITE);Aero.bold(headline);headline.setPadding(0,dp(12),0,dp(12));headline.setFocusable(true);headline.setOnClickListener(v->open.accept("favorite-web:"+url));headline.setContentDescription("Read story: "+title);story.addView(headline);
            String summary=item.optString("summary");if(!summary.isEmpty()){TextView excerpt=text(summary,13,0xffd4d4d4);excerpt.setMaxLines(n==0?4:3);excerpt.setEllipsize(android.text.TextUtils.TruncateAt.END);story.addView(excerpt);}
            LinearLayout actions=new LinearLayout(getContext());actions.addView(command(isSaved(url)?"Unsave":"Save","Save story: "+title,()->{bookmark(item);render();}),new LayoutParams(0,dp(44),1));actions.addView(command("Nth Nota","Discuss headline with Nth Nota",()->{if(getContext() instanceof MainActivity)((MainActivity)getContext()).nota(new TenNotaContext("News headline",title+(summary.isEmpty()?"":"\n\n"+summary)+"\n\nSource: "+url,false));}),new LayoutParams(0,dp(44),1));story.addView(actions);n++;
        }
        LinearLayout sidebar=col[columns-1];weather(sidebar);LinearLayout games=card(sidebar,"Nth games card");games.addView(text("Games",17,Color.WHITE));games.addView(command("Tile puzzle","Play tile puzzle",()->open.accept("@gadget:puzzle")));games.addView(command("Dice roller","Open dice roller",()->open.accept("@gadget:dice")));games.addView(command("Calculator","Open calculator",()->open.accept("@gadget:calculator")));
    }
    private void weather(LinearLayout column){LinearLayout panel=card(column,"News weather card");panel.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff526f93,0xff273c61}));panel.addView(text(weather.city(),15,Color.WHITE));WeatherClient.Snapshot snapshot=weather.snapshot();
        if(snapshot==null){panel.addView(text("Choose your weather location",19,Color.WHITE));}else{boolean f=store.prefs.getBoolean("weather_f",java.util.Locale.getDefault().getCountry().equals("US"));panel.addView(text(snapshot.temperature(f)+(f?"°F":"°C"),38,Color.WHITE));panel.addView(text(snapshot.condition()+(weather.stale()?" · Cached":""),14,Color.WHITE));
            try{JSONArray points=new JSONObject(store.prefs.getString("weather_json","{}")).getJSONObject("properties").getJSONArray("timeseries");LinearLayout hours=new LinearLayout(getContext());for(int i=1;i<Math.min(5,points.length());i++){JSONObject p=points.getJSONObject(i);java.time.Instant instant=java.time.Instant.parse(p.getString("time"));double temp=p.getJSONObject("data").getJSONObject("instant").getJSONObject("details").getDouble("air_temperature");String time=android.text.format.DateFormat.getTimeFormat(getContext()).format(java.util.Date.from(instant));TextView hour=text(time+"\n"+Math.round(f?temp*9/5+32:temp)+"°",11,Color.WHITE);hour.setGravity(Gravity.CENTER);hour.setPadding(0,dp(12),0,dp(12));hours.addView(hour,new LayoutParams(0,-2,1));}panel.addView(hours);}catch(Exception ignored){}
        }panel.addView(command("See forecast","Open weather forecast",()->open.accept("@weather")));
    }
    private JSONArray saved(){try{return new JSONArray(store.prefs.getString("ten_saved_news","[]"));}catch(JSONException ignored){return new JSONArray();}}
    private boolean isSaved(String url){JSONArray rows=saved();for(int i=0;i<rows.length();i++)if(rows.optJSONObject(i)!=null&&url.equals(rows.optJSONObject(i).optString("url")))return true;return false;}
    private void bookmark(JSONObject item){JSONArray before=saved(),after=new JSONArray();boolean found=false;for(int i=0;i<before.length();i++){JSONObject row=before.optJSONObject(i);if(row==null)continue;if(row.optString("url").equals(item.optString("url")))found=true;else if(after.length()<39)after.put(row);}if(!found)after.put(item);store.prefs.edit().putString("ten_saved_news",after.toString()).apply();}
    void configure(){LinearLayout body=Aero.column(getContext());Aero.pad(body,16);body.addView(text("Choose an HTTPS RSS or Atom feed. Connecting downloads titles, summaries and links from the chosen site. Saved stories stay on this device.",13,Aero.INK));EditText address=new EditText(getContext());address.setSingleLine();address.setText(feed.url());address.setHint("https://…/feed.xml");address.setContentDescription("News feed address");body.addView(address);CheckBox thumbnails=Aero.check(getContext(),"Load article images from the feed’s image hosts",store.prefs.getBoolean("ten_news_images",false));body.addView(thumbnails);body.addView(Aero.button(getContext(),"Use BBC News",()->address.setText("https://feeds.bbci.co.uk/news/rss.xml")));android.app.AlertDialog dialog=Aero.dialog(getContext()).setTitle("Manage news and interests").setView(body).setPositiveButton("Connect",null).setNegativeButton("Cancel",null).show();dialog.getButton(-1).setOnClickListener(v->{String url=address.getText().toString().trim();if(!FeedClient.validUrl(url)){address.setError("Enter an HTTPS RSS or Atom feed address");return;}store.prefs.edit().putBoolean("ten_news_images",thumbnails.isChecked()).apply();started=true;feed.active(true);feed.configure(url);dialog.dismiss();});}
    @Override public void onVisibilityAggregated(boolean visible){super.onVisibilityAggregated(visible);if(feed!=null)feed.active(visible&&started);if(weather!=null)weather.active(visible);}
    @Override protected void onDetachedFromWindow(){feed.close();weather.close();images.close();super.onDetachedFromWindow();}
}
