package com.dush1729.cfseeker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dush1729.cfseeker.ui.UserStats
import com.dush1729.cfseeker.ui.UserViewModel
import com.dush1729.cfseeker.ui.base.UiState
import com.dush1729.cfseeker.ui.computeUserStats
import com.dush1729.cfseeker.ui.verdictLabel
import com.dush1729.cfseeker.utils.getRatingColor
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private sealed interface DuelState {
    data object Idle : DuelState
    data object Loading : DuelState
    data class Error(val message: String) : DuelState
    data class Success(val stats1: UserStats, val stats2: UserStats) : DuelState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuelScreen(
    userViewModel: UserViewModel,
    onMenuClick: () -> Unit,
    showMenuBadge: Boolean,
    modifier: Modifier = Modifier
) {
    val uiState by userViewModel.uiState.collectAsStateWithLifecycle()
    val trackedHandles = remember(uiState) {
        (uiState as? UiState.Success)?.data?.map { it.handle } ?: emptyList()
    }

    var handle1 by remember { mutableStateOf("") }
    var handle2 by remember { mutableStateOf("") }
    var duelState by remember { mutableStateOf<DuelState>(DuelState.Idle) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Duel") },
                navigationIcon = {
                    BadgedBox(badge = { if (showMenuBadge) Badge() }) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            UserHandlePicker(
                value = handle1,
                onValueChange = { handle1 = it },
                label = "Player 1",
                suggestions = if (handle1.isNotEmpty())
                    trackedHandles.filter { it.contains(handle1, ignoreCase = true) && !it.equals(handle1, ignoreCase = true) }
                else emptyList()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Filled.SwapHoriz,
                    contentDescription = "VS",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            UserHandlePicker(
                value = handle2,
                onValueChange = { handle2 = it },
                label = "Player 2",
                suggestions = if (handle2.isNotEmpty())
                    trackedHandles.filter { it.contains(handle2, ignoreCase = true) && !it.equals(handle2, ignoreCase = true) }
                else emptyList()
            )

            Button(
                onClick = {
                    scope.launch {
                        duelState = DuelState.Loading
                        duelState = try {
                            coroutineScope {
                                val d1 = async { userViewModel.fetchUserSubmissions(handle1.trim()) }
                                val d2 = async { userViewModel.fetchUserSubmissions(handle2.trim()) }
                                DuelState.Success(computeUserStats(d1.await()), computeUserStats(d2.await()))
                            }
                        } catch (e: Exception) {
                            DuelState.Error(e.message ?: "Failed to fetch submissions")
                        }
                    }
                },
                enabled = handle1.isNotBlank() && handle2.isNotBlank() && duelState !is DuelState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (duelState is DuelState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fetching…")
                } else {
                    Text("Compare")
                }
            }

            when (val s = duelState) {
                is DuelState.Idle, is DuelState.Loading -> {}
                is DuelState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                        Text(s.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    }
                }
                is DuelState.Success -> {
                    DuelResults(
                        handle1 = handle1.trim(),
                        handle2 = handle2.trim(),
                        stats1 = s.stats1,
                        stats2 = s.stats2,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserHandlePicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        if (suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
            ) {
                suggestions.take(4).forEachIndexed { idx, handle ->
                    Text(
                        text = handle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onValueChange(handle) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = getRatingColor(null)
                    )
                    if (idx < suggestions.size - 1 && idx < 3) HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun DuelResults(
    handle1: String,
    handle2: String,
    stats1: UserStats,
    stats2: UserStats,
    modifier: Modifier = Modifier
) {
    val total1 = stats1.totalSubmissions
    val total2 = stats2.totalSubmissions
    val acCount1 = stats1.verdictCounts.firstOrNull { it.first == "OK" }?.second ?: 0
    val acCount2 = stats2.verdictCounts.firstOrNull { it.first == "OK" }?.second ?: 0
    val acRate1 = if (total1 > 0) acCount1.toFloat() / total1 * 100 else 0f
    val acRate2 = if (total2 > 0) acCount2.toFloat() / total2 * 100 else 0f

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    handle1,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    "VS",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    handle2,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Summary stats
        DuelSectionTitle("Summary")
        DuelStatRow("Submissions", total1.toString(), total2.toString(), numericWinner(total1, total2))
        DuelStatRow("Unique Solved", stats1.uniqueSolved.toString(), stats2.uniqueSolved.toString(), numericWinner(stats1.uniqueSolved, stats2.uniqueSolved))
        DuelStatRow("AC Rate", "${acRate1.roundToInt()}%", "${acRate2.roundToInt()}%", numericWinner(acRate1, acRate2))

        // Verdicts
        val allVerdicts = (stats1.verdictCounts.map { it.first } + stats2.verdictCounts.map { it.first }).distinct()
        if (allVerdicts.isNotEmpty()) {
            DuelSectionTitle("Verdicts")
            allVerdicts.forEach { verdict ->
                val c1 = stats1.verdictCounts.firstOrNull { it.first == verdict }?.second ?: 0
                val c2 = stats2.verdictCounts.firstOrNull { it.first == verdict }?.second ?: 0
                // For verdicts, "OK" higher is better; others lower is better
                val winner = if (verdict == "OK") numericWinner(c1, c2) else numericWinner(c2, c1).let {
                    when (it) { 1 -> 2; 2 -> 1; else -> 0 }
                }
                DuelStatRow(verdictLabel(verdict), c1.toString(), c2.toString(), winner)
            }
        }

        // Languages
        if (stats1.languageCounts.isNotEmpty() || stats2.languageCounts.isNotEmpty()) {
            DuelSectionTitle("Top Languages")
            DuelTwoColumnList(
                items1 = stats1.languageCounts.take(5).map { "${it.first} (${it.second})" },
                items2 = stats2.languageCounts.take(5).map { "${it.first} (${it.second})" }
            )
        }

        // Tags
        if (stats1.tagCounts.isNotEmpty() || stats2.tagCounts.isNotEmpty()) {
            DuelSectionTitle("Top Tags (Accepted)")
            DuelTwoColumnList(
                items1 = stats1.tagCounts.take(8).map { "${it.first} (${it.second})" },
                items2 = stats2.tagCounts.take(8).map { "${it.first} (${it.second})" }
            )
        }

        // Rating distribution
        val allBuckets = (stats1.ratingBuckets.map { it.first } + stats2.ratingBuckets.map { it.first }).distinct().sorted()
        if (allBuckets.isNotEmpty()) {
            DuelSectionTitle("Solved by Rating")
            allBuckets.forEach { bucket ->
                val c1 = stats1.ratingBuckets.firstOrNull { it.first == bucket }?.second ?: 0
                val c2 = stats2.ratingBuckets.firstOrNull { it.first == bucket }?.second ?: 0
                DuelStatRow("$bucket–${bucket + 99}", c1.toString(), c2.toString(), numericWinner(c1, c2))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DuelSectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
    HorizontalDivider()
}

@Composable
private fun DuelStatRow(
    label: String,
    value1: String,
    value2: String,
    winner: Int = 0
) {
    val winColor = getRatingColor(2400) // green-ish color matching GM rank
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            value1,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (winner == 1) FontWeight.Bold else FontWeight.Normal,
            color = if (winner == 1) winColor else MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            modifier = Modifier.weight(1.4f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value2,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (winner == 2) FontWeight.Bold else FontWeight.Normal,
            color = if (winner == 2) winColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DuelTwoColumnList(items1: List<String>, items2: List<String>) {
    val rowCount = maxOf(items1.size, items2.size)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(rowCount) { i ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = items1.getOrNull(i) ?: "",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = items2.getOrNull(i) ?: "",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

private fun numericWinner(v1: Number, v2: Number): Int {
    val d1 = v1.toDouble()
    val d2 = v2.toDouble()
    return when {
        d1 > d2 -> 1
        d2 > d1 -> 2
        else -> 0
    }
}
