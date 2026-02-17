package com.dush1729.cfseeker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.platform.PlatformActions
import com.dush1729.cfseeker.ui.ContestDetailsViewModel
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.UserViewModel
import com.dush1729.cfseeker.ui.screens.ContestDetailsScreen
import com.dush1729.cfseeker.ui.screens.MainScreen
import com.dush1729.cfseeker.ui.screens.UserDetailsScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CFSeekerNavGraph(
    navController: NavHostController,
    userViewModel: UserViewModel,
    contestViewModel: ContestViewModel,
    analyticsService: AnalyticsService,
    crashlyticsService: CrashlyticsService,
    platformActions: PlatformActions
) {
    NavHost(
        navController = navController,
        startDestination = MainRoute
    ) {
        composable<MainRoute> {
            MainScreen(
                navController = navController,
                userViewModel = userViewModel,
                contestViewModel = contestViewModel,
                analyticsService = analyticsService,
                platformActions = platformActions
            )
        }

        composable<UserDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<UserDetailsRoute>()
            UserDetailsScreen(
                handle = route.handle,
                navController = navController,
                viewModel = userViewModel,
                analyticsService = analyticsService,
                crashlyticsService = crashlyticsService
            )
        }

        composable<ContestDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ContestDetailsRoute>()
            val contestDetailsViewModel: ContestDetailsViewModel = koinViewModel()
            ContestDetailsScreen(
                contestId = route.contestId,
                contestName = route.contestName,
                contestType = route.contestType,
                navController = navController,
                viewModel = contestDetailsViewModel,
                analyticsService = analyticsService,
                crashlyticsService = crashlyticsService
            )
        }
    }
}
