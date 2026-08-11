## Learned User Preferences

- Do not commit or push until explicitly asked.
- Prefer neutral gray borders on UI elements, not colored accent borders.
- UI strings should be in Croatian.

## Learned Workspace Facts

- DartScore is an Android app built with Kotlin and Jetpack Compose.
- App package name is `com.example.dartscore`.
- Firebase project ID is `dartscore-7df93` (Auth and Firestore for logged-in match stats).
- CLI builds on macOS may require `JAVA_HOME` set to Android Studio's bundled JBR at `/Applications/Android Studio.app/Contents/jbr/Contents/Home`.
- Android SDK is typically at `~/Library/Android/sdk`; `settings.gradle.kts` auto-creates `local.properties` if missing.
- Open Android Studio at the repo root (`DartScore/`), not the `app/` subfolder; opening `app/` breaks Gradle sync and hides the Android run target.
- Firestore rules and indexes deploy via Firebase CLI: `npx firebase-tools@latest deploy --only firestore`.
