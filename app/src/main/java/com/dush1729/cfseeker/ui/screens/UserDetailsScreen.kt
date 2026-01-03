package com.dush1729.cfseeker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dush1729.cfseeker.R
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.ui.UserViewModel
import com.dush1729.cfseeker.utils.getRatingColor
import com.dush1729.cfseeker.utils.toRelativeTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(
    handle: String,
    navController: NavController,
    viewModel: UserViewModel,
    analyticsService: AnalyticsService,
    crashlyticsService: CrashlyticsService,
    modifier: Modifier = Modifier
) {
    val user by viewModel.getUserByHandle(handle).collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }

    // Track screen view
    LaunchedEffect(Unit) {
        analyticsService.logScreenView("user_details")
        crashlyticsService.log("Screen: UserDetails (handle=$handle)")
    }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text(user?.handle ?: "User Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        user?.let { currentUser ->
            UserDetailsContent(
                user = currentUser,
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                onNavigateBack = { navController.popBackStack() },
                analyticsService = analyticsService,
                crashlyticsService = crashlyticsService,
                modifier = Modifier.padding(paddingValues)
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun UserDetailsContent(
    user: UserEntity,
    viewModel: UserViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    analyticsService: AnalyticsService,
    crashlyticsService: CrashlyticsService,
    modifier: Modifier = Modifier
) {
    var isSyncing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val isSyncUserEnabled = remember { viewModel.isSyncUserEnabled() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with avatar and handle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.avatar,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                error = painterResource(id = R.drawable.ic_launcher_foreground)
            )

            Column {
                Text(
                    text = user.handle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = getRatingColor(user.rating)
                )
                user.rank?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = getRatingColor(user.rating)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Info Section
            if (user.firstName != null || user.lastName != null || user.email != null) {
                SectionTitle("Basic Info")
                user.firstName?.let { DetailRow("First Name", it) }
                user.lastName?.let { DetailRow("Last Name", it) }
                user.email?.let { DetailRow("Email", it) }
            }

            // Stats Section
            SectionTitle("Stats")
            user.rating?.let { DetailRow("Rating", it.toString(), getRatingColor(it)) }
            user.maxRating?.let { DetailRow("Max Rating", it.toString(), getRatingColor(it)) }
            user.maxRank?.let { DetailRow("Max Rank", it, getRatingColor(user.maxRating)) }
            DetailRow("Contribution", user.contribution.toString())
            DetailRow("Friend of", "${user.friendOfCount} users")

            // Location Section
            if (user.country != null || user.city != null || user.organization != null) {
                SectionTitle("Location & Organization")
                user.country?.let { DetailRow("Country", it) }
                user.city?.let { DetailRow("City", it) }
                user.organization?.let { DetailRow("Organization", it) }
            }

            // Metadata Section
            SectionTitle("Metadata")
            DetailRow("Registered", user.registrationTimeSeconds.toRelativeTime())
            DetailRow("Last Online", user.lastOnlineTimeSeconds.toRelativeTime())
            DetailRow("Last Synced", user.lastSync.toRelativeTime())
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Error message
        errorMessage?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        try {
                            errorMessage = null
                            viewModel.deleteUser(user.handle)
                            onNavigateBack()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to delete user"
                        }
                    }
                },
                enabled = !isSyncing,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text("Delete")
            }

            Button(
                onClick = {
                    if (isSyncUserEnabled) {
                        scope.launch {
                            try {
                                isSyncing = true
                                errorMessage = null
                                analyticsService.logUserSyncedFromDetails(user.handle)
                                crashlyticsService.log("Action: Sync user from details (handle=${user.handle})")
                                viewModel.fetchUser(user.handle)
                                isSyncing = false
                            } catch (e: Exception) {
                                isSyncing = false
                                errorMessage = e.message ?: "Failed to sync user"
                                crashlyticsService.logException(e)
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Feature disabled")
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = "Sync",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Sync")
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
