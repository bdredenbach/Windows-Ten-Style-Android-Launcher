# Nth Desktop Ten 0.25.0 — categorized Nth app tiles

The default Start pack includes 43 built-in apps and tools, with distinct colors, mixed Normal/Wide/Large shapes and nine supported live tiles. Fresh installations open the full-screen tile view, with the app list available from the rail. Existing layouts are kept until the user chooses **Add all Nth app tiles** from Start or Settings → Personalization → Start. This retains custom pins and explicit per-tile categories and sizes.

Settings → Personalization → Start → **Tile size** selects Normal or Large overall scale. Hold a tile → Resize offers Small, Normal, Wide and Large. Supported tiles have individual live on/off controls. Group rename, reorder, accessibility and drag/drop controls remain available. Independent category columns use landscape space while preserving the same grouping in portrait.

| Category | Default apps and tools |
| --- | --- |
| Productivity | Nth Nota, Notepad, Sticky Notes, Calendar, Calculator, Checklist, Unit Converter, Color Studio, Text Counter, Clock, World Clock, Countdown, Stopwatch, Days Until |
| Internet & information | Nth Browser, Weather, News and interests, Feed Headlines |
| Photos & media | Photos, Nth Media Player, Nth Media Center |
| Files & desktop | File Explorer, Computer, Recycle Bin, Backup and Restore, Recent Items, Saved Workspaces, Desktop Organization, Quick Launch |
| System tools | Settings, Task Manager, Battery, Event Viewer, Device Manager, Computer Management, Nth PowerShell, Run, Storage Meter, Network Meter, Compass, Welcome Center |
| Games | Tile Puzzle, Dice Roller |

Calendar, Clock, Notepad, Sticky Notes, Checklist, Weather, News and interests, Feed Headlines and Battery support live details. They read local or cached state; tiles introduce no new network requests or permissions. Cached headlines rotate while Start is open. Preference changes update existing tile views in place, preserving scrolling and interaction.

Backups retain the expanded pin set, Normal/Large scale and per-tile live preferences. Legacy Medium values remain accepted and display as Normal. The pin capacity is 128 so the built-in pack and custom app pins can coexist.

## Verification

Tested source: `d8e3af9536512fc1037aa24fa9644e552cf0134b`, merged in [PR #6](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/pull/6). Both [final focused Android 11/16 jobs](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42628482804) passed:

- All 21 unit tests per OS, including catalog coverage and 128-pin mixed-size packing.
- All 43 tile action routes, known built-in destinations, and actual Notepad/Calculator launches.
- Fresh tile-first defaults, six categories, mixed shapes/colors, native resize/scale/live controls, local content updates and unchanged tile objects during live refresh.
- Tile bounds, overlap and text-fitting checks in Ten dark/light/Aero, portrait/landscape and both overall scales. Twenty-four Start captures and four lower-category views reviewed.
- 43-pin backup retention, legacy Medium sizes, live choices, recreation, and existing custom layout preservation when the pack is added.
- Previous window/taskbar position tests: active caption dragging through workspace resize, maximize/restore, taskbar controls, note preservation and recreation.

Both OS application payloads match the signed deliverable. Same Ten application ID and preview certificate, version 0.25.0 (25); APK v2/v3 signatures, 16 KiB alignment, ZIP CRC and byte-identical archived APK verified.

APK SHA-256: `d234d92bdc22bcfe043ff75b298860da18759167c4e7b3fd899fc2fef80412bd`.

Download ZIP SHA-256: `d3a6b0e43fdeb0ac54dfe9bfeae8d9df8d3e2352510e0781d96ee2aee7eb21d8`.

Physical Pixel 10/Android 17 was not newly tested. The broader theme/facet run is separate from these completed focused checks.
