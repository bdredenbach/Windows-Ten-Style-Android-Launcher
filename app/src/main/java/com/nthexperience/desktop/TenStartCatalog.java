package com.nthexperience.desktop;

import java.util.*;

/** Built-in Start apps, their initial categories, tile sizes and individual colors. */
final class TenStartCatalog {
 static final String[] GROUPS={"Productivity","Internet & information","Photos & media","Files & desktop","System tools","Games"};
 static final class App {
  final String id,title,icon,group,size;final int color;final boolean live;
  App(String id,String title,String icon,int category,String size,int color,boolean live){this.id=id;this.title=title;this.icon=icon;this.group=GROUPS[category];this.size=size;this.color=color;this.live=live;}
 }
 static final List<App> APPS=Collections.unmodifiableList(Arrays.asList(
  a("@nota","Nth Nota","notes",0,"Large",0xff744da9,false),
  a("@notes","Notepad","notes",0,"Wide",0xff0063b1,true),
  a("@sticky","Sticky Notes","sticky",0,"Normal",0xffffb900,true),
  a("@calendar","Calendar","calendar",0,"Large",0xff0078d7,true),
  a("@gadget:calculator","Calculator","calculator",0,"Normal",0xff3b3b3b,false),
  a("@gadget:checklist","Checklist","checklist",0,"Wide",0xff008272,true),
  a("@gadget:converter","Unit Converter","converter",0,"Normal",0xff038387,false),
  a("@gadget:colors","Color Studio","appearance",0,"Normal",0xffc239b3,false),
  a("@gadget:textcounter","Text Counter","notes",0,"Normal",0xff8764b8,false),
  a("@clock","Clock","clock",0,"Wide",0xff005a9e,true),
  a("@gadget:worldclock","World Clock","worldclock",0,"Normal",0xff2d7d9a,false),
  a("@gadget:timer","Countdown","timer",0,"Normal",0xff69797e,false),
  a("@gadget:stopwatch","Stopwatch","stopwatch",0,"Normal",0xff4c4a48,false),
  a("@gadget:daysuntil","Days Until","calendar",0,"Normal",0xffb146c2,false),
  a("@browser","Nth Internet Explorer","browser",1,"Large",0xff0063b1,false),
  a("@weather","Weather","weather",1,"Wide",0xff0078d7,true),
  a("@news","News and interests","feeds",1,"Wide",0xffd13438,true),
  a("@gadget:feeds","Feed Headlines","feeds",1,"Normal",0xffca5010,true),
  a("@photos","Photos","pictures-app",2,"Large",0xff0078d7,false),
  a("@mediaplayer","Nth Media Player","mediaplayer",2,"Wide",0xffda3b01,false),
  a("@mediacenter","Nth Media Center","mediacenter",2,"Normal",0xff107c10,false),
  a("@files","File Explorer","files",3,"Wide",0xffffb900,false),
  a("@computer","Computer","computer",3,"Normal",0xff2d7d9a,false),
  a("@recycle","Recycle Bin","recycle",3,"Normal",0xff69797e,false),
  a("@backup","Backup and Restore","drive",3,"Wide",0xff038387,false),
  a("@recent","Recent Items","document",3,"Normal",0xff8764b8,false),
  a("@workspaces","Saved Workspaces","appearance",3,"Normal",0xff486860,false),
  a("@organize","Desktop Organization","folderapps",3,"Normal",0xffb17d00,false),
  a("@quicklaunch","Quick Launch","programs",3,"Normal",0xff986f0b,false),
  a("@settings","Settings","control",4,"Normal",0xff5d5a58,false),
  a("@taskmanager","Task Manager","taskmanager",4,"Wide",0xff107c10,false),
  a("@battery","Battery","power",4,"Normal",0xff498205,true),
  a("@events","Event Viewer","notes",4,"Normal",0xff4c4a48,false),
  a("@devices","Device Manager","computer",4,"Normal",0xff2d7d9a,false),
  a("@management","Computer Management","computer",4,"Normal",0xff69797e,false),
  a("@powershell","Nth PowerShell","run",4,"Wide",0xff003966,false),
  a("@run","Run","run",4,"Normal",0xff005a9e,false),
  a("@gadget:storage","Storage Meter","drive",4,"Normal",0xff567c73,false),
  a("@gadget:network","Network Meter","network",4,"Normal",0xff2d7d9a,false),
  a("@gadget:compass","Compass","compass",4,"Normal",0xffa4262c,false),
  a("@welcome","Welcome Center","orb",4,"Normal",0xff0078d7,false),
  a("@gadget:puzzle","Tile Puzzle","puzzle",5,"Large",0xff744da9,false),
  a("@gadget:dice","Dice Roller","puzzle",5,"Normal",0xff107c10,false)
 ));
 private static App a(String id,String title,String icon,int group,String size,int color,boolean live){return new App(id,title,icon,group,size,color,live);}
 static App app(String id){for(App app:APPS)if(app.id.equals(id))return app;return null;}
 static List<String> ids(){List<String> ids=new ArrayList<>();for(App app:APPS)ids.add(app.id);return ids;}
}
