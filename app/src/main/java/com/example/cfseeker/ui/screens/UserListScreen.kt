package com.example.cfseeker.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cfseeker.data.local.entity.RatingChangeEntity
import com.example.cfseeker.data.local.entity.UserEntity
import com.example.cfseeker.data.local.entity.UserRatingChanges
import com.example.cfseeker.ui.UserViewModel
import com.example.cfseeker.ui.base.UiState
import com.example.cfseeker.ui.components.ErrorState
import com.example.cfseeker.ui.components.LoadingState
import com.example.cfseeker.ui.components.UserCard
import com.example.cfseeker.ui.theme.CFSeekerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    viewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Users") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Loading -> {
                LoadingState(modifier = Modifier.padding(paddingValues))
            }

            is UiState.Error -> {
                ErrorState(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is UiState.Success -> {
                UserList(
                    users = state.data,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun UserList(
    users: List<UserRatingChanges>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = users,
            key = { it.user.handle }
        ) { userRatingChange ->
            UserCard(userRatingChange = userRatingChange)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserListPreview() {
    CFSeekerTheme {
        UserList(
            users = listOf(
                UserRatingChanges(
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
                        lastSync = System.currentTimeMillis() / 1000
                    ),
                    ratingChanges = listOf(
                        RatingChangeEntity(
                            handle = "tourist",
                            contestId = 1234,
                            contestName = "Codeforces Round #XXX",
                            contestRank = 1,
                            oldRating = 3937,
                            newRating = 3979,
                            ratingUpdateTimeSeconds = System.currentTimeMillis() / 1000 - 86400,
                            lastSync = System.currentTimeMillis()
                        )
                    )
                ),
                UserRatingChanges(
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
        )
    }
}
