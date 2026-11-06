package com.nthexperience.desktop;

import android.content.Context;

/** Data providers for News, Weather and built-in tools. There is no desktop gadget surface. */
final class TenServices {
    final WeatherClient weather;
    final FeedClient feeds;
    final GadgetHardware hardware;
    private final Runnable changed;
    private boolean active,weatherOpen,feedOpen,networkOpen,compassOpen;
    TenServices(Context context,DesktopStore store,Runnable changed){this.changed=changed;weather=new WeatherClient(store,this::refresh);feeds=new FeedClient(store,this::refresh);hardware=new GadgetHardware(context,this::refresh);}
    void refresh(){changed.run();}
    void details(boolean weather,boolean feed,boolean network,boolean compass){if(weatherOpen==weather&&feedOpen==feed&&networkOpen==network&&compassOpen==compass)return;weatherOpen=weather;feedOpen=feed;networkOpen=network;compassOpen=compass;update();}
    void setActive(boolean value){active=value;update();}
    private void update(){weather.active(active&&weatherOpen);feeds.active(active&&feedOpen);hardware.active(active&&(networkOpen||compassOpen),networkOpen,compassOpen);}
    void close(){weather.close();feeds.close();hardware.close();}
}
