# Nth Desktop Ten 0.18.0 — dark-mode consistency

The Browser screenshot exposed a shared appearance bug: native text changed to its dark-mode color while ordinary rounded gradient backgrounds kept their light fills. This affected the address field and selected tab, and could also affect other custom fields.

## Changes

- Theme ordinary rounded fills and state drawables in place while retaining their original light palette, geometry and borders.
- Resolve text and editable hints against their actual control surfaces. Controls created in dark mode remain readable when the same view returns to light mode.
- Use visible dark-mode strokes for browser navigation and photo controls, including mode-change invalidation and consistent disabled fading.
- Retain unchanged text palette objects when scrolled folder cells attach to their parent, so repeated appearance passes remain stable before the first draw.
- Draw Calendar date labels, weekday headings, separators and selected dates using the app palette.
- Respect tool backgrounds supplied by the tool itself. PowerShell retains its blue console, pale command hint/cursor and readable Run-button focus state instead of receiving the generic window fill.
- Keep sticky-note paper and literal artwork/previews in their intended colors; explicitly supply a readable hint for the note editor.
- Apply the app palette to the separate file chooser used for browser uploads, picture selection and other file requests, including its heading, search and footer controls. Refresh the native control theme on return to that activity.
- Preserve existing windows, browser tabs, text, selections, image content and WebView content during appearance changes.

## Validation

Verified source revision: `8cedccbb0e3a4e38d74a6c9c3cdbfea94206e7ae`.

| Check | Android 11 / API 30 | Android 16 / API 36 |
| --- | --- | --- |
| Native appearance audit | PASS: 372 surface passes; 6,692 text and 336 hint checks | PASS: 372 surface passes; 6,768 text and 336 hint checks |
| Accent, theme and scrolling regression | PASS: 128 combinations | PASS: 128 combinations |
| Unit tests and APK/test compilation | PASS | PASS |
| Full existing regression suite | PASS | PASS |

The audit opens all 48 Settings pages, legacy Control Panel destinations and built-in tools/utilities in portrait and landscape. It measures enabled native text and hint contrast against actual fills, cycles all 17 presets through repeated app-mode changes in a retained browser, checks rendered browser/photo glyph and Calendar date pixels, and checks real menus and retained dialog fields. The real file chooser is launched in each orientation; its ordinary theme binding must update the field fill before the explicit audit, and its query and selection must survive mode changes. Console commands and focus are exercised in both app modes. Typed text and selection are retained.

- [Native audit and evidence](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42423447828)
- [Full regression and evidence](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42423447827)

Web pages and third-party Android app screens keep their own appearance. These results describe automated native contrast/state/pixel checks. After the workspace recovered, Browser, Calendar, dialog and file-chooser screenshots from the API 30 run were also manually reviewed in portrait and landscape. API 36 coverage uses automated state and pixel checks.

## Signed APK

The signed 0.18.0 preview is packaged and verified. Install it over the existing Nth Desktop Ten preview to keep the current desktop setup and notes. The download ZIP contains only the APK and install notes.

| Item | Verified result |
| --- | --- |
| Application | `com.nthexperience.desktop.ten` |
| Version | `0.18.0` / code `18` |
| Android support | API 26 minimum; target API 36 |
| APK | `Nth-Desktop-Ten-0.18.0.apk`, 10,840,323 bytes |
| APK SHA-256 | `6a343d0c6b241ca1447e1dbe98cd741a35be53ece0d07697c2552c4dd8b4aae5` |
| Existing preview certificate SHA-256 | `4c9efb14377b1609083ac30236b5395ba38738c91dc4054273b3162b1a84d4a8` |
| Signatures | v2 and v3 verified |
| Alignment | Final ZIP alignment verified, including 16 KiB page alignment |
| Application payload | All 492 non-signature entries match both tested CI APKs |
| Download ZIP | CRC verified; embedded APK and notes match the standalone files exactly |

The test artifacts in GitHub Actions use the CI signing key. The distributed preview uses the existing Nth Desktop Ten preview key.
