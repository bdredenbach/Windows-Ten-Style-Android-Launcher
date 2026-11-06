# Ten 0.11.0 validation

Tested source: `882d889f9aa7334296c2f2cb83fcdc1c1d31ac05`.

[Android 11 and 16 build and regression](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42223638380).

Both API 30 and API 36 builds pass the app, fixture and instrumentation builds, ten unit tests, portable command/geometry checks, manifest checks and all ten native suites. The existing 17-theme and 128-case accent matrices remain enabled on each OS.

The command suite now exercises the user's exact `can you open audible` wording against a synthetic installed launcher alias named Audible. It observes one actual Android launch without a shortcut tap and checks that returning to Nota does not launch it again. It also opens the real Nth Browser directly from Send, keeps settings/help/missing-app fallbacks explicit, and verifies that duplicate names require a button click. App-choice controls are checked for real PackageManager icons, 18 sp bold labels, readable light/dark contrast, minimum touch size and visibility in portrait/landscape. Imported passages and generated replies still cannot enter the command resolver or launch apps. No live AI inference or cloud request is needed for these app commands.

Reviewed API 30 portrait/landscape captures in light and dark modes. The shortcut cards have a visible button surface, app icon, large bold label and chevron. The landscape reply area scrolls to show additional choices. The fixture icon is the test application's real icon, not Audible artwork; on a user's phone, Nota uses the installed app's icon. API 36 retains the documented ATD screenshot limitation and is verified through native assertions.

## Signed package

Package `com.nthexperience.desktop.ten`, versionCode 11 / versionName 0.11.0. APK Signature Schemes v2/v3 and 16 KB ZIP alignment pass. The certificate matches Ten 0.1–0.10 for in-place updates. All 463 payload entries match the tested CI APK byte for byte. The Download ZIP passes CRC validation and embeds that exact APK and its install notes. Test launcher aliases belong to the separate fixture APK.

- APK SHA-256: `1c47c65d63f50c0f9344d9d3f394e4cc07af3fa525e0bb993bb8ac1505111ce2`
- Download ZIP SHA-256: `9bc4b79015ad1048962b97118ae19936a2d96a7f21c60cd65492e939a73c9208`
- Certificate SHA-256: `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8`

[Behavior and command boundaries](TEN_0_11_NOTA_OPENING.md).
