# Material Explorer (`materialexp`)

[![Android Build](https://github.com/isaiahscape/materialexp/actions/workflows/android-build.yml/badge.svg)](https://github.com/isaiahscape/materialexp/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Material Explorer** is a modern, minimalist, and feature-packed Android file manager built using **Jetpack Compose** and **Material Design 3**. Designed for high productivity and intuitive interaction, it features multi-tab file browsing, dynamic view modes, storage analysis, built-in text/code editing, archive management, and responsive floating controls.

---

## Features

- **Full-Featured File Operations**: Browse internal storage, create files/folders, copy, cut, paste, rename, batch-select, and safely send items to the Recycle Bin.
- **Material Expressive UI**: Clean floating navigation capsule with an interactive Expressive Speed Dial menu (`+` expander) for fast actions.
- **Storage Analyzer**: Gain insight into storage distribution with interactive category breakdowns (Documents, Media, Archives, Large Files).
- **Custom View Modes & Sorting**: Toggle between Detailed List, Compact List, 2-Column Grid, and 3-Column Grid with flexible sort rules (Name, Size, Date, Type).
- **Built-in Editor & Media Viewer**: Directly view images and edit text or code files without leaving the application.
- **Zip & Archive Tools**: Compress files into `.zip` archives and inspect or extract compressed contents effortlessly.
- **Bookmarks & Recycle Bin**: Bookmark frequently accessed folders and restore accidentally deleted items.
- **Automated CI/CD**: Integrated GitHub Actions workflow for building both Debug and Release APKs automatically.

---

## Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (`androidx.compose.material3`)
- **State & Architecture**: MVVM architecture utilizing `ViewModel`, Kotlin Coroutines, and `StateFlow`
- **Build System**: Gradle (Kotlin DSL - `.gradle.kts`)

---

## Building & Running

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android SDK (API 34+)

### Gradle Commands

Clone the repository and build using standard Gradle tasks:

```bash
# Clone the repository
git clone https://github.com/isaiahscape/materialexp.git
cd materialexp

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

The compiled APKs will be generated in `app/build/outputs/apk/debug/` and `app/build/outputs/apk/release/`.

---

## License

This project is open-source and released under the [MIT License](LICENSE). Third-party Android libraries and Jetpack components are covered under their respective Apache 2.0 licenses.
