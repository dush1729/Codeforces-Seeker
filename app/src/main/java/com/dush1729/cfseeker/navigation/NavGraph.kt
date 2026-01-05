package com.dush1729.cfseeker.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.ui.ContestDetailsViewModel
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.UserViewModel
import com.dush1729.cfseeker.ui.screens.ContestDetailsScreen
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

        composable(
            route = Screen.ContestDetails.route,
            arguments = listOf(
                navArgument("contestId") { type = NavType.IntType },
                navArgument("contestName") { type = NavType.StringType },
                navArgument("contestType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val contestId = backStackEntry.arguments?.getInt("contestId") ?: return@composable
            val contestName = backStackEntry.arguments?.getString("contestName") ?: return@composable
            val contestType = backStackEntry.arguments?.getString("contestType") ?: return@composable
            val contestDetailsViewModel: ContestDetailsViewModel = hiltViewModel()
            ContestDetailsScreen(
                contestId = contestId,
                contestName = contestName,
                contestType = contestType,
                navController = navController,
                viewModel = contestDetailsViewModel,
                analyticsService = analyticsService,
                crashlyticsService = crashlyticsService
            )
        }
    }
}
