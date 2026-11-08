# Nth Desktop Ten 0.20.0 — Nth Nota category guides

Ports the native Nota behavior from the supplied Eight 0.7.8 handoff into Ten. This is the launcher assistant, separate from the standalone Nth Nota canvas app.

All six categories keep their indices and display their matching description, instruction paragraph and composer hint. Guidance and real replies use separate views/data. Changing category preserves the reply; Copy reply and Save to Notepad use that reply only and stay disabled in guidance-only state. New command clears the current composer, reply and writing origin without deleting history. It stays beside the category selector in compact windows.

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

Local build and unit validation: 18 tests pass, app/test/fixture/install-probe APKs compile, packaged runner/permission checks pass, and tile/settings/consent/command checks pass. Native verification and final package hashes are recorded below when complete.

Physical Pixel 10/Android 17, live AICore model preparation/inference and live cloud writing are not newly validated by this port. The native writing fixture exercises the real assistant dispatch/result/cancellation path using controlled local results without model downloads or cloud quota.

The distributed artifact uses the established development-preview build type. A local non-debuggable release build was attempted but could not fetch the unavailable lint dependency through the environment's network route; it is not claimed as a successful release build.
