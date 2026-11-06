package com.nthexperience.desktop;

import android.content.Context;
import android.graphics.drawable.Drawable;

/** Original Windows 10 build 10125 artwork supplied by the project owner. */
final class TenIconAssets {
    static int resource(String kind) {
        switch (kind) {
            case "files": case "folderapps": return R.drawable.win10_folder;
            case "file-explorer": return R.drawable.win10_explorer;
            case "documents": return R.drawable.win10_documents;
            case "pictures": return R.drawable.win10_pictures;
            case "music": return R.drawable.win10_music;
            case "videos": return R.drawable.win10_videos;
            case "downloads": return R.drawable.win10_downloads;
            case "computer": case "system": return R.drawable.win10_computer;
            case "network": return R.drawable.win10_network;
            case "recycle": return R.drawable.win10_recycle_empty;
            case "recycle-full": return R.drawable.win10_recycle_full;
            case "drive": return R.drawable.win10_drive;
            case "security": return R.drawable.win10_security;
            case "accounts": return R.drawable.win10_accounts;
            case "control": return R.drawable.win10_control_panel;
            case "programs": case "apps": return R.drawable.win10_application;
            case "documentfile": return R.drawable.win10_document_file;
            case "picturefile": return R.drawable.win10_picture_file;
            case "musicfile": return R.drawable.win10_music_file;
            case "videofile": return R.drawable.win10_video_file;
            case "archivefile": return R.drawable.win10_archive;
            case "copy": return R.drawable.win10_copy;
            case "paste": return R.drawable.win10_paste;
            case "cut": return R.drawable.win10_cut;
            case "delete": return R.drawable.win10_delete;
            case "search": return R.drawable.win10_search;
            case "appearance": return R.drawable.win10_personalize;
            case "sound": return R.drawable.win10_sound;
            case "puzzle": return R.drawable.win10_games;
            case "fonts": return R.drawable.win10_font;
            default: return 0;
        }
    }
    static Drawable drawable(Context context, String kind) {
        int resource = resource(kind);
        return resource == 0 ? null : context.getDrawable(resource);
    }
    private TenIconAssets() {}
}
