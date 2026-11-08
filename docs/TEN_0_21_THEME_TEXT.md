# Nth Desktop Ten 0.21.0 — theme text and popup colors

The dark Nota category list exposed a gap in the native appearance pass: Android creates dropdown and menu rows in separate window roots. Recoloring the application body did not reach those rows. The shared selector adapter now sets the foreground, selected-row fill and check mark before drawing, including reused rows. Attached rows observe palette changes and remove their listeners when detached.

| Surface | Correction |
| --- | --- |
| Nota categories | Explicit light/dark text, selection background and checked marker |
| Background location, picture position, slideshow interval and sound schemes | Shared themed choice rows |
| Recycle Bin capacity, world-clock zones and conversion units | Shared themed choice rows |
| Run history suggestions | Themed autocomplete rows and dropdown background |
| Calculator, Start/Search and Task View native menus | Dedicated light/dark popup contexts, including readable disabled labels |
| App buttons and fixed dark News/Task View controls | Hover, pressed and focus fills follow the control's surface role |
| Window captions | Minimize/maximize highlights match the caption; close uses white on red; inactive symbols keep explicit readable gray |
| Taskbar device controls | Shell-appropriate highlights replace the pale Aero selection beneath white labels; flyout bounds only request layout when the geometry changes |
| Taskbar thumbnail previews | Preview cards receive the app palette; the outer command strip follows the shell palette |
| Aero back-arrow gradients | Midtone blue/purple stops receive the dark surface palette with their lighter gradient stops |
| Puzzle numbers | Tile artwork retains its supplied colors; label contrast is calculated against the complete tile gradient |
| Media Player navigation and custom context menus | Hidden pressed/focused background states receive the palette before they appear |
| Calculator memory and equals keys | Hover text uses the same contrast-safe state palette as pressed and focused keys |

The selected preset, accent choice, user documents, pictures, browser-page content and literal color swatches retain their existing meaning. Shell light/dark and application light/dark remain independent.

## Native audit

`TenThemeTextSmoke` uses Android's public `WindowInspector` API in the test package to enumerate real popup roots. It opens all 17 presets with both app palettes in portrait and landscape: 68 combinations per Android version. CI divides this into four 17-combination batches per OS (app palette × orientation). The portrait/light batch on each OS also runs the existing 25 native scenarios. Each combination opens built-in tools/utilities, all 48 Settings destinations, the desktop Control Panel pages, live selectors, Run suggestions, menus, taskbar previews, News, Task View, Action Center, device-control flyouts and dialogs. It checks enabled text and input hints against their opaque surfaces, including gradient stops, normal/pressed/hovered controls, and active/inactive captions. Recycled selector rows and a retained popup's light/dark transitions are checked too.

The audit does not call `TenAppearance.apply` before inspecting text. Drawable appearance methods mirror their normal draw behavior. Wallpaper-backed glass and Task View use their semantic surface colors for the contrast calculation; arbitrary image pixels, webpage content and literal preview artwork are outside that numeric text audit. Existing visual and interaction regressions continue to exercise custom calendar/browser/photo artwork and retained input/selection.

Each idle wait is bounded to eight seconds and reports the current surface if rendering does not settle. Popup cleanup dispatches the popup window's normal outside-touch event; dialog cleanup uses Cancel. This keeps palette checks independent of emulator Back-key injection behavior.

## Verification

Verified application source: `3e54c1a135ef0ea0dbd55e55bbee7a4b71103b11`. The [complete matrix/regression run](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42516422235) supplies seven completed theme partitions and both complete native regression sets. The [Android 11 portrait/dark follow-up](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42518311732) supplies the eighth completed partition after a test-only converter lookup moved onto the UI thread (`23ae0db422e094bd0a462f7c57dd9c4a5d3906e0`). All 455 non-signature APK entries match across the original builds, follow-up build and signed deliverable. Production sources did not change for the follow-up. The normal eight-partition CI matrix is restored for subsequent changes.

| OS | Theme combinations | Native surfaces | Text checks | Hint checks | Real popups | Result |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| Android 11 / API 30 | 68 | 12,932 | 435,410 | 5,780 | 1,296 | PASS |
| Android 16 / API 36 | 68 | 12,932 | 438,940 | 5,780 | 1,296 | PASS |

All 885,910 recorded text/hint checks passed; the smallest recorded ratio was 4.50:1. Each partition passed the same 18 unit tests. Each OS also passed all 25 existing native scenarios, including the 128 accent/app-mode/orientation combinations covering all 24 named accents and eight Aero tints, real Nota dispatch/state/privacy fixtures, Explorer/provider/file/install flows, media playback, window interactions and retained browser inputs.

The separate popup images were reviewed in Ten and Aero light/dark palettes. Signing retains the existing Ten preview certificate; APK v2/v3 signatures and 16 KiB alignment passed. The ZIP passed CRC verification and contains byte-identical APK and installation notes.

Signed APK SHA-256: `6f3a1d97130c0e3c454ae549fd72c5f95aee912b7996c68beb948b74c896bb18`.

Download ZIP SHA-256: `bc0584baf23c1f0ad1d5eb11e8d4c11e345e9d0dbb08ae89939ebc489a7915fe`.

Physical Pixel 10/Android 17, live AICore inference and live cloud writing were not newly exercised by this theme pass.
