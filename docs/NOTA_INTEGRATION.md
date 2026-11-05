# Nth Nota inside Ten

First preview adapts `CloudAI.java` and `CloudConsent.java` from Nth Nota main at `16e4d16cecf48238b5857eb658db0874d93a8db3`. The native UI lives in `TenNotaPanel`.

The existing owner-operated HTTPS writing endpoint supports generate, brainstorm, proofread, polish and summarize. It performs the Cloudflare/Groq provider fallback server-side. Credentials stay on that service. Free quotas and provider availability remain shared with Nota and are not bypassed by Ten.

Cloud assistance is off until the user explicitly allows it in Ten. Ten uses separate consent preferences. Only the composer text and selected task are sent; other launcher notes, app activity and files are not attached. Draft and latest reply stay on-device. Stop, disabling cloud access, hiding the panel, leaving the launcher and closing the window cancel active work. Saving a reply appends it to Notepad.

The device-local ML Kit/AICore path was reviewed but is not included in 0.1.0. Porting it requires the ML Kit dependency and device-availability/download lifecycle. Ten does not label cloud assistance as local AI. The complete Nota canvas, audio notes and other widgets are outside this first assistant panel.

Regression uses a synthetic reply to test state and local saving; no production prompt is sent during tests. Live provider response and free allowance are not asserted by CI.
