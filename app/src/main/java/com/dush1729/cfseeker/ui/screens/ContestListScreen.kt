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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
fun ContestListScreen(
    viewModel: ContestViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedPhase by viewModel.selectedPhase.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val refreshIntervalMinutes = remember { viewModel.getRefreshIntervalMinutes() }

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
                title = { Text("Contests") }
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
                    val nextSync = lastSync + (refreshIntervalMinutes * 60)
                    append(", Next sync: ${nextSync.toRelativeTime()}")
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
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ContestList(
                            contests = state.data,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContestList(
    contests: List<ContestEntity>,
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
            ContestCard(contest = contest)
        }
    }
}

@Composable
private fun EmptyContestsView(
    phase: ContestPhase,
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
            Text(
                text = "No ${phase.displayName.lowercase()} contests",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Check back later for updates",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
