# Ten 0.9 — accent stability, Calculator and portrait Start

## Accent colors and the reported recording

The recording shows shortcut names intermittently dark/invisible while other cells are already white. Adapter rows were created with their original light-mode ink and recolored only during a later global layout pass. Appearance handling also inferred accent roles from RGB values (including the selected accent), so ordinary white or black text could be mistaken for an accent when those colors were selected. Broad blue-color heuristics could recolor unrelated content. Each pass allocated new ColorStateLists and invalidated every view even when nothing changed.

Shortcut rows/cells and replaced shell content now receive their appearance before attachment. The application binding checks before drawing, covering dynamically created children. Mapped text states are cached and remain unchanged across repeated passes. Color changes no longer infer accent roles from the user's chosen RGB value: selected Settings labels, Nota controls and shell links declare that role explicitly. Ordinary shortcut names retain neutral ink in light apps and a light foreground in dark apps, regardless of accent. Original source colors remain available when switching modes.

Settings → Personalization → Colors provides 24 named, literal swatches: White, Black, Graphite, Gray, Silver, Navy, Blue, Sky, Cyan, Teal, Emerald, Green, Lime, Sage, Yellow, Gold, Amber, Orange, Red, Ruby, Pink, Magenta, Purple and Violet. Black/white checkmarks adapt to each fill. Text versions of accent colors are adjusted independently for contrast; the stored accent and preview swatch remain literal. Colored title bars choose a black or white foreground. Tinting a dark shell is bounded so its text remains readable; the separate light-shell control still selects the light appearance. Nota glyphs and tile labels use their actual surfaces rather than assuming every accent is readable everywhere.

The native matrix tries all 24 named accents plus the eight Aero glass-tint accents in light/dark apps and portrait/landscape. It checks actual folder labels, repeated palette identity, swatch RGB, text contrast, Start columns and dynamic scrolling. The existing 17-theme matrix remains enabled. The text-contrast target for enabled text is at least 4.5:1 on the tested surfaces.

## Calculator

Calculator opens as its own native panel without inherited Explorer gadget chrome. Its layout follows the supplied reference: large right-aligned result, blue MC/MR/M+/M− row, orange C, gray controls and digits, double-width zero and double-height green equals. Short wide windows place the display beside the keypad; narrow windows use the reference's vertical layout.

The controls implement decimal entry, Delete, four arithmetic operations, operator replacement, sequential calculation, repeated equals, memory add/subtract/recall/clear, sign change and percent (in the calculator menu), copy result and keyboard input. Invalid arithmetic reports an error that clears on the next number entry. Result, memory and pending operations persist when closing/reopening; older calculator results migrate. Backup validation rejects malformed calculator state. Calculator keypad colors remain literal for the reference look, while the display and surrounding app/title appearance follow the selected theme.

## Start

Normal Start now keeps the app list and pinned tiles side by side from 320 dp, with a narrower app column in portrait. Each column scrolls separately. Tile sizing accounts for the real available width, including wide/large/small tiles, and smaller cells adjust their icon/caption spacing. Search results retain a full-width list. Expanding the rail and explicit full-screen/tablet app-list settings retain their separate compact navigation behavior.

## References

- User-provided recording and screenshots; the personal files remain outside Git.
- https://developer.android.com/reference/android/view/ViewTreeObserver.OnPreDrawListener
- https://developer.android.com/reference/android/content/res/ColorStateList
- https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum

### Retained toolbar backgrounds

Final screenshot review caught pale Explorer menu and command strips in dark app mode. Theme panels used to update their gradient palette only inside `draw()`, so a retained hardware display list could keep a light background after its text had changed. Appearance passes now refresh the panel palette immediately and invalidate only when the mode, accent or finish changes. Check and radio drawables use the same change-driven invalidation. The accent runner checks each enabled toolbar label against the actual current gradient stops, rather than an assumed dark surface.

Breadcrumb separators also use the regular foreground instead of a low-contrast blue-gray, in app folders and real-file Explorer.

Accent text uses its real background gradient stops and interaction states, inheriting the parent surface when the control itself is transparent. It retains the selected hue as far as contrast permits, with independent foregrounds for flat and Aero surfaces. A surface-key cache keeps repeated appearance passes stable.
