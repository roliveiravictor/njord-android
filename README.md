# Njord Android

Native Android implementation of the Njord mobile dashboard.

## Architecture

### Offline-first data layer

Every screen that fetches remote data follows a strict offline-first pattern:

1. **Cache read first** — on composition, the screen immediately reads the last
   persisted JSON payload from `NjordApiCache` and renders it. The user sees
   real (possibly stale) data instantly, with no blank loading state.
2. **Live fetch overlays** — a background IO coroutine then fetches the live
   API response. On success the UI re-renders with fresh data and the new
   payload is written back to cache. On failure the cached data stays on screen.
3. **Cache write-through** — only a successful, parseable API response is
   written to cache. A parse error deletes the stale cache entry to prevent
   the app from repeatedly loading bad data.

The cache is file-based (`NjordApiCache`, stored in `filesDir/api-cache/`). Each
endpoint has a dedicated `ApiCacheKey` enum entry and a corresponding JSON file.

### Fetch pattern (canonical example from `PortfolioScreen`)

```kotlin
LaunchedEffect(strategy) {
    withContext(Dispatchers.IO) {
        // 1. Serve cache immediately
        NjordApiCache.read(filesDir, cacheKey)?.let { body ->
            NjordApiClient.parsePortfolioResponse(body).let { result ->
                if (result is PortfolioResult.Success)
                    dispatchUiAction(onAction, NjordAction.PortfolioLoaded(mapApiPortfolio(result.response)))
                else
                    NjordApiCache.delete(filesDir, cacheKey)
            }
        }
        // 2. Fetch live
        dispatchUiAction(onAction, NjordAction.PortfolioLoading)
        when (val result = NjordApiClient.fetchPortfolioPayload(...)) {
            is ApiPayloadResult.Success -> { /* parse → write cache → PortfolioLoaded */ }
            is ApiPayloadResult.Error  -> dispatchUiAction(onAction, NjordAction.PortfolioError)
        }
    }
}
```

All screens must follow this pattern. Skipping the loading or error dispatch
leaves the UI in an indeterminate state and is a bug.

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
- Risk
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
