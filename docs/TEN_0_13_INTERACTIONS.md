# Ten 0.13.0 taskbar and desktop interactions

- Ten adopts scrollable taskbar buttons on the first 0.13 launch, including older profiles that had Static selected. A later explicit Static choice is remembered. Settings / Taskbar exposes the fit-without-scrolling switch.
- Nth Nota has one fixed taskbar button between Task View and the scrolling apps. It remains visible when the app strip scrolls, shows its active state and minimizes/restores its window. Old saved Nota pins remain compatible without creating duplicates in the scrolling area.
- Device controls use a flat icon grid, the actual shell palette, themed foreground colors and the Aero shell surface when selected. App light/dark mode is independent. Tiles open real battery, network, volume, brightness, calendar, Bluetooth, Task Manager and Settings destinations.
- Creating a virtual desktop through Nota opens the real Task View immediately. An italic guide explains opening Start and returning through the desktop strip or Task View button. Return to previous desktop and Continue on new desktop are explicit actions. Task View toggles closed on a second taskbar tap; Start and app buttons leave the overview. The interactive layout follows the supplied Task View reference without embedding a user's personal desktop screenshot into everyone’s APK.
- External Android apps have icon-led Task View cards using their installed PackageManager artwork, app name and Tap to return action. The entire card switches to the app. No screenshot permission or background screen recording is introduced.
- Shake toggles the other built-in windows on the current virtual desktop. If any are visible, they are hidden; if all are minimized, they are restored, including windows minimized before the shake. The held window remains active. Recognition accepts four alternating 26 dp strokes within 1.2 seconds with moderate vertical movement. Normal title dragging, snapping and minimize/restore animation state remain supported.

## Platform and artwork limits

Android’s regular app APIs do not supply other apps’ task snapshots. MediaProjection can capture screens with explicit user consent, but is a separate screen-sharing feature; it is not required for these icon cards. Android apps remain Android tasks, and the launcher does not force-close or restore multiple external activities as desktop windows.

- https://developer.android.com/reference/android/app/ActivityManager#getRecentTasks(int,int)
- https://developer.android.com/media/grow/media-projection

The linked Winaero icon page returned HTTP 403 to the review tools. No icon archive was retrieved, and the existing artwork is retained. The user can supply the archive for a separate exact-asset pass.

`TenInteractionSmoke` covers the upgrade default, later user choice, fixed Nota coordinates under scroll and rotation, all 17 device-control themes with opposite app modes, real volume routing, Nota-created desktop guidance and preserved content on return, real installed-app cards/launch, and physical title-bar shake and restored controls. The complete existing shell and shared-engine regression remains enabled.
