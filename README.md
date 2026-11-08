# Nth Desktop Ten

**0.19.0 — one browser Nota button.** Removes the duplicate Nota button beside the address bar. The title-bar Nota button still brings browser selections or the page reference into Nota, and the address field gains the freed toolbar space. See [the change and verification](docs/TEN_0_19_BROWSER_NOTA.md).

**0.18.0 — dark-mode consistency.** Browser fields/tabs, photo controls, Calendar, shared fields/hints and PowerShell stay readable. The file chooser follows app mode, and folder palettes stay stable while scrolling. Native dark-mode, accent and full regression checks pass on Android 11 and 16 in both orientations. See [the fixes and verification](docs/TEN_0_18_DARK_MODE.md). The signed 0.18.0 APK and download ZIP are verified and ready to install over the existing preview.

**0.17.0 — working desktop icon controls and discoverable Usage Access.** Icon size and spacing opens the existing live controls, with size and spacing sliders first. Basic and advanced label/lock controls stay synchronized. Personalization → Taskbar includes Usage Access status and a setup link; Settings search finds it. See [the settings fix and validation](docs/TEN_0_17_SETTINGS.md).

**0.16.0 — correct identity in the Usage Access guide.** The App info example uses the installed launcher's icon and app name instead of an embedded Seven screenshot. The example is responsive and keeps its Android Settings colors across launcher themes. See [the guide fix and validation](docs/TEN_0_16_USAGE_GUIDE.md).

**0.15.0 — Nota taskbar state and matching highlight size.** Closing Nota clears its running highlight, its next tap reopens it, and its fixed button now matches normal app-button bounds. See [the taskbar fix](docs/TEN_0_15_NOTA_TASKBAR.md).

**0.14.0 — Action Center toggle and original Windows icon artwork.** The Action Center taskbar button now closes its open panel on a second tap. Matching desktop, folder, Explorer, Recycle Bin, drive, file and menu icons use the owner-supplied build 10125 originals. Nth branding and installed-app icons remain their own. See [the interaction and artwork changes](docs/TEN_0_14_ACTION_CENTER_AND_ICONS.md).

**0.11.0 — open apps directly from Nth Nota.** Send a clear request such as “open Audible” to launch one matching installed app or Nth tool immediately. Ambiguous names show large, bold shortcuts with actual app icons. Settings and other desktop actions keep clear, explicit shortcut instructions. See [0.11 behavior](docs/TEN_0_11_NOTA_OPENING.md).

**0.10.0 — reference Search, Task View, News and Win+X layouts, device specifications and Fonts.** Adds dimensional yellow folder artwork, a Search landing page and working category filters, wallpaper-backed window thumbnails, a lead-story news layout and grouped Start hold menu. About uses real Android hardware/build metadata with Copy and a local device name. Personalization → Fonts adds searchable previews, custom sample text and a saved launcher typeface. See [0.10 behavior](docs/TEN_0_10_REFERENCE_LAYOUTS.md).

**0.9.0 — stable accent colors, a reference-style Calculator and portrait Start columns.** Adds 24 named accent choices, including white and black. Ordinary app-folder labels keep their normal readable foreground; semantic accent text uses a separate contrast-safe color. Dynamic adapter cells are themed before drawing and repeated passes retain stable text state. Calculator now has the large display, blue memory row, orange Clear, wide zero and tall green equals from the reference. Normal portrait Start displays apps beside tiles. See [0.9 behavior](docs/TEN_0_9_POLISH.md).

**0.8.0 — desktop commands in Nth Nota and permanent split Settings.** Nota recognizes local requests to open Nth tools, installed apps and settings, search the web or linked files, and create/switch virtual desktops. Replies offer named action buttons; model output and imported writing never execute commands. Discover examples in the suggestion strip or ask “What can you do?”. Settings keeps a full-height category pane beside its page in portrait, landscape and narrow windows. Calculator is under Nth tools instead of Games. See [0.8 behavior](docs/TEN_0_8_COMMANDS.md).

**0.7.0 — large news feeds and honest connection feedback.** Streams the first twelve RSS/Atom stories within a bounded decoded input limit, supports gzip/declared encodings, fixes immediate manual retry and preserves a working source when a new connection fails. Connect stays open for actionable errors. Includes TechRadar/BBC choices, cache/rate-limit/cancellation checks and live-feed probe evidence.

**0.6.0 — one complete theme gallery and faithful color samples.** The desktop Personalize menu and older personalization/color links now lead to native Ten Settings. Restored legacy pages also expose all 17 presets. Theme previews, glass tints and background swatches retain their actual colors in light/dark mode; Aero tint, opacity and intensity controls are available in Colors. The native theme matrix applies every preset in both orientations across 12 built-in windows.

**0.5.0 — clean desktops and application-wide appearance.** Removes the inherited floating gadget layer and its gallery entry points. New desktops start empty with separate shortcuts and portrait/landscape positions. Desktop 1 is migrated in place; closing a desktop retains its shortcuts and windows. Dark, light, accent and Aero finishes apply to built-in application controls, including existing and inactive-desktop windows. See the [0.5 behavior notes](docs/TEN_0_5_BEHAVIOR.md).

**0.4.0 — N¹⁰ branding and complete theme selection.** Uses a new black glass N-to-the-tenth mark throughout the shell and Android icon resources. Personalization includes eight Ten presets, nine Aero presets from Seven, live color/transparency controls and native saved-theme links. Adds Run, updates performance-graph colors and preserves newer settings in backups. See the [0.4 audit](docs/TEN_0_4_AUDIT.md).


**0.3.0 — Ten taskbar, News and interests, and Nota throughout the launcher.** Adds a more compact, flat taskbar with running-app markers, a real taskbar news dashboard, slot-directed window transitions, contextual Nota actions, source-safe reply application, local writing history, and an Android text share/selection target. See the [review of all 49 reference images](docs/TEN_0_3_SCREENSHOT_REVIEW.md).


A Windows 10 inspired Android launcher by **An Nth Experience**. Current preview: **0.18.0**. Android 8.0+; targets Android 16.

Package: `com.nthexperience.desktop.ten`. Installs alongside Nth Desktop Seven and Vista.

## What is included

- Flat taskbar with responsive search, Task View, app buttons, clock, Action Center and Show desktop; updated original folder artwork.
- Start rail/app list, compact tiles, named groups, four tile sizes, full-screen/tablet mode and local calendar/battery/note details. Drag to rearrange or move between groups; Ctrl+Left/Right and accessibility reorder actions are supported.
- Named virtual desktops for built-in windows. Each desktop has its own shortcuts and icon layout. Keep tool contents and minimized states, move windows, switch with Windows+Ctrl+Left/Right and restore the desktop banks after activity recreation.
- Searchable Ten Settings category grid and sidebar, real system-component/feature inventories, lock-screen and casting destinations, device specification cards, font previews and selection, real launcher controls and explicit Android settings destinations.
- File/Home/View/Manage Recycle Bin ribbon with real restore-all/selected-item controls.
- File/Home/Share/View Explorer ribbon with collapse, enabled/disabled capability states, actual file operations and inherited folder grants/pickers/recovery.
- Modern browser tab/address chrome, real WebView security behavior, InPrivate where supported, and an Nth Nota entry.
- Local-first Nota writing assistance; model preparation is explicit, cloud fallback is optional and free, Stop discards late replies, and Save to Notepad updates open notes. No provider secret is embedded.
- Optional real Android notifications in Action Center, in-memory cards, lock-screen redaction, local Focus assist, quick actions and brightness slider. Grant/revoke access in Android settings.
- Optional RSS/Atom News and interests with explicit refresh and cached headlines, plus a route to Weather.
- Resizable windows, Snap, grouped previews, guided Home/file/Usage Access setup, media tools, photos, recycle bin and backups inherited from Seven.

Ten installs alongside Seven and Vista. Android apps use Android tasks; Nth windows use the launcher desktops. Moving or closing a desktop never replaces another open singleton tool; a collision asks you to move or close the duplicate first. No Windows applications, remote desktop or Microsoft account functionality is simulated.

## Build and checks

Open in Android Studio or use Java 17 and `./gradlew :app:assembleDebug :app:assembleDebugAndroidTest`. The workflow builds on Android 36 and runs twelve Ten native suites and ten shared-engine scenarios on Android 11 and 16. APKs and screenshots are workflow artifacts. See the [0.15.0 validation record](docs/TEN_0_15_VALIDATION.md) for the successful run and tested scope.

`checks/TenTileLayoutCheck.java` exercises 9,600 mixed-size tile placements and opt-in rules. `checks/TenPolishCheck.java` adds 19,200 placements across narrow/wide groups, Settings catalog checks and writing/fallback policies. `TenSmoke` and `TenPolishSmoke` check the separate package, Start search and tile persistence, assistant opt-in and local save using a synthetic reply, Settings, Explorer ribbon, Task View, Action Center, backup and both orientations. `TenNotaActionsCheck` checks local app/settings resolution, ambiguity, command boundaries, searches and utility classification. `TenCommandsSmoke` checks actual split Settings, local action buttons, native browser/settings/app launch, source/reply boundaries and empty desktop creation. `TenShellSmoke` covers taskbar, News, contextual Nota and window transitions. `TenReferenceSmoke` checks Search categories, About metadata/Copy, Fonts application/backup, Task View bounds, Win+X destinations and news layout in portrait and landscape. `TenWorkspaceSmoke` covers legacy gadget flags in both orientations, shortcut/layout migration and isolation, desktop close/recovery, backup rejection, app palettes, content preservation and recreation. `TenThemeSmoke` covers N¹⁰ resources, visible theme cards, Ten/Aero application, saved/legacy themes, validated backup fields and Run. Native tests do not call the live AI endpoint. Fake-backend unit tests cover model preparation gating and late-reply cancellation; a separate synthetic live gateway check verifies cloud response.

Inherited Seven test drivers remain available as reference. They include Seven appearance assertions and do not constitute a green Ten regression suite. The original baseline's known Android 11 issues are recorded in [the baseline notes](docs/SEVEN_BASELINE.md).

## Preview scope

The Settings hierarchy now covers the reference categories, while several advanced profile/icon/media editors still use inherited dialogs. More live tiles, same-desktop tool instances and broader browser parity remain future work. The complete Nota canvas is separate. Supported-device on-phone inference needs real AICore hardware; emulator validation and fake-backend tests do not claim to prove it.

See [the implementation checklist](docs/TEN_ROADMAP.md) for exact behavior and limits.

## Design and lineage

The design inventory uses the owner's 49 Windows 10 screenshots plus Microsoft references. See [design notes](docs/TEN_DESIGN.md), [assistant scope](docs/NOTA_INTEGRATION.md), [implementation checklist](docs/TEN_ROADMAP.md) and [source lineage](docs/SOURCE_LINEAGE.md).

Independent Nth project; not affiliated with Microsoft. Windows is a Microsoft trademark. Existing open-source notices remain in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

The [49-image reference audit](docs/TEN_0_12_REFERENCE_AUDIT.md) records every archive screenshot and the Android equivalents. `TenAuditSmoke` navigates all 48 Settings pages in both orientations and app modes, checks inventory/search/destination routes and selected quick actions across 24 accents in both shell modes. The broader regression reuses actual Browser, Photo Gallery, Media Player, Explorer, storage/setup/revocation/resize and file-opening engines, including separate-UID selected-file grants and Package Installer fixtures.

The [0.13 interaction pass](docs/TEN_0_13_INTERACTIONS.md) adds a scrollable taskbar default with fixed Nota button, themed device controls, interactive new-desktop guidance, installed-app icon cards and inclusive shake/restore behavior. `TenInteractionSmoke` exercises those flows and physical title-bar gestures on both OS images.
