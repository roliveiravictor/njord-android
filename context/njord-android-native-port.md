# Njord Android Native Port

## Source Mock

The native app ports the static HTML mock from:

`/Users/vrocha/Downloads/njord_mobile_compose_activity_icon_list_alt.html`

The HTML mock is the visual and text source of truth for this v1 native Android
prototype.

## Scope Implemented

- Recreated the dark mobile dashboard visual system with Compose cards, chips,
  badges, bottom navigation, sheets, dialogs, and Canvas charts.
- Implemented the top-level destinations Home, Portfolio, Live, and More.
- Implemented More child destinations Activity, Reports, Heartbeat, and Logs.
- Implemented Risk as a Home-reachable screen.
- Added static mock data for account KPIs, strategy health, positions,
  incidents, heartbeat routines, logs, candle-close activity, and Hunch report
  content.
- Refined the Home screen against the June 9, 2026 native screenshots. Those
  screenshots supersede the earlier generic Home layout for the Home hero,
  Strategies, Activity, Heartbeat, and Incidents sections.
- Refined the Portfolio screen against the June 9, 2026 native screenshots.
  Portfolio now presents static performance analytics rather than position
  cards, including the performance hero, strategy chips, live metrics, monthly
  stats, and performance-history cards.
- Refined the More Activity screen against the June 10, 2026 native screenshot.
  Activity now presents the candle-close summary, compact non-wrapping action
  chips, grouped strategy action panels, and reference-matched bottom navigation
  icons while still reading from `NjordMockData`.
- Refined the More screen against the June 10, 2026 grouped-list screenshot.
  More now omits the page header and subtitles, presents Activity, Reports,
  Heartbeat, and Logs in one inset rounded list with muted icons, and preserves
  the existing child navigation.
- Refined the Reports screen against the June 10, 2026 Summary reference.
  Reports now omits the generic page header, embeds the report header and
  signal banner in one reference-style panel, expands the Summary rows, and
  preserves the existing factor, risk, and layer-score sections below it.
- Added real coin logo loading for Live and position cards. Known symbols use
  CryptoLogos URLs first, Trust Wallet assets as the fallback, and CoinGecko
  search-derived image URLs as the third recovery path. Successful logo bytes
  are cached under the app `cacheDir` in `coin-logos/` so later cold starts can
  render from local cache before trying the network. Unknown labels, cache
  misses, and failed downloads keep the native initials badge.
- Added offline-first API caching for the remote-backed Home, Activity,
  Heartbeat, Logs, and Reports views. Each view reads its last successful raw
  JSON payload from private app storage first, renders that cached data
  immediately when available, then refreshes from the API and replaces the cache
  only after a successful parse.

## Static Data Boundary

The app now uses a hybrid data boundary. Static prototype-only surfaces still
come from `NjordMockData`, while Home, Activity, Heartbeat, Logs, and Reports
are API-backed and offline-first. Screen state remains local Compose state
reduced through `NjordAction` and `reduce`.

Future API integration should replace `NjordMockData` with a repository-backed
state source while preserving:

- `Destination`
- filter enums
- screen-level model shapes where practical
- dialog/sheet selection state

## Non-Goals For This Step

- No authentication.
- No database-backed persistence layer. API view snapshots are stored as raw
  endpoint JSON files under `filesDir/api-cache/`; coin logos remain cached
  under `cacheDir/coin-logos/`.
- Coin icons may load remote logo images; cached logos are preferred when
  available, and initials remain the native fallback.
