package com.dush1729.cfseeker.di

import androidx.room.Room
import androidx.work.WorkManager
import com.dush1729.cfseeker.BuildConfig
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.analytics.DummyAnalyticsService
import com.dush1729.cfseeker.analytics.FirebaseAnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.crashlytics.DummyCrashlyticsService
import com.dush1729.cfseeker.crashlytics.FirebaseCrashlyticsService
import com.dush1729.cfseeker.data.local.AppDatabase
import com.dush1729.cfseeker.data.local.AppDatabaseMigrations
import com.dush1729.cfseeker.data.local.AppDatabaseService
import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.local.AppPreferencesImpl
import com.dush1729.cfseeker.data.local.DatabaseService
import com.dush1729.cfseeker.data.remote.config.FirebaseRemoteConfigService
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.data.repository.ContestRepository
import com.dush1729.cfseeker.data.repository.ContestStandingsRepository
import com.dush1729.cfseeker.data.repository.UserRepository
import com.dush1729.cfseeker.ui.ContestDetailsViewModel
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.UserViewModel
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidModule = module {
    // Database
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(*AppDatabaseMigrations.ALL_MIGRATIONS)
            .build()
    }

    single<DatabaseService> { AppDatabaseService(get()) }

    // WorkManager
    single { WorkManager.getInstance(androidContext()) }

    // Firebase
    single { Firebase.analytics }
    single<AnalyticsService> {
        if (BuildConfig.DEBUG) DummyAnalyticsService
        else FirebaseAnalyticsService(get())
    }

    single { Firebase.crashlytics }
    single<CrashlyticsService> {
        if (BuildConfig.DEBUG) DummyCrashlyticsService
        else FirebaseCrashlyticsService(get())
    }

    single { Firebase.remoteConfig }
    single<RemoteConfigService> { FirebaseRemoteConfigService(get(), get()) }

    // Preferences
    single<AppPreferences> { AppPreferencesImpl(androidContext()) }

    // Repositories
    single { UserRepository(get(), get()) }
    single { ContestRepository(get(), get(), get()) }
    single { ContestStandingsRepository(get(), get()) }

    // ViewModels
    viewModel { UserViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ContestViewModel(get(), get(), get()) }
    viewModel { ContestDetailsViewModel(get(), get(), get(), get()) }
}
