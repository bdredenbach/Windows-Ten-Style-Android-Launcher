# Nth Desktop Ten 0.22.0 — taskbar size and initial preferences

The taskbar has a normal 48 dp row and an optional 96 dp layout. Extended mode places pinned and running application buttons across the upper row. Start, Search, Task View, fixed Nota, News, device controls, clock, Action Center and Show desktop remain in the lower row. The existing app scrolling, grouping, previews, reordering and animation anchors continue to use their actual button views. The workspace gives both rows their own space.

The saved `ten_taskbar_extended` switch is available in Personalization → Taskbar, Control Panel → Taskbar and Start Menu, and the taskbar hold menu. The controls synchronize after changing size. The preference is included in validated desktop backups. Normal size remains the initial choice.

Only the existing first-initialization branch changes the color and Start defaults. Fresh installs select White, keep shell and title-bar accent fills off, and use the dark shell with white text. Full-screen Start, the app list, and live local tile details are all enabled. Initialized installations retain their saved colors and Start choices.

## Validation status

- Local Java compilation passed for all 202 production, native-test, unit-test and generated support sources against Android 36/dependency compile jars. The resource declarations and BuildConfig were generated locally for Java validation; this does not constitute an assembled APK.
- All 18 existing JUnit tests passed.
- Shell syntax, manifest-check Python syntax, and Git whitespace checks passed.
- Added `TenTaskbarSizeSmoke` for fresh-install settings, exact 48/96 dp bounds, real Settings/Control Panel/hold-menu controls, scrolling, saved preferences, document preservation, backups and activity recreation. It covers portrait and landscape with Ten dark, Ten light and Aero palettes.
- Android 11/16 emulator execution, complete theme/regression CI, APK assembly/signing and visual inspection remain pending. Automated approval review blocked publishing the local branch to the public repository, so these checks have not been represented as completed.

Local implementation branch: `polish/ten-taskbar-defaults`.
