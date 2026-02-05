# Codeforces Seeker

**A modern Android app to track and monitor Codeforces users, contests, and rating changes.**

Codeforces Seeker helps competitive programmers track their friends, teammates, or favorite competitive programmers on Codeforces. Get real-time updates on rating changes, contest standings, problems, and user statistics with a beautiful Material 3 UI.

[![Get it on Google Play](https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=com.dush1729.cfseeker)

## ✨ Features

### Users
- 📊 Track multiple Codeforces users with real-time sync
- 📈 Interactive rating history chart in user details
- 🔍 Search and sort users by handle, rating, last sync, or last rating update
- 🔄 Auto-refresh with visual sync status indicators
- 🔴 Red border highlights for outdated users
- 🔗 Click to open contests from rating history
- 🎨 Adaptive color themes based on user ratings

### Contests
- 🏆 Browse and search past Codeforces contests
- 📊 View full contest standings with participant details
- 🔍 Search participants within contest standings
- 📈 Ratings tab showing all rating changes from a contest
- 📝 Problems tab with "Hide spoilers" filter
- 📋 Long-press to copy participant handles
- 🔗 Open problems directly in browser

### General
- 🎨 Modern Material Design 3 UI
- 📱 Optimized image loading with smooth scrolling
- 🔥 Firebase integration (Analytics, Crashlytics, Remote Config)
- ⚡ Optimized database queries with indexes and views

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
│   │   ├── dao/            # Room DAOs (UserDao, ContestDao, ContestStandingsDao)
│   │   ├── entity/         # Room entities
│   │   ├── view/           # Database views for optimized queries
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
│   ├── screens/            # App screens (Users, Contests, UserDetails, ContestDetails)
│   ├── theme/              # Material 3 theme
│   ├── UserViewModel.kt
│   └── ContestViewModel.kt
├── utils/                  # Utility classes
├── worker/                 # WorkManager workers
└── MyApplication.kt
```

## 🔄 Development History

This project has undergone significant modernization and feature additions:

### Architecture Migrations

#### Dagger 2 → Dagger Hilt
**Commit:** [`d38ca97`](../../commit/d38ca97) - migration: dagger to hilt

Migrated from Dagger 2 to Dagger Hilt for simplified dependency injection with less boilerplate and better Android integration.

#### XML Views → Jetpack Compose
**Migration Commits:**
- [`ed5d446`](../../commit/ed5d446) - setup compose dependencies in gradle(#1)
- [`d11477b`](../../commit/d11477b) - setup compose ui: Color, Theme and Type(#1)
- [`8b7d771`](../../commit/8b7d771) - compose: create components and screen(#1)

Fully migrated from XML-based UI to Jetpack Compose for a modern, declarative UI approach with better maintainability.

### Release History

#### v4.1
- Native debug symbols for better crash analysis

#### v4.0 - Contest Filters & Performance
- "Show local only" filter in standings and rating changes
- "Hide spoilers" filter chip for problems
- Long-press to copy participant handles
- Click-to-open contest links from rating history
- Database views for optimized user list with latest rating changes
- Debouncer to reduce database load
- Clear contest data in preferences when clearing cache
- App name update in Share title and content
- Show "Sync All" only when outdated users present
- Play Store link in README

#### v3.4 - Sync Improvements
- Red border highlights for outdated users
- Sync All dialog with confirmation
- Clear contest data functionality
- Case-insensitive user sorting fix

#### v3.3 - Auto Refresh
- Auto-refresh with visual sync status indicators

#### v3.2 - Rating Charts & Performance
- Interactive user rating chart in details screen
- Ratings tab in contest details
- App renamed from CF Seeker to Codeforces Seeker
- AsyncImage optimizations for smooth scrolling
- Reduced API calls from 2n to n+1
- ProGuard rules for optimized release builds
- Sync indicators in contest screens
- Fixed blank screen on quick back presses

#### v3.1 - Bug Fixes
- Fixed tie handling in contest standings

#### v3.0 - Contests Feature
- Browse and search past Codeforces contests
- Full contest standings with participant search
- Full-screen user details (replaced bottom sheet)
- Tabbed interface (Info, Ratings) in user details
- Search within user's rating history
- Open problems directly in browser
- Auto-refresh for contests

#### v2.3 - UI Improvements
- Scrollable bottom sheet
- Hide empty results during search

#### v2.2 - Onboarding
- Empty users view for better onboarding
- Auto-popup keyboard on bottom sheet

#### v2.1 - Performance
- Database query optimizations with indexes
- IO thread improvements for DataStore & Firebase

#### v2.0 - Firebase Integration
- Firebase Analytics with event tracking
- Crashlytics for crash reporting
- Remote Config for feature flags
- Sync all cooldown to prevent API abuse

#### v1.3 - Polish
- About screen
- Snackbar notifications for actions
- Animated list items
- Search feature for users

#### v1.2 - Sync & Sort
- Sort by handle option
- Sync all users functionality
- Rank-based color themes

#### v1.1 - Initial Release
- Track Codeforces users
- View rating changes
- Add/delete users
- User detail bottom sheet
- Dagger to Hilt migration
- XML to Jetpack Compose migration

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
