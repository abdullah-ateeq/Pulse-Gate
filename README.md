# ⚡ Pulse Gate

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" alt="Pulse Gate Banner" width="160" height="160" style="border-radius: 24px;" />
</p>

<p align="center">
  <strong>An intense, fast-paced 3D neon tunnel runner & timing game built natively for Android using 100% Jetpack Compose.</strong>
</p>

<p align="center">
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" /></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Android-Min_SDK_24_%7C_Target_37-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="License" /></a>
</p>

---

## 🚀 Overview

**Pulse Gate** is an arcade-style 3D tunnel timing game. Players navigate down a high-speed cybernetic tunnel toward rotating neon gates. Line up your trajectory with the open gaps in each gate to pass through safely. As you survive longer, the speed accelerates, gates rotate faster, gap angles tighten, and specialized gate mechanics challenge your reflexes.

Designed with a modern **Neon Cyberpunk Aesthetic** and **Glassmorphism**, Pulse Gate showcases advanced custom canvas rendering, real-time particle mechanics, camera impact effects, dynamic audio, and rich tactile haptics in Jetpack Compose.

---

## ✨ Features

- **🌀 Custom 3D Tunnel Canvas Engine**:
  - Pseudo-3D perspective projection with animated tunnel depth rings.
  - Interactive player control with rotational inertia and smooth dampening.
  - Dynamic particle bursts, shockwave expansions, camera shake, and slow-motion time dilation on perfect passes.

- **🎯 Precision Gameplay & Progressive Difficulty**:
  - Dynamic gate generation with rotating obstacles, narrow gaps, and special gate behavior.
  - Multi-tier scoring system with `"PERFECT!"` pass detection and streak multipliers.
  - Built-in interactive step-by-step tutorial for first-time players.

- **🎨 Cyberpunk Aesthetic & Glassmorphism**:
  - Glowing neon UI elements, glassmorphism panels, and smooth animated transitions.
  - Full support for **Dark Mode**, **Light Mode**, and **System Theme** preferences with adaptive color contrast.

- **🔊 Dynamic Audio & Custom Haptics**:
  - Low-latency sound pool engine (`SoundManager`) for pass effects, collisions, countdowns, and UI feedback.
  - Multi-pattern tactile haptics (`HapticsManager`) providing tactile immersion on passage and game-over impacts.

- **📊 Data Persistence & Settings**:
  - Powered by **Jetpack DataStore Preferences** to store high scores, tutorial state, sound/haptic toggles, and performance preferences.
  - Performance toggles for low-spec devices (particle count toggles, motion blur reduction).

---

## 🛠 Tech Stack & Architecture

Pulse Gate follows modern Android development best practices and clean MVVM architecture:

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **Async & State**: Kotlin Coroutines & `StateFlow`
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Storage**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Graphics**: Custom `Canvas` API drawing math, vector operations, and particle engines
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`) & Kotlin DSL

---

## 📁 Project Structure

```
PulseGate/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/pulsegate/
│   │   │   │   ├── audio/           # SoundPool audio engine & effects
│   │   │   │   ├── data/            # DataStore preferences & models
│   │   │   │   ├── game/            # Core GameEngine, Gate, Player, Particles & Difficulty
│   │   │   │   ├── haptics/         # Android Haptics & Vibration patterns
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/  # GamePlayCanvas, GlassPanel, NeonButtons, AnimatedTunnel
│   │   │   │   │   ├── navigation/  # Navigation host & bottom navigation bar
│   │   │   │   │   ├── screens/     # Home, Game, Result, Settings, About, Splash
│   │   │   │   │   └── theme/       # Cyberpunk Neon palette, Typography & Themes
│   │   │   │   ├── viewmodel/       # GameViewModel & SettingsViewModel
│   │   │   │   └── MainActivity.kt  # Single-activity entry point & edge-to-edge layout
│   │   │   └── AndroidManifest.xml
│   │   └── test/                    # JUnit unit tests for game logic
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml           # Dependency Version Catalog
├── build.gradle.kts
└── README.md
```

---

## 🕹 How to Play

1. **Launch the Game**: Tap **PLAY** on the Home screen to start a run.
2. **Control your Position**: Touch and drag anywhere on the screen (or use side tap controls) to rotate your ship around the tunnel circumference.
3. **Time the Gap**: Align your position with the open sector of each approaching gate.
4. **Build Streaks**: Pass cleanly through the middle of the gap to score a **PERFECT** pass, triggering time dilation and bonus multiplier points!
5. **Avoid Collisions**: Hitting any solid segment of a gate ends the run and displays your final performance stats.

---

## 🛠️ Building & Running

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java 11 or higher
- **Android SDK**: Min SDK 24 (Android 7.0), Target SDK 37

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/Pulse-Gate.git
   cd Pulse-Gate
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync dependencies.
4. Build and run on an Android Device or Emulator:
   ```bash
   ./gradlew assembleDebug
   ```

### Running Tests
Run unit tests for core game loop logic:
```bash
./gradlew test
```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).

Developed with ❤️ by **Abdullah Ateeq**.
