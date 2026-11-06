# Ten 0.7.0 validation

Build commit: `5ca1129e02bf3b6518709dfbfc44fcddb26d2141`.

[Android 11 and 16 build and native regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42149921984).

## Automated checks

| Check | Android 11 / API 30 | Android 16 / API 36 |
| --- | --- | --- |
| App, fixture and instrumentation build | Pass | Pass |
| Model preparation and cancellation unit tests | 2 passed | 2 passed |
| Portable tile, consent and polish checks | Pass | Pass |
| Packaged manifest and seven native runners | Pass | Pass |
| TenSmoke | Pass | Pass |
| TenPolishSmoke | Pass | Pass |
| TenShellSmoke | Pass | Pass |
| TenThemeSmoke | Pass | Pass |
| TenWorkspaceSmoke | Pass | Pass |
| TenThemeMatrixSmoke | Pass: 17 themes × 2 orientations | Pass: 17 themes × 2 orientations |
| TenFeedSmoke | Pass | Pass |

The feed suite exercises the production streaming parser and FeedClient with scripted HTTP connections: a generated multi-megabyte RSS body and gzip body; twelve-item early stop; the 8 MiB decoded cap; Atom, XML encoding, nested source titles, entities and relative links; DTD rejection; valid empty feeds; rejected website/HTTP 403/404 responses; manual retries despite cache expiry and ordinary failure backoff; server Retry-After; preservation of the prior source, cached stories and bookmarks; cancelled stale responses; and an actual editable connection dialog followed by successful cards in portrait and landscape.

## Live public sources

These probes use the native Android HTTPS connection and parser, separate from the deterministic regression result. They are observations from this run; publishers can change availability, size or content.

| Android version | Source | Result | Stories | Decoded bytes read before stopping |
| --- | --- | --- | --- | --- |
| 11 / API 30 | BBC News | Pass | 12 | 18,806 |
| 11 / API 30 | TechRadar | Pass | 12 | 255,354 |
| 16 / API 36 | BBC News | Pass | 12 | 18,656 |
| 16 / API 36 | TechRadar | Pass | 12 | 259,816 |

Raw publisher articles are not committed. The run artifacts retain `screenshots/ten7-feed-live.tsv` for each device image.

## Regression scope

All seven native suites pass on both OS versions. The previous six suites remain part of this run, including 68 passing theme/orientation/OS combinations. The theme matrix applies all eight Ten and nine Aero presets in both orientations, verifies twelve built-in windows, preserved note text and selection, independent previews and literal glass colors, and exercises current and legacy Personalize routes. Workspace tests cover empty secondary desktop icon banks, folder recovery, backup roundtrips, shared app palettes and the removal of the Seven gadget sidebar.

Android 16 ATD full-screen captures are transparent on the headless image; its UI assertions and native View comparison sheets remain usable. Android 11 provides full screen-capture evidence. The portrait connection error dialog was visually reviewed, along with successful feed layouts and taskbar News flyouts in both orientations. The additional feed-suite board is an instrumentation overlay; taskbar flyout screenshots show the normal bounded dashboard. These emulator checks do not claim a physical-device or external-app theme test.

## Signed download

The downloadable preview uses the same Ten certificate as versions 0.1–0.6. APK Signature Schemes v2 and v3 and 16 KB ZIP alignment pass. All 424 non-signature APK entries match the successful API 30 CI build exactly. The Download ZIP passes CRC validation and contains that exact signed APK plus install notes.

- APK SHA-256: `bcac3650a000780c586d0d91d587816c41e877d0bc83f894bda1267ed6bfacb5`.
- ZIP SHA-256: `7a8af69e507e7eb6da259e25c247b0208a7d79277a4a75e4cd09ee07843e60ee`.
- Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8`.

Signing material and raw publisher feeds remain outside Git.

[Reproduced news defect and implemented fixes](TEN_0_7_NEWS_FIXES.md).
