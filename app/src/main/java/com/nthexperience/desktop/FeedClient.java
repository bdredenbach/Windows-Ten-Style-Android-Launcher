package com.nthexperience.desktop;

import android.net.Uri;
import android.os.*;
import org.json.*;
import org.xmlpull.v1.XmlPullParserException;
import java.io.*;
import java.net.*;
import javax.net.ssl.SSLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.LongSupplier;
import java.util.zip.GZIPInputStream;

/** RSS/Atom connection, streaming parsing and a cache that never hides a failed retry. */
final class FeedClient {
    static final String BBC="https://feeds.bbci.co.uk/news/rss.xml",TECHRADAR="https://www.techradar.com/feeds.xml";
    static final String AGENT="NthDesktopTen/"+BuildConfig.VERSION_NAME+" (+https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher)";
    interface Connections {HttpURLConnection open(URL url)throws IOException;}
    private final DesktopStore store;private final Runnable changed;private final Connections connections;private final LongSupplier clock;
    private final Handler main=new Handler(Looper.getMainLooper());private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private boolean active,closed,setup;private volatile boolean loading;private volatile HttpURLConnection connection;private volatile int generation;
    private int success=-1;private String error="",attempted="";long lastBytes;
    FeedClient(DesktopStore store,Runnable changed){this(store,changed,url->(HttpURLConnection)url.openConnection(),System::currentTimeMillis);}
    FeedClient(DesktopStore store,Runnable changed,Connections connections,LongSupplier clock){this.store=store;this.changed=changed;this.connections=connections;this.clock=clock;String url=url();if(url.equals(store.prefs.getString("feed_error_url",""))&&clock.getAsLong()<store.prefs.getLong("feed_retry_at",0))error=store.prefs.getString("feed_error","");}
    static boolean validUrl(String value){try{if(value==null||value.length()>2048)return false;for(int i=0;i<value.length();i++)if(Character.isWhitespace(value.charAt(i))||Character.isISOControl(value.charAt(i)))return false;Uri uri=Uri.parse(value);return "https".equalsIgnoreCase(uri.getScheme())&&uri.getHost()!=null&&!uri.getHost().isEmpty()&&uri.getUserInfo()==null&&(uri.getPort()==-1||uri.getPort()>0&&uri.getPort()<=65535);}catch(RuntimeException ex){return false;}}
    String url(){return store.prefs.getString("feed_url","");}String attemptedUrl(){return attempted.isEmpty()?url():attempted;}String error(){return error;}boolean loading(){return loading;}boolean succeeded(int token){return token==success;}
    static String source(String url){Uri uri=Uri.parse(url);String host=uri.getHost();if(host==null)return "this feed";if(host.equals("feeds.bbci.co.uk"))return "BBC News";if(host.equals("www.techradar.com")||host.equals("techradar.com"))return "TechRadar";return host.replaceFirst("^www\\.","");}
    String status(){if(loading)return (setup?"Connecting to ":"Updating ")+source(attempted)+"…";if(!error.isEmpty())return error+(items(store).length()>0?" Showing cached headlines from "+source(url())+".":"");if(url().isEmpty())return "Choose an RSS or Atom feed.";long updated=store.prefs.getLong("feed_updated",0);return source(url())+(updated>0?" • Updated "+android.text.format.DateFormat.getTimeFormat(store.context).format(new Date(updated)):" • No headlines yet.");}
    static JSONArray items(DesktopStore store){try{return new JSONArray(store.prefs.getString("feed_items","[]"));}catch(JSONException ex){return new JSONArray();}}
    void active(boolean value){active=value;if(value)refresh(false);}
    int configure(String address){if(!validUrl(address))throw new IllegalArgumentException("Use an HTTPS feed address");active=true;return start(address,true,true);}
    void cancelConfiguration(int token){if(!setup||generation!=token||!loading)return;cancel();error="";changed.run();}
    private void cancel(){generation++;HttpURLConnection old=connection;if(old!=null)old.disconnect();loading=false;setup=false;}
    void refresh(boolean requested){String address=url();if(requested&&!validUrl(address)&&validUrl(attempted))address=attempted;if(closed||!active||loading||!validUrl(address))return;long now=clock.getAsLong();boolean hasCache=store.prefs.getLong("feed_updated",0)>0;if(!requested&&hasCache&&now<store.prefs.getLong("feed_expires",0))return;start(address,!address.equals(url()),requested);}
    private int start(String address,boolean configuring,boolean requested){
        cancel();int token=generation;attempted=address;setup=configuring;error="";long now=clock.getAsLong(),retry=store.prefs.getLong("feed_retry_at",0);boolean same=address.equals(store.prefs.getString("feed_error_url",""));
        if(same&&now<retry&&(!requested||store.prefs.getBoolean("feed_retry_server",false))){setup=false;error=store.prefs.getString("feed_error","Try this feed again later.");changed.run();return token;}
        loading=true;changed.run();worker.execute(()->{
            if(closed||token!=generation)return;JSONArray result=null;String failure="";long expires=clock.getAsLong()+900000,retryAt=0,bytes=0;boolean serverDelay=false;
            try{String next=address;for(int redirect=0;redirect<5;redirect++){
                if(!validUrl(next))throw new IOException("FEED_REDIRECT");HttpURLConnection current=connections.open(new URL(next));if(closed||token!=generation){current.disconnect();return;}connection=current;
                try{current.setInstanceFollowRedirects(false);current.setConnectTimeout(6000);current.setReadTimeout(8000);current.setRequestProperty("User-Agent",AGENT);current.setRequestProperty("Accept","application/rss+xml, application/atom+xml, application/rdf+xml, application/xml, text/xml;q=0.9, */*;q=0.1");current.setRequestProperty("Accept-Encoding","gzip");int code=current.getResponseCode();long delay=retryAfter(current,clock.getAsLong());
                    if(code>=300&&code<400){if(delay>clock.getAsLong()){serverDelay=true;retryAt=delay;throw new IOException("FEED_WAIT");}String location=current.getHeaderField("Location");if(location==null)throw new IOException("FEED_REDIRECT");next=new URL(new URL(next),location).toString();continue;}
                    if(code!=200){if(code==429||code==503&&delay>clock.getAsLong()){serverDelay=true;retryAt=delay>clock.getAsLong()?delay:clock.getAsLong()+120000;}throw new IOException("FEED_HTTP_"+code);}
                    long header=current.getHeaderFieldDate("Expires",0);if(header>clock.getAsLong())expires=Math.max(clock.getAsLong()+60000,Math.min(clock.getAsLong()+3600000,header));
                    InputStream input=current.getInputStream();if("gzip".equalsIgnoreCase(current.getContentEncoding()))input=new GZIPInputStream(input);try(FeedXml.Limited bounded=new FeedXml.Limited(input)){result=FeedXml.parse(bounded,next,current.getContentType());bytes=bounded.count;}break;
                }finally{current.disconnect();if(connection==current)connection=null;}
            }if(result==null)throw new IOException("FEED_REDIRECT");
            }catch(Exception exception){failure=describe(exception,address,retryAt);}
            JSONArray headlines=result;String message=failure;long until=expires,waitUntil=retryAt>0?retryAt:clock.getAsLong()+60000,read=bytes;boolean limited=serverDelay;
            main.post(()->{if(closed||token!=generation)return;error=message;lastBytes=read;
                android.content.SharedPreferences.Editor edit=store.prefs.edit();if(headlines!=null&&message.isEmpty()){success=token;edit.putString("feed_url",address).putString("feed_items",headlines.toString()).putLong("feed_updated",clock.getAsLong()).putLong("feed_expires",until).remove("feed_error_url").remove("feed_error").remove("feed_retry_at").remove("feed_retry_server");}
                else edit.putString("feed_error_url",address).putString("feed_error",message).putLong("feed_retry_at",waitUntil).putBoolean("feed_retry_server",limited);
                edit.apply();setup=false;loading=false;changed.run();});
        });return token;
    }
    private String describe(Exception e,String address,long retryAt){String message=e.getMessage()==null?"":e.getMessage();String site=source(address);
        if(message.startsWith("FEED_HTTP_")){String code=message.substring(10);if(code.equals("404"))return "That feed address was not found (HTTP 404). Check the RSS or Atom link.";if(code.equals("401")||code.equals("403"))return site+" denied access to its feed (HTTP "+code+"). Try another feed.";if(code.equals("429")||retryAt>clock.getAsLong())return site+" asked us to wait. Try again after "+time(retryAt)+".";return site+" returned HTTP "+code+". Try again later.";}
        if(message.equals("FEED_WAIT"))return site+" asked us to wait. Try again after "+time(retryAt)+".";
        if(message.equals("FEED_HTML"))return "That address returned a web page, not a news feed. Use the site's RSS or Atom link.";
        if(message.equals("FEED_TOO_LARGE"))return "This feed is too large to read. Try a section-specific feed.";
        if(message.equals("FEED_DOCTYPE"))return "This feed uses an unsupported XML document declaration. Try another feed.";
        if(message.equals("FEED_REDIRECT"))return "This feed redirected to an unsupported address or too many times. Use its current HTTPS feed link.";
        if(message.equals("FEED_FORMAT")||e instanceof XmlPullParserException)return "The address did not return readable RSS or Atom XML. Check the feed link.";
        if(e instanceof SocketTimeoutException)return site+" took too long to respond. Tap Refresh to try again.";
        if(e instanceof SSLException)return "A secure connection to "+site+" could not be established. Try another feed.";
        return "Could not reach "+site+". Check your connection, then tap Refresh.";
    }
    private String time(long millis){return android.text.format.DateFormat.getTimeFormat(store.context).format(new Date(millis));}
    private static long retryAfter(HttpURLConnection connection,long now){String value=connection.getHeaderField("Retry-After");if(value==null)return 0;try{long seconds=Long.parseLong(value.trim());if(seconds>=0)return Math.addExact(now,Math.multiplyExact(seconds,1000));}catch(Exception ignored){}return connection.getHeaderFieldDate("Retry-After",0);}
    static JSONArray parse(String xml,String base)throws Exception{return FeedXml.parse(xml,base);}
    void close(){closed=true;active=false;cancel();worker.shutdownNow();main.removeCallbacksAndMessages(null);}
}
