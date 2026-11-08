# Nth Desktop Ten 0.19.0 — browser Nota button

The browser uses its window title-bar Nota button. The duplicate button beside the address bar is removed, and the address field uses the extra space. The existing browser selection/page-reference handler remains connected to the title-bar action.

Favorites anchors to its star button when visible, and to the menu button in narrow windows.

Verified source: `940362d1b54be626c2b5390130807a14d6fae12c`.

| Validation | Result |
| --- | --- |
| Unit tests and APK/test compilation | PASS in both Android build jobs |
| Android 16 / API 36 native regression | PASS, including browser controls, navigation, menus, tabs, uploads, downloads, privacy and video |
| Android 11 / API 30 native regression | Still running at packaging; unit/build checks passed |
| Updated native browser check | No toolbar duplicate; title-bar Nota control present |
| Package/version | `com.nthexperience.desktop.ten`, `0.19.0`, code `19` |
| Preview signing key | Existing certificate retained; v2/v3 signatures verified |
| APK alignment | Verified, including 16 KiB page alignment |
| Application payload | All 492 non-signature entries match both CI builds |
| Download ZIP | CRC and exact embedded APK/notes bytes verified |

[Current regression results](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42428676955).

Signed APK SHA-256: `b24e423df4491ce362bd6ab323cc9be0f2aef90da1e051405cfb4f9ecf7ad65a`.

Install the signed preview over the current Nth Desktop Ten installation to retain the desktop setup and notes.
