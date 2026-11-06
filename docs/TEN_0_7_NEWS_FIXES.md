# Ten 0.7 news connection fixes

## Reproduced cause

The user supplied `https://www.techradar.com/feeds.xml`. A public HTTPS request with the app's previous user agent returned HTTP 200 and 2,443,072 bytes of valid RSS containing 50 entries; the first twelve entries had titles and links. The reader eagerly downloaded the entire response through `CitySearch.read(..., 512000)`, which rejected the feed before XML parsing. The failure was then shown as a generic address/connection error.

The same probe downloaded BBC News successfully at 28,494 bytes. These are observed response sizes, not permanent publisher limits. The raw third-party feeds are not committed. TechRadar's own RSS directory is https://www.techradar.com/how-to/techradar-rss.

A second defect stored a fifteen-minute expiry after failures and returned early even on manual refresh, so Refresh could appear ineffective. Configuring a feed also cleared cached stories before confirming that the replacement source worked.

## Changes

- Feed XML is parsed directly from a bounded stream. Reading stops after twelve usable stories; there is no need to retain or consume a multi-megabyte tail. Decoded input remains capped at 8 MiB. Gzip, XML encoding declarations, RSS/Atom/RDF, relative links and bounded text are supported.
- XML document declarations remain unprocessed and are rejected before resolving entities. HTML pages, bad feed XML, missing/denied/server HTTP responses, TLS/network failures and timeouts have distinct user-facing feedback. There is no TLS or access-control bypass.
- Manual Refresh bypasses success-cache expiry and ordinary error backoff, including a failed first source. Explicit server Retry-After is honored. Failed requests do not replace a successful cache expiry or clear cached stories.
- A candidate source becomes the configured source only after a valid feed is read. Existing source, cached headlines and bookmarks survive a failed replacement. Cancelled/stale results cannot overwrite a newer source.
- Connect remains open while connecting and displays errors inline. Failed attempts leave the address editable. BBC and TechRadar buttons fill the address; Connect initiates the request. The dialog scrolls and resizes with the keyboard. Closing News cancels an in-flight setup and closes its dialog.
- A valid empty feed is identified as connected with no readable articles yet rather than a network failure. Reopening a previous failed 0.6 setup with no usable cache attempts it again instead of trusting its old expiry.

The deterministic native feed suite covers these paths with scripted HTTP responses and generated multi-megabyte data. Optional real BBC/TechRadar probes use the Android transport and parser and record their outcomes separately so a provider outage cannot masquerade as a code regression.

Primary API references:

- https://developer.android.com/reference/org/xmlpull/v1/XmlPullParser
- https://www.rfc-editor.org/info/rfc9110/
