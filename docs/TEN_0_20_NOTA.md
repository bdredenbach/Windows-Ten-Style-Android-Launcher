# Nth Desktop Ten 0.20.0 — Nth Nota category guides

Ports the native Nota behavior from the supplied Eight 0.7.8 handoff into Ten. This is the launcher assistant, separate from the standalone Nth Nota canvas app.

All six categories keep their indices and display their matching description, instruction paragraph and composer hint. Guidance and real replies use separate views/data. Changing category preserves the reply; Copy reply and Save to Notepad use that reply only and stay disabled in guidance-only state. New command clears the current composer, reply and writing origin without deleting history. It stays beside the category selector in compact windows. When the keyboard leaves very little height, Send joins that row, the composer shows one visible line without truncating its text, and sending dismisses the keyboard so the reply or guidance can be read. The normal controls return as space becomes available.

Fresh app commands still resolve locally on Send. An exact app match opens once; ambiguity waits for a labeled icon choice. Launcher activities sharing a label and package now include the activity name. Imported passages, cleared source context, restored writing, history, private text and model replies do not gain command authority. Replacing a passage or starting a new command invalidates the previous request ID before cancellation, so a late result cannot replace the new passage.

## Desktop state

`ten_nota_windows` holds a versioned ordinary state per desktop ID: the full composer (15,000 characters), selected task, writing origin, actual reply, source label, guide visibility and reply presentation. A moving window transfers its ordinary saved state to the destination desktop. Retired desktops remove their cached entry; initialization drops orphaned owners. Different Nota windows never restore from one shared global draft.

The old global draft migrates once to the first desktop, then its old keys are removed. A nonempty legacy draft lacking an origin flag restores as writing. New desktops start with an empty composer.

`ToolViewState` stops recursive capture at Nota. Ordinary Android session entries contain only a versioned desktop reference and hydrate from that desktop's full saved state, avoiding large saved-state transactions. Private entries contain only a redaction marker, restoring an empty private placeholder. Neither restores editor references, action cards or live requests. Legacy generic child-path snapshots are ignored. Hydration suppresses persistence listeners until the complete state is installed.

Private passages/replies are absent from automatic drafts, history, backup and sessions. Importing a private passage leaves the previous ordinary cache intact. Closing and reopening Nota may recover that prior ordinary draft; restoring the private window's session clears the private passage. Explicit Copy/Share/Save remain user actions. Privacy does not silently disable the existing optional cloud setting.

Backups validate scoped owners, state versions, field types, task range, text limits and absence of private content. Cloud consent remains in its existing app-local store and is not transferred by the backup.

## Integration retained

Ten's settings routes/catalogue, real Task View and virtual desktop commands, fixed Nota taskbar slot, themes, package and signing identity remain intact. The duplicate browser-toolbar Nota button stays removed. Reviewed browser/text imports and confirmed source Apply keep the existing contracts. The focused blank-window touch fix prevents desktop click-through while preserving child controls, resize gestures and the uncovered desktop.

Writing retains the local-first/free optional cloud stack. Model preparation remains explicit. A package-private backend injection supports the controlled native writing fixture; the production constructors keep the existing backend.

## Verification

Verified application source: `6122b0ab4d4c4e29b893006f1192c7e512afd4b5`. [PR #1](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/pull/1) merged that exact tested tree as `24a7a4571cc7868258eb464892a67697f7382f6b`.

[Completed regression run](https://github.com/bdredenbach/Windows-Ten-Style-Android-Launcher/actions/runs/42505557099).

| Check | Result |
| --- | --- |
| Application/test/fixture/install-probe compilation | PASS in both CI jobs |
| Unit tests | 18/18 PASS in both jobs |
| Android 11 / API 30 | All 25 native scenarios PASS |
| Android 16 / API 36 | All 25 native scenarios PASS |
| Packaged runners, permissions, tile/settings/consent/command checks | PASS |
| Nota visual review | Final category/portrait and both landscape rotations, light/dark and real Android 11 keyboard screenshots reviewed |
| Signing identity | Existing Ten preview certificate retained; v2/v3 verified |
| APK alignment | PASS, including 16 KiB page alignment |
| Application payload | All 453 non-signature entries exactly match both CI APKs |
| Download ZIP | CRC and embedded APK/notes byte equality PASS |

The 25 scenarios comprise fourteen Ten suites, a stopped-process Nota check and ten shared engine scenarios. The focused Nota scenario exercises a controlled writing backend through the real assistant, all category/reply states, history/import/private boundaries, independent 15,000-character desktop drafts, moving windows, recreation, backup rejection and actual compact Send/keyboard interaction.

Signed APK SHA-256: `6e155ed6426140e1e872db6806a21bfd9e99e17b703cef598f420e62c57d6da0`.

Download ZIP SHA-256: `6f474d6bb1b88058e79a5aef8cc6c2f69ee844b753f9db5c32d5a2c2411722ae`.

Install the signed 0.20.0 preview over the existing Ten installation to keep its desktop setup and notes.

Physical Pixel 10/Android 17, live AICore model preparation/inference and live cloud writing are not newly validated by this port. The native writing fixture exercises the real assistant dispatch/result/cancellation path using controlled local results without model downloads or cloud quota.

The distributed artifact uses the established development-preview build type. A local non-debuggable release build was attempted but could not fetch the unavailable lint dependency through the environment's network route; it is not claimed as a successful release build.
