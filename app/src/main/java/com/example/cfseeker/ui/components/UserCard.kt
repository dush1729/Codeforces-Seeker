package com.example.cfseeker.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cfseeker.R
import com.example.cfseeker.data.local.entity.RatingChangeEntity
import com.example.cfseeker.data.local.entity.UserEntity
import com.example.cfseeker.data.local.entity.UserRatingChanges
import com.example.cfseeker.ui.theme.CFSeekerTheme
import com.example.cfseeker.ui.theme.RatingNegative
import com.example.cfseeker.ui.theme.RatingPositive
import com.example.cfseeker.utils.toRelativeTime

@Composable
fun UserCard(
    userRatingChange: UserRatingChanges,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                model = userRatingChange.user.avatar,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                error = painterResource(id = R.drawable.ic_launcher_foreground)
            )

            val latestRatingChange = userRatingChange.ratingChanges.lastOrNull()
            // User info (handle + last update time)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // User handle - bold, 16sp
                Text(
                    text = userRatingChange.user.handle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val updateText = latestRatingChange?.ratingUpdateTimeSeconds?.toRelativeTime()
                    ?: "No rating update"
                // Last rating update
                Text(
                    text = updateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Rating info (delta + new rating)
            latestRatingChange?.let { ratingChange ->
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val delta = ratingChange.newRating - ratingChange.oldRating
                    val deltaText = if (delta > 0) "+$delta" else delta.toString()
                    val deltaColor = when {
                        delta > 0 -> RatingPositive
                        delta < 0 -> RatingNegative
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    // Rating delta
                    Text(
                        text = deltaText,
                        style = MaterialTheme.typography.titleSmall,
                        color = deltaColor
                    )

                    // New rating
                    Text(
                        text = ratingChange.newRating.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            userRatingChange = UserRatingChanges(
                user = UserEntity(
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
                    lastSync = System.currentTimeMillis()
                ),
                ratingChanges = listOf(
                    RatingChangeEntity(
                        handle = "tourist",
                        contestId = 1234,
                        contestName = "Codeforces Round #XXX",
                        contestRank = 1,
                        oldRating = 3937,
                        newRating = 3979,
                        ratingUpdateTimeSeconds = System.currentTimeMillis() / 1000 - 86400 * 2,
                        lastSync = System.currentTimeMillis()
                    )
                )
            )
        )
    }
}

@Preview(showBackground = true, name = "User Card without Rating Change")
@Composable
private fun UserCardNoRatingPreview() {
    CFSeekerTheme {
        UserCard(
            userRatingChange = UserRatingChanges(
                user = UserEntity(
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
                    lastSync = System.currentTimeMillis()
                ),
                ratingChanges = emptyList()
            )
        )
    }
}

@Preview(showBackground = true, name = "User Card - Dark Theme")
@Composable
private fun UserCardDarkPreview() {
    CFSeekerTheme(darkTheme = true) {
        UserCard(
            userRatingChange = UserRatingChanges(
                user = UserEntity(
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
                    lastSync = System.currentTimeMillis()
                ),
                ratingChanges = listOf(
                    RatingChangeEntity(
                        handle = "petr",
                        contestId = 5678,
                        contestName = "Educational Round",
                        contestRank = 15,
                        oldRating = 3180,
                        newRating = 3150,
                        ratingUpdateTimeSeconds = System.currentTimeMillis() / 1000 - 3600,
                        lastSync = System.currentTimeMillis()
                    )
                )
            )
        )
    }
}
