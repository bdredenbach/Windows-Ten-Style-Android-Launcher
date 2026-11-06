# Ten 0.10.0 validation

Tested source: `91503bf6ab6018d5c077d5b99163e6e4470fe46d`.

[Final Android 11 and 16 build and regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42218859342).

| Check | Android 11 / API 30 | Android 16 / API 36 |
| --- | --- | --- |
| App, fixture and instrumentation builds | Pass | Pass |
| Unit tests | 10 passed | 10 passed |
| Portable geometry, Settings and command checks | Pass | Pass |
| Packaged manifest / ten native runners | Pass | Pass |
| TenSmoke | Pass | Pass |
| TenPolishSmoke | Pass | Pass |
| TenShellSmoke | Pass | Pass |
| TenThemeSmoke | Pass | Pass |
| TenWorkspaceSmoke | Pass | Pass |
| TenThemeMatrixSmoke | Pass: 34 combinations | Pass: 34 combinations |
| TenFeedSmoke | Pass | Pass |
| TenCommandsSmoke | Pass | Pass |
| TenAccentSmoke | Pass: 128 combinations | Pass: 128 combinations |
| TenReferenceSmoke | Pass | Pass |

The new native runner checks actual Search landing sections and app/document filtering, hardware summary cards, copying real device specifications to Android's clipboard, font search/custom sample/application/validated backup, proportional Task View thumbnails inside the viewport, and a fully opaque wallpaper snapshot. It opens the actual Win+X popup and its Device Manager destination, verifies the news hero/Top stories/weather/game sections, and opens the new system tools. Both orientations are exercised. The retained theme and accent matrices still run against real built-in windows and shortcut labels.

The first run exposed newly tracked management commands missing from the backup shortcut catalog. Tracking is now limited to supported shortcuts and the new tools have valid portable IDs. Visual review also found translucent wallpaper drawing could let live windows show through Task View's snapshot. The final snapshot starts opaque, and the new runner samples its bitmap alpha across a grid to prevent regression.

## Visual review and limits

Reviewed API 30 portrait and landscape captures for Search, About, Fonts, Task View, Win+X and News. The final captures confirm an opaque wallpaper behind the previews, two-column portrait font previews, four About cards on a wide page and icon-free grouped Win+X rows. Narrow layouts scroll instead of squeezing every desktop control into a short phone screen. Feed layout screenshots use clearly labeled synthetic stories; real feed transport is verified separately by TenFeedSmoke. Article image loading retains its existing optional setting.

API 36 uses native view assertions; its headless ATD image retains the previously documented full-screen screenshot limitation. Physical Pixel hardware, on-device AICore inference and proprietary Windows functionality are outside these emulator checks. About shows Android's actual exposed metadata and graphics APIs. The PowerShell entries open the limited Nth command tool with Android app privileges.

API 30 live probes read twelve stories each from BBC News and TechRadar, using the real Android streaming parser and HTTPS transport. Provider outcomes remain separate from deterministic regression.

## Signed download

Package: `com.nthexperience.desktop.ten`. Version code 10 / version name 0.10.0. The preview certificate matches Ten 0.1–0.9 for an in-place Android update. APK Signature Schemes v2/v3 and 16 KB ZIP alignment pass. All 463 payload entries match the final tested CI APK byte for byte. The Download ZIP passes CRC validation and contains exactly that signed APK and its install notes.

- APK SHA-256: `a1415af9c32c547fc1d9c83049ea6cfe145ec3f1ed3db7256147e1c356a94cc2`
- Download ZIP SHA-256: `04ba6804865d6fc1c8f29b864e4e6836e57a44f4c6c4f71155cd389063f68583`
- Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8`

[Behavior and Android counterparts](TEN_0_10_REFERENCE_LAYOUTS.md).
