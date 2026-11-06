# Ten 0.14.0 — Action Center toggle and supplied Windows artwork

The Action Center taskbar button now closes its already-open panel on the second tap. Previously the common `open` route dismissed the panel, then dispatched the same request to `show`, recreating it immediately. Both `@tenactions` and `@actioncenter`, including Windows+A, now return after dismissing an existing panel. Close, outside dismissal, Back and switching to another shell overlay still remove the notification observer.

## Original icon assets

The owner supplied `10125_icons.zip` from the Windows 10 build 10125 icon collection on 6 November 2026. Thirty matching ICO resources are included as lossless, transparent PNG conversions of their largest supplied frames. Their artwork is used directly, without drawing replacements, color filters, cropping or accent recoloring. The package contains early build 10125 designs; these are the supplied originals, rather than a claim that every resource matches every later Windows 10 release.

`TenIconAssets` maps the shared `AeroIcon` renderer to these originals. That puts matching artwork in desktop shortcuts, user folders, Start/search results, taskbar entries, window captions, Explorer lists/navigation/detail panes and icon-bearing menus. File Explorer uses its own application icon; folder entries use the actual folder asset. Empty/full Recycle Bin resources remain distinct. Thirty resources cover 33 icon kind aliases, including folder libraries, computer/network/storage, account/security, file types, clipboard commands, Control Panel and personalization.

Nth branding, Nth Nota, installed Android apps and tools without a matching supplied resource retain their own icons. Settings keeps its gear, distinct from the supplied Control Panel artwork. The supplied archive does not contain every installed application's icon.

[The asset manifest](TEN_0_14_ICON_ASSETS.json) records each archive filename, mapped kind, dimensions, source SHA-256 and converted PNG SHA-256. Import verification compared every decoded RGBA pixel and alpha value to its supplied ICO frame. No whole archive or unrelated resources are bundled. These Microsoft assets are not reclassified as original Nth artwork or covered by the source code license.

## Regression coverage

`TenInteractionSmoke` adds physical touchscreen taps on the actual taskbar Action Center button in portrait and landscape with Ten dark, Ten light and Aero shells. It checks opening, second-tap closure, both aliases, Windows+A, Close, Back, outside dismissal, switching to/from Task View and notification observer cleanup. It also draws all 33 mapped icon kinds through the native shared renderer and compares them to the packaged original bitmap, with visual captures of desktop, Explorer and open/closed Action Center.

The normal dual-OS regression remains enabled. The signed release validation record records the actual run and its outcome; this implementation note alone does not assert a passing run.
