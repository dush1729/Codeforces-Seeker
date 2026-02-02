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
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dush1729.cfseeker.BuildConfig
import com.dush1729.cfseeker.R
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.analytics.DummyAnalyticsService
import com.dush1729.cfseeker.ui.theme.CFSeekerTheme
import com.dush1729.cfseeker.utils.openPlayStore
import com.dush1729.cfseeker.utils.openUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    analyticsService: AnalyticsService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val githubUrl = stringResource(R.string.github_url)
    val feedbackUrl = stringResource(R.string.feedback_form_url)

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
                    openUrl(context, feedbackUrl)
                }
            )

            // GitHub Card
            AboutCard(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = "GitHub",
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = "GitHub",
                description = "View source code on GitHub",
                onClick = {
                    analyticsService.logGitHubOpened()
                    openUrl(context, githubUrl)
                }
            )

            // Play Store Card
            AboutCard(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_play_store),
                        contentDescription = "Rate",
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = "Rate on Play Store",
                description = "Rate and review the app on Play Store",
                onClick = {
                    analyticsService.logPlayStoreOpened("about_screen")
                    openPlayStore(context)
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
                    val shareIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(
                            android.content.Intent.EXTRA_TEXT,
                            "Check out Codeforces Seeker on Google Play!\nhttps://play.google.com/store/apps/details?id=com.dush1729.cfseeker"
                        )
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share via"))
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Version Info
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
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

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    CFSeekerTheme {
        AboutScreen(analyticsService = DummyAnalyticsService)
    }
}
