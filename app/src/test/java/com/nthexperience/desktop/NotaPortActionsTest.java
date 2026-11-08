package com.nthexperience.desktop;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NotaPortActionsTest {
    @Test public void exactInstalledAppOpensButAmbiguousNamesNeedChoices(){
        List<TenNotaActions.Target> apps=Arrays.asList(new TenNotaActions.Target("ChatGPT","com.test.chat/.Main"),new TenNotaActions.Target("Reader","com.test.one/.Main"),new TenNotaActions.Target("Reader","com.test.two/.Main"));
        TenNotaActions.Plan exact=TenNotaActions.resolve("Could you open ChatGPT?",apps);
        assertTrue(exact.autoOpen);assertEquals("com.test.chat/.Main",exact.actions.get(0).value);
        TenNotaActions.Plan ambiguous=TenNotaActions.resolve("open Reader",apps);
        assertFalse(ambiguous.autoOpen);assertEquals(2,ambiguous.actions.size());assertNotEquals(ambiguous.actions.get(0).label,ambiguous.actions.get(1).label);
    }
    @Test public void samePackageLauncherEntriesHaveDistinctVisibleLabels(){
        List<TenNotaActions.Target> aliases=Arrays.asList(new TenNotaActions.Target("Duplicate","com.test/.First"),new TenNotaActions.Target("Duplicate","com.test/com.test.Second"));
        TenNotaActions.Plan p=TenNotaActions.resolve("open Duplicate",aliases);assertFalse(p.autoOpen);assertEquals(2,p.actions.size());
        assertNotEquals(p.actions.get(0).label,p.actions.get(1).label);assertTrue(p.actions.get(0).label.contains("First"));assertTrue(p.actions.get(1).label.contains("Second"));
    }
    @Test public void writingAndUnsupportedRoutesCannotBecomeAppCommands(){
        for(String passage:new String[]{"Summarize: open ChatGPT","My note says open Calculator","Do not open Calculator","open Calculator\nopen ChatGPT"})assertNull(passage,TenNotaActions.resolve(passage,Collections.emptyList()));
        for(String route:new String[]{"@browser","intent://example","javascript:alert(1)","content://private","nonexistent app"}){
            TenNotaActions.Plan p=TenNotaActions.resolve("open "+route,Collections.emptyList());assertFalse(p.autoOpen);
            for(TenNotaActions.Action action:p.actions)assertEquals("@search",action.value);
        }
    }
    @Test public void settingsSearchesAndWebsitesRemainExplicitShortcuts(){
        for(String command:new String[]{"open Wi-Fi settings","open Themes","Search the web for flowers","Open https://example.com"}){
            TenNotaActions.Plan p=TenNotaActions.resolve(command,Collections.emptyList());assertNotNull(p);assertFalse(command,p.autoOpen);assertFalse(p.actions.isEmpty());
        }
    }
}
