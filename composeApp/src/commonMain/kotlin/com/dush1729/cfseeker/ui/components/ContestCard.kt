package com.dush1729.cfseeker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dush1729.cfseeker.data.local.entity.ContestEntity
import com.dush1729.cfseeker.utils.toFormattedDate
import com.dush1729.cfseeker.utils.toRelativeTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContestCard(
    contest: ContestEntity,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Contest Name
            Text(
                text = contest.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Time and Relative Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = contest.startTimeSeconds.toFormattedDate(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = getRelativeTimeText(contest),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Duration and Type as chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElevatedSuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = formatDuration(contest.durationSeconds),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                ElevatedSuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = contest.type,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Category,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )

                // Show phase chip only for ongoing contests
                if (contest.phase in listOf("CODING", "PENDING_SYSTEM_TEST", "SYSTEM_TEST")) {
                    ElevatedSuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = formatPhase(contest.phase),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationSeconds: Long): String {
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    return if (minutes > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${hours}h"
    }
}

private fun formatPhase(phase: String): String {
    return phase.replace("_", " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}

private fun getRelativeTimeText(contest: ContestEntity): String {
    return when (contest.phase) {
        "BEFORE" -> "Starts ${contest.startTimeSeconds.toRelativeTime()}"
        "FINISHED" -> {
            val endTime = contest.startTimeSeconds + contest.durationSeconds
            "Ended ${endTime.toRelativeTime()}"
        }
        else -> {
            val endTime = contest.startTimeSeconds + contest.durationSeconds
            "Ends ${endTime.toRelativeTime()}"
        }
    }
}
