# Nth Desktop Ten 0.23.0 — refined system artwork

Uses sixteen 256-pixel icon groups from the owner's uploaded `7tsp_windows_10_system_icons_by_ardentaa_dfe8n0v.7z`: fifteen refreshed folder/file/control resources and one new Task Manager icon. The shared renderer displays the selected PNGs throughout desktop shortcuts, taskbar buttons, Start/Search, Explorer and built-in window captions/menus.

The selection refreshes the generic folder, Explorer, Documents, Pictures, Music, Videos, Downloads, Control Panel, document/picture/music/video file types, Delete, Personalization and Fonts. Task Manager now uses the matching supplied performance-monitor artwork. Nth branding and installed Android app icons retain their established sources.

The 7z contains Microsoft Visual C `.res` data. It was parsed as resource data: `RT_GROUP_ICON` entries reference `RT_ICON` images, which were reassembled into ICO directories and decoded from their existing 256 × 256 frames. PNG output retains alpha; no artwork editing or upscaling was applied. Small or unmatched groups were excluded. Exact archive/resource/group/PNG hashes and previous asset hashes are recorded in [the provenance manifest](TEN_0_23_ICON_ASSETS.json).

## Validation

The existing native icon-renderer checks now include Task Manager. Every mapped icon is compared pixel-for-pixel with its packaged PNG at 256 pixels. A native contact sheet and Explorer screenshots in Ten dark/light and Aero, portrait/landscape, support visual review. The focused Android 11/16 suite also exercises desktop icon settings, taskbar geometry/scrolling/recreation, Nota lifecycle, device-control themes, Action Center, desktop switching and window interaction. Build, native results, signing and download hashes are recorded after verification.
