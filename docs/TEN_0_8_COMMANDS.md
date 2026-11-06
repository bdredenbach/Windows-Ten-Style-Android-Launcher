# Ten 0.8 — Nota commands and split Settings

## Settings and utilities

Settings now retains its category pane beside the current page in portrait and landscape. The pane uses about 32% of a narrow window (bounded at 88–192 dp), with the rest for page content. The category list and page scroll independently. The category scroll container and its fill-viewport child carry the gray surface through the bottom of the window. Category labels and headings wrap instead of hiding the page in a drawer. Search results and Settings Home retain their full-width layouts.

Calculator appears in Apps → Nth tools, alongside Unit converter and Text counter. Gaming → Games contains Tile puzzle and Dice roller. News and interests also uses separate Games and Tools cards. The inherited gadget catalog already classifies Calculator as Productivity; there is no floating gadget sidebar to restore.

## Nth Nota commands

In a new chat, type a direct request and press Send. The default Write mode checks for local commands first; Desktop & phone mode is also available. Commands produce named buttons. Tap the intended button to run it. Suggestions fill the composer for review and editing; they do not execute immediately. Ask “What can you do?” or use Tools → What can Nota do? for examples.

| Request | Result |
| --- | --- |
| Can you open Nth Browser? | Open the built-in browser |
| Open Calculator / File Explorer / Notepad | Open the named Nth tool |
| Open Themes / Colors / Background | Navigate to the real Settings page |
| Open Wi-Fi settings / Bluetooth | Open the Android settings screen |
| Open Nth Reader | Match an installed launchable app by its displayed or renamed label |
| Search the web for Android tips | Open the search in Nth Browser after the action is tapped |
| Find files named budget | Search files/folders linked to the launcher, under existing grants |
| Open https://example.com | Open the site in Nth Browser |
| Show my desktop | Minimize built-in windows; restore using the taskbar |
| Create a new desktop | Create and select an empty desktop; retain the old desktop |
| Next desktop / Previous desktop | Switch to an adjacent desktop when one exists |

The app list, command matching and action dispatch stay on the phone and need no AI model download or cloud permission. Installed apps use the launcher's existing app catalog and Android activity launch path; duplicate names produce separate choices with package names. Unknown apps offer Start search. Actions on the phone open normal Android settings rather than claiming that a system switch changed. Web searches and websites contact their destination only after the corresponding button is tapped.

Imported context and proofreading/polish/summarize modes remain writing requests. AI-generated replies are displayed as text and are never parsed as executable actions. Editing the composer removes previous action buttons, and opening chat history does not restore runnable buttons. There is no arbitrary shell, package/component string or intent execution from a model response. Existing local-first writing, optional free cloud fallback and source-safe reply application remain in place. No new permission or paid service is required.

Primary platform references:

- https://developer.android.com/training/package-visibility/declaring
- https://developer.android.com/training/package-visibility/use-cases
- https://developer.android.com/guide/components/intents-common#Settings
