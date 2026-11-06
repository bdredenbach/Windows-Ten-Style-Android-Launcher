# Ten 0.17.0 - Desktop icon controls and Usage Access

Clicking Settings > Personalization > Desktop icons > Icon size and spacing opened a generic empty Control Panel page. The link used `page:desktop`; the existing live controls are under `page:icons`.

## Resulting behavior

- The link opens the actual advanced Desktop Settings. The old route is also normalized, so existing links work.
- Icon size, horizontal spacing and vertical spacing appear first, before the preview. Label size, separate portrait/landscape grids and reset remain available below.
- The existing preference keys, ranges and backup support are reused. No size/spacing migration overwrites the user's choices or saved positions.
- Basic and advanced label/lock controls stay synchronized without rebuilding the slider currently being dragged.
- The unused inherited gadget-snap control and Sidebar guidance are removed from this Ten page.
- Settings > Personalization > Taskbar displays Usage Access status and a Set up Usage Access action. Settings search finds it; icon-size search finds Desktop icons.
- When access is off, the action opens the existing recovery guide. When already allowed, it opens the established app-button inclusion controls. Status refreshes on return from Android Settings.
- Welcome Center > Set up Usage Access remains available, including through Update & Security > Recovery > Guided setup.

If Android blocks Usage Access, the guide's Open App info action leads to the app's Android settings. Choose the three-dot menu and Allow restricted settings if shown, then return to Usage Access. Access remains optional. The Ten icon/name example from 0.16.0 is retained.

## Validation

Tested source: `34181167b2849e8c91452bde88883445cab4cb84`.

[Focused Android 11/16 interaction regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42301804900): both jobs succeed. Downloaded result files contain native PASS records and ten passing unit tests per OS, with no failure markers. The tests follow the real icon-settings link, send native accessibility input to the actual sliders, observe desktop icon/cell geometry changes, reopen controls, reset defaults, verify both directions of label synchronization, find Usage Access in Settings search, open/cancel the guide and return to the same page. Both orientations are exercised. Existing Nota, theme, scrolling, Action Center, external-card and shake checks remain enabled.

Actual Android 11 captures were reviewed for the controls and Taskbar route. The portrait capture shows all three size/spacing sliders immediately; landscape shows size and horizontal spacing immediately, with additional controls reached by scrolling. Android 16 ATD retains its black-screen capture limitation; native assertions pass. Physical-device coverage is separate.

[Full regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42301804853): the Android 16 job succeeds with twelve Ten native suites and ten shared-engine scenarios. The Android 11 job remains in progress at packaging; this record does not claim that result.

## Signed update

Package `com.nthexperience.desktop.ten`, versionCode 17 / versionName 0.17.0; minimum SDK 26, target SDK 36. The existing Ten certificate supports an in-place update. v2/v3 signature and 16 KB ZIP alignment checks pass. All 453 non-signature APK payload entries match the main build and both focused-source CI APKs. Download ZIP CRC and embedded APK/notes equality pass. No test fixtures or signing material are bundled.

- APK SHA-256: `d585d0159d0fcc2a389faef7452dac00883dfc0fea1127068e164bc23ad2c4f9`
- ZIP SHA-256: `cd1d3a54d665631846f7fe036bea574181c06a546f1bcf67a3aed719a20dfe69`
- Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8`
