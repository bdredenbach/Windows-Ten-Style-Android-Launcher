# Desktop consistency and portable restore

## Task Manager

Replaces the inherited classic Task Manager with Windows 10-style compact/details navigation: Processes, Performance, App history, Startup, Users, Details and Services. Resource selectors, measured histories, task commands, update controls and saved state remain functional. Its details preference is separate from Eight.

## Desktop caching

Each launcher keeps a rebuildable, atomic cache of app identifiers, labels and bounded icon images in its own Android cache directory. A worker displays the cached inventory before refreshing the package catalog. A failed or transiently empty scan retains the last working inventory instead of clearing the desktop. An in-memory catalog is reused for 60 seconds; package changes, locale changes and explicit Refresh invalidate it immediately. Configuration changes invalidate incompatible cached labels/icons. Android may evict the cache, which is rebuilt on the next successful scan. The cache is excluded from portable desktop backups; existing saved shortcut and layout preferences remain the source of truth.

## Welcome and restore

Every Welcome Center now says **Restore a desktop**, and the backup panel places that action first. Saving a backup remains available.

New backups retain the version 1 `nth-desktop-backup` format and add source-variant and wallpaper-name metadata. All five launchers also accept the original `nth-desktop-eight-backup` format. Imports from another variant or older backups without source metadata filter unsupported built-in shortcuts from lists, folders, aliases and icon positions; apply destination list limits; and translate stock backgrounds by name, including slideshows and saved themes. An unavailable background is skipped. Older backups without background names use only the common stock range to avoid treating another variant's stock image as the same picture.

Each destination still validates the compatible settings. Unsupported optional settings are omitted and named in the preview. Malformed JSON, unsupported schema versions, missing core data, invalid field types and the existing size/nesting limits still reject the import before preferences change. New same-variant backups retain strict validation. A partial restore preserves destination settings for skipped fields and keeps the existing undo transaction. On launchers with virtual desktops, importing an older primary-desktop layout updates its visible shortcut bank while preserving the other destination banks; incoming compatible banks retain their positions after unsupported shortcuts are filtered.

File and photo links still need Android access on the receiving app/device. A backup does not transfer file contents or grants. Task Manager shows actual Nth/device data exposed by Android; other apps' privileged process controls remain Android's responsibility.

## Validation

Run with Java 17 and Android SDK 36:

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest
```

`DesktopConsistencyTest` covers five source fixtures in each receiving launcher (25 source/destination combinations across the repositories), legacy Quick Launch/background handling, the historical Eight format, strict same-variant rejection, malformed/incomplete files, partial restore and undo, stock-background names inside themes, cold icon-cache reload, failed scans, damaged caches and generation-specific Task Manager navigation. Eight, Ten and Eleven also exercise older-backup membership migration and compatible imported desktop banks/positions.

The local unit tests and APK/instrumentation-APK builds are the validation for this change. Android device/emulator instrumentation suites are compiled but have not been executed in this workspace; existing CI runs the device suites. These are source changes for review, not a newly signed production release.

The corrected companion baselines are Vista 0.26.1 and Seven 0.7.1. Eight 0.7.12, Ten 0.26.0 and Eleven 0.8.2 match the user’s existing version numbers. First-scan catalog failures also skip external-task refresh instead of passing it an unavailable catalog.

## Extended taskbars and rendering caches

The companion Vista, Seven and Eight patches fit labels above extended taskbars. This variant retains its established grid geometry and reuses unchanged placement plans.

All five variants now share static built-in artwork in a bounded 4 MiB bitmap cache keyed by icon kind, pixel size, padding, density and UI mode. Existing drawing commands remain unchanged; live gadget faces are separate. Folder names, shortcut aliases and normalized preference lists reuse parsed snapshots until their raw saved data changes. Mutable folder/list return values remain independent so edits cannot accidentally corrupt the cache. Desktop placement plans are reused until dimensions, orientation, cutouts, shortcut membership or saved positions change. An unchanged in-memory app catalog no longer recreates the desktop and hidden Start contents on every Home/resume.

`LauncherRenderingTest` checks geometry reuse/invalidation, mutable model safety, static icon reuse and bounded cache size. The Vista/Seven/Eight companion suites also check expanded taskbars, two-line labels, larger system text, whole overflow rows and temporary fitting without rewriting saved positions. Phone/emulator timing and visual checks remain pending; the APKs are development-signed test builds.

Ten defers Start tile and All apps construction until their surface is visible. It indexes built-in/installed results once per catalog/name/link change, groups pins in a single pass, and retains tile views between openings until pins, names, sizes, groups, width or appearance change. Live tile contents refresh independently without rebuilding their views; unchanged live text/layout values no longer request redundant layout work. The sRGB channel conversion used by contrast calculations uses an exact 256-value lookup.

`StartPerformanceTest` uses all 43 Nth tiles, 32 folders with 12 members each and 128 shortcut aliases. Before this change, the fixture built 43 hidden tiles and read each folder/alias preference 33,411 times. The initial revised fixture built zero hidden tiles and read those preferences 44/87 times. These are work counts under Robolectric, not phone latency measurements. Regression checks also cover 200 installed apps, unchanged catalog reuse, deferred hidden updates, search visibility, rename/resize/regroup invalidation and retention of the same tile views when Start reopens.

## Ordered initialization and restored folders

After the existing startup introduction, all five desktops use the same device-local setup sequence:

1. Default Launcher.
2. Enable File Browsing.
3. Enable Usage Access.
4. Allow Notifications Access (Android Notification access for showing the phone's notifications).
5. Modify System Settings (Brightness).
6. Allow Location with approximate/precise access.
7. Fix unlinked folders from a restored backup.

Granted steps are skipped. Each permission remains optional; Later/cancellation advances, and unavailable Settings activities do not block setup. Pending Android requests and progress survive recreation. An interrupted folder transaction can be retried, and a fresh launch clears an orphaned picker request. The existing Welcome/start surface follows this flow. The introduction artwork and boot-playback policy are retained. Setup progress and Android grants are excluded from desktop backups.

A successful restore/undo queues folder repair after any remaining setup steps. Folder checks and path lookup run on the worker. Reconnecting a root updates its existing Computer link, favorites and desktop folder-shortcut pages without recreating shortcut IDs, titles, positions or unrelated preferences. Descendants are verified by document IDs or resolved by unique folder names. Missing/ambiguous subfolders are kept, and partial repairs can be retried with **Fix linked folders** in Backup and Restore. A plan is applied only by its live setup flow and only if the referenced saved data has not changed.

Links to a known Nth storage provider can translate to this desktop's own provider when its file permission is already granted; this verifies the destination path and does not transfer permission from another app. Other folders use Android's folder picker and retain its read grant. The setup cannot grant Android permissions itself.

Eight, Ten and Eleven use their existing notification-listener components. Vista and Seven gain the same in-memory listener support, protected by Android's notification-listener binding permission, and phone notifications on their classic tray surfaces. In Vista, Notification Center is available from Device controls (hold the tray chevron); Seven also shows phone notifications in its existing Action Center. Notification content remains in memory and respects the existing locked-phone filtering.

`InitializationFlowTest` exercises the ordered dialogs, granted-step skipping, Settings intents, denial/cancellation, focus/recreation, interrupted/orphaned requests, unavailable Settings, notification-service declarations and last-step folder repair. `FolderRepairTest` exercises root/favorite/shortcut updates, preserved identities/layouts, concurrent-edit protection, partial repairs, de-duplication, real provider grants/path lookup, and missing/ambiguous descendants. The instrumentation APK is compiled; Android device/emulator instrumentation is not run in this workspace.

## Location choice and uninstall from Start

Initialization now follows Default Launcher, File Browsing, Usage Access, Notifications Access, System Settings for Brightness, Location, then restored-folder repair. Location stays optional. Android 12 and later receive fine and coarse requests together so Android offers its native approximate/precise choice. Older Android versions receive separate approximate/precise choices in the setup dialog. Coarse-only grants are accepted, saved weather choices are kept, and background-location permission is not requested. Precise permission enables the GPS provider when available; all location work remains foreground-only.

Device-local initialization schema v2 inserts Location without replaying completed earlier steps. Existing completed or folder-repair-stage v1 sessions offer Location once and retain in-flight folder picker state; folder repair remains last. Desktop backups still do not transfer permissions or initialization progress.

Installed removable Android apps now have an explicit **Uninstall** command in their Start context menus. Vista/Seven support touch hold and mouse context clicks on program rows; Ten exposes it directly for apps and pinned tiles; Eleven exposes it directly in Start app options; Eight exposes it in its selected-app command bar and 8.1 Update tile popup, including app-row context clicks. Built-in launcher tools, folders, missing apps, this launcher and protected system apps do not offer the command. Unpinning/removing a shortcut remain separate actions.

Uninstall opens Android's package-uninstall confirmation using ACTION_DELETE, with REQUEST_DELETE_PACKAGES declared. It does not open App info/settings. Android retains its confirmation and policy checks. Canceled or blocked requests keep launcher data. Confirmed removal updates Start pins (including both Eight tile banks) and the cached app inventory; package replacements do not count as uninstalls. The worker forgets an explicitly removed app from disk/memory so removing the last cached app cannot revive it through the empty-scan fallback.

`LocationAccessTest`, the updated `InitializationFlowTest`, `AppUninstallerTest` and per-variant `StartUninstallMenuTest` cover coarse/fine declarations and requests, migration, real Start menu commands, normal app opening, native uninstall intents, cancellation, policy failures, protected/built-in filtering, both Eight banks and cold-cache removal. Real-device Android permission/uninstall dialogs still require phone/emulator verification.
