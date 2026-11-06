# Ten 0.5 desktop and application appearance

## Desktop gadgets

Ten no longer constructs or attaches `GadgetSidebar`. The Weather, feed and tool data providers live in `TenServices`, which is not a View and has no desktop drawing or gadget timer. Rotation, old visible flags, workspace presets and restored backups cannot attach gadgets. Desktop context menus and legacy Personalization no longer offer a gallery or visibility switch; old gallery routes open News and interests. Taskbar News, Weather, Calculator and Sticky Notes remain available as built-in tools. Old gadget notes, photos and tool values are retained.

Microsoft removed Desktop Gadgets beginning with Windows 8; Windows 10's News and interests taskbar experience is distinct. Primary reference: https://learn.microsoft.com/en-us/windows/win32/w8cookbook/desktop-gadgets-removed

## Independent shortcut sheets

Windows traditionally shares the physical Desktop folder across virtual desktops. Nth deliberately goes further at the owner's request: each named desktop owns its shortcut membership and portrait/landscape icon positions as well as built-in windows. The default desktop's pre-0.5 shortcuts, folder groups and both layouts migrate in place. Existing secondary desktops start with empty icon sheets, and newly created desktops start empty.

Files, folder contents, Start pins, taskbar pins and the appearance theme are shared. Adding a shortcut does not copy or move a physical file. Removing an icon changes the current sheet. Closing a desktop merges its shortcuts into another desktop and moves its windows, preserving folder contents; duplicate shortcuts are combined. The destination's icon positions are retained, and incoming icons are arranged into free space. Existing singleton-window collision protection remains. A full destination or duplicate open tool prevents closing rather than dropping content.

App-folder recycling records memberships across desktops; restore returns the folder to its original surviving desktop sheets. If all original desktops were closed, it returns to the current desktop so the restored folder remains reachable. Backups include every desktop's membership and both layouts, validate IDs and coordinates before restore, and still import older backups. Actual folder access remains subject to Android grants.

## One application appearance

Ten Midnight, Ten Spectrum and Graphite select dark application surfaces. Ten Blue, Ten Light, scenic and Aero presets select light application surfaces. Colors now exposes a separate dark-app switch, alongside the existing shell mode and accent options, following Windows' separate app and system modes. Saved Nth themes and backups include this app mode; older theme files default to light applications.

Shared application palettes cover native backgrounds, text and hints, selected rows, buttons, switches, sliders, dialog content, context menus, folder/browser chrome and Settings. Neutral surfaces adapt in place; an Aero preset restores the inherited gradient and corner treatment. Window chrome uses the same accent and selected finish. Existing windows, including windows on inactive desktops, are updated without recreation. Newly generated rows inherit the active palette. Notepad text and selection, browser sessions and window instances remain intact.

Photographs, video, website-rendered content, theme previews and explicitly colored paper/art retain their colors. External Android apps and Android system Settings retain their own appearance controls. This is application theming, not a global screen color filter.

Primary references:

- https://learn.microsoft.com/en-us/windows/apps/develop/settings/settings-common
- https://learn.microsoft.com/en-us/windows/apps/design/signature-experiences/color
- https://support.microsoft.com/en-us/windows/experience/configure-multiple-desktops-in-windows
