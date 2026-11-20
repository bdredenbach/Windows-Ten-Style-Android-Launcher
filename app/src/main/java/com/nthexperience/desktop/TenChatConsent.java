package com.nthexperience.desktop;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Switch;

/** Chat consent is installation-local and independent of the writing fallback. */
final class TenChatConsent {
    static final String KEY = "chat_search_v1";
    static SharedPreferences preferences(Context c) {
        return c.getSharedPreferences("ten-nota-chat-privacy", Context.MODE_PRIVATE);
    }

    static Switch control(Context c) {
        SharedPreferences prefs = preferences(c);
        Switch control = new Switch(c);
        control.setText("Cloud questions");
        control.setTextSize(13);
        control.setMinHeight(Aero.dp(c, 44));
        control.setContentDescription("Cloud questions for Nth Nota AI");
        Runnable sync = () -> control.setChecked(prefs.getBoolean(KEY, false));
        SharedPreferences.OnSharedPreferenceChangeListener listener = (p, key) -> sync.run();
        control.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
                prefs.registerOnSharedPreferenceChangeListener(listener);
                sync.run();
            }
            public void onViewDetachedFromWindow(View v) {
                prefs.unregisterOnSharedPreferenceChangeListener(listener);
            }
        });
        sync.run();
        control.setOnClickListener(v -> {
            if (!control.isChecked()) {
                prefs.edit().putBoolean(KEY, false).apply();
                return;
            }
            sync.run();
            Aero.dialog(c).setTitle("Allow cloud questions?")
                .setMessage("Nth Nota AI sends your question and up to six recent cloud exchanges from this chat to its service. Search providers may receive search terms. Page readers may receive links you ask Nota to read. Questions about the launcher can include its theme and taskbar settings. Your app list, desktop contents and notes stay on this device. You can turn this off at any time.")
                .setNegativeButton("Keep off", null)
                .setPositiveButton("Enable cloud", (dialog, which) -> prefs.edit().putBoolean(KEY, true).apply()).show();
        });
        return control;
    }
    private TenChatConsent() {}
}
