# Ten 0.12.0 reference and continuity audit

Reviewed all 49 images in the two user archives on 2026-11-06: 42 images in `Nth Desktop Ten.zip`, seven in `Nth Desktop Ten Part 2.zip`. The files are `Untitled.png`, then numbered `Untitled1.png` onwards. This audit supersedes the old 0.3 image mapping; in particular, Part 1 / Untitled16 is the Explorer taskbar Jump List, not a desktop menu.

The launcher adapts the reference layout to a phone. Functionality below refers to the Android counterpart and the launcher’s own windows, not Windows applications or services.

## Changes from this review

- Added System sidebar entries for Projecting to this PC, Shared experiences, System Components, Remote Desktop and Optional features, plus Personalization / Lock screen.
- System Components shows actual installed Android system apps, their icons, package names, installed versions and disabled state. Search filters the list; selecting an app opens that package’s Android App info.
- Optional features lists the actual Android hardware/software feature names and versions, sorted and searchable. Android/device updates manage installation.
- Lock screen exposes Android wallpaper, security, notification and timeout destinations. Casting and connected-device pages use Android settings, with the existing Settings fallback when an OEM omits a destination. Remote Desktop explains opening the user’s installed remote app.
- Selected Action Center tiles use a readable foreground on the literal selected accent. Focus assist and Tablet mode expose selected accessibility state. Both black and white accents remain literal choices.
- Start settings opens the Start surface. Search keeps its separate landing page. File Explorer’s taskbar and Jump List use the same name. Desktop tools uses Task View instead of the inherited Flip 3D name. Exported Steps files use the Ten name.
- Recycle Bin now shares the Ten File/Home/View/Manage ribbon style, with separate restore-all and selected-item commands. Disabled recovery commands remain disabled. Ribbon labels fit without clipping, and File tabs share a readable white-on-blue surface. Desktop menus use Display settings, All apps and Settings.
- Settings section headings grow when their text wraps on narrow windows, preventing overlap with the following description.
- Browser Home now remains reachable through Settings and more when the phone-width toolbar hides its dedicated button.
- Explorer layout/command menus anchor to the visible Ten ribbon. Native regression exercises its actual Home commands as well as held file/folder menus.
- Expanded native regression beyond the shell to the shared browser, media, photo, file picker, file opening, shared storage, setup, resizing and permission-recovery engines.

## Individual image review

| Archive / file | Reference | Current treatment / audit result |
| --- | --- | --- |
| 1 / Untitled | Desktop and taskbar | Shared folder renderer, aligned taskbar, search, news, tray, date and clock retained. |
| 1 / Untitled1 | Hidden notification icons | Chevron flyout retained; phone controls use Android destinations. |
| 1 / Untitled2 | Clock and calendar | Clock/calendar flyout and real date navigation retained. |
| 1 / Untitled3 | Taskbar context menu | Search/news visibility, Task View, window arrangement, desktop, Task Manager and Settings retained. |
| 1 / Untitled4 | Calculator and collapsed Explorer ribbon | Ten calculator and working ribbon collapse retained. |
| 1 / Untitled5 | Computer / Explorer Home ribbon | File operations and native/granted folder navigation retained. |
| 1 / Untitled6 | Explorer View ribbon | Layout, sorting, navigation pane, preview, hidden items and refresh retained. |
| 1 / Untitled7 | Explorer File menu | Native file menu and new-window flow retained; Windows-only tasks use Android equivalents. |
| 1 / Untitled8 | Explorer Home ribbon | Capability-aware copy/cut/paste/delete/rename/folder/properties/select retained. |
| 1 / Untitled9 | Explorer View ribbon and details | Details/layout and navigation controls retained. |
| 1 / Untitled10 | Drive Tools contextual ribbon | Storage information and Android storage management retained; Windows disk utilities are not offered. |
| 1 / Untitled11 | Task View | Wallpaper backdrop, wrapping proportional thumbnails and new desktop retained. |
| 1 / Untitled12 | Search landing | Dedicated tabs, Recent, Top apps, Quick searches and filtered results retained. |
| 1 / Untitled13 | Start hold / Win+X | Full grouped text menu and real Android/Nth tool destinations retained. |
| 1 / Untitled14 | Expanded Start rail | Account/folder/Settings/power rail and tile panel retained. |
| 1 / Untitled15 | Start apps and tiles | Both columns, alphabetic apps and tiles retained on supported phone widths. |
| 1 / Untitled16 | Explorer taskbar Jump List | Pinned/recent folders, new window and task actions retained; renamed File Explorer consistently. |
| 1 / Untitled17 | Start groups and app list | Named tile groups and app list retained. |
| 1 / Untitled18 | Start app context menu | Pin/unpin, app details and Android uninstall actions retained. |
| 1 / Untitled19 | Settings home categories | Category grid, local profile, search and shared catalog retained. |
| 1 / Untitled20 | About | Hardware cards, local device name, actual metadata and copyable specifications retained. |
| 1 / Untitled21 | Optional features | Added searchable real device feature inventory. |
| 1 / Untitled22 | Clipboard | Local file/shortcut clipboard count and Clear retained; no Microsoft sync is simulated. |
| 1 / Untitled23 | System Components | Added searchable actual Android system apps and per-package App info. |
| 1 / Untitled24 | Multitasking | Snap, Task View and per-desktop built-in windows retained. |
| 1 / Untitled25 | Tablet | Real full-screen Start and app-list preference retained. |
| 1 / Untitled26 | Storage | Actual capacity, File Explorer, file-access setup and Android storage management retained. |
| 1 / Untitled27 | Power & sleep | Android battery and screen-timeout destinations retained. |
| 1 / Untitled28 | Focus assist | Local notification filtering retained; selected quick action contrast corrected. |
| 1 / Untitled29 | Sound | Actual media volume and Android sound settings retained. |
| 1 / Untitled30 | Display | Brightness, desktop spacing and Android display controls retained. |
| 1 / Untitled31 | Background | Built-in backgrounds, personal pictures and preview retained. |
| 1 / Untitled32 | Colors | Independent shell/app modes, transparency and 24 accents retained; quick-action contrast gap fixed. |
| 1 / Untitled33 | Lock screen | Added Android wallpaper, screen lock, notifications and timeout controls. |
| 1 / Untitled34 | Themes | Real theme gallery, saved themes and related settings retained. |
| 1 / Untitled35 | Fonts | Searchable previews, custom sample and persistent launcher font retained. |
| 1 / Untitled36 | Start settings | Settings retained; Open Start corrected to show Start rather than Search. |
| 1 / Untitled37 | Taskbar settings | Grouping, labels, previews, search and news preferences retained. |
| 1 / Untitled38 | Taskbar notification area | Phone controls/tray and Android notification access retained; fixed-height taskbar adapts to phone insets. |
| 1 / Untitled39 | Recycle Bin context menu | Real restore/delete/empty workflows retained; recovery commands now share the Ten ribbon. |
| 1 / Untitled40 | Recycle Bin Manage ribbon | Added the Ten Manage ribbon over real recovery controls; source files are removed only after verified recovery copies. |
| 1 / Untitled41 | News and interests | Hero, top stories, weather, games and market-source links retained; no fabricated live prices. |
| 2 / Untitled | File operation progress | Real asynchronous progress, verification/cancellation and source preservation retained. |
| 2 / Untitled1 | Start app More menu | Actual Android app management retained; Windows administrator execution is not simulated. |
| 2 / Untitled2 | Start power menu | Desktop/restart/Android battery controls remain explicit; system shutdown requires Android privileges. |
| 2 / Untitled3 | Desktop context menu | View/sort/refresh/paste/folder/tools/background/personalization retained; Display settings and Ten tool names corrected. |
| 2 / Untitled4 | Copilot | Nth Nota is the functional replacement, including automatic unique app opening and visible icon shortcuts. |
| 2 / Untitled5 | Edge menu | Browser tabs/address bar, favorites/history, downloads, find and privacy functions retained. |
| 2 / Untitled6 | File selection / Send to | Real file grants, Share/Open with, copy/cut/rename/delete/properties and picker selection retained. |

## Boundaries

Windows drive letters, Windows-only software, Microsoft account sync, Windows administrator privileges, and remotely hosting Windows are outside the Android launcher. Optional features and System Components intentionally show this device’s real data. Android controls its secure lock screen, permissions, app installation, system power menu and external application tasks. Built-in Nth windows use the launcher’s virtual desktops.

Live news/weather/images depend on configured providers and connectivity; live AI depends on a prepared local model or optional free cloud service. Native deterministic checks use controlled fixtures and do not prove every external app, OEM settings screen, live provider or physical device behaves identically. See the release validation record for the exact tested build and any remaining limitations.
