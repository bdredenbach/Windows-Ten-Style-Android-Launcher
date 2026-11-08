package com.nthexperience.desktop;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.*;

/** Composer-only local commands. Generated replies and imported passages never enter this resolver. */
final class TenNotaActions {
    enum Kind { OPEN, WEB_SEARCH, FILE_SEARCH, WEBSITE, DESKTOP, NEW_DESKTOP, NEXT_DESKTOP, PREVIOUS_DESKTOP }
    static final class Action {
        final String label,value;final Kind kind;
        Action(String label,Kind kind,String value){this.label=label;this.kind=kind;this.value=value;}
    }
    static final class Target {
        final String title,route;final List<String> names;
        Target(String title,String route,String...aliases){this.title=title;this.route=route;names=new ArrayList<>();names.add(key(title));for(String alias:aliases)names.add(key(alias));}
        Action action(){return new Action("Open "+title,Kind.OPEN,route);}
    }
    static final class Plan {
        final String message;final List<Action> actions;final boolean autoOpen;
        Plan(String message,Action...actions){this(message,Arrays.asList(actions),false);}
        Plan(String message,List<Action> actions){this(message,actions,false);}
        Plan(String message,List<Action> actions,boolean autoOpen){this.message=message;this.actions=actions;this.autoOpen=autoOpen&&actions.size()==1&&app(actions.get(0));}
    }
    static final String[] EXAMPLES={"Open Nth Browser","Open Themes","Open Wi-Fi settings","Create a new desktop","What can you do?"};
    static List<Target> builtins(){List<Target> out=new ArrayList<>();
        out.add(new Target("Nth Browser","@browser","browser","nth internet explorer","internet explorer"));
        out.add(new Target("Notepad","@notes","nth notepad","notes"));out.add(new Target("File Explorer","@files","files","explorer","nth explorer","file manager"));
        out.add(new Target("Calculator","@gadget:calculator","calc","nth calculator"));out.add(new Target("Unit converter","@gadget:converter"));out.add(new Target("Text counter","@gadget:textcounter"));
        out.add(new Target("Calendar","@calendar"));out.add(new Target("Weather","@weather","forecast"));out.add(new Target("News and interests","@news","news","news feed"));
        out.add(new Target("Settings","@settings","control panel","nth settings"));out.add(new Target("Task View","@taskview","virtual desktops","desktops"));out.add(new Target("Task Manager","@taskmanager"));
        out.add(new Target("Action Center","@tenactions","notifications"));out.add(new Target("Recycle Bin","@recycle"));out.add(new Target("Photos","@photos","photo gallery"));out.add(new Target("Sticky Notes","@sticky"));
        out.add(new Target("Documents","@documents","documents folder"));out.add(new Target("Pictures","@pictures","pictures folder"));out.add(new Target("Music","@music","music folder"));out.add(new Target("Nth Media Player","@mediaplayer","media player"));
        out.add(new Target("Tile puzzle","@gadget:puzzle","puzzle"));out.add(new Target("Dice roller","@gadget:dice","dice"));
        out.add(new Target("Wi-Fi settings","setting:android:wifi","wifi","wi fi","wireless settings"));out.add(new Target("Bluetooth settings","setting:android:bluetooth","bluetooth"));
        out.add(new Target("Android settings","setting:android:settings","phone settings","device settings"));
        for(TenSettingsCatalog.Page p:TenSettingsCatalog.PAGES)out.add(new Target(p.title+" settings","ten-setting:"+p.id,p.title));
        return out;
    }
    static Plan help(){return new Plan("I can open Nth tools and installed phone apps, jump to Settings pages, search the web or linked files, show your desktop, and create or switch virtual desktops. These commands stay on this device and work without an AI model. Send a clear app-opening request to open it automatically. If more than one app matches, click the shortcut you want. Other desktop actions use the named shortcuts.\n\nTry: “Open Nth Browser”, “Open Themes”, “Open Wi-Fi settings”, “Search the web for Android tips”, “Find files named budget”, or “Create a new desktop”. Android settings open for you to change their switches. I can also draft, proofread, polish and summarize with writing assistance.",new Action("Open Nth Browser",Kind.OPEN,"@browser"),new Action("Open Themes",Kind.OPEN,"ten-setting:themes"),new Action("Open Wi-Fi settings",Kind.OPEN,"setting:android:wifi"),new Action("Open Task View",Kind.OPEN,"@taskview"));}
    static Plan resolve(String input,Collection<Target> installed){
        if(input==null||input.length()>1500||input.indexOf('\n')>=0||input.indexOf('\r')>=0)return null;
        String text=input.trim().replaceAll("[?!.]+$","").trim();
        text=text.replaceFirst("(?i)^(?:hey |hi )?(?:nth )?nota[, ]+","").replaceFirst("(?i)^(?:can you|could you|would you|will you)(?: please)?\\s+","").replaceFirst("(?i)^please\\s+","").replaceFirst("(?i)[, ]+please$","").trim();
        String k=key(text);
        if(k.equals("switch apps"))return new Plan("Switch between apps and desktops in Task View.",new Action("Open Task View",Kind.OPEN,"@taskview"));
        if(Arrays.asList("what can you do","what else can you do","help","help me use nth nota","show commands","show suggestions","what can nota do").contains(k))return help();
        if(k.matches("(?:show|go to)(?: the| my)? desktop"))return new Plan("Show your desktop by minimizing its built-in windows. Use their taskbar buttons to restore them.",new Action("Show desktop",Kind.DESKTOP,""));
        if(k.matches("(?:create|make|add)(?: a)?(?: new)?(?: virtual)? desktop"))return new Plan("Create a new, empty desktop and switch to it. Your current desktop and windows are kept.",new Action("Create new desktop",Kind.NEW_DESKTOP,""));
        if(k.matches("(?:(?:switch|go)(?: to)?(?: the)? )?next(?: virtual)? desktop"))return new Plan("Switch to the next virtual desktop.",new Action("Next desktop",Kind.NEXT_DESKTOP,""));
        if(k.matches("(?:(?:switch|go)(?: to)?(?: the)? )?(?:previous|prior)(?: virtual)? desktop"))return new Plan("Switch to the previous virtual desktop.",new Action("Previous desktop",Kind.PREVIOUS_DESKTOP,""));
        Matcher web=Pattern.compile("(?i)^search (?:the )?web for (.+)$").matcher(text);if(web.matches())return query(web.group(1),true);
        Matcher files=Pattern.compile("(?i)^(?:find (?:my )?files (?:named|called|for)|search (?:my )?(?:linked )?files for) (.+)$").matcher(text);if(files.matches())return query(files.group(1),false);
        Matcher toggle=Pattern.compile("(?i)^(?:turn|switch) (?:on|off) (wi-?fi|bluetooth)$").matcher(text);if(toggle.matches()){boolean wifi=key(toggle.group(1)).replace(" ","").equals("wifi");return new Plan("Open Android "+(wifi?"Wi-Fi":"Bluetooth")+" settings, where you can change the switch.",new Action("Open "+(wifi?"Wi-Fi":"Bluetooth")+" settings",Kind.OPEN,wifi?"setting:android:wifi":"setting:android:bluetooth"));}
        Matcher open=Pattern.compile("(?i)^(?:open|launch|start|show(?: me)?|take me to|go to) (.+)$").matcher(text);if(!open.matches())return null;
        String target=open.group(1).trim();
        if(target.matches("https?://\\S+")){try{java.net.URI uri=new java.net.URI(target);if(uri.getHost()!=null&&uri.getUserInfo()==null)return new Plan("Open this website in Nth Browser: "+target,new Action("Open website",Kind.WEBSITE,target));}catch(Exception ignored){}return new Plan("Enter a complete HTTP or HTTPS website address.");}
        if(target.startsWith("@")||target.matches("(?i)(?:intent|setting|ten-setting|javascript|file|content):.*"))return new Plan("Use an app name, Settings page, or HTTP/HTTPS website address.");
        String name=key(target);List<Action> matches=target(name,installed);if(matches.isEmpty())matches=target(name.replaceFirst("^(?:the|my) ",""),installed);
        if(matches.isEmpty())return new Plan("I couldn't find “"+target+"”. Try the app's full name, or open Start to find it. Ask “What can you do?” for examples.",new Action("Find an app in Start",Kind.OPEN,"@search"));
        if(matches.size()>1){
            List<Action> identified=new ArrayList<>();
            for(Action action:matches){
                boolean owned=action.value.startsWith("@")||action.value.startsWith("ten-setting:")||action.value.startsWith("setting:");
                String label=action.label,source=owned?"Nth Desktop":action.value.split("/")[0];
                if(!owned){
                    long siblings=matches.stream().filter(other->other.label.equals(action.label)&&other.value.startsWith(source+"/")).count();
                    if(siblings>1){String component=action.value.substring(action.value.indexOf('/')+1);if(component.startsWith(source+"."))component=component.substring(source.length()+1);if(component.startsWith("."))component=component.substring(1);label+=" · "+component;}
                }
                identified.add(new Action(label+" · "+source,action.kind,action.value));
            }
            matches=identified;
        }
        boolean auto=matches.size()==1&&app(matches.get(0));
        return new Plan(matches.size()==1?(auto?"Opening "+matches.get(0).label.replaceFirst("^Open ","")+"…":"Found the shortcut. Click the shortcut above."):"Found matching apps. Click the shortcut you want above.",matches,auto);
    }
    private static boolean app(Action action){return action.kind==Kind.OPEN&&(action.value.startsWith("@")||action.value.matches("[^\\s/:]+/[^\\s/:]+"));}
    private static Plan query(String query,boolean web){query=query.trim();if(query.isEmpty()||query.length()>512)return new Plan("Use a search of up to 512 characters.");return new Plan(web?"Search the web in Nth Browser. Your search is sent to the search engine when you tap the button.":"Search the files and folders you've linked to Nth Desktop. File permissions still apply.",new Action(web?"Search web: "+query:"Find files: "+query,web?Kind.WEB_SEARCH:Kind.FILE_SEARCH,query));}
    private static List<Action> target(String name,Collection<Target> installed){List<Action> known=matches(name,builtins()),apps=matches(name,installed),out=new ArrayList<>();if(!known.isEmpty())out.add(known.get(0));for(Action app:apps)if(out.stream().noneMatch(a->a.value.equals(app.value))&&out.size()<8)out.add(app);return out;}
    private static List<Action> matches(String name,Collection<Target> targets){List<Action> actions=new ArrayList<>();Set<String> seen=new HashSet<>();if(targets!=null)for(Target target:targets)if(target.names.contains(name)&&seen.add(target.route)&&actions.size()<8)actions.add(target.action());return actions;}
    static String key(String value){return Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+"," ").trim().replaceAll("\\s+"," ");}
    private TenNotaActions(){}
}
