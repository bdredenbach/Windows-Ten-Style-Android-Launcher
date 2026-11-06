# Ten 0.15.0 validation

Tested source: `94959a673ebd26244c20783c493c9379fc418d9e`.

[Focused Android 11/16 taskbar and interaction regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42267710260). Both jobs succeed. Downloaded evidence contains the native interaction PASS record, zero assertion/failure markers and ten passing unit tests in three result files per OS.

## Verified behavior

The native suite checks Nota in portrait and landscape with Ten dark, Ten light and Aero shells. Aero includes window animations. It opens Nota, compares its measured 48 × 48 dp button bounds with an ordinary app button, brings another window forward, minimizes/restores Nota, closes through its caption, confirms no selection or running-background pixels remain, reopens through the same fixed button, checks no stale Close menu entry remains, closes through the taskbar menu and reopens again. It also switches to another virtual desktop and back, checking the correct idle/running/active state.

This exercises the actual stale cached-window case: a closed window is detached even though its object may remain in the map. Rendering, click behavior and menu commands now use the same attached-window lookup.

Existing interaction checks remain enabled: fixed Nota coordinates during taskbar scrolling, later static preference after recreation, all 17 device-control themes, real volume routing, physical Action Center toggles and cleanup, all 33 original-artwork aliases, Nota desktop guidance, installed-app card launch and title-bar shake/restore.

Reviewed all twelve API 30 Nota open/closed captures (three shell styles × two orientations × two states). The taskbar highlight is the normal app-button width when active and disappears when closed. API 36 ATD retains its black-screen capture limitation; native state/geometry/background-drawing assertions pass there. Tests do not prove every physical phone or OEM behavior.

The prior 0.14.0 full regression also completed successfully on both OS images at source `5ee530b3c5f395dbde947a881931135aa315d442`: [full baseline](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42265752737). The 0.15.0 full matrix remains enabled separately; this record claims the completed focused checks above, not an uncompleted full run. No live cloud-writing claim is made.

## Signed package

Package `com.nthexperience.desktop.ten`, versionCode 15 / versionName 0.15.0; Android 8.0+ and target SDK 36. Signature v2/v3 and 16 KB alignment verification pass. All 493 payload entries in the signed APK match both focused-source CI APKs and the main-build APK byte for byte. ZIP CRC validation passes, and the embedded APK and notes exactly match their originals. Test/fixture APKs are excluded.

- APK SHA-256: `9ba3983329443840b2845d386791f47dc077523788c3d4549aa94ec0101e8860`
- Download ZIP SHA-256: `c0eea7d00fbeb5b2d5921dd911b56d0b1f0eb1daea01dcc5e55861f20c47d816`
- Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8`

The existing Ten certificate supports in-place updates from the earlier Ten previews.
