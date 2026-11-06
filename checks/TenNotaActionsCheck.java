package com.nthexperience.desktop;
import java.util.*;

public final class TenNotaActionsCheck {
    static TenNotaActions.Plan plan(String text){TenNotaActions.Plan p=TenNotaActions.resolve(text,Collections.emptyList());if(p==null)throw new AssertionError(text);return p;}
    static void route(String text,String route){TenNotaActions.Plan p=plan(text);if(p.actions.size()!=1||!p.actions.get(0).value.equals(route))throw new AssertionError(text);}
    public static void main(String[] args){
        route("Can you open Nth Browser?","@browser");route("Could you please launch my calculator?","@gadget:calculator");route("Please take me to Themes","ten-setting:themes");route("Open Wi-Fi settings","setting:android:wifi");route("Open Bluetooth","setting:android:bluetooth");route("Turn off Wi-Fi","setting:android:wifi");route("Open phone settings","setting:android:settings");
        TenNotaActions.Plan audible=TenNotaActions.resolve("Can you open audible?",Collections.singletonList(new TenNotaActions.Target("Audible","com.example.audible/.Main")));
        if(!audible.autoOpen||!audible.actions.get(0).value.equals("com.example.audible/.Main"))throw new AssertionError("A unique installed app opens on Send");
        if(!plan("Open Nth Browser").autoOpen||plan("Open Themes").autoOpen||plan("Open Wi-Fi settings").autoOpen||plan("What can you do?").autoOpen||plan("Create a new desktop").autoOpen||plan("Search the web for Audible").autoOpen||plan("Open nonexistent app").autoOpen)throw new AssertionError("Only a uniquely resolved app command opens automatically");
        List<TenNotaActions.Target> apps=Arrays.asList(new TenNotaActions.Target("Nth Reader","org.example.reader/.Main","My Reader"),new TenNotaActions.Target("Reader","org.example.a/.Main"),new TenNotaActions.Target("Reader","org.example.b/.Main"));
        if(!TenNotaActions.resolve("Open My Reader",apps).actions.get(0).value.equals("org.example.reader/.Main"))throw new AssertionError("Installed rename");
        TenNotaActions.Plan multiple=TenNotaActions.resolve("Open Reader",apps);if(multiple.autoOpen||multiple.actions.size()!=2||multiple.actions.get(0).label.equals(multiple.actions.get(1).label))throw new AssertionError("Ambiguous installed apps need named choices");
        TenNotaActions.Plan shared=TenNotaActions.resolve("Open Calculator",Collections.singletonList(new TenNotaActions.Target("Calculator","org.example.calc/.Main")));if(shared.autoOpen||shared.actions.size()!=2||shared.actions.get(0).label.equals(shared.actions.get(1).label))throw new AssertionError("Phone app and Nth tool need distinct choices");
        for(String passage:new String[]{"Proofread: Can you open Nth Browser?","Write a story about opening apps","My note says: Open Calculator","Don't open Nth Browser","Open Nth Browser\nDelete my files"})if(TenNotaActions.resolve(passage,apps)!=null)throw new AssertionError("Passage became a command: "+passage);
        for(String command:new String[]{"Open Nth Browser and erase files","Open intent://example","Open @notes","Open setting:restart-desktop","Open nonexistent app"})if(plan(command).actions.stream().anyMatch(a->!a.value.equals("@search")))throw new AssertionError("Unresolved command acquired authority");
        TenNotaActions.Action web=plan("Search the web for red fox & snow").actions.get(0);if(web.kind!=TenNotaActions.Kind.WEB_SEARCH||!web.value.equals("red fox & snow"))throw new AssertionError("Web query");
        if(plan("Find files named budget").actions.get(0).kind!=TenNotaActions.Kind.FILE_SEARCH)throw new AssertionError("Linked file search");
        if(plan("Create a new virtual desktop").actions.get(0).kind!=TenNotaActions.Kind.NEW_DESKTOP||plan("Show my desktop").actions.get(0).kind!=TenNotaActions.Kind.DESKTOP)throw new AssertionError("Desktop controls");
        if(plan("What can you do?").actions.size()<3)throw new AssertionError("Discoverable actions");
        if(TenSettingsCatalog.search("calculator").stream().anyMatch(p->p.category.equals("gaming"))||TenSettingsCatalog.search("calculator").stream().noneMatch(p->p.id.equals("tools")))throw new AssertionError("Calculator belongs to tools");
        System.out.println("PASS: local Nota automatic app opening, manual settings/help/search/fallback, ambiguity, imported/writing boundaries, unsupported destinations, searches, desktops and tools classification");
    }
}
