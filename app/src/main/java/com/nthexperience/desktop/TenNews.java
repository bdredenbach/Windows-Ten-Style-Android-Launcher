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
    final TenNewsImages images=new TenNewsImages();final LinearLayout cards; final TextView status; boolean savedOnly,started; int columns=1;ConfigSession configuration;
    TenNews(Context c,DesktopStore store,Consumer<String> open,WeatherClient weather){this(c,store,open,weather,url->(java.net.HttpURLConnection)url.openConnection(),System::currentTimeMillis);}
    TenNews(Context c,DesktopStore store,Consumer<String> open,WeatherClient weather,FeedClient.Connections connections,java.util.function.LongSupplier clock){
        super(c);this.store=store;this.open=open;this.weather=new WeatherClient(store,()->post(this::render));feed=new FeedClient(store,()->post(this::render),connections,clock);started=!feed.url().isEmpty();setOrientation(VERTICAL);setBackgroundColor(0xff202020);setContentDescription("News and interests dashboard");
        LinearLayout toolbar=new LinearLayout(c);toolbar.setGravity(Gravity.CENTER_VERTICAL);toolbar.setPadding(dp(12),0,dp(8),0);
        TextView brand=text("N  /  News and interests",16,Color.WHITE);toolbar.addView(brand,new LayoutParams(0,dp(48),1));
        toolbar.addView(command("↻","Refresh news",()->{if(!FeedClient.validUrl(feed.attemptedUrl())){configure();return;}started=true;feed.active(true);feed.refresh(true);weather.refresh(true);}),new LayoutParams(dp(44),dp(44)));
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
        if(feed==null)return;updateConfiguration();cards.removeAllViews();status.setText(feed.status());status.setContentDescription("News connection status");
        LinearLayout[] col=new LinearLayout[columns];for(int i=0;i<columns;i++){col[i]=Aero.column(getContext());cards.addView(col[i],new LayoutParams(0,-2,1));}
        JSONArray source=savedOnly?saved():FeedClient.items(store);int n=0;
        if(source.length()==0){LinearLayout welcome=card(col[0],"News feed setup");boolean busy=feed.loading(),failed=!feed.error().isEmpty(),connected=!feed.url().isEmpty();String title=savedOnly?"Your saved stories":busy?"Connecting to your feed":failed?"Could not read this feed":connected?"No headlines published yet":"Your interests, at a glance";welcome.addView(text(title,23,Color.WHITE));String message=savedOnly?"Use Save on any story to keep its title and link here.":busy?"Headlines will appear here when the connection finishes.":failed?feed.error():connected?"The feed is connected but has no readable articles yet. Refresh later or choose another feed.":"Connect a news feed to see real headlines here. Weather uses your selected city. You can also add your own HTTPS RSS or Atom feed.";TextView hint=text(message,13,0xffcccccc);hint.setPadding(0,dp(12),0,dp(12));welcome.addView(hint);if(!savedOnly)welcome.addView(command(connected?"Manage news feed":"Connect a news feed","Choose news feed",this::configure));}

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
    private static final class ConfigSession {android.app.AlertDialog dialog;EditText address;TextView feedback;CheckBox thumbnails;final java.util.List<View> sources=new java.util.ArrayList<>();int token=-1;}
    void configure(){
        if(configuration!=null&&configuration.dialog.isShowing())return;
        ConfigSession session=new ConfigSession();configuration=session;LinearLayout body=Aero.column(getContext());Aero.pad(body,16);
        body.addView(text("Choose a source below, or enter an HTTPS RSS or Atom feed. Then tap Connect. Your current feed and saved stories are kept if the connection fails.",13,Aero.INK));
        session.address=new EditText(getContext());session.address.setSingleLine();session.address.setText(feed.attemptedUrl());session.address.setHint("https://…/feed.xml");session.address.setContentDescription("News feed address");body.addView(session.address);
        session.thumbnails=Aero.check(getContext(),"Load article images from the feed's image hosts",store.prefs.getBoolean("ten_news_images",false));body.addView(session.thumbnails);
        for(String[] source:new String[][]{{"Use BBC News",FeedClient.BBC},{"Use TechRadar",FeedClient.TECHRADAR}}){TextView choice=Aero.button(getContext(),source[0],()->{session.address.setText(source[1]);session.feedback.setVisibility(GONE);});session.sources.add(choice);body.addView(choice);}
        session.feedback=text("",13,Aero.INK);session.feedback.setContentDescription("Feed connection result");session.feedback.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);session.feedback.setPadding(0,dp(10),0,0);session.feedback.setVisibility(GONE);body.addView(session.feedback);
        ScrollView scroll=new ScrollView(getContext());scroll.addView(body);session.dialog=Aero.dialog(getContext()).setTitle("Manage news and interests").setView(scroll).setPositiveButton("Connect",null).setNegativeButton("Cancel",null).show();
        if(session.dialog.getWindow()!=null)session.dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        session.dialog.setOnDismissListener(d->{feed.cancelConfiguration(session.token);if(configuration==session)configuration=null;});
        session.dialog.getButton(-1).setOnClickListener(v->{String url=session.address.getText().toString().trim();if(!FeedClient.validUrl(url)){session.address.setError("Enter an HTTPS RSS or Atom feed address");return;}store.prefs.edit().putBoolean("ten_news_images",session.thumbnails.isChecked()).apply();started=true;((android.view.inputmethod.InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(session.address.getWindowToken(),0);busy(session,true);session.token=feed.configure(url);});
    }
    private void busy(ConfigSession session,boolean value){session.address.setEnabled(!value);for(View source:session.sources)source.setEnabled(!value);session.dialog.getButton(-1).setEnabled(!value);session.dialog.getButton(-1).setText(value?"Connecting…":"Connect");if(value){session.feedback.setText("Connecting to "+FeedClient.source(session.address.getText().toString())+"…");session.feedback.setVisibility(VISIBLE);}}
    private void updateConfiguration(){ConfigSession session=configuration;if(session==null||session.token<0)return;if(feed.loading())return;if(feed.succeeded(session.token)){configuration=null;session.dialog.dismiss();return;}busy(session,false);session.feedback.setText(feed.error());session.feedback.setVisibility(feed.error().isEmpty()?GONE:VISIBLE);session.feedback.post(()->session.feedback.requestRectangleOnScreen(new android.graphics.Rect(0,0,session.feedback.getWidth(),session.feedback.getHeight()),true));}
    @Override public void onVisibilityAggregated(boolean visible){super.onVisibilityAggregated(visible);if(feed!=null)feed.active(visible&&started);if(weather!=null)weather.active(visible);}
    @Override protected void onDetachedFromWindow(){if(configuration!=null){android.app.AlertDialog dialog=configuration.dialog;configuration=null;dialog.dismiss();}feed.close();weather.close();images.close();super.onDetachedFromWindow();}
}
