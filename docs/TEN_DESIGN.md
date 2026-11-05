# Ten design references

Reviewed 49 screenshots supplied on November 5, 2026: 42 in `Nth Desktop Ten.zip` and 7 in `Nth Desktop Ten Part 2.zip`.

| Source | Observed pattern | Implementation target |
| --- | --- | --- |
| Part 1, Untitled–3 | Desktop, system tray, clock/calendar, taskbar menu | Left-aligned flat shell, inherited native flyouts |
| Part 1, Untitled4–10 | Explorer commands, navigation, selection | File/Home/View ribbon, real file operations |
| Part 1, Untitled11 | Task View previews | Adaptive window card overview |
| Part 1, Untitled12–18 | Search, Win+X, Start rail, app list and tiles | Search button, Start rail, tiles and context menus |
| Part 1, Untitled19–38 | Settings home, System, personalization, taskbar | Native Settings entry and staged hierarchy |
| Part 1, Untitled39–40 | File icon views and folder context commands | Inherited list/icon modes and selection |
| Part 1, Untitled41 | News and interests | Follow-up opt-in data panel |
| Part 2, Untitled | File transfer progress | Existing Explorer progress and cancellation |
| Part 2, Untitled1–3 | Start app context menus, expanded rail, desktop menu | Start/tile options and desktop context menu |
| Part 2, Untitled4 | Copilot writing entry | Nth Nota native writing panel |
| Part 2, Untitled5–6 | Browser menu and Explorer selection context | Browser redesign follow-up; inherited file menus |

The screenshots guide spacing, organization and behavior. Nth artwork is original; app icons come from installed Android apps or existing Nth artwork.

Primary references checked November 5, 2026:

- https://learn.microsoft.com/en-us/windows/configuration/start/layout — Windows 10 pin/unpin, tile resize and grouping behavior.
- https://learn.microsoft.com/en-us/windows-hardware/customize/desktop/customize-the-taskbar — Windows 10 taskbar layout and Action Center.
- https://support.microsoft.com/en-gb/windows/experience/personalization/customize-the-windows-start-menu — Start behavior and customization.

Touch adaptations: keep clickable targets around 44–48 dp, show tiles or All apps on narrow screens, horizontally scroll ribbon commands, retain remembered per-orientation window sizes and Android cutout bounds. Long press supplies right-click menus; hold then move supplies Start tile dragging.
