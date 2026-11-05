# Source lineage

- Seven source: https://github.com/bdredenbach/Windows-Seven-Style-Android-Launcher
- Baseline commit: `3e6e2900a5884c028f15beeb6236e7764818d5c8`, branch `feature/seven-window-resize`, draft PR #1, version 0.6.1.
- Chosen over Seven main (`075b4eefe81e5a2d837c57af1243aab35cdf3b2c`) to include current window resizing, setup recovery and taskbar fixes.
- Nota reference: https://github.com/bdredenbach/Nth-Nota at `16e4d16cecf48238b5857eb658db0874d93a8db3`.
- Ten destination: https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher

The first Ten import retains Seven engine and third-party assets, notices and reference test drivers. Internal class names such as Aero and VistaExplorer are implementation lineage; product-facing Ten shell uses the new flat visual treatment. Seven baseline history is archived in SEVEN_BASELINE.md.

Original Seven 0.6.1 notes report three older Android 11 regression failures: a Recycle Bin visibility assertion, a photo-list callback-index check and gadget puzzle/window recreation. They are inherited investigation items, not claims of verified Ten behavior.
