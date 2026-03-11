package com.dush1729.cfseeker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.bridge.AnalyticsBridge
import com.dush1729.cfseeker.bridge.CrashlyticsBridge
import com.dush1729.cfseeker.bridge.RemoteConfigBridge
import com.dush1729.cfseeker.di.commonModule
import com.dush1729.cfseeker.di.iosModule
import com.dush1729.cfseeker.navigation.CFSeekerNavGraph
import com.dush1729.cfseeker.platform.PlatformActions
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.UserViewModel
import com.dush1729.cfseeker.ui.theme.CFSeekerTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun initKoin(
    analyticsBridge: AnalyticsBridge,
    crashlyticsBridge: CrashlyticsBridge,
    remoteConfigBridge: RemoteConfigBridge
) {
    startKoin {
        modules(commonModule, iosModule(analyticsBridge, crashlyticsBridge, remoteConfigBridge))
    }
}

fun MainViewController() = ComposeUIViewController {
    val userViewModel: UserViewModel = koinViewModel()
    val contestViewModel: ContestViewModel = koinViewModel()
    val analyticsService = KoinPlatform.getKoin().get<AnalyticsService>()
    val crashlyticsService = KoinPlatform.getKoin().get<CrashlyticsService>()
    val platformActions = KoinPlatform.getKoin().get<PlatformActions>()

    CFSeekerTheme {
        val navController = rememberNavController()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CFSeekerNavGraph(
                navController = navController,
                userViewModel = userViewModel,
                contestViewModel = contestViewModel,
                analyticsService = analyticsService,
                crashlyticsService = crashlyticsService,
                platformActions = platformActions
            )
        }
    }
}
