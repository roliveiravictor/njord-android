# Njord Android

Native Android implementation of the Njord mobile dashboard mock.

This first version intentionally uses static in-app sample data copied from the
HTML mock at:

`/Users/vrocha/Downloads/njord_mobile_compose_activity_icon_list_alt.html`

API integration is deliberately out of scope for this step. The UI reads from
`NjordMockData`, which can later be replaced by a repository/API-backed data
source without changing the screen structure.

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
dialogs with dismissal, and Live position bottom sheets. The Live screen matches
the reference operations view with a swipe incident carousel, expanded position
cards, static open P&L analytics, responsive metric grids, and position integrity
tiles. The Portfolio screen is a static performance analytics view with a
reference-matched hero, live metrics, monthly stats, and performance history
charts.
