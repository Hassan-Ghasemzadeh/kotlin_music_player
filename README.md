# Music Player Android App 🎵

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0%2B)-blue.svg)](https://developer.android.com/about/dashboards)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-37-orange.svg)](https://developer.android.com/about/dashboards)
[![Framework](https://img.shields.io/badge/Framework-Jetpack%20Compose-blueviolet.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture%20%2F%20MVVM-informational.svg)](https://developer.android.com/topic/architecture)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A modern, feature-rich, production-grade Android music player application built with **Jetpack
Compose**, **Clean Architecture**, **Media3 (ExoPlayer)**, **Jetpack Glance Widgets**, and a
modularized multi-layer codebase.

---

## 📑 Table of Contents

- [Overview & Value Proposition](#overview--value-proposition)
- [Key Features](#key-features)
- [Architecture & Project Structure](#architecture--project-structure)
- [Tech Stack & Key Dependencies](#tech-stack--key-dependencies)
- [Getting Started (Setup, Build & Run)](#getting-started-setup-build--run)
- [Glance Widgets & App Shortcuts](#glance-widgets--app-shortcuts)
- [Drive Mode & Advanced Audio](#drive-mode--advanced-audio)
- [Localization & Accessibility](#localization--accessibility)
- [Troubleshooting & Build Notes](#troubleshooting--build-notes)
- [Contributing](#contributing)
- [License](#license)

---

## 🔍 Overview & Value Proposition

**Music Player** is designed to deliver a premium, fluid local music playback experience on Android
devices. It adheres strictly to modern Android development best practices, leveraging **Jetpack
Compose** for reactive UI, **Hilt** for robust dependency injection, **Room & DataStore** for local
persistence, and **Media3** for seamless background audio playback and crossfade effects.

The project is structured into modular layers (`:app`, `:core:data`, `:core:domain`, `:core:model`,
`:core:designsystem`, `:feature:library`) to ensure high testability, separation of concerns, and
maintainability.

---

## ✨ Key Features

- **Comprehensive Library Management**: Browse local music categorized by **Songs**, **Albums**,
  **Artists**, **Folders**, **Genres**, and custom **Playlists**.
- **Advanced Media Playback**: Powered by Media3 ExoPlayer with support for crossfade playback
  (`CrossfadeForwardingPlayer`), custom equalizer presets (`EqualizerAudioFxManager`), and audio
  visualizers.
- **Synchronized & Online Lyrics**: Built-in `.lrc` file parser (`LrcParser`), synchronized lyric
  view (`SynchronizedLyricView`), and online lyric fetcher/search dialogs.
- **Jetpack Glance Home-Screen Widgets**:
    - `GlassmorphismPlayerWidget`: A sleek glassmorphism-themed playback widget with media controls
      and artwork.
    - `LyricTickerWidget`: A real-time scrolling lyric ticker widget on your home screen.
- **App Shortcuts / Quick Actions**: Quick launch actions defined in `shortcuts.xml` for Favorite,
  Play, Search, and Shuffle.
- **Drive Mode (Car Mode)**: A specialized high-contrast driving interface with large touch targets
  and voice command matching (`CommandMatcher`).
- **Audio Tag Editor**: Edit song metadata and tags directly within the app (`AudioTagEditor`).
- **Sleep Timer**: Configurable sleep timer with background WorkManager support
  (`SleepTimerWorker`).

---

## 🏛️ Architecture & Project Structure

The project implements **Clean Architecture** combined with the **MVVM (Model-View-ViewModel)**
pattern across multiple Gradle modules:

```text
music_player/
├── app/                  # Application entry point, DI modules, navigation graph, & Glance widget receivers
├── core/
│   ├── data/             # Repositories, Room database (DAOs, Entities), MediaStore scanners, & audio utilities
│   ├── domain/           # Domain models, Repository interfaces, and Use Cases
│   ├── model/            # Pure data models and state representations
│   └── designsystem/     # Reusable Jetpack Compose design components, glass cards, indexers, & themes
└── feature/
    └── library/          # Feature module for music library, player screen, equalizer, drive mode, and lyric management
```

---

## 🛠️ Tech Stack & Key Dependencies

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material 3
- **Navigation**: Jetpack Navigation Compose (`2.8.7`)
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) (`2.x`)
- **Database & Persistence**: [Room](https://developer.android.com/training/data-storage/room) &
  Jetpack DataStore Preferences
- **Asynchronous & Reactive**: Kotlin Coroutines & `StateFlow` / `SharedFlow`
- **Media & Audio**: AndroidX Media3 (ExoPlayer), AudioEffects (`Equalizer`, `Visualizer`)
- **Widgets**: Jetpack Glance (`glance-appwidget`, `glance-material3`)
- **Background Processing**: WorkManager (`androidx.work.runtime.ktx`)
- **Image Loading**: Coil Compose (`coil.compose`)
- **Monetization**: Tapsell Mediation SDK

---

## 🚀 Getting Started (Setup, Build & Run)

### Prerequisites

- **Android Studio**: Ladybug, Koala, or newer (recommended).
- **JDK**: Java 17 (automatically configured via Gradle toolchains).
- **Android SDK**: Compile SDK & Target SDK set to `37`, Min SDK `24`.

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/music-player.git
cd music_player
```

### 2. Open in Android Studio

Open Android Studio, select **Open**, and choose the `music_player` root directory. Allow Gradle to
synchronize dependencies.

### 3. Build via Command Line

To build a debug APK from the terminal:

```bash
./gradlew assembleDebug
```

### 4. Run on Device / Emulator

Connect an Android device with USB Debugging enabled or start an Android Emulator (API 24+), then
click **Run** in Android Studio or execute:

```bash
./gradlew installDebug
```

---

## 📱 Glance Widgets & App Shortcuts

- **Widgets**: Add `GlassmorphismPlayerWidget` or `LyricTickerWidget` to your home screen from the
  Android widget picker. They communicate with the player state via `WidgetStateNotifier` and Glance
  state updaters.
- **Shortcuts**: Long-press the app launcher icon to access quick actions:
    - ⭐ Favorites
    - ▶️ Play
    - 🔍 Search
    - 🔀 Shuffle

---

## 🌍 Localization & Accessibility

The app supports multi-language localization out-of-the-box:

- **English** (`res/values/strings.xml`)
- **German** (`res/values-de/strings.xml`)
- **Persian** (`res/values-fa/strings.xml`)
- **Turkish** (`res/values-tr/strings.xml`)

---

## 🛠️ Troubleshooting & Build Notes

- **Gradle Sync Issues**: If dependency resolution warnings occur, ensure your Android Gradle Plugin
  (AGP) and Gradle versions match Android Studio's recommended toolchains. The project forces
  `androidx.lifecycle` and `androidx.navigation` versions to `2.8.7` for stability.
- **Storage Permissions**: On Android 13+ (API 33+), ensure runtime permissions for audio media
  files (`READ_MEDIA_AUDIO`) are granted when prompted by the app's permission flow
  (`PermissionsScreen`).

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests, open issues, or suggest
enhancements.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the **MIT License**. See the [LICENSE](LICENSE) file for more details.
