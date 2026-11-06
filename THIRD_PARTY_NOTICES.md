# Third-party build tooling

The Gradle wrapper scripts and `gradle/wrapper/gradle-wrapper.jar` come from the official Gradle 8.13 distribution. Gradle is distributed under the Apache License, Version 2.0; a copy is in `third_party/gradle/LICENSE.txt`. Gradle's wrapper script headers preserve their upstream copyright notices. The Gradle distribution is downloaded separately by the wrapper and verified using its official SHA-256 checksum.

Android SDK/build tools and the Android Gradle Plugin are downloaded as development dependencies under their respective licenses; they are not source assets from the inspiration project. The Internet browser uses AndroidX WebKit 1.13.0 and its transitive AndroidX support/annotation dependencies, distributed under the Apache License, Version 2.0. The license is included in `third_party/androidx/LICENSE.txt`. Source: https://android.googlesource.com/platform/frameworks/support/ . WebKit supplies compatibility APIs; the installed Android WebView provider supplies its own Chromium engine under its provider licenses.

The Nth Desktop code, wallpaper and vector/primitives icon artwork were created for this project. No Microsoft assets or jovanovski/windowslauncher code are included. Android app icons and labels shown at runtime come from apps installed on the user's device.

## Selawik typeface

The unmodified Selawik Regular (`selawk.ttf`) and Semibold (`selawksb.ttf`) fonts are copyright 2015 Microsoft Corporation, with Reserved Font Name Selawik. They are redistributed under the SIL Open Font License 1.1; the complete license is in `third_party/selawik/LICENSE.txt`. Source: https://github.com/microsoft/Selawik/releases/tag/1.01 . Selawik is an open-source Segoe UI replacement; no proprietary Segoe UI font files are included.

## Weather data

Weather forecasts are provided by the Norwegian Meteorological Institute (MET Norway), under CC BY 4.0. This application formats temperatures and condition descriptions from the original data, caches results until the provider's expiry time, and identifies itself in the User-Agent header. Attribution and a licensing link are available in Weather settings. No API key is required. Forecast requests are made only while the configured Weather gadget and launcher are visible. Location search uses Android's Geocoder service; approximate device location is optional and requested only after the user chooses that action.

- https://api.met.no/
- https://docs.api.met.no/doc/TermsOfService
- https://creativecommons.org/licenses/by/4.0/

Nth Media Player and Nth Media Center use AndroidX Media3 ExoPlayer 1.10.1 and its support modules under the Apache License, Version 2.0. The license is included in `third_party/androidx/LICENSE.txt` and in the app's license assets. Source: https://github.com/androidx/media .

## Nth Desktop Ten lineage

Ten is derived from the owner's Nth Desktop Seven and adapts the Nth Nota cloud writing client. The Nth tile icon, blue light wallpaper and Ten launch animation are original code-drawn Nth artwork. Existing license notices continue to apply.

## ML Kit direct model access

Ten 0.2 uses Google ML Kit GenAI Prompt API 1.0.0-beta4 and Android AICore on supported devices. Model preparation and SDK diagnostics may contact Google. On-device inference and user-authorized cloud fallback are separate processing paths. Google ML Kit terms and Android data disclosures apply: https://developers.google.com/ml-kit/terms and https://developers.google.com/ml-kit/android-data-disclosure . The client integration is adapted from the owner's Nth Nota source.

## Owner-supplied Windows 10 icon artwork

Ten 0.14.0 uses thirty original Microsoft Windows icon resources from the owner's supplied `10125_icons.zip` (Windows 10 build 10125 icon collection). The selected ICO frames were converted losslessly to transparent PNG without editing the artwork. Windows and these icon designs belong to Microsoft. These assets are separate from the original Nth artwork and are not relicensed under the source code license. Exact resource filenames, kind mappings and source/output SHA-256 values are recorded in `docs/TEN_0_14_ICON_ASSETS.json`. Collection link supplied by the owner: https://winaero.com/download-icons-from-windows-10-build-10125/ .
