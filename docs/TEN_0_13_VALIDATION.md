# Ten 0.13.0 validation

Tested source: `4812b9a35dac0ec73656cb70faa481f3f633937d`.

[Android 11 and 16 build and regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42256284856).

Both final API 30 and API 36 jobs pass all twelve Ten native suites and ten shared-engine scenarios: 22 native scenarios per OS. Ten unit tests, portable checks and packaged-manifest checks also pass. Downloaded evidence was checked for every expected PASS record and zero failure markers.

The twelve Ten suites and ten shared-engine scenarios remain enabled on each OS, together with ten unit tests, portable geometry/consent/command checks and packaged-manifest checks.

`TenInteractionSmoke` checks a scrollable upgrade default and preservation of a later explicit Static choice after actual activity recreation. It verifies the fixed Nota button's screen coordinates before/after scrolling in both orientations, excludes duplicates, opens Nota, tests all 17 device-control themes with the opposite app mode, checks shell surface/text contrast and actual volume routing, creates a virtual desktop through Nota's action button, checks italic guidance, returns to preserved notes, verifies Task View/Start entry points, inspects actual installed-app icons and launches the fixture app, and injects physical title-bar gestures to verify hide/restore cycles, initially minimized windows and restored window state.

Recreation uses application lifecycle callbacks, and screenshots use bounded state-based waits. The initial test monitor/idle wait stalled; its replacement progressed to the guide test. The guide selector was corrected to the control's existing `Nota action:` accessibility label. Existing product assertions remain enabled.

The complete shared-engine coverage includes WebView navigation/TLS/download/upload/video and privacy, gallery decoding/EXIF/slideshow/grants, real audio/video playback and seeking, first-run Home/file/Usage Access setup, direct and granted storage operations, permission revocation, Explorer cancellation/verification/pickers, resizing, file opening and real Package Installer fixtures.

Reviewed final API 30 portrait/landscape taskbar captures, Aero/light device controls, new-desktop guidance and restored windows. Installed-app card artwork and launch behavior are verified through native view/action assertions. The desktop guide is italic and offers direct return/continue actions; app cards show real installed artwork instead of the generic placeholder. API 30 supplies visual captures. API 36's ATD image has the documented black-screen capture limitation, so native state/geometry/operation assertions are used there. Tests do not prove every physical phone, OEM setting or third-party app. No live cloud-writing claim is made by this release.

[Interaction behavior and Android/artwork limits](TEN_0_13_INTERACTIONS.md).

## Signed package

Package `com.nthexperience.desktop.ten`, versionCode 13 / versionName 0.13.0. Signature and 16 KB alignment checks pass with the existing Ten certificate. All 463 payload entries match the final tested-source CI APK byte for byte. The download ZIP passes CRC validation and contains that exact APK and its install notes.

- APK SHA-256: `691972838f32d96ce827c8e747ddce50c78977ff5649b3008e9f2e3990d28525`
- Download ZIP SHA-256: `9465f655fdc155a7b9436a3813cbe8e1b2d157e4ae668faccae473934cde3832`
- Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8`

The signing certificate matches Ten 0.1–0.12 for in-place updates. Fixture/test APKs are excluded from the download.
