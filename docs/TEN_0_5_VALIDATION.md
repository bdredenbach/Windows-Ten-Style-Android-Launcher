# Ten 0.5.0 validation

Build commit: `43dda0c227660524145f979936f117207f58bc63`.

[Android 11 and 16 run](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42135758815).

| Check | Result |
| --- | --- |
| App, fixture and instrumentation builds | Pass on API 30 and 36 |
| Model preparation and cancellation unit tests | 2 passed on each job |
| Portable tile/consent/polish checks | Pass on each job |
| Packaged manifest | Five native suites, protected notification listener and Nota selection target registered |
| TenSmoke | Pass on both OS versions |
| TenPolishSmoke | Pass on both OS versions |
| TenShellSmoke | Pass on both OS versions |
| TenThemeSmoke | Pass on both OS versions |
| TenWorkspaceSmoke | Pass on both OS versions |
| Signed download | Existing Ten certificate; v2/v3 signatures and 16 KB ZIP alignment pass |
| Signed payload | All 424 non-signature entries match the CI APK exactly |
| Download ZIP | CRCs and embedded APK match |

## New native coverage

TenWorkspaceSmoke seeds an upgrade fixture with all old gadgets enabled and both orientation flags visible. It verifies no GadgetSidebar is attached in portrait or landscape and that old gadget note data remains. The fixture starts with a Desktop 1 folder group and different portrait/landscape positions; migration preserves both, while an existing secondary desktop and a newly created desktop have empty icon sheets. Checks cover actual grid refresh on switching, pin/group/layout isolation, shortcut preservation when closing a desktop, recycling/restoring the same app folder from two desktops, and a restore after its only original desktop has been closed. The latter returns a reachable shortcut to the current desktop.

The suite snapshots while the empty secondary desktop is selected, verifies Desktop 1 is retained in the portable backup, and rejects a forged folder ID without changing live state. It applies dark/light/Aero appearance to existing Notepad, Explorer, Calendar, browser, Nota and Settings windows without replacing their instances; preserves note text and cursor selection; checks a new dynamically added row inherits dark foreground/background; preserves image and web-view content backgrounds; round-trips the app-mode field in Nth themes; and checks an inactive-desktop Calculator window and activity recreation.

Android 11 screenshots were inspected for the empty desktop, dark Settings and Explorer, dark Run, light Explorer and Aero Settings. The review corrected a low-contrast colored Explorer heading and allowed the system rotation transition to finish before capturing the empty desktop. Old portrait/landscape native suites remain green. Android 16 ATD captures are transparent on this headless image; its passing native view/action assertions are the API 36 evidence, not a visual claim.

The tests use synthetic feed and Nota data. They do not submit live cloud requests or claim on-device AICore inference on a supported physical phone. External Android apps and system Settings keep their own theme behavior. See [0.5 behavior and migration details](TEN_0_5_BEHAVIOR.md).

Preview APK SHA-256: `e709d3d94528a7ac6d7986938803e0f8823cf9f48bdb29a083f7872ad8e0b6f0`.

Download ZIP SHA-256: `0a29e2d0c24ec74d01c63486ad4cbe6cab308784b790a5990397e5b775410bfc`.

Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8` (same Ten preview certificate as 0.1–0.4).

Signing material and personal screenshots remain outside Git.
