package com.example.cfseeker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    var showBottomSheet by remember { mutableStateOf(false) }
    var userHandle by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Users") },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBottomSheet = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add User") },
                text = { Text("Add User") }
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

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                userHandle = ""
            },
        ) {
            AddUserBottomSheet(
                userHandle = userHandle,
                onUserHandleChange = { userHandle = it },
                onAddClick = {
                    viewModel.fetchUser(userHandle)
                    showBottomSheet = false
                    userHandle = ""
                },
                onCancelClick = {
                    showBottomSheet = false
                    userHandle = ""
                }
            )
        }
    }
}

@Composable
private fun AddUserBottomSheet(
    userHandle: String,
    onUserHandleChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add User",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = userHandle,
            onValueChange = onUserHandleChange,
            label = { Text("User Handle") },
            placeholder = { Text("Enter Codeforces handle") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (userHandle.isNotBlank()) {
                        onAddClick()
                    }
                }
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancelClick) {
                Text("Cancel")
            }

            Button(
                onClick = onAddClick,
                enabled = userHandle.isNotBlank(),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Add")
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
