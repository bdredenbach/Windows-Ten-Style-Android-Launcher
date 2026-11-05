# Ten design references

Reviewed 49 screenshots supplied on November 5, 2026: 42 in `Nth Desktop Ten.zip` and 7 in `Nth Desktop Ten Part 2.zip`.

| Source | Observed pattern | Implementation target |
| --- | --- | --- |
| Part 1, Untitled–3 | Desktop, system tray, clock/calendar, taskbar menu | Left-aligned flat shell, inherited native flyouts |
| Part 1, Untitled4–10 | Explorer commands, navigation, selection | File/Home/Share/View ribbon, real file operations |
| Part 1, Untitled11 | Task View previews | Adaptive window card overview |
| Part 1, Untitled12–18 | Search, Win+X, Start rail, app list and tiles | Search button, Start rail, tiles and context menus |
| Part 1, Untitled19–38 | Settings home, System, personalization, taskbar | Searchable Settings category grid and page sidebar |
| Part 1, Untitled39–40 | File icon views and folder context commands | Inherited list/icon modes and selection |
| Part 1, Untitled41 | News and interests | Opt-in RSS/Atom panel with explicit refresh |
| Part 2, Untitled | File transfer progress | Existing Explorer progress and cancellation |
| Part 2, Untitled1–3 | Start app context menus, expanded rail, desktop menu | Start/tile options and desktop context menu |
| Part 2, Untitled4 | Copilot writing entry | Nth Nota native writing panel |
| Part 2, Untitled5–6 | Browser menu and Explorer selection context | Modern browser chrome and Nota entry; inherited file menus |

The screenshots guide spacing, organization and behavior. Nth artwork is original; app icons come from installed Android apps or existing Nth artwork.

Primary references checked November 5, 2026:

- https://learn.microsoft.com/en-us/windows/configuration/start/layout — Windows 10 pin/unpin, tile resize and grouping behavior.
- https://learn.microsoft.com/en-us/windows-hardware/customize/desktop/customize-the-taskbar — Windows 10 taskbar layout and Action Center.
- https://support.microsoft.com/en-gb/windows/experience/personalization/customize-the-windows-start-menu — Start behavior and customization.

Touch adaptations: keep clickable targets around 44–48 dp, show tiles or All apps on narrow screens, horizontally scroll ribbon commands, retain remembered per-orientation window sizes and Android cutout bounds. Long press supplies right-click menus; hold then move supplies Start tile dragging.

## 0.2 comparison decisions

The reference Start menu fits three medium tiles across its right-hand group; Ten now uses six base cells where the available touch width permits it. A four-cell layout remains available at narrow widths. The rail expands to names and keeps Settings/Power reachable in short landscape workspaces.

The Settings home uses the 13-category grid and local-profile header from Untitled19. Page navigation follows the pale left pane, selected accent row, search box and large right-side page title from Untitled20–38. System-specific actions open their actual Android counterpart.

Explorer follows the File/Home/Share/View ribbon, then address navigation, then the folder contents from Untitled4–10. The compact fallback retains a separate search row when address and query cannot fit together.

The browser follows the tab-above-navigation structure, light address pill and ellipsis menu in Part 2 Untitled5. The original Nth blue/teal browser artwork replaces the old IE illustration.

Task View follows the reference overview, with a desktop strip, window cards, close controls and move menus. See Microsoft's desktop behavior and shortcuts:
- https://support.microsoft.com/en-us/windows/experience/how-to-multitask-in-windows
- https://support.microsoft.com/en-us/accessibility/windows/keyboard-shortcuts-in-windows

Android notification integration follows:
- https://developer.android.com/reference/android/service/notification/NotificationListenerService
- https://developer.android.com/about/versions/15/behavior-changes-all
