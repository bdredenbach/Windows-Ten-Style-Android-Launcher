# Ten 0.3 screenshot review

Reviewed all 49 owner-supplied images in both archives. The images describe the desktop layout and the organization of individual tools. They are still images, so the timing of opening and closing animations is an implementation decision, informed by Microsoft's animation guidance and tested separately.

## Design decisions implemented in 0.3

- Keep one flush, flat taskbar: original N Start mark, adaptive search, Task View, pinned/running apps, News and interests, device notification area, two-line clock/date, Action Center and a narrow Show desktop edge.
- Reserve useful room for actual app buttons on phones. Use a search box only at wider desktop sizes; the icon remains on narrow screens. Use 24 dp app artwork inside 48 dp touch buttons. Pinned apps become their running button. Active apps have a wider accent underline and shaded background; inactive running apps have a shorter underline. Folder stacks get an edge marker.
- Keep hidden tray commands above the chevron instead of expanding a row of tools across the phone's taskbar. Wide displays show battery/network/volume inline. Task Manager remains in the chevron panel.
- Replace inherited Flip 3D wording with Task View in the taskbar menu. The Start context menu follows Win+X organization with real Nth/Android destinations.
- Add short open/close transitions and minimize/restore toward each tool's own visible taskbar slot. Interruptions settle correctly. Close releases the real tool immediately while a short decorative snapshot fades; private browser windows skip that snapshot. Respect Reduce motion and Android's animator setting. Android apps use a launch transition request; Android controls their actual task lifecycle.
- Replace the standalone News window with a dashboard flyout above the taskbar. It has adaptive story columns, a large lead headline, publisher attribution, optional feed thumbnails, excerpts, local bookmarks, real selected-city weather with upcoming hourly temperatures, and Nth games. Tap outside, press Back/Escape, or open another shell surface to dismiss it.
- The initial news connection is a user action. Users can select BBC News or supply any supported HTTPS RSS/Atom feed. Article images require a separate opt-in. Headlines are cached, feed downloads honor expiry, and unavailable services retain old stories. No invented stock quotes, social reactions, or financial charts are displayed.
- Make Nota reachable from window captions, Start queries, selected Notepad/sticky text, text-file context menus, browser selections, news headlines, and Android's text selection/share menu. Each route imports text into the visible composer. Sending remains explicit. Replies can be copied, shared, appended to Notepad or applied to the original selection after confirmation and an unchanged-source check.
- Give Nota a new-chat command, local recent-writing history, task chips and compact layout. Private browser passages are excluded from automatic draft/reply/history storage. The on-device-first/free-cloud policy is retained.

## Individual references

“Retained” means the working behavior already existed and was preserved. “Platform” means a real Android destination or platform limitation, rather than a simulated Windows service.

| Archive / image | Observed detail | Response |
| --- | --- | --- |
| 1 / Untitled | Flush dark taskbar, search box, aligned app slots, news chip, time over date | New taskbar sizing, running markers, chip and numeric date |
| 1 / Untitled1 | Hidden notification icons in a popup above the taskbar | Chevron opens the device-controls flyout; no phone-width inline expansion |
| 1 / Untitled2 | Clock/calendar flyout anchored to the right | Existing real calendar retained; consistent taskbar clock hover surface |
| 1 / Untitled3 | Taskbar context menu and window arrangement | News, Nota, Task View, arrangement and native Ten taskbar settings |
| 1 / Untitled4 | Multiple tools with separate title bars; compact Explorer ribbon | Retained independent windows; Nota caption action and window motion |
| 1 / Untitled5 | Home ribbon and capability-based commands | Existing command capability checks retained |
| 1 / Untitled6 | Home ribbon sections, then breadcrumb/address row | Existing tab/command/address order retained |
| 1 / Untitled7 | File menu and frequent destinations | Existing real file destinations retained; Nota text-file command added |
| 1 / Untitled8 | Details view, column headings, folder navigation | Existing sortable details, tree and navigation retained |
| 1 / Untitled9 | Share ribbon | Existing real Android share operations retained |
| 1 / Untitled10 | View ribbon with alternate layouts | Existing Details/Tiles/icons and preview modes retained |
| 1 / Untitled11 | Task View cards over the desktop | Existing window/desktop cards retained; taskbar menu now consistently says Task View |
| 1 / Untitled12 | Search as a distinct taskbar surface | Adaptive search box/icon; Start results gain Ask Nth Nota |
| 1 / Untitled13 | Win+X style system menu | New Start context menu with Task Manager, Settings, Explorer, System, Network and power routes |
| 1 / Untitled14 | Expanded Start navigation rail | Existing expandable rail retained |
| 1 / Untitled15 | Start app list alongside pinned tiles | Existing list/tiles and wide-screen side-by-side layout retained |
| 1 / Untitled16 | Desktop context menu | Existing working desktop actions retained |
| 1 / Untitled17 | Mixed-size app tiles | Existing small/medium/wide/large layout retained |
| 1 / Untitled18 | App list hierarchy and alphabet groups | Existing alphabet jump and app organization retained |
| 1 / Untitled19 | Settings profile header, search and category grid | Existing 13-category native Settings retained; Nota available in caption |
| 1 / Untitled20 | About page and selected-sidebar accent | Real Android device/version/memory information retained |
| 1 / Untitled21 | Optional features | Uses Android's app/settings destinations; no Windows feature installer |
| 1 / Untitled22 | Clipboard controls | Existing launcher file/shortcut clipboard and clear action retained; Nota has explicit Copy reply |
| 1 / Untitled23 | System components | Uses Android app-management destinations |
| 1 / Untitled24 | Multitasking and virtual desktops | Existing real Nth desktop banks/Snap retained; slot-directed minimize/restore added |
| 1 / Untitled25 | Tablet controls | Existing full-screen Start and narrow-screen app/list choice retained |
| 1 / Untitled26 | Storage totals and category links | Real device storage and Explorer destinations retained |
| 1 / Untitled27 | Power and sleep | Real Android power/display routes retained |
| 1 / Untitled28 | Focus assist | Existing local notification filtering retained |
| 1 / Untitled29 | Sound device/volume settings | Real phone media volume/settings retained |
| 1 / Untitled30 | Display settings | Existing brightness/status-bar/icon controls and Android display route retained |
| 1 / Untitled31 | Background preview, picture choices and fit | Existing actual background preview/import/fit retained |
| 1 / Untitled32 | Light/dark shell and accent palette | Existing light/dark surfaces retained; task labels now honor shell text color |
| 1 / Untitled33 | Lock-screen settings | Uses Android's lock-screen route; launcher does not replace Android security |
| 1 / Untitled34 | Theme previews and customization | Existing original backgrounds/themes retained |
| 1 / Untitled35 | Font previews | Existing launcher font options retained through advanced settings |
| 1 / Untitled36 | Start toggles | Existing local tile details/full-screen/list settings retained |
| 1 / Untitled37 | Taskbar size/combining controls | Unified pinned/running family with label/grouping settings retained |
| 1 / Untitled38 | Notification-area controls | New chevron behavior and News icon/text/hidden/hover preferences |
| 1 / Untitled39 | Large document icons and folder thumbnails | Existing real file views retained; selected text documents can be brought into Nota |
| 1 / Untitled40 | Explorer selection tools and context menu | Existing selection/clipboard/delete safeguards retained; Nota works on the explicitly chosen text file |
| 1 / Untitled41 | Large News dashboard, weather, story cards, games and finance | New adaptive News flyout, real feed metadata/optional images, saved stories, real weather/hourly strip and Nth games; live financial feed remains future work |
| 2 / Untitled | Copy-progress dialog | Existing real copy/move progress and cancellation retained |
| 2 / Untitled1 | App context menu and pinning | Existing taskbar pin/unpin/Jump Lists retained |
| 2 / Untitled2 | Expanded navigation rail | Retained accessible named navigation |
| 2 / Untitled3 | Desktop menu and personalization | Retained real desktop/background routes |
| 2 / Untitled4 | Copilot greeting, composer, task suggestions and recent chats | Nota gains new chat, local history, task chips, context review and reply actions; no paid provider added |
| 2 / Untitled5 | Browser chrome, ellipsis commands and assistant entry | Existing tabs/address menu retained; browser Nota imports only selected text or explicitly chosen title/address |
| 2 / Untitled6 | Selected-file context actions | New text-file Nota command alongside existing Cut/Copy/Share/Properties |

## Practical limits and next comparisons

Installed Android apps remain Android tasks. A launcher cannot reproduce Windows process termination or put every third-party app into its own built-in frame. Built-in tools have the complete open/minimize/restore/close treatment.

News card geometry follows the reference; content depends on the connected RSS/Atom feed. Financial quotes, account personalization, social counts and Microsoft's editorial service are not simulated. Article bodies are not fetched into Nota automatically. Text-file import accepts readable text/Markdown/JSON, at most 64 KB, with a 15,000-character composer cap. Word/PDF conversion is outside this pass.

The full Nota canvas remains its own app. This launcher embeds Nota's writing tools, shared policy, real model preparation and optional free gateway. Real supported-device AICore inference still needs phone testing.

Primary references checked November 5, 2026:

- https://support.microsoft.com/en-us/windows/experience/stay-up-to-date-with-news-and-interests
- https://support.microsoft.com/en-us/windows/experience/personalization/customize-the-taskbar-in-windows
- https://learn.microsoft.com/en-us/windows/win32/uxguide/vis-animations
- https://developer.android.com/reference/android/app/ActivityOptions
- https://developer.android.com/guide/components/activities/tasks-and-back-stack
- https://downloads.bbc.co.uk/usingthebbc/bbc_terms_of_use_31March2022english.pdf
