package com.dush1729.cfseeker

import android.app.Application
import androidx.work.Configuration
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.data.repository.UserRepository
import com.dush1729.cfseeker.di.androidModule
import com.dush1729.cfseeker.di.commonModule
import com.dush1729.cfseeker.worker.SyncUsersWorkerFactory
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(commonModule, androidModule)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(
                SyncUsersWorkerFactory(
                    repository = get<UserRepository>(),
                    analyticsService = get<AnalyticsService>(),
                    crashlyticsService = get<CrashlyticsService>(),
                    remoteConfigService = get<RemoteConfigService>()
                )
            )
            .build()
}
