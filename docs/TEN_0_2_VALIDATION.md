# 0.2.0 validation

Build commit: `39effc800f2176d9a518d7cc3ec456a29c73a41b`.

[Successful Android 11 and 16 run](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42114594117).

| Check | Result |
| --- | --- |
| Build app, fixture and instrumentation APKs | Pass on both jobs |
| Model preparation / cancellation unit tests | 2 passed, 0 failures on each job |
| Portable Start tile and consent checks | 9,600 placements passed |
| Portable polish checks | 19,200 placements; 40 searchable Settings destinations; writing and fallback policies passed |
| Packaged runner / notification service checks | Both runners registered, Ten provider, protected notification listener |
| TenSmoke on API 30 and 36 | Pass on both |
| TenPolishSmoke on API 30 and 36 | Pass on both |
| Signed APK | v2/v3 signatures and ZIP alignment verify; certificate matches 0.1.0 |
| Application payload after signing | All 416 non-signature entries match the CI APK exactly |
| Download ZIP | Contents and embedded APK verified |

The native checks cover Start/search persistence and mixed tile modes, Settings category grid/search/sidebar, named desktop banks, move collision protection, recreation and puzzle recovery, dialog selection/cancellation callbacks, file ribbon capability state, provider-backed Recycle Bin recovery with exact bytes and a retained conflicting original, keyboard/accessibility tile ordering, real notification delivery/focus/revocation, browser chrome/security, assistant unavailable-device behavior and both orientations.

A repeated-layout loop in Action Center was identified when the Android 11 first-preview test stalled. The panel now updates its layout parameters only when its bounds change; Task View also avoids redundant padding changes. The final run completes both suites on both OS versions. Final Android 11 screenshots were inspected for Start, Settings, Explorer, Action Center and browser chrome.

A separate synthetic live request to the existing Nota gateway returned `The desktops are ready.` for the proofreading task. Native tests do not submit live cloud requests. Unit tests use a fake backend to check explicit model preparation and cancellation that discards late replies.

Supported-device on-phone inference still requires real AICore hardware. The two native suites are focused Ten validation and representative engine regressions, not a claim that all inherited Seven drivers are adapted or green.

The privately distributed preview APK SHA-256 is `aad8433240cc21e868c469d2fc6a55fb1b3e5af32aad56d1715b9927fe97d0e8`. Signing material and personal reference screenshots are not committed.
