# Nth Desktop Ten

A Windows 10 inspired Android launcher by **An Nth Experience**. First preview: **0.1.0**. Android 8.0+; targets Android 16.

Package: `com.nthexperience.desktop.ten`. Installs alongside Nth Desktop Seven and Vista.

## First preview

- Flat left-aligned taskbar with Nth Start, Search, Task View, running apps, clock, Action Center and Show desktop.
- Start with a narrow account/folder/settings/power rail, alphabetical app list and Productivity/Explore tile groups. Hold a tile for Small, Medium, Wide or Large sizing, unpinning and group selection. Hold then drag to rearrange. Calendar tiles display today's date; installed Android app tiles are shortcuts.
- Search installed apps, launcher tools, settings, linked files and recent items. Web search is an explicit separate action.
- Task View shows built-in window previews and tracked Android app shortcuts. Private browser tabs are excluded from preview capture.
- Right-edge Action Center with actual Nth file/backup alerts and quick actions for Android network, Bluetooth, battery and brightness controls.
- Settings home, System, About, light/dark shell mode, accent colors and routes to real desktop/background options.
- Explorer File/Home/View ribbon: copy, cut, paste, new folder, delete, properties, layout and pane controls; inherited real file operations, folder grants, clipboard, thumbnails and file pickers.
- Nth Nota writing companion: Write, Brainstorm, Proofread, Polish and Summarize through the existing free gateway, opt-in before cloud use, stop/revoke controls, local drafts and Save to Notepad. Only the composer text is sent. No provider secret is embedded in the APK.
- Original Nth blue light wallpaper, Nth tile icon and launch animation.
- Seven's movable/resizable windows, Snap, grouped taskbar previews, guided Home/file/Usage Access setup, browser, media tools, photos, recycle bin and backup foundation.

In portrait, Start switches between tiles and All apps using the rail button. At larger widths, both panes appear together. Android apps open in Android; built-in Nth tools use launcher windows. System quick actions open the appropriate Android settings where direct control is restricted.

## Build and checks

Open in Android Studio or use Java 17 and `./gradlew :app:assembleDebug :app:assembleDebugAndroidTest`. The workflow builds on Android 36 and runs the Ten smoke suite on Android 11 and 16. APKs and screenshots are workflow artifacts.

`checks/TenTileLayoutCheck.java` exercises 9,600 mixed-size tile placements and opt-in rules. `TenSmoke` checks the separate package, Start search and tile persistence, assistant opt-in and local save using a synthetic reply, Settings, Explorer ribbon, Task View, Action Center, backup and both orientations. It does not call the live AI endpoint.

Inherited Seven test drivers remain available as reference. They include Seven appearance assertions and do not constitute a green Ten regression suite. The original baseline's known Android 11 issues are recorded in [the baseline notes](docs/SEVEN_BASELINE.md).

## Scope of this first preview

This is a working foundation, not completed Windows 10 feature parity. Multiple virtual desktops, arbitrary tile group names, full-screen/tablet Start, broader live-tile data, a fully rebuilt Settings hierarchy, Explorer ribbon polish and an Edge-style browser redesign remain follow-up work. The assistant uses Nota's writing gateway; Nota's on-device AICore integration and full notebook/canvas are not yet ported. No Android-wide notification feed is claimed.

## Design and lineage

The design inventory uses the owner's 49 Windows 10 screenshots plus Microsoft references. See [design notes](docs/TEN_DESIGN.md), [assistant scope](docs/NOTA_INTEGRATION.md), [implementation checklist](docs/TEN_ROADMAP.md) and [source lineage](docs/SOURCE_LINEAGE.md).

Independent Nth project; not affiliated with Microsoft. Windows is a Microsoft trademark. Existing open-source notices remain in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
