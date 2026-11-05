# Nth Desktop Ten

**0.4.0 — N¹⁰ branding and complete theme selection.** Uses a new black glass N-to-the-tenth mark throughout the shell and Android icon resources. Personalization includes eight Ten presets, nine Aero presets from Seven, live color/transparency controls and native saved-theme links. Adds Run, updates performance-graph colors and preserves newer settings in backups. See the [0.4 audit](docs/TEN_0_4_AUDIT.md).


**0.3.0 — Ten taskbar, News and interests, and Nota throughout the launcher.** Adds a more compact, flat taskbar with running-app markers, a real taskbar news dashboard, slot-directed window transitions, contextual Nota actions, source-safe reply application, local writing history, and an Android text share/selection target. See the [review of all 49 reference images](docs/TEN_0_3_SCREENSHOT_REVIEW.md).


A Windows 10 inspired Android launcher by **An Nth Experience**. Current preview: **0.2.0**. Android 8.0+; targets Android 16.

Package: `com.nthexperience.desktop.ten`. Installs alongside Nth Desktop Seven and Vista.

## What is included

- Flat taskbar with responsive search, Task View, app buttons, clock, Action Center and Show desktop; updated original flat artwork.
- Start rail/app list, compact tiles, named groups, four tile sizes, full-screen/tablet mode and local calendar/battery/note details. Drag to rearrange or move between groups; Ctrl+Left/Right and accessibility reorder actions are supported.
- Named virtual desktops for built-in windows. Keep tool contents and minimized states, move windows, switch with Windows+Ctrl+Left/Right and restore the desktop banks after activity recreation.
- Searchable Ten Settings category grid and sidebar, real launcher controls and explicit Android settings destinations.
- File/Home/Share/View Explorer ribbon with collapse, enabled/disabled capability states, actual file operations and inherited folder grants/pickers/recovery.
- Modern browser tab/address chrome, real WebView security behavior, InPrivate where supported, and an Nth Nota entry.
- Local-first Nota writing assistance; model preparation is explicit, cloud fallback is optional and free, Stop discards late replies, and Save to Notepad updates open notes. No provider secret is embedded.
- Optional real Android notifications in Action Center, in-memory cards, lock-screen redaction, local Focus assist, quick actions and brightness slider. Grant/revoke access in Android settings.
- Optional RSS/Atom News and interests with explicit refresh and cached headlines, plus a route to Weather.
- Resizable windows, Snap, grouped previews, guided Home/file/Usage Access setup, media tools, photos, recycle bin and backups inherited from Seven.

Ten installs alongside Seven and Vista. Android apps use Android tasks; Nth windows use the launcher desktops. Moving or closing a desktop never replaces another open singleton tool; a collision asks you to move or close the duplicate first. No Windows applications, remote desktop or Microsoft account functionality is simulated.

## Build and checks

Open in Android Studio or use Java 17 and `./gradlew :app:assembleDebug :app:assembleDebugAndroidTest`. The workflow builds on Android 36 and runs both Ten native suites on Android 11 and 16. APKs and screenshots are workflow artifacts. See the [0.2.0 validation record](docs/TEN_0_2_VALIDATION.md) for the successful run and tested scope.

`checks/TenTileLayoutCheck.java` exercises 9,600 mixed-size tile placements and opt-in rules. `checks/TenPolishCheck.java` adds 19,200 placements across narrow/wide groups, Settings catalog checks and writing/fallback policies. `TenSmoke` and `TenPolishSmoke` check the separate package, Start search and tile persistence, assistant opt-in and local save using a synthetic reply, Settings, Explorer ribbon, Task View, Action Center, backup and both orientations. Native tests do not call the live AI endpoint. Fake-backend unit tests cover model preparation gating and late-reply cancellation; a separate synthetic live gateway check verifies cloud response.

Inherited Seven test drivers remain available as reference. They include Seven appearance assertions and do not constitute a green Ten regression suite. The original baseline's known Android 11 issues are recorded in [the baseline notes](docs/SEVEN_BASELINE.md).

## Preview scope

The Settings hierarchy now covers the reference categories, while several advanced theme/profile/icon editors still use inherited dialogs. More live tiles, same-desktop tool instances and broader browser parity remain future work. The complete Nota canvas is separate. Supported-device on-phone inference needs real AICore hardware; emulator validation and fake-backend tests do not claim to prove it.

See [the implementation checklist](docs/TEN_ROADMAP.md) for exact behavior and limits.

## Design and lineage

The design inventory uses the owner's 49 Windows 10 screenshots plus Microsoft references. See [design notes](docs/TEN_DESIGN.md), [assistant scope](docs/NOTA_INTEGRATION.md), [implementation checklist](docs/TEN_ROADMAP.md) and [source lineage](docs/SOURCE_LINEAGE.md).

Independent Nth project; not affiliated with Microsoft. Windows is a Microsoft trademark. Existing open-source notices remain in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
