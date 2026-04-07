package com.dush1729.cfseeker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dush1729.cfseeker.data.local.entity.RatedUserEntity
import com.dush1729.cfseeker.navigation.WebViewRoute
import com.dush1729.cfseeker.ui.SearchFilters
import com.dush1729.cfseeker.ui.SearchSortOption
import com.dush1729.cfseeker.ui.SearchViewModel
import com.dush1729.cfseeker.utils.getRatingColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel,
    onMenuClick: (() -> Unit)? = null,
    showMenuBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isCacheLoading by viewModel.isCacheLoading.collectAsStateWithLifecycle()
    val cachedUserCount by viewModel.cachedUserCount.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val countries by viewModel.countries.collectAsStateWithLifecycle()
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    val organizations by viewModel.organizations.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    var showSortMenu by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<RatedUserEntity?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Search") },
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
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        if (filters.hasFilters) {
                            BadgedBox(badge = { Badge { Text(filters.activeCount.toString()) } }) {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filters")
                            }
                        } else {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filters")
                        }
                    }
                    IconButton(onClick = { showDetails = !showDetails }) {
                        Icon(
                            imageVector = if (showDetails) Icons.Filled.Info else Icons.Outlined.Info,
                            contentDescription = if (showDetails) "Hide details" else "Show details"
                        )
                    }
                    FilledTonalButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Sort by ${sortOption.displayName}",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SearchSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    viewModel.setSortOption(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Cache status
            if (isCacheLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = "Loading user database...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            } else if (cachedUserCount > 0) {
                Text(
                    text = "$cachedUserCount rated users cached",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Search bar
            TextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    viewModel.setSearchQuery(newValue)
                    if (newValue.isEmpty()) {
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by handle...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.setSearchQuery("")
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
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

            // Active filter chips
            if (filters.hasFilters) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filters.country.isNotEmpty()) {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.setFilters(filters.copy(country = "")) },
                            label = { Text(filters.country) },
                            trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) }
                        )
                    }
                    if (filters.city.isNotEmpty()) {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.setFilters(filters.copy(city = "")) },
                            label = { Text(filters.city) },
                            trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) }
                        )
                    }
                    if (filters.organization.isNotEmpty()) {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.setFilters(filters.copy(organization = "")) },
                            label = { Text(filters.organization) },
                            trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Results
            if (searchResults.isEmpty() && (searchQuery.isNotBlank() || filters.hasFilters)) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No users found for \"$searchQuery\""
                               else "No users match the selected filters",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val listState = rememberLazyListState()

                val shouldLoadMore by remember {
                    derivedStateOf {
                        val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisibleItem >= searchResults.size - 10
                    }
                }

                LaunchedEffect(shouldLoadMore) {
                    if (shouldLoadMore && searchResults.isNotEmpty()) {
                        viewModel.loadMore()
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = searchResults,
                        key = { it.handle }
                    ) { user ->
                        SearchResultCard(
                            user = user,
                            showDetails = showDetails,
                            onClick = { selectedUser = user },
                            onLongClick = {
                                clipboardManager.setText(AnnotatedString(user.handle))
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                    }
                }
            }
        }
    }

    selectedUser?.let { user ->
        UserDetailDialog(
            user = user,
            onDismiss = { selectedUser = null },
            onViewProfile = {
                selectedUser = null
                navController.navigate(
                    WebViewRoute(
                        url = "https://codeforces.com/profile/${user.handle}",
                        title = user.handle
                    )
                )
            }
        )
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filters = filters,
            countries = countries,
            cities = cities,
            organizations = organizations,
            onApply = { newFilters ->
                viewModel.setFilters(newFilters)
                showFilterSheet = false
            },
            onClear = {
                viewModel.clearFilters()
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultCard(
    user: RatedUserEntity,
    showDetails: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = user.handle,
                    style = MaterialTheme.typography.titleMedium,
                    color = getRatingColor(user.rating),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showDetails && !user.country.isNullOrBlank()) {
                    Text(
                        text = user.country,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = user.rating.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = getRatingColor(user.rating)
                )
                if (showDetails && !user.organization.isNullOrBlank()) {
                    Text(
                        text = user.organization,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun UserDetailDialog(
    user: RatedUserEntity,
    onDismiss: () -> Unit,
    onViewProfile: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!user.titlePhoto.isNullOrBlank()) {
                    AsyncImage(
                        model = user.titlePhoto,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.handle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = getRatingColor(user.rating)
                    )
                    val fullName = listOfNotNull(user.firstName, user.lastName)
                        .joinToString(" ")
                    if (fullName.isNotBlank()) {
                        Text(
                            text = fullName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onViewProfile) {
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "View profile"
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                UserDetailRow("Rank", user.rank ?: "unrated", getRatingColor(user.rating))
                UserDetailRow("Rating", user.rating.toString(), getRatingColor(user.rating))
                if (user.maxRating != null) {
                    UserDetailRow("Max Rating", user.maxRating.toString(), getRatingColor(user.maxRating))
                }
                if (user.maxRank != null) {
                    UserDetailRow("Max Rank", user.maxRank, user.maxRating?.let { getRatingColor(it) })
                }
                if (!user.organization.isNullOrBlank()) {
                    UserDetailRow("Organization", user.organization)
                }
                if (!user.city.isNullOrBlank() || !user.country.isNullOrBlank()) {
                    val location = listOfNotNull(user.city, user.country)
                        .joinToString(", ")
                    UserDetailRow("Location", location)
                }
                UserDetailRow("Contribution", user.contribution.toString())
                UserDetailRow("Friend of", user.friendOfCount.toString())
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun UserDetailRow(label: String, value: String, valueColor: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    filters: SearchFilters,
    countries: List<String>,
    cities: List<String>,
    organizations: List<String>,
    onApply: (SearchFilters) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember(filters) { mutableStateOf(filters) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filters", style = MaterialTheme.typography.titleLarge)
                if (draft.hasFilters) {
                    TextButton(onClick = {
                        draft = SearchFilters()
                        onClear()
                    }) {
                        Text("Clear all")
                    }
                }
            }

            FilterField(
                label = "Country",
                value = draft.country,
                suggestions = countries,
                onValueChange = { draft = draft.copy(country = it) }
            )

            FilterField(
                label = "City",
                value = draft.city,
                suggestions = cities,
                onValueChange = { draft = draft.copy(city = it) }
            )

            FilterField(
                label = "Organization",
                value = draft.organization,
                suggestions = organizations,
                onValueChange = { draft = draft.copy(organization = it) }
            )

            FilledTonalButton(
                onClick = { onApply(draft) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply filters")
            }
        }
    }
}

@Composable
private fun FilterField(
    label: String,
    value: String,
    suggestions: List<String>,
    onValueChange: (String) -> Unit
) {
    var query by remember(value) { mutableStateOf(value) }
    val filtered = remember(query, suggestions) {
        if (query.isBlank()) emptyList()
        else suggestions.filter { it.contains(query, ignoreCase = true) }.take(50)
    }

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (it.isEmpty()) onValueChange("")
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        onValueChange("")
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }
                }
            }
        )
        if (filtered.isNotEmpty() && query != value) {
            LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                items(filtered) { suggestion ->
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(onClick = {
                                query = suggestion
                                onValueChange(suggestion)
                            })
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
