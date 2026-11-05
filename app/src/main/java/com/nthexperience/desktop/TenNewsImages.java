package com.nthexperience.desktop;
import android.graphics.*;
import android.os.*;
import android.util.LruCache;
import android.widget.ImageView;
import java.lang.ref.WeakReference;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/** Optional, bounded thumbnails. A repeated card layout shares pending and cached requests. */
final class TenNewsImages {
    final ExecutorService worker=Executors.newFixedThreadPool(2);
    final Set<HttpURLConnection> connections=Collections.synchronizedSet(new HashSet<>());
    final Map<String,List<WeakReference<ImageView>>> waiting=new HashMap<>();
    final Handler main=new Handler(Looper.getMainLooper());
    final LruCache<String,Bitmap> cache=new LruCache<String,Bitmap>(6_000_000){@Override protected int sizeOf(String key,Bitmap value){return value.getAllocationByteCount();}};
    volatile boolean closed;
    void load(String address,ImageView view){
        if(!FeedClient.validUrl(address)||closed)return;Bitmap hit=cache.get(address);if(hit!=null){view.setImageBitmap(hit);return;}
        List<WeakReference<ImageView>> pending=waiting.get(address);if(pending!=null){pending.add(new WeakReference<>(view));return;}
        List<WeakReference<ImageView>> targets=new ArrayList<>();targets.add(new WeakReference<>(view));waiting.put(address,targets);
        worker.execute(()->{Bitmap bitmap=download(address);main.post(()->{List<WeakReference<ImageView>> views=waiting.remove(address);if(bitmap==null)return;if(closed){bitmap.recycle();return;}cache.put(address,bitmap);if(views!=null)for(WeakReference<ImageView> weak:views){ImageView target=weak.get();if(target!=null&&target.isAttachedToWindow())target.setImageBitmap(bitmap);}});});
    }
    private Bitmap download(String address){HttpURLConnection connection=null;try{String url=address;byte[] bytes=null;for(int redirect=0;redirect<4&&!closed;redirect++){if(!FeedClient.validUrl(url))throw new IOException();connection=(HttpURLConnection)new URL(url).openConnection();connections.add(connection);connection.setConnectTimeout(5000);connection.setReadTimeout(6000);connection.setInstanceFollowRedirects(false);connection.setRequestProperty("User-Agent",WeatherClient.AGENT);int code=connection.getResponseCode();if(code>=300&&code<400){url=new URL(new URL(url),connection.getHeaderField("Location")).toString();connection.disconnect();connections.remove(connection);connection=null;continue;}String mime=connection.getContentType();if(code!=200||mime==null||!mime.startsWith("image/")||connection.getContentLengthLong()>1_500_000)throw new IOException();try(InputStream in=connection.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] chunk=new byte[8192];int n;while(!closed&&(n=in.read(chunk))!=-1){if(out.size()+n>1_500_000)throw new IOException();out.write(chunk,0,n);}bytes=out.toByteArray();}break;}if(bytes==null||closed)return null;BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);if(options.outWidth<=0||options.outHeight<=0)return null;options.inSampleSize=1;while((long)(options.outWidth/options.inSampleSize)*(options.outHeight/options.inSampleSize)>700_000)options.inSampleSize*=2;options.inJustDecodeBounds=false;return BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);}catch(Exception|OutOfMemoryError ignored){return null;}finally{if(connection!=null){connection.disconnect();connections.remove(connection);}}}
    void close(){closed=true;synchronized(connections){for(HttpURLConnection connection:connections)connection.disconnect();connections.clear();}worker.shutdownNow();waiting.clear();cache.evictAll();}
}
