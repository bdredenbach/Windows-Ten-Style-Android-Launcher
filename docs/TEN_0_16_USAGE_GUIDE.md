# Ten 0.16.0 - Usage Access guide identity

The App info illustration in Usage Access setup still contained the Seven logo and name. It was an embedded screenshot, independent of the correctly branded launcher icon.

## Change

Replace the bitmap with `UsageAccessExample`, a non-interactive native layout. It loads the installed application's actual icon through ApplicationInfo and the application name from `app_name`. Ten now shows its N10 icon and Nth Desktop Ten label. The old `usage_access_app_info.webp` is removed.

The example is marked Example, retains its existing accessible description, wraps at the available width, honors text scaling and preserves its Android Settings colors rather than inheriting launcher recoloring. Actual permission routing, optional setup, OK, Later/Cancel, App info and return behavior are unchanged.

The only other visible Seven label in production Java is the intentional theme section “Aero from Nth Desktop Seven.” It is not a stale product identity. No Eight repository or handoff was changed.

## Validation

Tested source: `42aeca990677c318374ab44572233078b276188b`.

[Focused Android 11/16 regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42297204775): both jobs succeed. Downloaded evidence has ten passing unit tests per OS, native interaction PASS records and no failure markers. The focused APK payload matches the main build.

[Full regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42297204782): Android 16 succeeds, with twelve Ten native suites and ten shared-engine scenarios, including setup, Usage Access and App info recovery. All 22 result records contain PASS and no failure markers. The first full Android 11 attempt failed during emulator system startup before testing the app: Activity Manager and Job Scheduler were not ready. Only that failed job was retried; its result is pending at packaging. No completed full Android 11 pass is claimed here. The separate focused Android 11 job passed.

Android 16 ATD retains its black-screen capture limitation. Native setup assertions passed, but this release does not claim on-phone visual verification or every OEM's Settings behavior.

## Signed update

Package `com.nthexperience.desktop.ten`, versionCode 16 / versionName 0.16.0, minimum SDK 26 and target SDK 36. It uses the existing Ten certificate for an in-place update. v2/v3 signature and 16 KB ZIP alignment verification pass. All 453 non-signature payload entries match the source CI APK. The old Seven bitmap is absent. ZIP CRC and embedded APK/notes equality pass. No fixture APKs or signing material are included.

- APK SHA-256: `846229e837f3880709290f6b178607167dc19cf8203d8d72abe167e2722c4498`
- ZIP SHA-256: `a8b05cb4311bf10db93b419579bb1b5b1045196528f57bb2e82d50256d566180`
- Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8`
