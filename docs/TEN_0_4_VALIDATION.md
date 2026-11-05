# Ten 0.4.0 validation

Build commit: `4c2fafe3d0ffa19e5e58d9778ba897dd29cd43ff`.

[Android 11 and 16 run](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42130749264).

| Check | Result |
| --- | --- |
| App, fixture and instrumentation builds | Pass on API 30 and 36 |
| Model preparation and cancellation unit tests | 2 passed on each job |
| Portable Start/consent and polish checks | Pass on each job |
| Packaged manifest | Four native runners, notification listener and Nota selection target registered |
| TenSmoke | Pass on both OS versions |
| TenPolishSmoke | Pass on both OS versions |
| TenShellSmoke | Pass on both OS versions |
| TenThemeSmoke | Pass on both OS versions |
| Signed download | Existing preview certificate, v2/v3 signatures and 16 KB ZIP alignment pass |
| Signed payload | All 424 non-signature entries match the CI APK exactly |
| Download ZIP | CRCs and embedded APK match |

The additional native suite checks black/white N¹⁰ application resources and shared Start branding, all 17 theme cards, visible first-screen previews, real Ten Light/transparency and Aero settings, preserving the same open Notepad instance and text during appearance changes, retaining the Settings page, Nth theme packing/unpacking, legacy Seven compatibility, saved-theme discovery, portable backup keys and invalid-value rejection, Windows+R, real Notepad/Settings resolution, Run history and unknown-command rejection. Opening Start is checked to retain its AeroGlass renderer.

Android 11 screenshots were inspected for portrait Start branding, the theme gallery, Aero Start, Colors, About/toast, Run and Task Manager. The review caught a Start-opening path that replaced glass with a solid fill, gallery cards below introductory links and two light-theme contrast issues; the tested revision fixes them. Android 16 ATD screenshots are transparent on this headless image; its native view/action assertions pass, but those captures are not visual evidence.

The initial 0.4 run passed all native suites on Android 11; Android 16 failed while installing the emulator package, before app tests. The run linked above uses the final visual corrections and passes both jobs.

Tests use synthetic feed content and synthetic Nota replies. They do not call the live AI endpoint or prove hardware-specific AICore inference. Remaining application/platform differences are listed in the [0.4 audit](TEN_0_4_AUDIT.md).

Preview APK SHA-256: `04020ff3118118cde163fbc0f3bc44a8be5e3cd8b93346f09e552ecb95d69f65`.

Download ZIP SHA-256: `11095a92d4c809471be5edc2f7d78f64a6d782c3ff5f7fd5740c25f918c2b964`.

Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8` (same Ten preview certificate as 0.1–0.3).

Signing material and personal reference screenshots remain outside Git.
