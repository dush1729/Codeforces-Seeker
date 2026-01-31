# CFSeeker

**A modern Android app to track and monitor Codeforces users and their rating changes.**

CFSeeker helps competitive programmers track their friends, teammates, or favorite competitive programmers on Codeforces. Get real-time updates on rating changes, contest performance, and user statistics with a beautiful Material 3 UI.

[![Get it on Google Play](https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=com.dush1729.cfseeker)

## ✨ Features

- 📊 Track multiple Codeforces users
- 📈 View rating history and contest performance
- 🔍 Search and sort users by various criteria
- 🔄 Sync user data with background workers
- 🎨 Modern Material Design 3 UI
- 🌙 Adaptive color themes based on user ratings
- 📱 Optimized image loading with Coil
- 🔥 Firebase integration (Analytics, Crashlytics, Remote Config)

## 🏗️ Tech Stack

### Modern Android Development
- **Kotlin** - 100% Kotlin codebase
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material 3** - Latest Material Design components
- **Coroutines & Flow** - Asynchronous programming
- **Dagger Hilt** - Dependency injection
- **Room** - Local database with SQLite
- **Retrofit** - REST API client
- **Coil** - Image loading with caching
- **WorkManager** - Background task scheduling
- **DataStore** - Modern data persistence

### Architecture
- **MVVM** - Model-View-ViewModel pattern
- **Repository Pattern** - Data layer abstraction
- **Clean Architecture** - Separation of concerns
- **Single Activity** - Compose navigation

### Firebase
- **Analytics** - User behavior tracking
- **Crashlytics** - Crash reporting
- **Remote Config** - Feature flags

## 📁 Project Structure

```
app/src/main/java/com/dush1729/cfseeker/
├── analytics/              # Analytics service abstraction
│   ├── AnalyticsService.kt
│   ├── DummyAnalyticsService.kt
│   └── FirebaseAnalyticsService.kt
├── crashlytics/            # Crashlytics service abstraction
│   ├── CrashlyticsService.kt
│   ├── DummyCrashlyticsService.kt
│   └── FirebaseCrashlyticsService.kt
├── data/
│   ├── local/
│   │   ├── dao/            # Room DAOs
│   │   ├── entity/         # Room entities
│   │   ├── AppDatabase.kt
│   │   └── DatabaseService.kt
│   ├── remote/
│   │   ├── api/            # Retrofit API interfaces
│   │   ├── config/         # Firebase Remote Config
│   │   └── model/          # API response models
│   └── repository/         # Repository implementations
├── di/
│   └── module/             # Dagger Hilt modules
├── ui/
│   ├── base/               # Base UI classes
│   ├── components/         # Reusable Compose components
│   ├── screens/            # App screens
│   ├── theme/              # Material 3 theme
│   └── UserViewModel.kt
├── utils/                  # Utility classes
├── worker/                 # WorkManager workers
└── MyApplication.kt
```

## 🔄 Migration History

This project has undergone significant modernization:

### Dagger 2 → Dagger Hilt
**Commit:** [`d38ca97`](../../commit/d38ca97) - migration: dagger to hilt

Migrated from Dagger 2 to Dagger Hilt for simplified dependency injection with less boilerplate and better Android integration.

### XML Views → Jetpack Compose
**Migration Commits:**
- [`ed5d446`](../../commit/ed5d446) - setup compose dependencies in gradle(#1)
- [`d11477b`](../../commit/d11477b) - setup compose ui: Color, Theme and Type(#1)
- [`8b7d771`](../../commit/8b7d771) - compose: create components and screen(#1)

Fully migrated from XML-based UI to Jetpack Compose for a modern, declarative UI approach with better maintainability.

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11 or higher
- Android SDK with minimum API 24 (Android 7.0)

### Firebase Setup

This app uses Firebase services. To set it up:

1. **Create a Firebase project:**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or use an existing one
   - Add an Android app with package name: `com.dush1729.cfseeker`

2. **Download google-services.json:**
   - In Firebase Console, go to Project Settings
   - Download the `google-services.json` file
   - Place it in the `app/` directory

3. **Enable Firebase services:**
   - Enable **Firebase Analytics** in the Firebase Console
   - Enable **Crashlytics** for crash reporting
   - Enable **Remote Config** for feature flags

   📺 **Detailed Guide:** [Firebase Android Setup Tutorial](https://firebase.google.com/docs/android/setup)

### Keystore Setup (for release builds)

Create a `keystore.properties` file in the project root:

```properties
storeFile=/path/to/your/keystore.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

### Build and Run

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Release build
./gradlew assembleRelease
```

## 🤝 Contributing

**Contributions are welcome!** Whether it's bug fixes, new features, or improvements to documentation.

### How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Write meaningful commit messages
- Add tests for new features
- Ensure all builds pass before submitting PR
- Update documentation for significant changes

## 📄 License

This project is available for educational and personal use.

## 👨‍💻 Author

**Dushyant Singh** ([@dush1729](https://github.com/dush1729))

## 🙏 Acknowledgments

- [Codeforces API](https://codeforces.com/apiHelp) for providing the data
- Android Jetpack team for amazing libraries
- Firebase team for excellent backend services

---

**Star ⭐ this repo if you find it useful!**
