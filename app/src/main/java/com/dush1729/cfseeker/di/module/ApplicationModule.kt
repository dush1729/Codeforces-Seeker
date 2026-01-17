package com.dush1729.cfseeker.di.module

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.WorkManager
import com.dush1729.cfseeker.BuildConfig
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.analytics.DummyAnalyticsService
import com.dush1729.cfseeker.analytics.FirebaseAnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.crashlytics.DummyCrashlyticsService
import com.dush1729.cfseeker.crashlytics.FirebaseCrashlyticsService
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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
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
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add indices to user table
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_handle ON user(handle)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_lastSync ON user(lastSync)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_rating ON user(rating)")

            // Add indices to rating_change table
            db.execSQL("CREATE INDEX IF NOT EXISTS index_rating_change_handle ON rating_change(handle)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_rating_change_ratingUpdateTimeSeconds ON rating_change(ratingUpdateTimeSeconds)")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create contest table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `contest` (
                    `id` INTEGER PRIMARY KEY NOT NULL,
                    `name` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `phase` TEXT NOT NULL,
                    `frozen` INTEGER NOT NULL,
                    `durationSeconds` INTEGER NOT NULL,
                    `startTimeSeconds` INTEGER NOT NULL,
                    `relativeTimeSeconds` INTEGER NOT NULL
                )
            """)

            // Add indices to contest table
            db.execSQL("CREATE INDEX IF NOT EXISTS index_contest_id ON contest(id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_contest_phase ON contest(phase)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_contest_startTimeSeconds ON contest(startTimeSeconds)")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Drop old individual indexes (except primary key)
            db.execSQL("DROP INDEX IF EXISTS index_contest_id")
            db.execSQL("DROP INDEX IF EXISTS index_contest_phase")
            db.execSQL("DROP INDEX IF EXISTS index_contest_startTimeSeconds")

            // Create composite index for optimal query performance
            db.execSQL("CREATE INDEX IF NOT EXISTS index_contest_phase_startTime ON contest(phase, startTimeSeconds)")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create contest_problem table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `contest_problem` (
                    `contestId` INTEGER NOT NULL,
                    `problemsetName` TEXT,
                    `index` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `points` REAL,
                    `rating` INTEGER,
                    `tags` TEXT NOT NULL,
                    PRIMARY KEY(`contestId`, `index`)
                )
            """)

            // Create contest_standing_row table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `contest_standing_row` (
                    `contestId` INTEGER NOT NULL,
                    `rank` INTEGER NOT NULL,
                    `points` REAL NOT NULL,
                    `penalty` INTEGER NOT NULL,
                    `successfulHackCount` INTEGER NOT NULL,
                    `unsuccessfulHackCount` INTEGER NOT NULL,
                    `lastSubmissionTimeSeconds` INTEGER,
                    `participantType` TEXT NOT NULL,
                    `teamId` INTEGER,
                    `teamName` TEXT,
                    `ghost` INTEGER NOT NULL,
                    `room` INTEGER,
                    `memberHandles` TEXT NOT NULL,
                    `problemResults` TEXT NOT NULL,
                    PRIMARY KEY(`contestId`, `rank`)
                )
            """)
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Drop and recreate contest_standing_row table with new primary key
            // This fixes the bug where tied participants were not all stored
            db.execSQL("DROP TABLE IF EXISTS `contest_standing_row`")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `contest_standing_row` (
                    `contestId` INTEGER NOT NULL,
                    `rank` INTEGER NOT NULL,
                    `points` REAL NOT NULL,
                    `penalty` INTEGER NOT NULL,
                    `successfulHackCount` INTEGER NOT NULL,
                    `unsuccessfulHackCount` INTEGER NOT NULL,
                    `lastSubmissionTimeSeconds` INTEGER,
                    `participantType` TEXT NOT NULL,
                    `teamId` INTEGER,
                    `teamName` TEXT,
                    `ghost` INTEGER NOT NULL,
                    `room` INTEGER,
                    `memberHandles` TEXT NOT NULL,
                    `problemResults` TEXT NOT NULL,
                    PRIMARY KEY(`contestId`, `memberHandles`)
                )
            """)

            // Add index on (contestId, rank) for efficient ORDER BY rank queries
            db.execSQL("CREATE INDEX IF NOT EXISTS index_contest_standing_contestId_rank ON contest_standing_row(contestId, rank)")
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add source column to rating_change table with default value 'USER'
            // Existing entries are from user sync, so 'USER' is the correct default
            db.execSQL("ALTER TABLE `rating_change` ADD COLUMN `source` TEXT NOT NULL DEFAULT 'USER'")

            // Add composite index on (contestId, source) for efficient contest data deletion
            db.execSQL("CREATE INDEX IF NOT EXISTS index_rating_change_contestId_source ON rating_change(contestId, source)")
        }
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Singleton
    @Provides
    fun provideNetworkService(gson: Gson): NetworkService {
        return Retrofit
            .Builder()
            .baseUrl("https://codeforces.com/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
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
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
            )
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
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return Firebase.crashlytics
    }

    @Singleton
    @Provides
    fun provideCrashlyticsService(crashlytics: FirebaseCrashlytics): CrashlyticsService {
        return if (BuildConfig.DEBUG) {
            DummyCrashlyticsService
        } else {
            FirebaseCrashlyticsService(crashlytics)
        }
    }

    @Singleton
    @Provides
    fun provideRemoteConfigService(
        remoteConfig: FirebaseRemoteConfig,
        crashlyticsService: CrashlyticsService
    ): RemoteConfigService {
        return FirebaseRemoteConfigService(remoteConfig, crashlyticsService)
    }
}