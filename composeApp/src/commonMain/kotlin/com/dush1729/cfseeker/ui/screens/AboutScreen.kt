package com.dush1729.cfseeker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.platform.PlatformActions
import com.dush1729.cfseeker.platform.appVersionName

private const val GITHUB_URL = "https://github.com/dush1729/CF-Seeker"
private const val FEEDBACK_URL = "https://docs.google.com/forms/d/e/1FAIpQLScMAsX0GYBHgGeX0xJQBumuEQDgdamuuJNFWv1ag4FXi19Nng/viewform?usp=dialog"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    analyticsService: AnalyticsService,
    platformActions: PlatformActions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("About") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Feedback Card
            AboutCard(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.BugReport,
                        contentDescription = "Bug Report",
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = "Feedback / Bugs",
                description = "Share feedback or report bugs",
                onClick = {
                    analyticsService.logFeedbackOpened()
                    platformActions.openUrl(FEEDBACK_URL)
                }
            )

            // GitHub Card
            AboutCard(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Code,
                        contentDescription = "GitHub",
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = "GitHub",
                description = "View source code on GitHub",
                onClick = {
                    analyticsService.logGitHubOpened()
                    platformActions.openUrl(GITHUB_URL)
                }
            )

            // Play Store Card
            AboutCard(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rate",
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = "Rate on Play Store",
                description = "Rate and review the app on Play Store",
                onClick = {
                    analyticsService.logPlayStoreOpened("about_screen")
                    platformActions.openPlayStore()
                }
            )

            // Share Card
            AboutCard(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = "Share App",
                description = "Share Codeforces Seeker with others",
                onClick = {
                    analyticsService.logAppShared("about")
                    platformActions.shareText(
                        "Check out Codeforces Seeker on Google Play!\nhttps://play.google.com/store/apps/details?id=com.dush1729.cfseeker"
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Version Info
            Text(
                text = "Version $appVersionName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
