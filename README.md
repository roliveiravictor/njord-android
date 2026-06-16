# Njord Android

Native Android implementation of the Njord mobile dashboard.

## Architecture

### Offline-first data layer

Every screen that fetches remote data follows a strict offline-first pattern:

1. **Fresh cache read first** — on composition, the screen immediately reads
   the last recent JSON payload from `NjordApiCache.readFresh` and renders it.
   Payloads older than the freshness window (default 5 minutes) are discarded
   and treated as a cache miss so stale values are never shown.
2. **Live fetch overlays** — a background IO coroutine then fetches the live
   API response. On success the UI re-renders with fresh data and the new
   payload is written back to cache. On failure, only a fresh cached payload
   stays on screen; a stale or missing cache leaves the screen in its loading
   placeholder state.
3. **Cache write-through** — only a successful, parseable API response is
   written to cache. Writes replace the previous file atomically, and parse
   errors delete invalid cache entries to prevent repeated bad renders.

The cache is file-based (`NjordApiCache`, stored in `filesDir/api-cache/`). Each
endpoint has a dedicated `ApiCacheKey` enum entry and a corresponding JSON file.

**Important:** always use `NjordApiCache.readFresh` (not `read`) in load
functions. `read` has no TTL and will return arbitrarily old data, causing the
UI to show stale values (e.g. an outdated open-position count) until the network
call completes — or indefinitely if it fails.

### Fetch pattern (canonical example from `loadPortfolioData`)

```kotlin
// 1. Serve cache immediately — skips payloads older than 5 minutes
NjordApiCache.readFresh(context.filesDir, cacheKey)?.let { body ->
    when (val cached = NjordApiClient.parsePortfolioResponse(body)) {
        is PortfolioResult.Success ->
            dispatchUiAction(onAction, NjordAction.PortfolioLoaded(mapApiPortfolio(cached.response)))
        is PortfolioResult.Error -> NjordApiCache.delete(context.filesDir, cacheKey)
        else -> {}
    }
}
// 2. Always fetch live; result overwrites the in-memory snapshot
when (val result = NjordApiClient.fetchPortfolioPayload(...)) {
    is ApiPayloadResult.Success -> {
        // parse → write cache → PortfolioLoaded
    }
    is ApiPayloadResult.Error -> showApiFailureToast(context, result)
}
```

The `Incidents` cache is the only exception: it uses `read` (no TTL) because
incidents accumulate across sessions until the user explicitly dismisses them.

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Android Gradle Plugin 8.7.2
- Min SDK 26
- Target/compile SDK 36
- Java toolchain 17

Use a JDK 17 runtime when building locally:

```sh
export JAVA_HOME=/Users/vrocha/Library/Java/JavaVirtualMachines/temurin-17.0.18/Contents/Home
```

## Build And Test

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

The included `gradlew` script bootstraps Gradle 8.10.2 into the project-local
`.gradle/` directory when needed. The first run may need network access to
download Gradle and AndroidX dependencies.

## Implemented Screens

- Home
- Portfolio
- Live
- More
- Activity
- Heartbeat
- Logs
- Reports

Implemented interactions include bottom navigation, More child navigation, Live
filters, Portfolio strategy chips, Logs search/severity filters, incident
dialogs with dismissal, and Live position bottom sheets. The Live screen fetches
`/v1/live` with the same cache-first refresh flow as the other remote-backed
screens, including open positions, live incidents, strategy contribution,
summary metrics, largest winner/loser, and position integrity panels. The
Portfolio screen is a static performance analytics view with a
reference-matched hero, live metrics, monthly stats, and performance history
charts.
