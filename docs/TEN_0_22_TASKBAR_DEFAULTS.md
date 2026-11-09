# Nth Desktop Ten 0.22.0 — taskbar size and initial preferences

The taskbar has a normal 48 dp row and an optional 96 dp layout. Extended mode places pinned and running application buttons across the upper row. Start, Search, Task View, fixed Nota, News, device controls, clock, Action Center and Show desktop remain in the lower row. The existing app scrolling, grouping, previews, reordering and animation anchors continue to use their actual button views. The workspace gives both rows their own space.

The saved `ten_taskbar_extended` switch is available in Personalization → Taskbar, Control Panel → Taskbar and Start Menu, and the taskbar hold menu. The controls synchronize after changing size. The preference is included in validated desktop backups. Normal size remains the initial choice.

Only the existing first-initialization branch changes the color and Start defaults. Fresh installs select White, keep shell and title-bar accent fills off, and use the dark shell with white text. Full-screen Start, the app list, and live local tile details are all enabled. Initialized installations retain their saved colors and Start choices.

## Verification

The [complete Android 11/16 run](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42559762160) passed all eight partitions. Tested source: `fd0ac18ae235fc5cde11740ac99166acb1b536d5`. It is merged in [PR #3](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/pull/3).

`TenTaskbarSizeSmoke` passed the real Settings, Control Panel and hold-menu controls; exact 48/96 dp row and workspace geometry; app scrolling under overflow; note preservation; backup parsing; activity recreation; and retention of custom Start/color choices. It covers portrait and landscape in Ten dark, Ten light and Aero. Native view screenshots were reviewed on both OS versions.

Each OS passed all 27 native scenarios/matrix outputs, including the existing browser, Explorer, file/provider/install, media, photo, resize, permission/setup/revocation, Nota and desktop interaction checks. Every build partition passed all 18 unit tests.

| OS | Theme combinations | Text checks | Hint checks | Real popups | Result |
| --- | ---: | ---: | ---: | ---: | --- |
| Android 11 | 68 | 435570 | 5780 | 1296 | PASS |
| Android 16 | 68 | 438494 | 5780 | 1296 | PASS |

All 885624 recorded text/hint checks passed across 17 themes, both app modes and both orientations.

All 455 non-signature application entries match both OS builds and the signed APK. Existing Ten preview certificate, APK v2/v3 signatures, package/version 0.22.0 (22), 16 KiB alignment, ZIP CRC and the ZIP’s byte-identical APK passed verification. Production code did not change during the test-fixture follow-ups.

APK SHA-256: `74832ce4f8a82a0ad2016093d192b55a0782c9ca9edacaa6ace6fe02bb86b9bf`.

Download ZIP SHA-256: `90bfd1efbbfc084f0c683221acb76267605c679ed60b39de4cdefe767869bd90`.

Physical Pixel 10/Android 17 was not newly exercised by this emulator pass.
