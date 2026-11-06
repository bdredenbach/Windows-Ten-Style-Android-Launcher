# Ten 0.12.0 validation

Tested source: `57edbbb552568a59e8a8e8ca28f34df32643b43e`.

[Android 11 and 16 build and regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42246996501).

Both final API 30 and API 36 jobs pass: ten unit tests, portable geometry/catalog/command checks, packaged-manifest checks, eleven Ten native suites and ten shared-engine scenarios on each OS. The downloaded evidence was checked for all 21 expected PASS records and zero failure markers on both images.

## Tested scope

- Eleven Ten native suites: shell/Start/Search, settings, virtual desktops, app modes, taskbar/insets, local Nota and actual installed-app opening, themes, accent contrast, calculator, notifications, news, About, fonts, Win+X and window lifecycle/recovery.
- New audit suite opens all 48 Settings pages in portrait and landscape, in both app modes. It verifies real component/feature inventory, search, App info routing, lock-screen/Cast/connected-device destinations, separate Start/Search behavior, Recycle Bin tabs and quick-action contrast for all 24 accents in both shell modes.
- Ten shared-engine scenarios: real WebView browsing/TLS/download/upload/video/private-session behavior; native/SAF Photo Gallery; actual playback/seek/video/visualizer; first-run Home/file/Usage Access setup; native storage and standard folders; revoked storage recovery; provider-backed Explorer operations, cancellation and cross-UID picker grants; native corner resizing; denied APK source; actual native/SAF file opening and Android Package Installer.
- Portable checks validate 28,800 mixed-tile placements, Settings/Nota command resolution and consent/routing policy. App unit tests and APK manifest checks run before the emulators.

## Initial test corrections

The first expanded run exposed the missing narrow-browser Home menu action, which was added. The inherited Explorer test retains real held file/blank-folder menus and uses Ten Home ribbon commands in populated folders instead of the removed Seven Organize button. The native audit also opens the real View/Layout menu. Browser tests exercise Ten’s Settings and more menu and its narrow-layout equivalents. Isolated test resets explicitly deny special-access appops, since Android’s `pm clear` can retain a previous suite’s grants. No product assertions were removed to ignore failures.

The screenshot review additionally found a wrapped Settings section title overlapping its paragraph; headings now grow with their text, with a native layout assertion. Explorer popups use a visible ribbon anchor.

## Evidence and limits

The 49-image review is recorded in [the reference audit](TEN_0_12_REFERENCE_AUDIT.md). Reviewed the final API 30 captures of the Recycle Bin ribbon, wrapped Settings headings, system component/feature inventories, quick-action contrast and Explorer layout. The ribbon labels fit, the File tab stays readable, and the narrow Settings section no longer overlaps its paragraph. API 30 supplies actual screenshots for visual inspection. API 36’s ATD image has the documented black-screen screenshot limitation; native view/state/operation assertions still run on it.

Tests use isolated providers and generated files on disposable emulators. They do not prove every physical phone, OEM settings activity, installed external app, live news/weather provider or AI model. Windows-only privileges and services remain outside the launcher.

A separate synthetic live cloud-writing request was rejected by Cloudflare from this workspace (HTTP 403, error 1010). Automatic approval review blocked a retry using Android client headers as bypassing the service access restriction; further live checks were stopped. Live cloud writing therefore still requires an in-app device check. Native/local AI tests use controlled backends and verify preparation, consent, fallback and cancellation behavior.

## Signed package

Package `com.nthexperience.desktop.ten`, versionCode 12 / versionName 0.12.0. APK v2/v3 signatures and 16 KB ZIP alignment pass. The certificate matches Ten 0.1–0.11 for in-place updates. All 463 ZIP payload entries match the tested-source CI APK byte for byte.

- APK SHA-256: `c12382c630fce2f392794bf096138f809635bbb88e48e091a789f5a7ab503b9a`
- Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8`
- Download ZIP SHA-256: `e948b4d9a002f748403aed78b36dc813c223cc9ba1ce243c69814c38588d7464`

The ZIP passes CRC validation and embeds that exact APK and its install notes. The separate test/fixture APKs are excluded from the download.
