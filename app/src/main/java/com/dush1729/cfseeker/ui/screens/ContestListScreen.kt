package com.dush1729.cfseeker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.dush1729.cfseeker.data.local.ContestCacheInfo
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dush1729.cfseeker.data.local.entity.ContestEntity
import com.dush1729.cfseeker.ui.ContestPhase
import com.dush1729.cfseeker.ui.ContestViewModel
import com.dush1729.cfseeker.ui.base.UiState
import com.dush1729.cfseeker.ui.components.ContestCard
import com.dush1729.cfseeker.ui.components.ErrorState
import com.dush1729.cfseeker.ui.components.LoadingState
import com.dush1729.cfseeker.utils.toRelativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// TODO: Decouple ViewModel here
fun ContestListScreen(
    viewModel: ContestViewModel,
    onContestClick: (contestId: Int, contestName: String, contestType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedPhase by viewModel.selectedPhase.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val refreshIntervalMinutes = remember { viewModel.getRefreshIntervalMinutes() }

    var showClearCacheDialog by remember { mutableStateOf(false) }
    var cacheInfo by remember { mutableStateOf(ContestCacheInfo(0, 0, 0)) }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Contests") },
                actions = {
                    FilledTonalIconButton(
                        onClick = {
                            scope.launch {
                                cacheInfo = viewModel.getCacheInfo()
                                showClearCacheDialog = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Clear cache"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Segmented Button Filter
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ContestPhase.entries.forEachIndexed { index, phase ->
                    SegmentedButton(
                        selected = selectedPhase == phase,
                        onClick = { viewModel.setSelectedPhase(phase) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ContestPhase.entries.size
                        )
                    ) {
                        Text(phase.displayName)
                    }
                }
            }

            // Last Sync Time Display
            val syncTimeText = buildString {
                val lastSync = lastSyncTime
                if (lastSync != null) {
                    append("Last sync: ${lastSync.toRelativeTime()}")
                    append(" • Auto syncs every $refreshIntervalMinutes minutes")
                } else {
                    append("Syncing contests...")
                }
            }

            Text(
                text = syncTimeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Sync progress indicator
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Search field (only for past contests)
            if (selectedPhase == ContestPhase.PAST) {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search past contests...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
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

            // Contest List
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
                    if (state.data.isEmpty()) {
                        EmptyContestsView(
                            phase = selectedPhase,
                            searchQuery = searchQuery,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ContestList(
                            contests = state.data,
                            onContestClick = onContestClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("Clear Contest Cache") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This will delete cached contest data:")
                    Text("• Standings: ${cacheInfo.formatSize(cacheInfo.standingSizeBytes)}")
                    Text("• Rating changes: ${cacheInfo.formatSize(cacheInfo.ratingChangeSizeBytes)}")
                    Text(
                        "Total: ${cacheInfo.formatSize(cacheInfo.totalSizeBytes)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Contest list will not be deleted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearCacheDialog = false
                        viewModel.clearCache()
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ContestList(
    contests: List<ContestEntity>,
    onContestClick: (contestId: Int, contestName: String, contestType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = contests,
            key = { it.id }
        ) { contest ->
            ContestCard(
                contest = contest,
                onClick = { onContestClick(contest.id, contest.name, contest.type) }
            )
        }
    }
}

@Composable
private fun EmptyContestsView(
    phase: ContestPhase,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            val (title, subtitle) = if (searchQuery.isEmpty()) {
                "No ${phase.displayName.lowercase()} contests" to "Check back later for updates"
            } else {
                "No contests found" to "Try a different search term"
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
