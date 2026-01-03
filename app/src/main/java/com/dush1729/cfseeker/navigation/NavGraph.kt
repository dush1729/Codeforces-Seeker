package com.dush1729.cfseeker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.UserViewModel
import com.dush1729.cfseeker.ui.screens.MainScreen
import com.dush1729.cfseeker.ui.screens.UserDetailsScreen

@Composable
fun CFSeekerNavGraph(
    navController: NavHostController,
    userViewModel: UserViewModel,
    contestViewModel: ContestViewModel,
    analyticsService: AnalyticsService,
    crashlyticsService: CrashlyticsService
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                userViewModel = userViewModel,
                contestViewModel = contestViewModel,
                analyticsService = analyticsService
            )
        }

        composable(
            route = Screen.UserDetails.route,
            arguments = listOf(navArgument("handle") { type = NavType.StringType })
        ) { backStackEntry ->
            val handle = backStackEntry.arguments?.getString("handle") ?: return@composable
            UserDetailsScreen(
                handle = handle,
                navController = navController,
                viewModel = userViewModel,
                analyticsService = analyticsService,
                crashlyticsService = crashlyticsService
            )
        }
    }
}
