package com.dush1729.cfseeker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dush1729.cfseeker.data.local.view.UserWithLatestRatingChangeView
import com.dush1729.cfseeker.ui.SortOption
import com.dush1729.cfseeker.ui.theme.RatingNegative
import com.dush1729.cfseeker.ui.theme.RatingPositive
import com.dush1729.cfseeker.utils.getRatingColor
import com.dush1729.cfseeker.utils.toRelativeTime

@Composable
fun UserCard(
    user: UserWithLatestRatingChangeView,
    sortOption: SortOption = SortOption.LAST_RATING_UPDATE,
    onClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick(user.handle) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (user.isRatingOutdated) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            AsyncImage(
                model = user.avatar,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // User info (handle + last update time)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // User handle - bold, 16sp
                Text(
                    text = user.handle,
                    style = MaterialTheme.typography.titleMedium,
                    color = getRatingColor(user.rating),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val (lastUpdateTitle, lastUpdatedTime) = when (sortOption) {
                    SortOption.LAST_SYNC -> {
                        val syncTime = user.lastSync.toRelativeTime()
                        "Last sync" to syncTime
                    }
                    else -> {
                        val updateTime = user.latestRatingUpdateTimeSeconds?.toRelativeTime()
                            ?: "No rating update"
                        "Last rating update" to updateTime
                    }
                }
                Text(
                    text = "$lastUpdateTitle: $lastUpdatedTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Rating info (delta + new rating)
            if (user.latestContestId != null && user.latestOldRating != null && user.latestNewRating != null) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (user.isRatingOutdated) {
                        // Show sync required when user.rating doesn't match latest rating change
                        Text(
                            text = "Sync required",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        // Show rating delta
                        val delta = user.latestNewRating - user.latestOldRating
                        val deltaText = if (delta > 0) "+$delta" else delta.toString()
                        val deltaColor = when {
                            delta > 0 -> RatingPositive
                            delta < 0 -> RatingNegative
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Text(
                            text = deltaText,
                            style = MaterialTheme.typography.titleSmall,
                            color = deltaColor
                        )
                    }

                    // New rating (from user info, which is more up-to-date)
                    Text(
                        text = (user.rating ?: user.latestNewRating).toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = getRatingColor(user.rating ?: user.latestNewRating)
                    )
                }
            }
        }
    }
}
