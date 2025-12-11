package com.example.cfseeker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.example.cfseeker.R
import com.example.cfseeker.data.local.entity.UserEntity
import com.example.cfseeker.utils.toRelativeTime
import kotlinx.coroutines.launch

@Composable
fun UserDetailsBottomSheet(
    user: UserEntity,
    onSyncClick: suspend () -> Unit,
    onDeleteClick: suspend () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSyncing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
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
                    fontWeight = FontWeight.Bold
                )
                user.rank?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
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
            user.rating?.let { DetailRow("Rating", it.toString()) }
            user.maxRating?.let { DetailRow("Max Rating", it.toString()) }
            user.maxRank?.let { DetailRow("Max Rank", it) }
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
                            onDeleteClick()
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to delete user"
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            isSyncing = true
                            errorMessage = null
                            onSyncClick()
                            isSyncing = false
                        } catch (e: Exception) {
                            isSyncing = false
                            errorMessage = e.message ?: "Failed to sync user"
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSyncing
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
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
private fun DetailRow(label: String, value: String) {
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
            modifier = Modifier.weight(1f)
        )
    }
}
