# Ten 0.9.0 validation

Build commit: `da8f10db1fac85639ccaac72607d8eb688f31bca`.

[Android 11 and 16 build and regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42162413774).

| Check | Android 11 / API 30 | Android 16 / API 36 |
| --- | --- | --- |
| App, fixture and instrumentation builds | Pass | Pass |
| Unit tests | 10 passed | 10 passed |
| Portable Start, consent, Settings and command checks | Pass | Pass |
| Packaged manifest and nine native runners | Pass | Pass |
| TenSmoke | Pass | Pass |
| TenPolishSmoke | Pass | Pass |
| TenShellSmoke | Pass | Pass |
| TenThemeSmoke | Pass | Pass |
| TenWorkspaceSmoke | Pass | Pass |
| TenThemeMatrixSmoke | Pass: 34 combinations | Pass: 34 combinations |
| TenFeedSmoke | Pass | Pass |
| TenCommandsSmoke | Pass | Pass |
| TenAccentSmoke | Pass: 128 combinations | Pass: 128 combinations |

The ten unit tests comprise two existing AI preparation/cancellation tests, six Calculator tests and two accent tests. Calculator checks cover decimal entry, Delete, sequential operators, operator replacement, repeated equals, memory add/subtract/recall/clear, error recovery, pending-operation restoration, legacy migration and invalid-state rejection. Accent checks cover all 24 literal choices, light/dark text variants and Aero gradient stops, plus a 4,096-color RGB sample for filled-control foreground contrast.

The native accent matrix uses the actual Colors picker: all 24 named accents plus eight Aero tints, light/dark apps, portrait/landscape and both OS versions, for 256 combinations. Every combination checks real shortcut labels for neutral ink, at least 4.5:1 contrast and stable ColorStateList identity across repeated appearance passes. At least 12 shortcut labels were present in every combination. Toolbar text is checked against its actual current gradient stops and opaque interaction-state colors, including Aero gradients. Literal swatches, Notepad foreground, shell text, Start columns and tile text are checked separately.

The scroll check observes freshly attached adapter cells before their first draw, exercises all five folder views and a context menu, then closes/reopens the folder. Calculator checks use actual buttons and verify wide-zero/tall-equals geometry, memory arithmetic, reopening, landscape bounds and malformed-backup rejection. Portrait Start exposes the app list beside pinned tiles.

The existing 17-theme matrix remains enabled: 34 theme/orientation combinations per OS, 68 total, with 12 built-in windows and preserved notes, selection and window instances. The eight previous native suites remain green.

## Visual review and scope

Reviewed final API 30 screenshots of the folder with Green, White and Black accents in dark app mode, portrait Start with both columns, and Calculator in portrait and landscape. Folder menu/command strips now change together with their text instead of retaining pale hardware-rendered backgrounds. Breadcrumb separators use regular neutral ink. Accent text accounts for background gradients and pressed/focus colors; normal app names retain neutral foregrounds.

API 36 validation uses native UI assertions and direct View theme reports; visual review uses API 30 captures because this headless Android 16 ATD image has a full-screen capture limitation. No native test sends a live AI gateway request or proves physical-device AICore inference.

## Live feed probes

Live HTTPS probes are recorded separately from deterministic feed regression.

| Source | API 30 | API 36 |
| --- | --- | --- |
| BBC News | Pass: 12 stories, 18,469 decoded bytes | Pass: 12 stories, 18,500 decoded bytes |
| TechRadar | Pass: 12 stories, 258,030 decoded bytes | Pass: 12 stories, 257,254 decoded bytes |

## Signed download

The final APK is versionCode 9 / versionName 0.9.0, package `com.nthexperience.desktop.ten`. It uses the same Ten preview certificate as versions 0.1–0.8. APK v2/v3 signatures and 16 KB ZIP alignment are verified. All 424 non-signature payload entries match the tested CI APK byte for byte. The Download ZIP passes CRC validation and embeds the exact signed APK with installation notes.

- APK SHA-256: `b609d003eb93f3ca4b9fa85410c85f5e85bd4330f94d996f1600c3afe43467d9`
- Download ZIP SHA-256: `9b8088b8548292c3aad71e6994ea27ffb1faf308d7fe282ea051fdaf1fa85286`
- Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8`

[Behavior and reproduced cause](TEN_0_9_POLISH.md).
