# Nth Desktop Ten 0.23.0 — refined system artwork

Uses sixteen 256-pixel icon groups from the owner's uploaded `7tsp_windows_10_system_icons_by_ardentaa_dfe8n0v.7z`: fifteen refreshed folder/file/control resources and one new Task Manager icon. The shared renderer displays the selected PNGs throughout desktop shortcuts, taskbar buttons, Start/Search, Explorer and built-in window captions/menus.

The selection refreshes the generic folder, Explorer, Documents, Pictures, Music, Videos, Downloads, Control Panel, document/picture/music/video file types, Delete, Personalization and Fonts. Task Manager now uses the matching supplied performance-monitor artwork. Nth branding and installed Android app icons retain their established sources.

The 7z contains Microsoft Visual C `.res` data. It was parsed as resource data: `RT_GROUP_ICON` entries reference `RT_ICON` images, which were reassembled into ICO directories and decoded from their existing 256 × 256 frames. PNG output retains alpha; no artwork editing or upscaling was applied. Small or unmatched groups were excluded. Exact archive/resource/group/PNG hashes and previous asset hashes are recorded in [the provenance manifest](TEN_0_23_ICON_ASSETS.json).

## Verification

The [focused Android 11/16 run](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42609376264) passed on both OS versions. Verified test source: `af5bd403ae9cc6f631b94d7394ed7c6125af9da1`. Application/asset source: `f2dbd6bd0bb955c5a57639412e22e75429932ad9`. Merged in [PR #4](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/pull/4).

| Check | Android 11 | Android 16 |
| --- | --- | --- |
| Build and 18 unit tests | PASS | PASS |
| All 34 mapped icon kinds/aliases match packaged PNG pixels at 256 pixels | PASS | PASS |
| Native icon sheet and six Explorer captures: Ten dark/light/Aero × portrait/landscape | Reviewed | Reviewed |
| Desktop controls, taskbar geometry/scrolling/recreation and Nota lifecycle | PASS | PASS |
| Seventeen device-control themes, Action Center, desktop guidance and external app cards | PASS | PASS |
| Physical window-shake behavior | PASS | PASS |

The screenshot helper restores its starting theme after capture. Continuous gesture input is enqueued before waiting for rendering so the slower emulator does not convert a drag into a caption long press. Initial Android 11 attempts completed the icon comparisons but exposed this test-fixture timing issue; the production and image payload did not change during the test-only follow-ups.

All sixteen selected source PNGs retain exact pixels inside the APK. All 456 non-signature application entries match both final focused OS builds and the signed APK. Existing Ten preview certificate, APK v2/v3 signatures, 16 KiB alignment, ZIP CRC and the ZIP's byte-identical APK passed verification.

This verifies the icon/interaction change. The broad theme/text matrix runs separately and is not claimed complete by this focused report. Physical Pixel 10/Android 17 was not newly tested.

APK SHA-256: `c96f403c7f9718214809835e0da4c11beb3038bda4891b1031cf6592b4164f97`.

Download ZIP SHA-256: `e529554e6fee855432f285d227b7222cb9b748ddb3beb6e7b8dfc046866b76a6`.
