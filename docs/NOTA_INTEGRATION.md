# Nth Nota writing integration in Ten 0.2

Ten adapts the owner's `CloudAI`, `CloudConsent` and `LocalAI` clients from Nth Nota. `TenAssistant` routes locally first, with `TenWritingPrompts` applying the selected writing task consistently to both engines.

`com.google.mlkit:genai-prompt:1.0.0-beta4` is pinned to the version used by the reviewed Nota source. Android API 26 is the SDK minimum; actual on-device inference requires a supported AICore device and a prepared model. Preparing is a separate user action. Pressing Send never silently starts a large model download.

If the local model is unavailable, busy, rate-limited or times out, the existing free cloud service may be used only after the user has allowed it. Safety refusals, cancellation, invalid requests and oversized context do not trigger fallback. Both engines receive only the current message composer and task instructions. Other notes, files, notification contents, app activity and desktop previews are not attached.

Model preparation and SDK diagnostics may contact Google. Local inference processes writing on the phone. Optional cloud writing sends the composer to the owner's HTTPS gateway, which handles Cloudflare Workers AI and its configured Groq fallback. Provider secrets remain server-side; quotas remain shared with Nota. Turning off cloud access cancels cloud requests and prevents new fallback.

Stop or leaving/hiding the panel detaches the active reply. A model download already authorized and started through AICore can finish independently; Ten does not cancel or interrupt the system's model futures. Late replies are not published after cancellation.

Validation distinguishes: fake-backend tests of download gating/inference/cancellation, emulator unsupported-device behavior, and a live synthetic proofreading request to the free Cloudflare gateway. Supported-phone model inference still requires a real AICore device.

Primary documentation checked November 5, 2026:
- https://developers.google.com/ml-kit/genai/prompt/android/get-started
- https://developers.google.com/ml-kit/genai/prompt/android
- https://developers.google.com/ml-kit/android-data-disclosure
