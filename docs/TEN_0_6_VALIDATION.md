# Ten 0.6.0 validation

Build commit: `dfe075f744a97b807c201c7fb738200aed894406`.

[Android 11 and 16 run](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42144436817).

| Check | Result |
| --- | --- |
| App, fixture and instrumentation builds | Pass on API 30 and 36 |
| Model preparation and cancellation unit tests | 2 passed on each job |
| Portable tile, consent and polish checks | Pass on each job |
| Packaged manifest | Six native runners, notification listener and Nota selection target registered |
| TenSmoke, TenPolishSmoke, TenShellSmoke | Pass on both OS versions |
| TenThemeSmoke, TenWorkspaceSmoke | Pass on both OS versions |
| TenThemeMatrixSmoke | All 17 presets in both orientations on both OS versions: 68 passing combinations |
| Signed download | Existing Ten certificate; v2/v3 signatures and 16 KB ZIP alignment pass |
| Signed payload | All 424 non-signature entries match the tested CI APK exactly |
| Download ZIP | CRCs and embedded APK match |

## Every shipped preset

| Theme | Finish | Apps | Android 11 landscape / portrait | Android 16 landscape / portrait |
| --- | --- | --- | --- | --- |
| Ten Blue | Ten | Light | Pass / Pass | Pass / Pass |
| Ten Light | Ten | Light | Pass / Pass | Pass / Pass |
| Ten Midnight | Ten | Dark | Pass / Pass | Pass / Pass |
| Ten Spectrum | Ten | Dark | Pass / Pass | Pass / Pass |
| Coastal Teal | Ten | Light | Pass / Pass | Pass / Pass |
| Warm Horizons | Ten | Light | Pass / Pass | Pass / Pass |
| Landscape Collection | Ten | Light | Pass / Pass | Pass / Pass |
| Graphite | Ten | Dark | Pass / Pass | Pass / Pass |
| Seven Blue | Aero | Light | Pass / Pass | Pass / Pass |
| Twilight | Aero | Light | Pass / Pass | Pass / Pass |
| Meadow | Aero | Light | Pass / Pass | Pass / Pass |
| Harmony | Aero | Light | Pass / Pass | Pass / Pass |
| Landscapes | Aero | Light | Pass / Pass | Pass / Pass |
| Architecture | Aero | Light | Pass / Pass | Pass / Pass |
| Midnight Circuit | Aero | Light | Pass / Pass | Pass / Pass |
| Paper Skies | Aero | Light | Pass / Pass | Pass / Pass |
| Seven Basic | Aero | Light | Pass / Pass | Pass / Pass |

## Tested behavior

The native matrix opens the actual desktop menu and activates Personalize, checks older personalization/color routes, and verifies that a restored legacy Personalization page includes the eight Ten plus nine Aero presets. Each of the 17 gallery cards is applied in portrait and landscape on API 30 and 36. Checks assert its full preset identity, wallpaper, application mode and finish, complete gallery with exactly one selected card, preserved window instances and note text/selection, app root surfaces and note foreground, selected Settings label contrast of at least 4.5:1, and Start/taskbar glass renderers.

The 12 open built-in windows are Notepad, Files, Calendar, Nth Browser, Nth Nota, Task Manager, Recycle Bin, Sticky Notes, Photo Gallery, Weather, Calculator and Settings. Website-rendered content and colored paper retain their own colors under the existing application-scope palette model.

For every combination the runner renders all eight glass samples and checks their center pixels against the intended RGB tint, requiring eight distinct values. It renders each theme's sample at a fixed size and checks that its pixels stay stable when other themes are applied. Tint choice updates glass and accent together; independent opacity and intensity changes mark an otherwise matching preset as Custom.

Seventeen landscape comparison sheets per OS render actual Notepad, Explorer, Settings, Start and taskbar views. All 34 sheets were visually reviewed using contact sheets. Native Android 11 screen captures and the passing matrix support the desktop-route repair. Android 16 ATD full-screen captures remain transparent on the headless image; its comparison sheets are direct native View renders, not screen-capture evidence. Both structured 34-row tables are retained in the run artifacts.

The five previous native suites remain green, including local Nota policy, News, window transitions, theme portability, folder/desktop recovery and recreation. Tests use synthetic feeds and Nota responses; they do not submit live cloud requests or prove hardware-specific AICore inference. External Android apps and Android system Settings retain their own theme controls.

[Root causes and fixes](TEN_0_6_THEME_FIXES.md).

Preview APK SHA-256: `4e15c7f6d3397700d5abae4f8659748619015c89fcdbb9c78b887176e3024a3a`.

Download ZIP SHA-256: `1c06d8beed3f0be73ad80c4d392bbb1dc667b9d26e86800d87b877ffc71629cf`.

Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8` (same Ten preview certificate as 0.1–0.5).

Signing material and personal reference screenshots remain outside Git.
