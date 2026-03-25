package com.dush1729.cfseeker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController
import com.dush1729.cfseeker.navigation.WebViewRoute
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dush1729.cfseeker.data.remote.firestore.DailyData
import com.dush1729.cfseeker.data.remote.firestore.DailyProblem
import com.dush1729.cfseeker.ui.DailyUiState
import com.dush1729.cfseeker.ui.DailyViewModel
import com.dush1729.cfseeker.ui.ProfileState
import com.dush1729.cfseeker.ui.ProfileViewModel
import com.dush1729.cfseeker.ui.VerificationResult
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.atStartOfDayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    navController: NavController,
    dailyViewModel: DailyViewModel,
    profileViewModel: ProfileViewModel,
    onMenuClick: (() -> Unit)? = null,
    showMenuBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val uiState by dailyViewModel.uiState.collectAsState()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Daily Challenge") },
                navigationIcon = {
                    if (onMenuClick != null) {
                        IconButton(onClick = onMenuClick) {
                            if (showMenuBadge) {
                                BadgedBox(badge = { Badge() }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                                }
                            } else {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DailyUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is DailyUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { dailyViewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is DailyUiState.Success -> {
                    DailyContent(
                        data = state.data,
                        signedInHandle = state.signedInHandle,
                        profileViewModel = profileViewModel,
                        navController = navController,
                        onSignOut = {
                            profileViewModel.signOut()
                            dailyViewModel.refresh()
                        }
                    )
                }
            }
        }
    }
}

private enum class DailyTab(val label: String) {
    Problems("Problems"),
    Leaderboard("Leaderboard")
}

@Composable
private fun DailyContent(
    data: DailyData,
    signedInHandle: String?,
    profileViewModel: ProfileViewModel,
    navController: NavController,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(DailyTab.Problems) }
    var now by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            now = Clock.System.now()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Profile section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ProfileSection(
                signedInHandle = signedInHandle,
                profileViewModel = profileViewModel,
                onSignOut = onSignOut
            )
        }

        // Segmented button row
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            DailyTab.entries.forEachIndexed { index, tab ->
                SegmentedButton(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = DailyTab.entries.size
                    )
                ) {
                    Text(tab.label)
                }
            }
        }

        // Tab content
        when (selectedTab) {
            DailyTab.Problems -> {
                val nextMidnight = Clock.System.todayIn(TimeZone.UTC)
                    .plus(1, DateTimeUnit.DAY)
                    .atStartOfDayIn(TimeZone.UTC)
                val remaining = nextMidnight - now
                val hours = remaining.inWholeHours
                val minutes = remaining.inWholeMinutes % 60
                val countdownText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

                if (data.problems.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Problems refresh in $countdownText",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        itemsIndexed(data.problems) { _, problem ->
                            ProblemCard(
                                problem = problem,
                                isSolved = data.submissions.any { sub ->
                                    sub.handle.equals(signedInHandle, ignoreCase = true) &&
                                        sub.contestId == problem.contestId &&
                                        sub.index == problem.index
                                },
                                navController = navController
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No daily problems yet.\nCheck back after midnight UTC.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            DailyTab.Leaderboard -> {
                var showCumulative by remember { mutableStateOf(false) }
                val activeLeaderboard = if (showCumulative) data.cumulativeLeaderboard else data.leaderboard
                val activeUpdatedAt = if (showCumulative) data.cumulativeUpdatedAt else data.leaderboardUpdatedAt

                val updatedText = activeUpdatedAt?.let { updatedAtMillis ->
                    val updatedAt = Instant.fromEpochMilliseconds(updatedAtMillis)
                    val since = now - updatedAt
                    when {
                        since.inWholeMinutes < 1 -> "just now"
                        since.inWholeMinutes < 60 -> "${since.inWholeMinutes}m ago"
                        else -> "${since.inWholeHours}h ${since.inWholeMinutes % 60}m ago"
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Today / All Time toggle
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 4.dp)
                    ) {
                        SegmentedButton(
                            selected = !showCumulative,
                            onClick = { showCumulative = false },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Today")
                        }
                        SegmentedButton(
                            selected = showCumulative,
                            onClick = { showCumulative = true },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("All Time")
                        }
                    }

                    if (activeLeaderboard.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            if (updatedText != null) {
                                item {
                                    Text(
                                        text = "Last updated $updatedText",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            itemsIndexed(activeLeaderboard) { index, entry ->
                                LeaderboardRow(
                                    rank = index + 1,
                                    handle = entry.handle,
                                    score = entry.score,
                                    isCurrentUser = entry.handle.equals(signedInHandle, ignoreCase = true)
                                )
                                if (index < activeLeaderboard.lastIndex) {
                                    HorizontalDivider()
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No leaderboard data yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val CF_SETTINGS_URL = "https://codeforces.com/settings/social"

@Composable
private fun ProfileSection(
    signedInHandle: String?,
    profileViewModel: ProfileViewModel,
    onSignOut: () -> Unit
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val verificationResult by profileViewModel.verificationResult.collectAsState()
    val isVerifying by profileViewModel.isVerifying.collectAsState()

    if (signedInHandle != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Signed in as ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = signedInHandle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onSignOut) {
                    Text("Sign Out")
                }
            }
        }
        return
    }

    when (val state = profileState) {
        is ProfileState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        is ProfileState.NotSignedIn -> {
            SignInCard(
                onSignIn = { handle -> profileViewModel.startVerification(handle) }
            )
        }

        is ProfileState.Verifying -> {
            VerificationCard(
                handle = state.handle,
                verificationCode = state.verificationCode,
                isVerifying = isVerifying,
                verificationResult = verificationResult,
                onCancel = { profileViewModel.cancelVerification() },
                onDone = { profileViewModel.verify() }
            )
        }

        is ProfileState.SignedIn -> {
            // Will be handled by signedInHandle != null on next recomposition
        }
    }
}

@Composable
private fun SignInCard(onSignIn: (String) -> Unit) {
    var handle by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign in to participate",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Verify your Codeforces account by temporarily changing your first name.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
            OutlinedTextField(
                value = handle,
                onValueChange = { handle = it.trim() },
                label = { Text("Codeforces Handle") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { if (handle.isNotBlank()) onSignIn(handle) }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onSignIn(handle) },
                enabled = handle.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign In")
            }
        }
    }
}

@Composable
private fun VerificationCard(
    handle: String,
    verificationCode: String,
    isVerifying: Boolean,
    verificationResult: VerificationResult?,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Verify: $handle",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "1. Go to your Codeforces social settings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            OutlinedButton(
                onClick = { uriHandler.openUri(CF_SETTINGS_URL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Codeforces Settings")
            }

            Text(
                text = "2. Change your First Name to:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Card(
                onClick = {
                    clipboardManager.setText(AnnotatedString(verificationCode))
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = verificationCode,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy to clipboard",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = "3. Save the settings, then tap Done below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            when (verificationResult) {
                is VerificationResult.NameMismatch -> {
                    Text(
                        text = "First name doesn't match. Expected \"$verificationCode\" but found \"${verificationResult.actual ?: "(empty)"}\".",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is VerificationResult.NetworkError -> {
                    Text(
                        text = "Error: ${verificationResult.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isVerifying,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onDone,
                    enabled = !isVerifying,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProblemCard(
    problem: DailyProblem,
    isSolved: Boolean,
    navController: NavController
) {
    val url = "https://codeforces.com/contest/${problem.contestId}/problem/${problem.index}"

    Card(
        onClick = { navController.navigate(WebViewRoute(url, problem.name)) },
        modifier = Modifier.fillMaxWidth(),
        colors = if (isSolved) {
            CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val ratingRanges = listOf(
                800 to 1000, 1100 to 1300, 1400 to 1600, 1700 to 1900,
                2000 to 2200, 2300 to 2500, 2600 to 2800, 2900 to 3100, 3200 to 3500
            )
            val range = ratingRanges.find { problem.rating in it.first..it.second }
            val rangeText = range?.let { "${it.first}-${it.second}" } ?: "${problem.rating}"

            Text(
                text = rangeText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(80.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = problem.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${problem.contestId}${problem.index}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSolved) {
                Text(
                    text = "Solved",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    handle: String,
    score: Int,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = handle,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrentUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$score",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
