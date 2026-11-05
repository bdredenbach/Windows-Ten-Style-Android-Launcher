# Ten implementation progress

## 0.2.0 — screenshot-led polish and working desktop features

- Start uses compact four- or six-cell placement, three medium tiles across when space allows, reliable vector rail buttons, an expanded rail, alphabet navigation, editable groups and full-screen/tablet mode. Drag between groups, use Ctrl+Left/Right, or choose accessibility reorder actions. Calendar, battery, local notes and selected weather location update locally.
- Settings has all 13 reference categories, a searchable catalog of real destinations, a selected-page sidebar, native launcher options and explicit Android settings links. Background selection and picture import return to the Ten page. Windows-only account/cloud/device features are not simulated.
- Files, the Notepad document, browser profile data and launcher preferences are shared across desktops. Multiple Notepad views update together.
- Named virtual desktops keep built-in windows attached and preserve their visibility, content and per-desktop/orientation sizes. Task View creates/renames/closes desktops and moves windows by drag or context menu. Windows-key+Ctrl+Left/Right switches desktops; Windows-key+Ctrl+D creates one. Desktop state restores with Android's saved activity state.
- A desktop can close by moving its windows to the first other desktop. If that would replace another instance of a singleton Nth tool, closing/moving is refused and the user is asked to move or close the duplicate. Android apps remain Android tasks across launcher desktops.
- Explorer has File/Home/Share/View tabs, a persistent collapse control and selection/capability-aware commands. The old Organize/Views command row is removed. The ribbon precedes address navigation.
- Browser has tabs above a flat navigation/address bar, a Settings and more menu, updated artwork and a Nota entry. Existing WebView permission, HTTPS, InPrivate and full-screen behavior remains in the engine.
- Nota tries the ML Kit/AICore model first. Preparing/downloading is an explicit action. Free cloud fallback still requires opt-in, and both engines receive the same composer-only writing instructions. Cancellation and safety failures do not trigger cloud fallback.
- Optional Android notification access supplies real Action Center cards. Contents stay in memory, lock-screen previews are redacted, revoked access clears the feed, and Focus assist hides app cards locally. Permission is chosen through Android settings.
- News and interests accepts a user-selected HTTPS RSS/Atom feed and displays cached headlines. Refresh is explicit; Weather opens the existing weather tool.
- New native and portable tests cover desktop banks/restoration, groups/reorder, Settings search/navigation, ribbon state, notifications/revocation, browser security, model preparation/cancellation, and representative dialog, Recycle Bin and puzzle regressions.

## Remaining product work

- True multiple instances of each singleton Nth tool within the same desktop, and automatic collision resolution when closing desktops.
- More live tile templates, tablet gestures and keyboard navigation; further comparisons on the owner's real phone and external display.
- Broader browser feature parity, advanced Settings replacements for the retained legacy theme/profile/layout editors, and optional weather headlines in the taskbar.
- End-to-end on-device inference on a supported AICore phone. Emulator checks cover the unsupported-device path; fake-backend unit tests cover prepare, inference and cancellation.
- Complete adaptation of every inherited Seven suite. Focused native regression coverage is recorded separately from full legacy parity.

The full Nota canvas and non-writing tools remain separate from the launcher writing panel. No remote desktop, Windows applications, Microsoft account or Windows Update functionality is implied.
