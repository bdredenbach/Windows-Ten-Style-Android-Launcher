# Ten 0.14.0 validation

Production source: `30330bd9981a6d476978a15819cadfc04f348aab`. Full-regression test source: `1768e155d224b92a99b6093ae7fa1ff2f1382fdc`. Gallery test follow-up: `5ee530b3c5f395dbde947a881931135aa315d442`. Production source and all 493 APK payload entries are identical across those builds; the follow-ups change tests and evidence collection.

[Full Android 11/16 regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42263206930) and [focused Gallery follow-up](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42265752702).

## Passing coverage

The full API 36 job passes all twelve Ten suites and ten shared-engine scenarios, plus ten unit tests, portable checks and packaged-manifest checks. API 30 passes the same twelve Ten suites and nine shared-engine scenarios, plus ten unit tests and portable/manifest checks. Its older Gallery test selected a clickable ListView ancestor and tapped the entire list's center, which can lie on the divider between two items. The test now taps the named label's actual bounds and waits for its callback before checking the exact index/cancellation count. The complete Gallery scenario then passes on both API 30 and API 36 in the focused follow-up. No product code changed for that correction.

Together these downloaded records contain passing results for all 22 native suites/scenarios on each OS and ten unit tests per OS. Evidence verification explicitly combines API 30's 21 full-run passes with its Gallery follow-up; it does not describe the original full API 30 job as green. The normal full matrix remains enabled, and the focused Gallery workflow supports bounded follow-up checks.

The initial run passed the new interaction checks, but the old accent audit assumed every `open` request recreated Action Center. That audit now closes the panel between color samples while preserving all 48 selected-tile, contrast and focus-toggle cases.

## Action Center and original artwork

The focused native interaction suite injects real touchscreen taps on the taskbar Action Center button in portrait and landscape across Ten dark, Ten light and Aero. It verifies second-tap closure, both aliases, Windows+A, Close, Back, outside dismissal, Task View transitions and notification observer removal. The native shared renderer draws all 33 mapped icon kinds, comparing their pixels and transparency to the thirty packaged original bitmaps.

Archive import separately verifies source/output SHA-256 and every RGBA pixel. Only thirty selected largest ICO frames are included. [Asset mapping and behavior](TEN_0_14_ACTION_CENTER_AND_ICONS.md).

Reviewed API 30 Action Center captures in all three shell styles and both orientations, the closed desktop, supplied desktop folder artwork, Explorer caption/taskbar/library/storage artwork, and the successful Gallery dialog. One interaction-stage Explorer capture caught the desktop; Explorer artwork was reviewed in the independent `ten-explorer` and `ten12-explorer-view` captures instead. API 36 ATD retains its black-screen capture limitation; native state, geometry, operation and software-rendered bitmap assertions remain available there. Emulator checks do not prove every phone or OEM setting. No live cloud-writing claim is made.

The complete shared-engine coverage includes browser navigation/TLS/download/upload/video/privacy, photo decoding/EXIF/slideshow/grants/revocation/recreation, real audio/video playback/seek, first-run Home/storage/Usage Access setup, Explorer operations/cancellation, resizing, selected-file grants and Package Installer fixtures.

## Signed package

Package `com.nthexperience.desktop.ten`, versionCode 14 / versionName 0.14.0. Signature v2/v3 and 16 KB alignment verification pass with the existing Ten certificate. All 493 signed APK payload entries match both focused-source CI APKs byte for byte, as well as the full-regression APK. The download ZIP passes CRC validation and contains the exact APK and install notes. No fixture/test APKs are included.

- APK SHA-256: `b3d4b4b0f485aeaaa214d4a52cdf93eb6a9ca040d0f770c559006f29ce4b14ba`
- Download ZIP SHA-256: `a8e40537d67a1d011a3ce6e50b808f6b9f3ce9214486d6c23e7f3f60a4a0ce2e`
- Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8`

The certificate matches Ten 0.1–0.13 for in-place updates.
