# Auto Clicker

A small Android fixed-point autoclicker built with Kotlin and an Accessibility Service.

## What it does

- Taps one user-entered screen coordinate.
- Supports a configurable start delay and interval.
- Supports a fixed number of taps or continuous tapping.
- Stores the most recent settings locally.
- Does not inspect screen content or search for buttons.
- Does not target a specific application.

## Build

Open the repository in Android Studio, allow Gradle sync, and run the `app` configuration on an Android 8.0 or newer device.

GitHub Actions also builds a debug APK on every push to `main`. Open the latest **Build Android APK** workflow run and download the `auto-clicker-debug-apk` artifact.

## Use

1. Install and open the app.
2. Tap **Open accessibility settings**.
3. Enable **Auto Clicker Service** and return to the app.
4. Enter X and Y screen coordinates in pixels.
5. Enter the interval, start delay, and tap count. A tap count of `0` means continuous.
6. Tap **Start clicking**, switch to the desired screen before the delay ends, and use **Stop** when finished.

## Responsible use

Use automated input only where it is permitted. Accessibility access is powerful, so install only builds you trust and disable the service when it is not needed.
