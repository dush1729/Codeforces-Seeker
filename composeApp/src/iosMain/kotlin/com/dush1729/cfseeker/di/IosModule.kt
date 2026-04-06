package com.dush1729.cfseeker.di

import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.bridge.AnalyticsBridge
import com.dush1729.cfseeker.bridge.BridgedAnalyticsService
import com.dush1729.cfseeker.bridge.BridgedCrashlyticsService
import com.dush1729.cfseeker.bridge.BridgedRemoteConfigService
import com.dush1729.cfseeker.bridge.CrashlyticsBridge
import com.dush1729.cfseeker.bridge.FirestoreBridge
import com.dush1729.cfseeker.bridge.RemoteConfigBridge
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.local.AppDatabase
import com.dush1729.cfseeker.data.local.AppDatabaseService
import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.local.DatabaseService
import com.dush1729.cfseeker.data.local.IosAppPreferences
import com.dush1729.cfseeker.data.local.createIosDatabase
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.data.remote.firestore.FirestoreService
import com.dush1729.cfseeker.data.remote.firestore.IosFirestoreService
import com.dush1729.cfseeker.data.repository.ContestRepository
import com.dush1729.cfseeker.data.repository.ContestStandingsRepository
import com.dush1729.cfseeker.data.repository.ProblemRepository
import com.dush1729.cfseeker.data.repository.RatedUserRepository
import com.dush1729.cfseeker.data.repository.UserRepository
import com.dush1729.cfseeker.platform.BackgroundSyncScheduler
import com.dush1729.cfseeker.platform.IosBackgroundSyncScheduler
import com.dush1729.cfseeker.platform.IosPlatformActions
import com.dush1729.cfseeker.platform.PlatformActions
import com.dush1729.cfseeker.ui.ContestDetailsViewModel
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.DailyViewModel
import com.dush1729.cfseeker.ui.ProfileViewModel
import com.dush1729.cfseeker.ui.SearchViewModel
import com.dush1729.cfseeker.ui.UserViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun iosModule(
    analyticsBridge: AnalyticsBridge,
    crashlyticsBridge: CrashlyticsBridge,
    remoteConfigBridge: RemoteConfigBridge,
    firestoreBridge: FirestoreBridge
) = module {
    // Database
    single<AppDatabase> { createIosDatabase() }
    single<DatabaseService> { AppDatabaseService(get()) }

    // Platform
    single<BackgroundSyncScheduler> { IosBackgroundSyncScheduler(get(), get(), get()) }
    single<PlatformActions> { IosPlatformActions() }

    // Services
    single<AnalyticsService> { BridgedAnalyticsService(analyticsBridge) }
    single<CrashlyticsService> { BridgedCrashlyticsService(crashlyticsBridge) }
    single<RemoteConfigService> { BridgedRemoteConfigService(remoteConfigBridge, get()) }
    single<FirestoreService> { IosFirestoreService(firestoreBridge) }

    // Preferences
    single<AppPreferences> { IosAppPreferences() }

    // Repositories
    single { UserRepository(get(), get()) }
    single { ContestRepository(get(), get(), get()) }
    single { ContestStandingsRepository(get(), get()) }
    single { ProblemRepository(get(), get()) }
    single { RatedUserRepository(get(), get(), get()) }

    // ViewModels
    viewModel { UserViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ContestViewModel(get(), get(), get()) }
    viewModel { ContestDetailsViewModel(get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { DailyViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
}
