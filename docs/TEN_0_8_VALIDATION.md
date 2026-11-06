# Ten 0.8.0 validation

Build commit: `78343e7662b6b0b1b9a3fc6e741facad9cdeec46`.

[Android 11 and 16 build and regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42155448312).

## Automated checks

| Check | Android 11 / API 30 | Android 16 / API 36 |
| --- | --- | --- |
| App, fixture and instrumentation build | Pass | Pass |
| Model preparation and cancellation unit tests | 2 passed | 2 passed |
| Portable tile, consent, polish and local command checks | Pass | Pass |
| Packaged manifest and eight native runners | Pass | Pass |
| TenSmoke | Pass | Pass |
| TenPolishSmoke | Pass | Pass |
| TenShellSmoke | Pass | Pass |
| TenThemeSmoke | Pass | Pass |
| TenWorkspaceSmoke | Pass | Pass |
| TenThemeMatrixSmoke | Pass: 17 themes × 2 orientations | Pass: 17 themes × 2 orientations |
| TenFeedSmoke | Pass | Pass |
| TenCommandsSmoke | Pass | Pass |

The new native suite checks permanently visible Settings panes in portrait and landscape, nonoverlapping pane bounds, full-height category background, independent scroll, the complete theme gallery, utility classification in Settings and News, and a narrow dark Settings window. It sends a local browser request through the real Nota composer, checks a fresh chat returns to Write mode, verifies the first shortcut is visible without scrolling, checks that there is no model request or pre-tap browser launch, taps the named action and verifies the actual browser window. It also navigates the real Themes page, intercepts and verifies an Android Wi-Fi settings intent without changing a radio, launches the installed fixture app and observes its UI, uses editable command suggestions, removes stale buttons on edit, checks imported-source and generated-reply boundaries, and creates/selects an empty virtual desktop.

The portable command check covers polite phrasing, renamed installed apps, ambiguous app names including collisions between phone apps and Nth tools, unknown or internal-route strings, writing/passage boundaries, web and file queries, desktop commands and calculator classification.

## Regression scope

All eight native suites pass on both OS versions. The previous seven suites remain enabled. The theme matrix applies all 17 presets in both orientations on each OS version across twelve built-in windows. The feed suite retains deterministic large/gzip RSS, retries, cancellation and error UI checks; its optional live BBC and TechRadar probes are recorded separately in each artifact's `screenshots/ten7-feed-live.tsv`.

No live AI gateway request is made by the native tests. The local action suite runs with cloud permission off and does not require model preparation. Android 16 ATD screen captures are transparent on the headless image; native UI assertions and direct View theme sheets remain usable. The final Android 11 Settings background, theme gallery, landscape layout and narrow dark window, plus Nota shortcut and capabilities screens, were visually reviewed. All 17 theme presets still pass in both orientations on both OS versions (68 combinations). These are emulator checks, not physical-device AICore inference tests.

## Live feed regression probes

| Android version | Source | Result | Stories | Decoded bytes read |
| --- | --- | --- | --- | --- |
| 11 / API 30 | BBC News | Pass | 12 | 18,817 |
| 11 / API 30 | TechRadar | Pass | 12 | 258,784 |
| 16 / API 36 | BBC News | Pass | 12 | 18,715 |
| 16 / API 36 | TechRadar | Pass | 12 | 255,522 |

These are observations from this run, separate from the deterministic feed suite. Publisher availability and contents can change.

## Signed download

The signed preview uses the same Ten certificate as versions 0.1–0.7. APK Signature Schemes v2/v3 and 16 KB ZIP alignment pass. All 424 non-signature APK entries match the successful API 30 CI build exactly. The ZIP passes CRC validation and contains that exact APK and install notes.

- APK SHA-256: `f8f1b9a2ecd7fff06e4a4a07ece3cafff9e6d39f98e4d5c0819bb860f9c8398a`.
- ZIP SHA-256: `41b911920a8a2a05f3593b2e214f11646a5f3fd432ef1bc38851cb5dea4e3105`.
- Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8`.

Signing material remains outside Git.

[Behavior and available commands](TEN_0_8_COMMANDS.md).
