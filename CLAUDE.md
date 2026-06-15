# CLAUDE.md — Njord Android

## Release Signing

This app is not distributed via Google Play. Release builds are signed with the Android debug keystore (`~/.android/debug.keystore`) so they can be installed directly via `adb install`. The signing config is already wired in `app/build.gradle.kts`. Never add a production keystore or modify the signing config without explicit user instruction.

To build and install a release APK on a connected device:

```bash
./gradlew assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```

## Local-Only Production Files

- During local mobile app/API development, if a file is required at runtime but is not tracked by git because it only exists on the remote or production Njord VM, create a local mock, fixture, or stub that preserves the expected path, filename, schema, and basic behavior.
- Keep those mocks local-only and out of version control unless the user explicitly asks to add a sanitized fixture. Never invent or commit real secrets, credentials, production databases, private keys, model artifacts, logs, or VM-only payloads.
- Prefer deterministic placeholder data that lets the Android app and local API flows build, launch, and exercise error/loading/success states without requiring production-only files.
- When mocking a missing VM-only file, make the fallback obvious in code, tests, or local setup notes so agents do not mistake mock data for production data.
