package com.dush1729.cfseeker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dush1729.cfseeker.data.local.view.UserWithLatestRatingChangeView
import com.dush1729.cfseeker.navigation.Screen
import com.dush1729.cfseeker.ui.SortOption
import com.dush1729.cfseeker.ui.UserViewModel
import com.dush1729.cfseeker.ui.base.UiState
import com.dush1729.cfseeker.ui.components.ErrorState
import com.dush1729.cfseeker.ui.components.LoadingState
import com.dush1729.cfseeker.ui.components.UserCard
import com.dush1729.cfseeker.ui.theme.CFSeekerTheme
import com.dush1729.cfseeker.utils.toRelativeTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// TODO: Decouple ViewModel here
fun UserListScreen(
    navController: NavController,
    viewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()
    val userCount by viewModel.userCount.collectAsStateWithLifecycle()
    val outdatedUserCount by viewModel.outdatedUserCount.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isAddUserEnabled = remember { viewModel.isAddUserEnabled() }
    val isSyncAllUsersEnabled = remember { viewModel.isSyncAllUsersEnabled() }
    val refreshIntervalMinutes = remember { viewModel.getRefreshIntervalMinutes() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    var userHandle by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }

    // Permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.syncAllUsers()
        } else {
            viewModel.logToCrashlytics("UserListScreen: Notification permission denied")
        }
    }

    fun requestNotificationPermissionAndSync() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> {
                    viewModel.syncAllUsers()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            viewModel.syncAllUsers()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("Users") },
                scrollBehavior = scrollBehavior,
                actions = {
                    // Sort button - only show when user list size > 1
                    if (userCount > 1) {
                        FilledTonalButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Sort by ${currentSortOption.displayName}",
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { sortOption ->
                                DropdownMenuItem(
                                    text = { Text(sortOption.displayName) },
                                    onClick = {
                                        viewModel.setSortOption(sortOption)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sync button - only show when there are outdated users
                if (outdatedUserCount > 0) {
                    BadgedBox(
                        badge = {
                            syncProgress?.let { (current, total) ->
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.offset(x = (-8).dp, y = 8.dp)
                                ) {
                                    Text(
                                        text = "$current/$total",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        FloatingActionButton(
                            onClick = {
                                if (isSyncAllUsersEnabled) {
                                    scope.launch {
                                        if (viewModel.canSyncAllUsers()) {
                                            showSyncDialog = true
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Feature disabled")
                                    }
                                }
                            },
                            containerColor = if (isSyncing)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Filled.Sync, contentDescription = "Sync users")
                            }
                        }
                    }
                }
                // Add User button - only show when user list size > 0
                if (userCount > 0) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (isAddUserEnabled) {
                                showBottomSheet = true
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Feature disabled")
                                }
                            }
                        },
                        icon = { Icon(Icons.Filled.Add, contentDescription = "Add User") },
                        text = { Text("Add User") }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Last Sync Time Display - only show when there are users and sync has happened
            if (userCount > 0) {
                if (lastSyncTime > 0) {
                    Text(
                        text = "Last sync: ${lastSyncTime.toRelativeTime()} • Auto syncs every $refreshIntervalMinutes minutes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Sync progress indicator
                if (isRefreshing) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Search bar - only show when user list size > 1
            if (userCount > 1) {
                TextField(
                    value = searchQuery,
                    onValueChange = { newValue ->
                        viewModel.setSearchQuery(newValue)
                        if (newValue.isEmpty()) {
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search by handle...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.setSearchQuery("")
                                focusManager.clearFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            }

            // User List
            when (val state = uiState) {
                is UiState.Loading -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }

                is UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is UiState.Success -> {
                    if (state.data.isEmpty() && searchQuery.isEmpty()) {
                        // Empty user list
                        EmptyUsersView(
                            onAddUserClick = {
                                if (isAddUserEnabled) {
                                    showBottomSheet = true
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Feature disabled")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Show user list
                        UserList(
                            users = state.data,
                            sortOption = currentSortOption,
                            onUserCardClick = { handle ->
                                navController.navigate(Screen.UserDetails.createRoute(handle))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showBottomSheet = false
                userHandle = ""
            },
        ) {
            AddUserBottomSheet(
                userHandle = userHandle,
                onUserHandleChange = { userHandle = it },
                onAddClick = {
                    viewModel.fetchUser(userHandle.trim())
                    showBottomSheet = false
                    userHandle = ""
                },
                onCancelClick = {
                    showBottomSheet = false
                    userHandle = ""
                },
                sheetState = sheetState
            )
        }
    }

    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            icon = { Icon(Icons.Filled.Sync, contentDescription = null) },
            title = { Text("Sync $outdatedUserCount outdated user${if (outdatedUserCount > 1) "s" else ""}?") },
            text = {
                Text(
                    text = "Uses one API call per user.\nThis may take a while.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSyncDialog = false
                        requestNotificationPermissionAndSync()
                    }
                ) {
                    Text("Sync")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmptyUsersView(
    onAddUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonSearch,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No users added yet",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Add your first Codeforces user to get started",
                style = MaterialTheme.typography.bodyMedium,
            )
            ExtendedFloatingActionButton(
                onClick = onAddUserClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null
                    )
                },
                text = { Text("Add User") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddUserBottomSheet(
    userHandle: String,
    onUserHandleChange: (String) -> Unit,
    onAddClick: suspend () -> Unit,
    onCancelClick: () -> Unit,
    sheetState: androidx.compose.material3.SheetState,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(sheetState.currentValue) {
        snapshotFlow { sheetState.currentValue }
            .collect { value ->
                if (value == SheetValue.Expanded) {
                    focusRequester.requestFocus()
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add User",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )

        errorMessage?.let { error ->
            Text(
                text = error,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error
            )
        }

        OutlinedTextField(
            value = userHandle,
            onValueChange = onUserHandleChange,
            label = { Text("User Handle") },
            placeholder = { Text("Enter Codeforces handle") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (userHandle.isNotBlank() && !isLoading) {
                        scope.launch {
                            try {
                                isLoading = true
                                errorMessage = null
                                onAddClick()
                                isLoading = false
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = e.message ?: "Failed to add user"
                            }
                        }
                    }
                }
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onCancelClick,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            isLoading = true
                            errorMessage = null
                            onAddClick()
                            isLoading = false
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = e.message ?: "Failed to add user"
                        }
                    }
                },
                enabled = userHandle.isNotBlank() && !isLoading,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
private fun UserList(
    users: List<UserWithLatestRatingChangeView>,
    sortOption: SortOption = SortOption.LAST_RATING_UPDATE,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onUserCardClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
            top = contentPadding.calculateTopPadding(),
            end = contentPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
            bottom = contentPadding.calculateBottomPadding() + 80.dp
        )
    ) {
        items(
            items = users,
            key = { it.handle }
        ) { user ->
            UserCard(
                user = user,
                sortOption = sortOption,
                onClick = onUserCardClick,
                modifier = if (users.size < 50) Modifier.animateItem() else Modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserListPreview() {
    CFSeekerTheme {
        UserList(
            users = listOf(
                UserWithLatestRatingChangeView(
                    handle = "tourist",
                    avatar = null,
                    city = "St. Petersburg",
                    contribution = 100,
                    country = "Russia",
                    email = null,
                    firstName = "Gennady",
                    friendOfCount = 5000,
                    lastName = "Korotkevich",
                    lastOnlineTimeSeconds = System.currentTimeMillis() / 1000,
                    maxRank = "legendary grandmaster",
                    maxRating = 3979,
                    organization = null,
                    rank = "legendary grandmaster",
                    rating = 3979,
                    registrationTimeSeconds = 1234567890,
                    titlePhoto = "",
                    lastSync = System.currentTimeMillis() / 1000,
                    latestContestId = 1234,
                    latestContestName = "Codeforces Round #XXX",
                    latestContestRank = 1,
                    latestOldRating = 3937,
                    latestNewRating = 3979,
                    latestRatingUpdateTimeSeconds = System.currentTimeMillis() / 1000 - 86400,
                    isRatingOutdated = false
                ),
                UserWithLatestRatingChangeView(
                    handle = "newuser",
                    avatar = null,
                    city = null,
                    contribution = 0,
                    country = null,
                    email = null,
                    firstName = null,
                    friendOfCount = 0,
                    lastName = null,
                    lastOnlineTimeSeconds = System.currentTimeMillis() / 1000,
                    maxRank = null,
                    maxRating = null,
                    organization = null,
                    rank = null,
                    rating = null,
                    registrationTimeSeconds = 1234567890,
                    titlePhoto = "",
                    lastSync = System.currentTimeMillis(),
                    latestContestId = null,
                    latestContestName = null,
                    latestContestRank = null,
                    latestOldRating = null,
                    latestNewRating = null,
                    latestRatingUpdateTimeSeconds = null,
                    isRatingOutdated = false
                )
            )
        )
    }
}
