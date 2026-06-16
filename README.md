# 🎮 GameVision

### Gaming Social Network & Discovery Platform for Android

> **Trabajo de Fin de Máster** · Tokio School · Curso 2026
>
> *A modern Android application focused on video game discovery, gaming news, social interaction, and personalized game tracking.*

---

<p align="center">
  <a href="https://github.com/AlejandroEscapa/GameVisionTFM/releases">
    <img src="https://img.shields.io/github/v/release/AlejandroEscapa/GameVisionTFM?style=flat-square&color=blue" alt="Release">
  </a>
  <img src="https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg?style=flat-square" alt="License: All Rights Reserved">
  <a href="https://github.com/AlejandroEscapa/GameVisionTFM/actions">
    <img src="https://img.shields.io/github/actions/workflow/status/AlejandroEscapa/GameVisionTFM/ci.yml?style=flat-square" alt="CI">
  </a>
  <img src="https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?style=flat-square&logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Ready-4285F4?style=flat-square&logo=jetpackcompose" alt="Compose">
  <img src="https://img.shields.io/badge/minSdk-24-green?style=flat-square" alt="minSdk">
  <img src="https://img.shields.io/badge/targetSdk-35-green?style=flat-square" alt="targetSdk">
</p>

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Screenshots](#-screenshots)
- [Key Features](#-key-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Core Modules](#-core-modules)
- [Security](#-security)
- [Testing](#-testing)
- [Contributing](#-contributing)
- [Citing This Work](#-citing-this-work)
- [Support](#-support)
- [Author](#-author)
- [License](#-license)
- [Project Vision](#-project-vision)

---

## 📖 Overview

GameVision is a native Android application built using modern Android development practices. The platform combines **video game discovery**, **real-time gaming news**, **social features**, **user profiles**, and **personalized game management** into a unified mobile experience.

The project follows an **MVVM + Clean Architecture** approach and leverages **Jetpack Compose** for a fully declarative UI, adhering to **Material Design 3** guidelines.

> **Academic context**: This project was developed as part of a Master's Thesis (TFM) to demonstrate proficiency in mobile application development, software architecture, and modern Android technologies.

---

## 📸 Screenshots

| Home | Game Details | Profile | Social |
|------|-------------|---------|--------|
| *[Screenshot]* | *[Screenshot]* | *[Screenshot]* | *[Screenshot]* |

---

## ✨ Key Features

### 🎮 Video Game Discovery
- **RAWG API** integration for comprehensive game search
- Detailed game information pages with metadata
- Ratings, genres, and platform support visualization
- Screenshot galleries and game trailers

### 📰 Gaming News
- Real-time gaming news feed via external API aggregation
- Dynamic content updates with pull-to-refresh
- Category-based filtering

### 👥 Social Platform
- Friend management system (add, remove, search)
- Social interaction capabilities
- User profile discovery

### 👤 User Profiles
- Editable profiles with avatar support
- Personal information management
- Country and username configuration
- Bio descriptions

### ❤️ Personal Library
- Game history tracking
- Favorite game management
- Personalized collections with categories

### 🔐 Authentication
- Firebase Authentication
- Google Sign-In via Credential Manager
- Guest access mode (no account required)

### 🌙 User Experience
- Dark / Light Theme with system-follow mode
- Persistent settings via Jetpack DataStore
- Material Design 3 components
- Responsive Compose UI

---

## 🏗 Architecture

GameVision follows **Clean Architecture** principles with **MVVM** in the presentation layer:

```text
┌─────────────────────────────────────────────-┐
│              PRESENTATION LAYER              │
│  ┌──────────────┐  ┌──────────────────────┐  │
│  │  Compose UI  │  │  Navigation Compose  │  │
│  └──────┬───────┘  └──────────┬───────────┘  │
│         │                     │              │
│  ┌──────▼─────────────────────▼───────────┐  │
│  │              ViewModels                 │ │
│  │  UserVM · SearchVM · NewsVM · ThemeVM   │ │
│  │  GoogleVM · DDBBVM                      │ │
│  └──────────────────┬──────────────────────┘ │
└─────────────────────┼────────────────────────┘
                      │
┌─────────────────────▼────────────────────────┐
│                DOMAIN LAYER                  │
│  ┌──────────────┐  ┌──────────────────────┐  │
│  │   Use Cases  │  │    Repositories      │  │
│  └──────────────┘  └──────────────────────┘  │
└─────────────────────┬────────────────────────┘
                      │
┌─────────────────────▼────────────────────────┐
│                 DATA LAYER                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │ Retrofit │ │Firestore │ │  Room +      │  │
│  │  (RAWG)  │ │ (Cloud)  │ │  DataStore   │  │
│  └──────────┘ └──────────┘ └──────────────┘  │
└──────────────────────────────────────────────┘
```

### Design Patterns Used
- **MVVM** — Separation of UI logic via ViewModels
- **Repository Pattern** — Single source of truth
- **Dependency Injection** — Hilt for compile-time DI
- **Observer Pattern** — StateFlow / SharedFlow
- **Navigation Component** — Type-safe navigation

---

## 🛠 Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Kotlin | 2.1+ |
| UI Toolkit | Jetpack Compose | 1.7+ |
| Architecture | MVVM + Clean Architecture | — |
| Navigation | Navigation Compose | 2.8+ |
| Authentication | Firebase Auth | Latest |
| Login | Google Sign-In + Credential Manager | Latest |
| Local DB | Room | 2.6+ |
| Cloud DB | Firebase Firestore | Latest |
| Networking | Retrofit + OkHttp | 2.11+ |
| Local Storage | Jetpack DataStore | 1.1+ |
| DI | Hilt | 2.51+ |
| Async | Kotlin Coroutines + Flow | 1.9+ |
| Image Loading | Coil | 3.0+ |
| Design System | Material Design 3 | Latest |
| Build System | Gradle KTS | 8.7+ |

---

## 📂 Project Structure

```text
app/
├── src/
│   ├── main/java/com/gamevision/app/
│   │   ├── datastore/            # DataStore preferences
│   │   ├── di/                   # Hilt modules
│   │   ├── domain/               # Use cases & domain models
│   │   ├── data/
│   │   │   ├── local/            # Room DAOs, entities
│   │   │   ├── remote/           # Retrofit, Firestore
│   │   │   └── repository/
│   │   ├── retrofit/             # API service interfaces
│   │   ├── ui/
│   │   │   ├── navigation/       # NavGraph & routes
│   │   │   ├── theme/            # Material 3 theming
│   │   │   └── views/            # Feature composables
│   │   └── viewmodel/            # ViewModels
│   ├── test/                     # Unit tests
│   └── androidTest/              # Instrumented tests
├── build.gradle.kts
└── google-services.json          # (gitignored)
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Minimum Version |
|------|----------------|
| Android Studio | Hedgehog (2023.1+) |
| Android SDK | 35 |
| JDK | 17+ |
| Gradle | 8.7+ (wrapper included) |

### Required Accounts & Keys

- **Firebase Project** — Auth + Firestore enabled → [console.firebase.google.com](https://console.firebase.google.com)
- **RAWG API Key** — Free tier → [rawg.io/apidocs](https://rawg.io/apidocs)
- **NewsAPI Key** — Free tier → [newsapi.org](https://newsapi.org)

### Installation

```bash
# 1. Clone
git clone https://github.com/AlejandroEscapa/GameVisionTFM.git
cd GameVisionTFM

# 2. Firebase setup
#    - Create project at console.firebase.google.com
#    - Enable Authentication + Firestore
#    - Download google-services.json → place in app/

# 3. API Keys (create local.properties — NEVER commit this file)
#    local.properties:
#    RAWG_API_KEY=your_rawg_key_here
#    NEWS_API_KEY=your_news_key_here

# 4. Build
./gradlew assembleDebug

# 5. Run on device/emulator
#    Open in Android Studio → Run 'app'
```

### 🔐 API Key Management (Production)

```kotlin
// build.gradle.kts — secure access via BuildConfig
android {
    defaultConfig {
        buildConfigField("String", "RAWG_API_KEY", 
            project.findProperty("RAWG_API_KEY") ?: "\"\"")
        buildConfigField("String", "NEWS_API_KEY", 
            project.findProperty("NEWS_API_KEY") ?: "\"\"")
    }
}
```

---

## 📱 Core Modules

### 🔐 Authentication Module
- Firebase Authentication (Email/Password)
- Google Sign-In via Credential Manager
- Guest mode — browse without account
- Session persistence

### 📰 News Module
- Gaming news retrieval from external APIs
- Pull-to-refresh feed
- Error handling with retry logic

### 🔍 Search Module
- Game search via RAWG API (debounced input)
- Detailed game views: metadata, ratings, platforms
- Screenshot gallery with zoom
- Pagination for large result sets

### 👥 Social Module
- Friend search by username
- Friend request / accept / decline flow
- User profile viewing
- Real-time updates via Firestore

### ❤️ Library Module
- Add/remove favorites
- Game history: played, playing, want to play
- Local persistence (Room) + Firestore sync

---

## 🔒 Security

### Current Measures
- API keys in `local.properties` (gitignored)
- Firebase Authentication for user identity
- Firestore security rules
- Google OAuth 2.0 standards

### 🔴 Before Publishing Publicly
- [ ] Rotate all exposed API keys
- [ ] Implement Firestore Security Rules (deny by default)
- [ ] Add Firebase App Check
- [ ] Enable Play Integrity API
- [ ] Use EncryptedSharedPreferences
- [ ] Implement Certificate Pinning (OkHttp)
- [ ] Enable ProGuard/R8 obfuscation
- [ ] Run OWASP dependency check plugin

> **Report vulnerabilities**: See [SECURITY.md](SECURITY.md)

---

## 🧪 Testing

```bash
# Unit tests (JVM)
./gradlew test

# Instrumented tests (device/emulator required)
./gradlew connectedAndroidTest

# Coverage report
./gradlew testDebugUnitTestCoverage
```

### Testing Strategy
| Layer | Framework | Focus |
|-------|-----------|-------|
| Unit | JUnit 5 + MockK | ViewModels, Use Cases, Repositories |
| Integration | Firebase Emulator | Firestore reads/writes |
| UI | Compose Testing | Composable rendering, navigation |
| E2E | Espresso + UIAutomator | Full user flows |

---

## 🤝 Contributing

We welcome contributions! See [CONTRIBUTING.md](CONTRIBUTING.md) for full guidelines.

### Quick Start
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit using [Conventional Commits](https://www.conventionalcommits.org/)
4. Push: `git push origin feature/amazing-feature`
5. Open a Pull Request against `main`

---

## 📚 Citing This Work

### APA 7th Edition
> Olivares Escapa, A. (2026). *GameVision: Gaming Social Network & Discovery Platform for Android* [Master's Thesis, Tokio School]. GitHub. https://github.com/AlejandroEscapa/GameVisionTFM

### BibTeX
```bibtex
@mastersthesis{olivares_gamevision,
  author  = {Alejandro Olivares Escapa},
  title   = {{GameVision}: Gaming Social Network \& Discovery Platform for Android},
  school  = {Tokio School},
  year    = {2026},
  type    = {Master's Thesis (TFM)},
  url     = {https://github.com/AlejandroEscapa/GameVisionTFM}
}
```

> See [CITATION.cff](CITATION.cff) for machine-readable metadata.

---

## 🆘 Support

- **Bugs & Features**: [GitHub Issues](https://github.com/AlejandroEscapa/GameVisionTFM/issues)
- **Security issues**: See [SECURITY.md](SECURITY.md) for private reporting

---

## 👨‍💻 Author

**Alejandro Olivares Escapa**

<p align="left">
  <a href="https://github.com/AlejandroEscapa">
    <img src="https://img.shields.io/badge/GitHub-000?style=flat-square&logo=github" alt="GitHub">
  </a>
  <a href="https://linkedin.com/in/alejandro-olivares-escapa">
    <img src="https://img.shields.io/badge/LinkedIn-0A66C2?style=flat-square&logo=linkedin" alt="LinkedIn">
  </a>
</p>

Software Engineer · Frontend & Mobile Developer · Kotlin & Angular Specialist

---

## 📄 License

**All Rights Reserved**

This project is the intellectual property of Alejandro Olivares Escapa.
It is shared publicly for **portfolio and academic evaluation purposes only**.

You may:
- View the code for learning/evaluation
- Reference it with proper citation (see [CITATION.cff](CITATION.cff))

You may NOT:
- Copy, fork, modify, or distribute this code
- Use it in whole or part for commercial or personal projects
- Claim authorship

For licensing inquiries, contact via [GitHub](https://github.com/AlejandroEscapa) or [LinkedIn](https://linkedin.com/in/alejandro-olivares-escapa).

---

## ⭐ Project Vision

> *GameVision aims to be the definitive mobile gaming ecosystem — where players discover new titles, stay informed with industry news, build communities, and track their gaming journey — all through a modern, performant Android-native experience.*

---

<div align="center">

<p>
  <img src="https://img.shields.io/badge/Built%20with-Kotlin-7F52FF?style=for-the-badge&logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Built%20with-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose" alt="Compose">
  <img src="https://img.shields.io/badge/Built%20with-Firebase-FFCA28?style=for-the-badge&logo=firebase" alt="Firebase">
</p>

**Built with ❤️ by [Alejandro Olivares Escapa](https://github.com/AlejandroEscapa)**

</div>
