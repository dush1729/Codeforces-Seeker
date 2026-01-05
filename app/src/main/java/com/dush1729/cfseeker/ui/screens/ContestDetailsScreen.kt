package com.dush1729.cfseeker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.local.entity.ContestProblemEntity
import com.dush1729.cfseeker.data.local.entity.ContestStandingRowEntity
import com.dush1729.cfseeker.data.remote.model.ProblemResult
import com.dush1729.cfseeker.ui.ContestDetailsViewModel
import com.dush1729.cfseeker.utils.toRelativeTime
import com.google.gson.Gson
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestDetailsScreen(
    contestId: Int,
    contestName: String,
    contestType: String,
    navController: NavController,
    viewModel: ContestDetailsViewModel,
    analyticsService: AnalyticsService,
    crashlyticsService: CrashlyticsService,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val problems by viewModel.getContestProblems(contestId).collectAsStateWithLifecycle(initialValue = emptyList())
    val standings by viewModel.getContestStandings(contestId).collectAsStateWithLifecycle(initialValue = emptyList())
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val refreshIntervalMinutes = remember { viewModel.getRefreshIntervalMinutes() }

    // Track screen view and load last sync time
    LaunchedEffect(Unit) {
        analyticsService.logScreenView("contest_details")
        crashlyticsService.log("Screen: ContestDetails (contestId=$contestId)")
        viewModel.loadLastSyncTime(contestId)
    }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Auto-fetch standings on first load if database is empty
    LaunchedEffect(problems.isEmpty() && standings.isEmpty()) {
        if (problems.isEmpty() && standings.isEmpty()) {
            viewModel.fetchContestStandings(contestId)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text(contestName) },
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
        ContestDetailsContent(
            problems = problems,
            standings = standings,
            lastSyncTime = lastSyncTime,
            refreshIntervalMinutes = refreshIntervalMinutes,
            contestType = contestType,
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun ContestDetailsContent(
    problems: List<ContestProblemEntity>,
    standings: List<ContestStandingRowEntity>,
    lastSyncTime: Long?,
    refreshIntervalMinutes: Long,
    contestType: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Problems", "Standings")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tabs
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, tab ->
                SegmentedButton(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size)
                ) {
                    Text(tab)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Last Sync Time Display
        val syncPrefix = lastSyncTime?.let { "Last sync: ${it.toRelativeTime()}" } ?: "Not synced yet"
        val syncTimeText = "$syncPrefix • Auto syncs every $refreshIntervalMinutes minutes"

        Text(
            text = syncTimeText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search field (only for Standings tab)
        if (selectedTabIndex == 1) {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Search participants...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
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

        // Tab content
        when (selectedTabIndex) {
            0 -> {
                // Problems Tab
                ProblemsContent(
                    problems = problems,
                    modifier = Modifier.weight(1f)
                )
            }
            1 -> {
                // Standings Tab
                StandingsContent(
                    standings = standings,
                    contestType = contestType,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProblemsContent(
    problems: List<ContestProblemEntity>,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    if (problems.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No problems available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = problems,
                key = { "${it.contestId}_${it.index}" }
            ) { problem ->
                ProblemCard(
                    problem = problem,
                    onClick = {
                        val url = if (problem.problemsetName != null) {
                            "https://codeforces.com/problemset/problem/${problem.problemsetName}/${problem.index}"
                        } else {
                            "https://codeforces.com/contest/${problem.contestId}/problem/${problem.index}"
                        }
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                            url.toUri())
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun ProblemCard(
    problem: ContestProblemEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = problem.index,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = problem.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            if (problem.rating != null || problem.points != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    problem.rating?.let {
                        Text(
                            text = "Rating: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    problem.points?.let {
                        Text(
                            text = "Points: ${it.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (problem.tags.isNotEmpty()) {
                Text(
                    text = "Tags: ${problem.tags.replace(",", ", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StandingsContent(
    standings: List<ContestStandingRowEntity>,
    contestType: String,
    modifier: Modifier = Modifier
) {
    if (standings.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No standings available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val showPenalty = contestType == "ICPC"

        Column(modifier = modifier) {
            // Table header
            StandingTableHeader(showPenalty = showPenalty)
            HorizontalDivider()

            LazyColumn(
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(
                    items = standings,
                    key = { "${it.contestId}_${it.rank}" }
                ) { standing ->
                    StandingTableRow(standing = standing, showPenalty = showPenalty)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun StandingTableHeader(showPenalty: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.5f)
        )
        Text(
            text = "Participant",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = "Points",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.8f)
        )
        if (showPenalty) {
            Text(
                text = "Penalty",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.8f)
            )
        }
        Text(
            text = "Solved",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.8f)
        )
    }
}

@Composable
private fun StandingTableRow(standing: ContestStandingRowEntity, showPenalty: Boolean) {
    val gson = remember { Gson() }
    val problemResults = remember(standing.problemResults) {
        try {
            gson.fromJson(standing.problemResults, Array<ProblemResult>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    val solvedCount = problemResults.count { it.points > 0 }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = standing.rank.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(0.5f)
        )
        Text(
            text = standing.memberHandles,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f),
            maxLines = 1
        )
        Text(
            text = standing.points.toInt().toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.8f)
        )
        if (showPenalty) {
            Text(
                text = standing.penalty.toString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.8f)
            )
        }
        Text(
            text = "$solvedCount/${problemResults.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(0.8f)
        )
    }
}
