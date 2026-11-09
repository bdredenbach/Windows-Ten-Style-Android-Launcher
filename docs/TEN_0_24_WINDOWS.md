# Nth Desktop Ten 0.24.0 — stable window positions

Changing the normal 48 dp taskbar to the extended 96 dp taskbar previously recomputed each floating window's position as a fraction of the newly available space. That moved windows even when their original bounds still fit. A workspace size change also reset an active caption drag, returning it to the saved placement. Configured desktop rows were resized against the shorter viewport, changing icon positions and spacing.

Floating windows now retain pixel anchors while the live work area changes and clamp only where needed. Explicit layout restoration and orientation changes still use the existing normalized geometry format. Active caption drags continue through a workspace resize. The desktop grid uses the normal taskbar canvas for row sizing and placement, while the visible viewport reserves both taskbar rows; lower icons remain reachable by scrolling.

## Completed verification

The baseline fixture reproduced the old taskbar-induced floating-window shift on Android 11 and Android 16: [baseline run](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42617792756).

Both final focused OS jobs passed: [verified run](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42620053423). Tested head: `9725feab5a47dfcbebc0b33e0d710a6a07d560b4`, merged in [PR #5](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/pull/5).

- All 18 unit tests per OS and Android application/test builds.
- Physical upward caption drags, release, and repeated layout/inset passes.
- Floating-window bounds and custom desktop row positions through real normal/extended controls.
- A workspace resize while the caption is being dragged, followed by release at the same location.
- Minimize/restore and maximize/restore, with maximized bounds ending above the taskbar.
- Existing Settings, Control Panel and taskbar hold-menu routes, overflow scrolling, backup validation, open-note preservation, first-launch defaults and activity recreation.
- Ten dark, Ten light and Aero in portrait and landscape. Twelve native window captures and native taskbar captures were visually reviewed.

The input fixture includes stationary endpoint moves before changing the work area, avoiding predicted motion from a single injected movement. Native view drawing provides inspectable captures where the emulator compositor returned blank images. These fixture follow-ups did not change the application payload.

## Package verification

Same `com.nthexperience.desktop.ten` application ID and Ten preview certificate, version 0.24.0 (24). All 456 non-signature application entries match both final focused OS builds and the signed APK. APK v2/v3 signatures, the existing certificate, 16 KiB ZIP alignment, ZIP CRC and byte-identical archived APK were verified.

APK SHA-256: `61547d1844f546d1eb17fe2ad918db6e3cafc0a2f8200426ec2fc91ff7778fb3`.

Download ZIP SHA-256: `6a50650c7b01a59b18b6e3685bb7dcfb65c65179f6c4b7838fc44e1f126b6b2c`.

Physical Pixel 10/Android 17 was not newly tested. The broader theme/facet run is separate from the completed focused checks above; this report does not claim that it completed.
