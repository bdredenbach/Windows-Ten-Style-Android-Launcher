package com.nthexperience.desktop;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

/** Chat history and unfinished questions are outside desktop presets and .nthdesktop. */
final class TenChatStore {
    private final SharedPreferences prefs;
    private String prefix;
    NotaConversation conversations;

    TenChatStore(Context c, String desktop) {
        prefs = preferences(c);
        prefix = desktop + "/";
        conversations = new NotaConversation(prefs.getString(prefix + "history", ""));
    }
    private static SharedPreferences preferences(Context c) {
        return c.getSharedPreferences("ten-nota-conversations", Context.MODE_PRIVATE);
    }
    void save() {
        Set<String> live = new HashSet<>();
        for (NotaConversation.Chat chat : conversations.chats) live.add(chat.id);
        SharedPreferences.Editor edit = prefs.edit().putString(prefix + "history", conversations.save());
        for (String key : prefs.getAll().keySet()) {
            if ((key.startsWith(prefix + "draft/") || key.startsWith(prefix + "content/"))
                    && !live.contains(key.substring(key.lastIndexOf('/')+1))) edit.remove(key);
        }
        edit.apply();
    }
    String title(NotaConversation.Chat chat) {
        if(!chat.turns.isEmpty()) return chat.title();
        String draft=prefs.getString(prefix+"draft/"+chat.id,"").trim();
        return draft.isEmpty() ? chat.title() : "Draft · "+LiveQuestionPolicy.limit(draft.replaceAll("\\s+"," "),60);
    }
    String draft() { return prefs.getString(draftKey(), ""); }
    boolean draftIsContent() { return prefs.getBoolean(prefix + "content/" + conversations.current().id, false); }
    void draft(String text, boolean content) {
        SharedPreferences.Editor edit = prefs.edit();
        String flag = prefix + "content/" + conversations.current().id;
        if (text.isEmpty()) edit.remove(draftKey()).remove(flag);
        else edit.putString(draftKey(), text).putBoolean(flag, content);
        edit.apply();
    }
    private String draftKey() { return prefix + "draft/" + conversations.current().id; }
    boolean writing(boolean fallback) { return prefs.getBoolean(prefix + "writing", fallback); }
    void setWriting(boolean value) { prefs.edit().putBoolean(prefix + "writing", value).apply(); }
    boolean web() { return prefs.getBoolean("always_search", false); }
    void web(boolean value) { prefs.edit().putBoolean("always_search", value).apply(); }

    static void remove(Context c, String desktop) {
        SharedPreferences prefs = preferences(c);
        SharedPreferences.Editor edit = prefs.edit();
        for (String key : prefs.getAll().keySet()) if (key.startsWith(desktop + "/")) edit.remove(key);
        edit.apply();
    }
    void move(Context c, String desktop) {
        String next = desktop + "/";
        if (next.equals(prefix)) return;
        // Move the open chat. Other conversations remain with their respective desktops.
        NotaConversation.Chat moved=conversations.current();
        String text=draft();
        boolean asContent=draftIsContent();
        boolean writing=writing(false);
        conversations.deleteCurrent();
        save();
        prefix=next;
        conversations=new NotaConversation(prefs.getString(prefix+"history",""));
        conversations.adopt(moved);
        draft(text,asContent);
        setWriting(writing);
        save();
    }
}
