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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.navigation.ContestDetailsRoute
import com.dush1729.cfseeker.platform.PlatformActions
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.DailyViewModel
import com.dush1729.cfseeker.ui.ProfileViewModel
import com.dush1729.cfseeker.ui.SearchViewModel
import com.dush1729.cfseeker.ui.UserViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private data class DrawerTab(val label: String, val icon: ImageVector, val analyticsName: String)

private val DRAWER_TABS = listOf(
    DrawerTab("Users", Icons.Filled.People, "users"),
    DrawerTab("Contests", Icons.Filled.EmojiEvents, "contests"),
    DrawerTab("Search", Icons.Filled.Search, "search"),
    DrawerTab("Daily", Icons.Filled.Today, "daily"),
    DrawerTab("About", Icons.Filled.Info, "about"),
)

@OptIn(ExperimentalMaterial3Api::class)
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
    val appPreferences: AppPreferences = koinInject()
    val searchViewModel: SearchViewModel = koinViewModel()
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val onMenuClick: () -> Unit = { scope.launch { drawerState.open() } }
    var showMenuBadge by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val knownCount = appPreferences.getKnownMenuItemCount()
        showMenuBadge = knownCount < DRAWER_TABS.size
    }

    LaunchedEffect(drawerState) {
        snapshotFlow { drawerState.currentValue }
            .filter { it == DrawerValue.Open }
            .collect {
                if (showMenuBadge) {
                    appPreferences.setKnownMenuItemCount(DRAWER_TABS.size)
                    showMenuBadge = false
                }
            }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(IntrinsicSize.Min)) {
                Spacer(Modifier.height(16.dp))
                DRAWER_TABS.forEachIndexed { index, tab ->
                    NavigationDrawerItem(
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            scope.launch { drawerState.close() }
                            analyticsService.logScreenView(tab.analyticsName)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        when (selectedTab) {
            0 -> UserListScreen(
                navController = navController,
                viewModel = userViewModel,
                onMenuClick = onMenuClick,
                showMenuBadge = showMenuBadge,
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
                showMenuBadge = showMenuBadge,
                modifier = Modifier.fillMaxSize()
            )
            2 -> SearchScreen(
                navController = navController,
                viewModel = searchViewModel,
                onMenuClick = onMenuClick,
                showMenuBadge = showMenuBadge,
                modifier = Modifier.fillMaxSize()
            )
            3 -> DailyScreen(
                navController = navController,
                dailyViewModel = dailyViewModel,
                profileViewModel = profileViewModel,
                onMenuClick = onMenuClick,
                showMenuBadge = showMenuBadge,
                modifier = Modifier.fillMaxSize()
            )
            4 -> AboutScreen(
                navController = navController,
                analyticsService = analyticsService,
                platformActions = platformActions,
                onMenuClick = onMenuClick,
                showMenuBadge = showMenuBadge,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
