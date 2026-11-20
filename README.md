# Water

A native Android water tracker built with Kotlin, Jetpack Compose and Material 3. Add 250 ml or 500 ml, undo today's most recent drink, and see the seven-day chart and timed drink history. Settings changes the daily goal (2,000 ml by default) and appearance. Drinks, goal and appearance are saved locally with DataStore.

## Build

Open this folder in Android Studio and run the `app` configuration on an Android 7.0 (API 24) or newer device or emulator. The project uses JDK 17 and its included Gradle wrapper. GitHub Actions builds the debug APK and runs unit tests; a commit message containing `[ui-test]` also runs the first-screen click test on an emulator. The APK is available as the `android-debug-apk` workflow artifact.

Water does not require an account, network access, or runtime permissions.
