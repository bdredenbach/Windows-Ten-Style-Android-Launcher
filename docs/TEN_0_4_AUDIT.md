# Ten 0.4 branding, themes and application audit

The pass covers the Android icon resources, launcher shell, Start, native Settings, inherited settings links, built-in window chrome, onboarding, file/media/browser entry points, Task Manager and portable settings. The 49-image reference map remains in [the screenshot review](TEN_0_3_SCREENSHOT_REVIEW.md). Source-wide searches covered the 140 main Java classes and packaged resources for old branding, stale help and unfinished placeholders; native integration checks cover representative flows, not every inherited branch.

## Findings and changes

| Finding | Resolution in 0.4 |
| --- | --- |
| Android launcher-density icons still carried inherited Seven artwork while shell marks were plain N shapes | One black sheen square with N¹⁰ is used for application icons, adaptive icons, Start, default profile/Welcome branding, startup, About and Run. Android's native toasts take the app icon from these resources. User-supplied profile pictures are preserved. |
| Android can crop adaptive foregrounds with circular masks | Safe adaptive foreground insets and a matching monochrome N¹⁰ layer. Android still controls icon masks and optional themed-icon recoloring. |
| Native Themes page had links rather than a visible gallery | Native Personalization > Themes now displays eight Ten presets, nine Aero/Basic presets, and My themes. Theme previews appear before related settings links so the gallery is visible immediately. Desktop Personalize opens it directly. |
| Saved Seven presets changed old preferences that Ten's renderer ignored | Explicit Ten/Aero shell styles now connect the preset to actual wallpaper-sampled glass, tints, opacity and reflection. The layout stays Ten while Aero supplies its optional finish, including when Start is reopened. |
| Only one Ten-specific background existed | Added original procedural Daylight, Midnight and Spectrum variants alongside the existing Ten Blue and scenic collections. |
| Changing shell colors recreated the activity | Theme, light/dark, transparency and accent changes apply in place. Open window instances and documents are retained; the selected Settings page stays open. |
| Windows-style accent controls were missing | Added Start/taskbar/Action Center accent, title-bar accent, transparency, shell-finish selection and Aero opacity controls. The light shell keeps its pale surface, matching the reference's separation of light mode and colored dark shell. |
| Theme files omitted Ten shell settings | Save/import/export retains Ten appearance fields. Older Nth Seven profiles remain readable and apply as Aero. These are Nth theme files, not Windows `.deskthemepack` files. |
| Light-theme flyout headings and thumbnail commands retained white text | They now use the shell text color for readable contrast. |
| Run was absent from the system menu and keyboard flow | Added Run to Start search and the Start context menu, Windows+R, Windows+X, recent commands, installed-app resolution and Nth tool aliases. It opens real destinations rather than executing a shell. |
| Task Manager still used the inherited green graph style for every theme | Ten uses blue graphs on a pale grid; Aero retains the green style. Actual sampling, pause/refresh and Android process limitations are unchanged. |
| Welcome help still referred to Flip 3D and Quick Launch terminology | Updated guidance for Task View, pinned taskbar apps, Nota, News, device controls, Themes and Run. |
| Newer preferences were omitted from backups | Portable backups now include shell style/color switches, News display/hover/image choices, saved story links, local Nota history and Run history, with validation before restore. Private-browser Nota passages remain excluded from automatic writing history. Cloud consent and Android permission grants remain device-specific. |

## Theme catalog

| Ten presets | Aero presets retained from Seven |
| --- | --- |
| Ten Blue | Seven Blue |
| Ten Light | Twilight |
| Ten Midnight | Meadow |
| Ten Spectrum | Harmony |
| Coastal Teal | Landscapes |
| Warm Horizons | Architecture |
| Landscape Collection | Midnight Circuit |
| Graphite | Paper Skies |
|  | Seven Basic |

My themes supports saved local themes and the existing preview, export and delete flow. Import remains the existing permission-scoped Android document picker. A theme can contain stock art or its portable embedded picture copies.

## Remaining differences

- Android owns installed-app processes, the lock screen, account/device settings and system permissions. Nth cannot provide Windows programs, registry tools, Windows Update or arbitrary process termination.
- Some advanced font, icon and media editors retain their inherited layouts. The main Personalization/theme path is native Ten.
- Same-desktop multi-instance tools, automatic virtual-desktop collision resolution, four-way Snap/Snap Assist and wider browser parity remain larger improvements.
- News uses connected RSS/Atom feeds. A financial-quote service, account-based editorial personalization and social reactions are not simulated.
- Hardware-specific local AI inference still requires testing on a supported real AICore phone. Native tests cover available/unavailable routing and source/context behavior.

Primary references checked November 5, 2026:

- https://support.microsoft.com/en-us/windows/experience/personalization/personalize-your-windows-experience-with-themes
- https://support.microsoft.com/en-US/Windows/Experience/Personalization/personalize-your-colors-in-windows
- https://support.microsoft.com/en-us/accessibility/windows/keyboard-shortcuts-in-windows
