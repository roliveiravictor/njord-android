# Njord Android

Native Android implementation of the Njord mobile dashboard.

## Architecture

### Offline-first data layer

Every screen that fetches remote data follows a strict offline-first pattern:

1. **Stale cache served immediately** — on composition, the screen reads the
   last cached JSON payload from `NjordApiCache.read` and renders it right away,
   giving users instant feedback even before the network responds.
2. **Live fetch always runs** — a background IO coroutine fetches the live API
   response unconditionally. On success the UI re-renders with fresh data and
   the new payload is written back to cache, replacing the stale render.
   On failure the stale cache stays visible and a toast is shown.
3. **Cache write-through** — only a successful, parseable API response is
   written to cache. Writes replace the previous file atomically, and parse
   errors delete invalid cache entries to prevent repeated bad renders.

The cache is file-based (`NjordApiCache`, stored in `filesDir/api-cache/`). Each
endpoint has a dedicated `ApiCacheKey` enum entry and a corresponding JSON file.

### Lifecycle refresh

`NjordDashboardScreen` uses two `LaunchedEffect` triggers to load data:

- `LaunchedEffect(state.destination)` — fires whenever the user navigates to a
  new screen, loading that screen's data.
- `LaunchedEffect(lifecycleOwner)` — fires once on initial composition and then
  again each time the app comes back to the foreground (RESUMED state). Uses
  `rememberUpdatedState` for both `state` and `onAction` so the **current**
  destination is always refreshed on resume, not the destination from initial
  composition.

**Important:** always use `rememberUpdatedState` when capturing `state` or
`onAction` inside `LaunchedEffect(lifecycleOwner)`. Without it, a one-time
effect captures those values at initial composition and never sees later
navigation changes.

### Local notifications

Njord generates operational notifications locally on the Android device; it does
not use Firebase, FCM, or remote push tokens. `NjordApplication` schedules
WorkManager jobs at app startup:

- Incidents: every hour, fetch `/v1/live`; if unacknowledged incidents are
  present, post a local notification.
- Heartbeat: every hour, fetch `/v1/heartbeat`; if any service is not healthy,
  post a local notification.
- Activity: once daily at 21:30 local device time, fetch `/v1/activity?limit=1`;
  if the latest cycle has opened/closed/kept positions and differs from the
  last notified cycle, post a local notification.

Android 13+ requires the user to grant `POST_NOTIFICATIONS`; the main activity
requests it on launch. If permission is denied, background checks still run but
the local notification is skipped by `NjordLocalNotifier`.

### Fetch pattern (canonical example from `loadPortfolioData`)

```kotlin
// 1. Serve stale cache immediately for instant render
NjordApiCache.read(context.filesDir, cacheKey)?.let { body ->
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

The `Incidents` cache is intentionally not cleared by a network refresh. When a
user dismisses an incident, the app records the incident ID in the
`IncidentAcknowledgements` cache for 24 hours, removes it from the visible
incident cache, and filters matching home/live API responses until that local
acknowledgement expires.

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
