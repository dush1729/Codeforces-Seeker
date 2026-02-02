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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dush1729.cfseeker.R
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.local.entity.UserEntity
import com.dush1729.cfseeker.ui.UserViewModel
import com.dush1729.cfseeker.ui.theme.RatingNegative
import com.dush1729.cfseeker.ui.theme.RatingPositive
import com.dush1729.cfseeker.utils.getRatingBackgroundColors
import com.dush1729.cfseeker.utils.getRatingColor
import com.dush1729.cfseeker.utils.toFormattedDate
import com.dush1729.cfseeker.utils.toRelativeTime
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// TODO: Decouple ViewModel here
fun UserDetailsScreen(
    handle: String,
    navController: NavController,
    viewModel: UserViewModel,
    analyticsService: AnalyticsService,
    crashlyticsService: CrashlyticsService,
    modifier: Modifier = Modifier
) {
    val user by viewModel.getUserByHandle(handle).collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }

    // Track screen view
    LaunchedEffect(Unit) {
        analyticsService.logScreenView("user_details")
        crashlyticsService.log("Screen: UserDetails (handle=$handle)")
    }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text(user?.handle ?: "User Details") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        user?.let { currentUser ->
            UserDetailsContent(
                user = currentUser,
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                onNavigateBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                },
                analyticsService = analyticsService,
                crashlyticsService = crashlyticsService,
                modifier = Modifier.padding(paddingValues)
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun UserDetailsContent(
    user: UserEntity,
    viewModel: UserViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    analyticsService: AnalyticsService,
    crashlyticsService: CrashlyticsService,
    modifier: Modifier = Modifier
) {
    var isSyncing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val isSyncUserEnabled = remember { viewModel.isSyncUserEnabled() }
    val ratingChanges by viewModel.getRatingChangesByHandle(user.handle, searchQuery).collectAsStateWithLifecycle(initialValue = emptyList())

    val tabs = listOf("Info", "Ratings")

    Column(
        modifier = modifier
            .fillMaxSize()
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
                    fontWeight = FontWeight.Bold,
                    color = getRatingColor(user.rating)
                )
                user.rank?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = getRatingColor(user.rating)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

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
        Text(
            text = "Last sync: ${user.lastSync.toRelativeTime()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tab content
        when (selectedTabIndex) {
            0 -> {
                // Info Tab
                InfoContent(
                    user = user,
                    ratingChanges = ratingChanges,
                    modifier = Modifier.weight(1f)
                )
            }
            1 -> {
                // Ratings Tab
                RatingsContent(
                    ratingChanges = ratingChanges,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    modifier = Modifier.weight(1f)
                )
            }
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
                            viewModel.deleteUser(user.handle)
                            onNavigateBack()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to delete user"
                        }
                    }
                },
                enabled = !isSyncing,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text("Delete")
            }

            Button(
                onClick = {
                    if (isSyncUserEnabled) {
                        scope.launch {
                            try {
                                isSyncing = true
                                errorMessage = null
                                analyticsService.logUserSyncedFromDetails(user.handle)
                                crashlyticsService.log("Action: Sync user from details (handle=${user.handle})")
                                viewModel.fetchUser(user.handle)
                                isSyncing = false
                            } catch (e: Exception) {
                                isSyncing = false
                                errorMessage = e.message ?: "Failed to sync user"
                                crashlyticsService.logException(e)
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Feature disabled")
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = "Sync",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Sync")
                }
            }
        }
    }
}

@Composable
private fun InfoContent(
    user: UserEntity,
    ratingChanges: List<com.dush1729.cfseeker.data.local.entity.RatingChangeEntity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
        user.rating?.let { DetailRow("Rating", it.toString(), getRatingColor(it)) }
        user.maxRating?.let { DetailRow("Max Rating", it.toString(), getRatingColor(it)) }
        user.maxRank?.let { DetailRow("Max Rank", it, getRatingColor(user.maxRating)) }
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

        // Rating Chart Section
        if (ratingChanges.isNotEmpty()) {
            SectionTitle("Rating History")
            RatingChart(
                ratingChanges = ratingChanges,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RatingChart(
    ratingChanges: List<com.dush1729.cfseeker.data.local.entity.RatingChangeEntity>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current.density

    // Rating changes come in descending order, reverse to get ascending (oldest to newest)
    val sortedChanges = remember(ratingChanges) { ratingChanges.reversed() }

    // Initialize with the last (most recent) contest - never null
    var selectedRatingChange by remember(sortedChanges) {
        mutableStateOf(sortedChanges.last())
    }

    val minRating = remember(sortedChanges) {
        sortedChanges.minOf { minOf(it.oldRating, it.newRating) }
    }
    val maxRating = remember(sortedChanges) {
        sortedChanges.maxOf { maxOf(it.oldRating, it.newRating) }
    }

    val chartHeight = 250.dp
    val pointWidth = 40.dp
    val shouldScroll = sortedChanges.size > 50
    val chartWidth = if (shouldScroll) {
        pointWidth * sortedChanges.size
    } else {
        null
    }

    // Smart scroll: handles both initial load and selection changes
    LaunchedEffect(selectedRatingChange) {
        if (shouldScroll) {
            // If selecting the last contest, just scroll to end (common case on load)
            if (selectedRatingChange.contestId == sortedChanges.last().contestId) {
                scope.launch {
                    scrollState.scrollTo(scrollState.maxValue)
                }
            } else if (chartWidth != null) {
                // For other selections, animate to center the point
                val timeRange = if (sortedChanges.size > 1) {
                    (sortedChanges.last().ratingUpdateTimeSeconds - sortedChanges.first().ratingUpdateTimeSeconds).toFloat()
                } else {
                    1f
                }

                val timeOffset = if (timeRange > 0) {
                    (selectedRatingChange.ratingUpdateTimeSeconds - sortedChanges.first().ratingUpdateTimeSeconds) / timeRange
                } else {
                    0f
                }

                // Calculate scroll position (center the selected point in view)
                val totalChartWidth = chartWidth.value * density
                val availableWidth = totalChartWidth
                val pointX = timeOffset * availableWidth

                // Center the point in the viewport
                val targetScroll = (pointX - scrollState.viewportSize / 2f).coerceIn(0f, scrollState.maxValue.toFloat())

                scope.launch {
                    scrollState.animateScrollTo(targetScroll.toInt())
                }
            }
        }
    }

    val uriHandler = LocalUriHandler.current

    Column(modifier = modifier) {
        // Selected contest info display - always visible since selectedRatingChange is non-null
        val currentIndex = sortedChanges.indexOfFirst { it.contestId == selectedRatingChange.contestId }
        val isFirst = currentIndex == 0
        val isLast = currentIndex == sortedChanges.size - 1

        Card(
            onClick = {
                uriHandler.openUri("https://codeforces.com/contest/${selectedRatingChange.contestId}")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = selectedRatingChange.contestName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Rank: ${selectedRatingChange.contestRank}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val delta = selectedRatingChange.newRating - selectedRatingChange.oldRating
                    val deltaText = if (delta > 0) "+$delta" else delta.toString()
                    val deltaColor = getRatingDeltaColor(
                        selectedRatingChange.oldRating,
                        selectedRatingChange.newRating,
                        neutralColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedRatingChange.oldRating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = getRatingColor(selectedRatingChange.oldRating)
                        )
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedRatingChange.newRating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = getRatingColor(selectedRatingChange.newRating)
                        )
                        Text(
                            text = "($deltaText)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = deltaColor
                        )
                    }
                }
                Text(
                    text = selectedRatingChange.ratingUpdateTimeSeconds.toFormattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val buttonModifier = Modifier.weight(1f)

                    NavigationButton(
                        onClick = { selectedRatingChange = sortedChanges.first() },
                        enabled = !isFirst,
                        icon = Icons.Filled.FirstPage,
                        text = "First",
                        modifier = buttonModifier
                    )

                    NavigationButton(
                        onClick = {
                            if (currentIndex > 0) {
                                selectedRatingChange = sortedChanges[currentIndex - 1]
                            }
                        },
                        enabled = !isFirst,
                        icon = Icons.AutoMirrored.Filled.NavigateBefore,
                        text = "Prev",
                        modifier = buttonModifier
                    )

                    NavigationButton(
                        onClick = {
                            if (currentIndex < sortedChanges.size - 1) {
                                selectedRatingChange = sortedChanges[currentIndex + 1]
                            }
                        },
                        enabled = !isLast,
                        icon = Icons.AutoMirrored.Filled.NavigateNext,
                        text = "Next",
                        modifier = buttonModifier,
                        iconFirst = false
                    )

                    NavigationButton(
                        onClick = { selectedRatingChange = sortedChanges.last() },
                        enabled = !isLast,
                        icon = Icons.AutoMirrored.Filled.LastPage,
                        text = "Last",
                        modifier = buttonModifier,
                        iconFirst = false
                    )
                }
            }
        }

        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .then(
                    if (shouldScroll) {
                        Modifier.horizontalScroll(scrollState)
                    } else Modifier
                )
        ) {
            Canvas(
                modifier = Modifier
                    .then(
                        if (chartWidth != null) {
                            Modifier.size(width = chartWidth, height = chartHeight)
                        } else {
                            Modifier.fillMaxSize()
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .pointerInput(sortedChanges) {
                        detectTapGestures { offset ->
                            val fullWidth = size.width.toFloat()
                            val chartStartX = 0f
                            val chartWidth = fullWidth

                            // Calculate time-based positions
                            val timeRange = if (sortedChanges.size > 1) {
                                (sortedChanges.last().ratingUpdateTimeSeconds - sortedChanges.first().ratingUpdateTimeSeconds).toFloat()
                            } else {
                                1f
                            }

                            // Find the closest point to the tap
                            val tapX = offset.x
                            var closestIndex = -1
                            var minDistance = Float.MAX_VALUE

                            for (i in sortedChanges.indices) {
                                val timeOffset = if (timeRange > 0) {
                                    (sortedChanges[i].ratingUpdateTimeSeconds - sortedChanges.first().ratingUpdateTimeSeconds) / timeRange
                                } else {
                                    0f
                                }
                                val pointX = chartStartX + timeOffset * chartWidth
                                val distance = kotlin.math.abs(tapX - pointX)

                                // Only consider points within a reasonable tap radius
                                if (distance < 30.dp.toPx() && distance < minDistance) {
                                    minDistance = distance
                                    closestIndex = i
                                }
                            }

                            // Only update selection if we found a close enough point
                            if (closestIndex >= 0) {
                                selectedRatingChange = sortedChanges[closestIndex]
                            }
                        }
                    }
            ) {
                val fullWidth = size.width
                val fullHeight = size.height

                // No label space needed anymore
                val chartStartX = 0f
                val chartWidth = fullWidth
                val chartHeight = fullHeight

                val ratingRange = (maxRating - minRating).toFloat().coerceAtLeast(1f)
                val padding = ratingRange * 0.1f

                // Get Codeforces rating background colors from centralized source
                val ratingRanges = getRatingBackgroundColors()

                // Draw background color bands for rating ranges
                for (i in ratingRanges.indices) {
                    val (rangeStart, color) = ratingRanges[i]
                    val rangeEnd = if (i < ratingRanges.size - 1) ratingRanges[i + 1].first else Int.MAX_VALUE

                    // Only draw if this range intersects with the visible chart area
                    if (rangeEnd > minRating && rangeStart < maxRating) {
                        val visibleStart = maxOf(rangeStart, minRating)
                        val visibleEnd = minOf(rangeEnd, maxRating)

                        val y1 = chartHeight - ((visibleEnd - minRating + padding) / (ratingRange + 2 * padding)) * chartHeight
                        val y2 = chartHeight - ((visibleStart - minRating + padding) / (ratingRange + 2 * padding)) * chartHeight

                        val paint = Paint().asFrameworkPaint().apply {
                            this.color = color
                            style = android.graphics.Paint.Style.FILL
                        }

                        drawContext.canvas.nativeCanvas.drawRect(
                            chartStartX,
                            y1.coerceAtLeast(0f),
                            fullWidth,
                            y2.coerceAtMost(chartHeight),
                            paint
                        )
                    }
                }

                // Draw grid lines
                val gridPaint = Paint().asFrameworkPaint().apply {
                    color = "#30FFFFFF".toColorInt()
                    strokeWidth = 1f
                }

                // Horizontal grid lines
                for (i in 0..4) {
                    val y = chartHeight * i / 4f
                    drawContext.canvas.nativeCanvas.drawLine(
                        chartStartX, y, fullWidth, y, gridPaint
                    )
                }

                // Calculate time-based positions
                val timeRange = if (sortedChanges.size > 1) {
                    (sortedChanges.last().ratingUpdateTimeSeconds - sortedChanges.first().ratingUpdateTimeSeconds).toFloat()
                } else {
                    1f
                }

                // Draw rating line and segments
                if (sortedChanges.size > 1) {
                    for (i in 0 until sortedChanges.size) {
                        val current = sortedChanges[i]

                        // Calculate x position based on time
                        val timeOffset = if (timeRange > 0) {
                            (current.ratingUpdateTimeSeconds - sortedChanges.first().ratingUpdateTimeSeconds) / timeRange
                        } else {
                            0f
                        }
                        val x = chartStartX + timeOffset * chartWidth
                        val y = chartHeight - ((current.newRating - minRating + padding) / (ratingRange + 2 * padding)) * chartHeight

                        // Determine color based on rating change for this contest
                        val pointColor = getRatingDeltaColor(
                            current.oldRating,
                            current.newRating,
                            neutralColor = Color.Gray
                        )

                        // Check if this is the selected point
                        val isSelected = selectedRatingChange.contestId == current.contestId

                        // Draw point with selection highlight
                        if (isSelected) {
                            // Draw outer white ring for selected point
                            drawCircle(
                                color = Color.White,
                                radius = 7.dp.toPx(),
                                center = Offset(x, y)
                            )
                            // Draw inner colored point
                            drawCircle(
                                color = pointColor,
                                radius = 5.dp.toPx(),
                                center = Offset(x, y)
                            )
                        } else {
                            // Draw normal point
                            drawCircle(
                                color = pointColor,
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }

                        // Draw line segment to next point
                        if (i < sortedChanges.size - 1) {
                            val next = sortedChanges[i + 1]
                            val nextTimeOffset = if (timeRange > 0) {
                                (next.ratingUpdateTimeSeconds - sortedChanges.first().ratingUpdateTimeSeconds) / timeRange
                            } else {
                                0f
                            }
                            val x2 = chartStartX + nextTimeOffset * chartWidth
                            val y2 = chartHeight - ((next.newRating - minRating + padding) / (ratingRange + 2 * padding)) * chartHeight

                            // Draw line segment with neutral color
                            drawLine(
                                color = Color.White.copy(alpha = 0.7f),
                                start = Offset(x, y),
                                end = Offset(x2, y2),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun RatingsContent(
    ratingChanges: List<com.dush1729.cfseeker.data.local.entity.RatingChangeEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Search field
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search contests...") },
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

        // Results
        if (ratingChanges.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "No rating changes available" else "No contests found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = ratingChanges,
                    key = { it.contestId }
                ) { ratingChange ->
                    RatingChangeCard(ratingChange)
                }
            }
        }
    }
}

@Composable
private fun RatingChangeCard(
    ratingChange: com.dush1729.cfseeker.data.local.entity.RatingChangeEntity
) {
    val uriHandler = LocalUriHandler.current
    val ratingDelta = ratingChange.newRating - ratingChange.oldRating
    val deltaColor = getRatingDeltaColor(
        ratingChange.oldRating,
        ratingChange.newRating,
        neutralColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Card(
        onClick = {
            uriHandler.openUri("https://codeforces.com/contest/${ratingChange.contestId}")
        },
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
            Text(
                text = ratingChange.contestName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rank: ${ratingChange.contestRank}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ratingChange.ratingUpdateTimeSeconds.toFormattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ratingChange.oldRating.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = getRatingColor(ratingChange.oldRating)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "to",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ratingChange.newRating.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = getRatingColor(ratingChange.newRating)
                    )
                    Text(
                        text = if (ratingDelta > 0) "+$ratingDelta" else ratingDelta.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = deltaColor
                    )
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
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
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
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun getRatingDeltaColor(
    oldRating: Int,
    newRating: Int,
    neutralColor: Color
): Color {
    val delta = newRating - oldRating
    return when {
        delta > 0 -> RatingPositive
        delta < 0 -> RatingNegative
        else -> neutralColor
    }
}

@Composable
private fun NavigationButton(
    onClick: () -> Unit,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconFirst: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (iconFirst) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(text, style = MaterialTheme.typography.bodySmall)
        } else {
            Text(text, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.size(2.dp))
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
