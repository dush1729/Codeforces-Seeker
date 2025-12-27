package com.dush1729.cfseeker.di.module

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.dush1729.cfseeker.BuildConfig
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.analytics.DummyAnalyticsService
import com.dush1729.cfseeker.analytics.FirebaseAnalyticsService
import com.dush1729.cfseeker.data.local.AppDatabase
import com.dush1729.cfseeker.data.local.AppDatabaseService
import com.dush1729.cfseeker.data.remote.config.FirebaseRemoteConfigService
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.local.AppPreferencesImpl
import com.dush1729.cfseeker.data.local.DatabaseService
import com.dush1729.cfseeker.data.remote.api.NetworkService
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Singleton
    @Provides
    fun provideNetworkService(): NetworkService {
        return Retrofit
            .Builder()
            .baseUrl("https://codeforces.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NetworkService::class.java)
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database")
            .build()
    }

    @Singleton
    @Provides
    fun provideDatabaseService(appDatabase: AppDatabase): DatabaseService {
        return AppDatabaseService(appDatabase)
    }

    @Singleton
    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideFirebaseAnalytics(): FirebaseAnalytics {
        return Firebase.analytics
    }

    @Singleton
    @Provides
    fun provideAnalyticsService(firebaseAnalytics: FirebaseAnalytics): AnalyticsService {
        return if (BuildConfig.DEBUG) {
            DummyAnalyticsService
        } else {
            FirebaseAnalyticsService(firebaseAnalytics)
        }
    }

    @Singleton
    @Provides
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferencesImpl(context)
    }

    @Singleton
    @Provides
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return Firebase.remoteConfig
    }

    @Singleton
    @Provides
    fun provideRemoteConfigService(remoteConfig: FirebaseRemoteConfig): RemoteConfigService {
        return FirebaseRemoteConfigService(remoteConfig)
    }
}