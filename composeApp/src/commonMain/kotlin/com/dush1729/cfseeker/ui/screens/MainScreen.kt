package com.dush1729.cfseeker.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.navigation.ContestDetailsRoute
import com.dush1729.cfseeker.platform.PlatformActions
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    contestViewModel: ContestViewModel,
    analyticsService: AnalyticsService,
    platformActions: PlatformActions,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            0 -> analyticsService.logScreenView("users")
            1 -> analyticsService.logScreenView("contests")
            2 -> analyticsService.logScreenView("about")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.People, contentDescription = "Users") },
                    label = { Text("Users") },
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Contests") },
                    label = { Text("Contests") },
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Info, contentDescription = "About") },
                    label = { Text("About") },
                    selected = pagerState.currentPage == 2,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> UserListScreen(
                    navController = navController,
                    viewModel = userViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> ContestListScreen(
                    viewModel = contestViewModel,
                    onContestClick = { contestId, contestName, contestType ->
                        navController.navigate(ContestDetailsRoute(contestId, contestName, contestType))
                    },
                    modifier = Modifier.fillMaxSize()
                )
                2 -> AboutScreen(
                    analyticsService = analyticsService,
                    platformActions = platformActions,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
