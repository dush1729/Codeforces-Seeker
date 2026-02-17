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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.dush1729.cfseeker.R
import com.dush1729.cfseeker.data.local.view.UserWithLatestRatingChangeView
import com.dush1729.cfseeker.ui.SortOption
import com.dush1729.cfseeker.ui.theme.CFSeekerTheme
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
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.avatar)
                    .size(96)
                    .build(),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                error = painterResource(id = R.drawable.ic_launcher_foreground)
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

@Preview(showBackground = true, name = "User Card with Rating Change")
@Composable
private fun UserCardPreview() {
    CFSeekerTheme {
        UserCard(
            user = UserWithLatestRatingChangeView(
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
                lastSync = System.currentTimeMillis(),
                latestContestId = 1234,
                latestContestName = "Codeforces Round #XXX",
                latestContestRank = 1,
                latestOldRating = 3937,
                latestNewRating = 3979,
                latestRatingUpdateTimeSeconds = System.currentTimeMillis() / 1000 - 86400 * 2,
                isRatingOutdated = false
            )
        )
    }
}

@Preview(showBackground = true, name = "User Card without Rating Change")
@Composable
private fun UserCardNoRatingPreview() {
    CFSeekerTheme {
        UserCard(
            user = UserWithLatestRatingChangeView(
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
    }
}

@Preview(showBackground = true, name = "User Card - Dark Theme")
@Composable
private fun UserCardDarkPreview() {
    CFSeekerTheme(darkTheme = true) {
        UserCard(
            user = UserWithLatestRatingChangeView(
                handle = "petr",
                avatar = null,
                city = null,
                contribution = 50,
                country = "Russia",
                email = null,
                firstName = "Petr",
                friendOfCount = 1000,
                lastName = "Mitrichev",
                lastOnlineTimeSeconds = System.currentTimeMillis() / 1000,
                maxRank = "legendary grandmaster",
                maxRating = 3200,
                organization = null,
                rank = "international grandmaster",
                rating = 3150,
                registrationTimeSeconds = 1234567890,
                titlePhoto = "",
                lastSync = System.currentTimeMillis(),
                latestContestId = 5678,
                latestContestName = "Educational Round",
                latestContestRank = 15,
                latestOldRating = 3180,
                latestNewRating = 3150,
                latestRatingUpdateTimeSeconds = System.currentTimeMillis() / 1000 - 3600,
                isRatingOutdated = false
            )
        )
    }
}
