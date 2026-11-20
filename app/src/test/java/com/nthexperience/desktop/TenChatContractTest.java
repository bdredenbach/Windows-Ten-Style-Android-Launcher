package com.nthexperience.desktop;

import java.time.*;
import java.util.*;
import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TenChatContractTest {
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));
    static class Fixture implements LiveQuestions.Transport {
        String reply="{\"text\":\"Hello\"}";
        JSONObject sent;
        int probes,posts;
        public String get(String url){ assertEquals(LiveQuestions.CAPABILITIES,url); probes++; return "{\"chatVersion\":1}"; }
        public String post(String url,String json)throws Exception{
            assertEquals(LiveQuestions.CHAT,url);posts++;sent=new JSONObject(json);return reply;
        }
        LiveAnswer ask(String q,JSONArray turns,boolean web)throws Exception {
            return new LiveQuestions(CLOCK).converse(q,"App theme: Light",turns,web,this,()->true,stage->{});
        }
    }
    @Test public void sourceCardsUseOnlyStructuredPublicHttpsAndPreserveTitles()throws Exception {
        Fixture f=new Fixture();
        f.reply="{\"text\":\"## Notes\",\"searched\":true,\"sources\":[{\"title\":\"Release notes\",\"url\":\"https://example.com/notes\"},{\"url\":\"file:///tmp/example\"},{\"url\":\"https://127.0.0.1/\"}]}";
        LiveAnswer answer=f.ask("Find today's release notes",new JSONArray(),true);
        assertEquals("search",f.sent.getString("mode"));assertFalse(f.sent.has("launcherContext"));
        assertEquals("UTC",f.sent.getString("timezone"));assertTrue(f.sent.getBoolean("consentAccepted"));
        assertEquals(1,f.posts);assertEquals(1,f.probes);assertEquals(1,answer.sources.size());
        assertEquals("Release notes",answer.sources.get(0).title);assertFalse(answer.searchFallback);
        assertEquals(CLOCK.millis(),answer.checkedAt);
    }
    @Test public void requestingSourcesWithoutEvidenceReturnsAnUnavailableReply()throws Exception {
        Fixture f=new Fixture();
        f.reply="{\"text\":\"An unsupported assertion\",\"searched\":true}";
        LiveAnswer answer=f.ask("Latest news",new JSONArray(),true);
        assertTrue(answer.searchFallback);assertFalse(answer.text.contains("unsupported assertion"));
        f.reply="{\"text\":\"Please try again\",\"unavailable\":true}";
        assertTrue(f.ask("Why?",new JSONArray(),false).searchFallback);
    }
    @Test public void privateOrLocalWorkStaysOutsideFollowups()throws Exception {
        NotaConversation chats=new NotaConversation("");
        chats.append("open Calculator",LiveAnswer.plain("Opened","Local"),false);
        chats.append("unverified",LiveAnswer.unavailable("Try again",0,Collections.emptyList()),true);
        for(int n=0;n<9;n++)chats.append("question "+n,LiveAnswer.plain("answer "+n,"AI answer"),true);
        Fixture f=new Fixture();f.ask("Explain the second point",chats.current().context(),false);
        assertEquals("auto",f.sent.getString("mode"));assertFalse(f.sent.has("launcherContext"));
        JSONArray messages=f.sent.getJSONArray("messages");assertEquals(12,messages.length());
        assertEquals("question 3",messages.getJSONObject(0).getString("content"));
        assertFalse(messages.toString().contains("Calculator"));assertFalse(messages.toString().contains("unverified"));
        chats.newChat();f.ask("How do I change my taskbar?",chats.current().context(),false);
        assertEquals(0,f.sent.getJSONArray("messages").length());assertEquals("App theme: Light",f.sent.getString("launcherContext"));
    }
    @Test public void historyRoundTripPreservesLongAnswersAndCapsContext() {
        NotaConversation chats=new NotaConversation("");
        String full="a".repeat(10001);chats.append("A long answer",LiveAnswer.plain(full,"AI answer"),true);
        NotaConversation restored=new NotaConversation(chats.save());
        assertEquals(full,restored.current().turns.get(0).answer.text);assertEquals(0,restored.current().context().length());
    }
}
