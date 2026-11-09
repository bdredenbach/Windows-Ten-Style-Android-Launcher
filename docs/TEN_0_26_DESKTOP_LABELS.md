# Nth Desktop Ten 0.26.0 — desktop label clearance

The extended taskbar already reserved its two rows, but the desktop continued to render against the normal taskbar canvas. Bottom folder labels could therefore be clipped. The displayed grid now fits complete reference rows into the visible height, with icon measurement reserving the full two-line font footprint.

Saved positions and collision checks stay in normal taskbar coordinates. Only displayed vertical positions and cell heights adapt. Columns and order remain stable, and collapsing restores the original spacing. Dragging, swaps and free placement use reversible conversion. Existing positions require no migration or reset. The taskbar retains its 48/96 dp geometry and touch targets.

## Completed verification

Tested source: `bcc6852862e28fbd32f5c639f98894f8027b4005`, merged in [PR #7](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/pull/7). Both [focused Android 11/16 jobs](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42635579544) passed:

- All 24 unit tests per OS, including fitted-row bounds, unchanged normal geometry, free-placement fitting and coordinate roundtrips.
- Dense layouts of 24 saved folders with two-line labels: five columns/eight rows in portrait; eight columns/three rows in landscape.
- Full label-view visibility above the taskbar and no fitted-cell overlap.
- Repeated size changes without rewriting saved positions; stable columns/order and exact restoration of normal cell bounds.
- Folder swaps in the fitted grid and free placement, followed by size changes without drift.
- Existing floating-window movement, active caption drag through workspace resize, maximize/restore, taskbar Settings/Control Panel/hold-menu routes, backup and note preservation, and activity recreation.
- Ten dark/light/Aero in both orientations; twelve native label captures visually reviewed.

The signed APK uses the same Ten application ID and preview certificate, version 0.26.0 (26). All 456 non-signature application entries match both tested OS builds. APK v2/v3 signatures, 16 KiB alignment, ZIP CRC and byte-identical archived APK passed verification.

APK SHA-256: `649fdf42d5fa1b6ad3dda23e6996a5cc281c61d30d9a0564c4a809e492e18e96`.

Download ZIP SHA-256: `38d87a0163f65e036b80065cdb41e80ed0ab1db3bc85e0b03f00924e83a16a93`.

Physical Pixel 10/Android 17 was not newly tested. The broader theme/facet run is separate from the completed focused checks above.
