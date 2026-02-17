package com.dush1729.cfseeker.di

import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.analytics.DummyAnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.crashlytics.DummyCrashlyticsService
import com.dush1729.cfseeker.data.local.AppDatabase
import com.dush1729.cfseeker.data.local.AppDatabaseService
import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.local.DatabaseService
import com.dush1729.cfseeker.data.local.IosAppPreferences
import com.dush1729.cfseeker.data.local.createIosDatabase
import com.dush1729.cfseeker.data.remote.config.IosRemoteConfigService
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.data.repository.ContestRepository
import com.dush1729.cfseeker.data.repository.ContestStandingsRepository
import com.dush1729.cfseeker.data.repository.UserRepository
import com.dush1729.cfseeker.platform.BackgroundSyncScheduler
import com.dush1729.cfseeker.platform.IosBackgroundSyncScheduler
import com.dush1729.cfseeker.platform.IosPlatformActions
import com.dush1729.cfseeker.platform.PlatformActions
import com.dush1729.cfseeker.ui.ContestDetailsViewModel
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.UserViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val iosModule = module {
    // Database
    single<AppDatabase> { createIosDatabase() }
    single<DatabaseService> { AppDatabaseService(get()) }

    // Platform
    single<BackgroundSyncScheduler> { IosBackgroundSyncScheduler() }
    single<PlatformActions> { IosPlatformActions() }

    // Services
    single<AnalyticsService> { DummyAnalyticsService }
    single<CrashlyticsService> { DummyCrashlyticsService }
    single<RemoteConfigService> { IosRemoteConfigService() }

    // Preferences
    single<AppPreferences> { IosAppPreferences() }

    // Repositories
    single { UserRepository(get(), get()) }
    single { ContestRepository(get(), get(), get()) }
    single { ContestStandingsRepository(get(), get()) }

    // ViewModels
    viewModel { UserViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ContestViewModel(get(), get(), get()) }
    viewModel { ContestDetailsViewModel(get(), get(), get(), get()) }
}
