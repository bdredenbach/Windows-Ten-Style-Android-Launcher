# Ten 0.3.0 validation

Build commit: `39c12d3b2aa83ff54ddc6b21ef768e67d2500fe8`.

[Android 11 and 16 run](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42126203692).

| Check | Result |
| --- | --- |
| App, fixture and instrumentation builds | Pass on API 30 and 36 |
| Model preparation and cancellation unit tests | 2 passed on each job |
| Portable Start/consent and polish checks | Pass on each job |
| Packaged native runners and text-selection target | Three runners and Nota target registered |
| TenSmoke | Pass on both OS versions |
| TenPolishSmoke | Pass on both OS versions |
| TenShellSmoke | Pass on both OS versions |
| Signed download | v2/v3 signatures, existing preview certificate, ZIP alignment pass |
| Signed payload | All 418 non-signature entries match the CI APK exactly |
| Download ZIP | CRCs and embedded APK match |

TenShellSmoke verifies taskbar bounds and nonempty clock in both orientations, News feed cards/bookmarks/Back/running-app dismissal, RSS summary/image parsing and DTD rejection, selected-text Nota import, confirmation before applying a reply, changed-source protection, private draft/history exclusion, Android text-selection import, and animated minimize/restore interruption and close/reopen.

The final Android 11 taskbar, News and Nota screenshots were inspected. The first visual review exposed an empty clock immediately after rotation and a squeezed landscape reply area; the final revision fixes both and adds regression checks.

Native suites use synthetic feed content and synthetic replies. They do not submit live cloud requests or claim supported-device model inference. Existing local-first/free-cloud policy checks remain in place. Remote feed-image availability depends on the chosen publisher; image loading is optional and bounded. Full inherited Seven parity, rich document conversion and Windows/third-party Android lifecycle parity are not claimed.

[All 49 screenshot references](TEN_0_3_SCREENSHOT_REVIEW.md).

Preview APK SHA-256: `941ba72a4233337b9040925654d87dc696422b3a1ae850ac50813d0735a95a42`. Signing material and personal reference screenshots remain out of Git.
