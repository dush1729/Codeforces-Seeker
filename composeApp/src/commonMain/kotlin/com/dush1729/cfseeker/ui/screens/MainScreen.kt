package com.dush1729.cfseeker.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.navigation.ContestDetailsRoute
import com.dush1729.cfseeker.platform.PlatformActions
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.DailyViewModel
import com.dush1729.cfseeker.ui.ProfileViewModel
import com.dush1729.cfseeker.ui.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    contestViewModel: ContestViewModel,
    dailyViewModel: DailyViewModel,
    profileViewModel: ProfileViewModel,
    analyticsService: AnalyticsService,
    platformActions: PlatformActions,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val onMenuClick: () -> Unit = { scope.launch { drawerState.open() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(IntrinsicSize.Min)) {
                Spacer(Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.People, contentDescription = null) },
                    label = { Text("Users") },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        scope.launch { drawerState.close() }
                        analyticsService.logScreenView("users")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = null) },
                    label = { Text("Contests") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        scope.launch { drawerState.close() }
                        analyticsService.logScreenView("contests")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Today, contentDescription = null) },
                    label = { Text("Daily") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        scope.launch { drawerState.close() }
                        analyticsService.logScreenView("daily")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    label = { Text("About") },
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        scope.launch { drawerState.close() }
                        analyticsService.logScreenView("about")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        when (selectedTab) {
            0 -> UserListScreen(
                navController = navController,
                viewModel = userViewModel,
                onMenuClick = onMenuClick,
                modifier = Modifier.fillMaxSize()
            )
            1 -> ContestListScreen(
                viewModel = contestViewModel,
                onContestClick = { contestId, contestName, contestType ->
                    navController.navigate(
                        ContestDetailsRoute(contestId, contestName, contestType)
                    )
                },
                onMenuClick = onMenuClick,
                modifier = Modifier.fillMaxSize()
            )
            2 -> DailyScreen(
                navController = navController,
                dailyViewModel = dailyViewModel,
                profileViewModel = profileViewModel,
                onMenuClick = onMenuClick,
                modifier = Modifier.fillMaxSize()
            )
            3 -> AboutScreen(
                analyticsService = analyticsService,
                platformActions = platformActions,
                onMenuClick = onMenuClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
