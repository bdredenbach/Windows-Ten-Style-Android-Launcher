# Ten 0.15.0 — Nota taskbar state and size

Nota's fixed taskbar button now uses the same 48 × 48 dp bounds as the normal icon-only app buttons. The fixed position and separate scrolling app strip are preserved.

Closed `DesktopWindow` objects can remain cached in the current desktop's window map. Ordinary app buttons already ignore detached windows; the fixed Nota button previously used any cached object, retaining a running/active background and sometimes trying to minimize the detached window instead of opening Nota. Its renderer, click action and context menu now share an attached-window lookup. Closing Nota returns the button to its idle state, removes the stale Close command and lets the next tap reopen it. Minimized Nota remains a running task with a Restore action.

The native interaction regression exercises caption and taskbar-menu closure, reopens through the same fixed button, minimize/restore, inactive/frontmost windows, another virtual desktop, equal app-button geometry and actual background drawing. These cases run in portrait and landscape with Ten dark, Ten light and Aero; Aero includes window animations. Existing fixed-position/scroll/recreation, Action Center, original artwork, device controls, desktop guidance, Android-app-card and shake checks remain enabled.
