package com.dush1729.cfseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.local.view.UserWithLatestRatingChangeView
import com.dush1729.cfseeker.data.repository.UserRepository
import com.dush1729.cfseeker.ui.base.UiState
import com.dush1729.cfseeker.utils.toRelativeTime
import com.dush1729.cfseeker.worker.SyncUsersWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class SortOption(val value: String, val displayName: String) {
    LAST_RATING_UPDATE("LAST_RATING_UPDATE", "default"),
    HANDLE("HANDLE", "handle"),
    RATING("RATING", "rating"),
    LAST_SYNC("LAST_SYNC", "last sync"),
}

class UserViewModel(
    private val repository: UserRepository,
    private val workManager: WorkManager,
    private val analyticsService: AnalyticsService,
    private val crashlyticsService: CrashlyticsService,
    private val appPreferences: AppPreferences,
    private val remoteConfigService: RemoteConfigService
): ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<UserWithLatestRatingChangeView>>>(
        UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.LAST_RATING_UPDATE)
    val sortOption = _sortOption.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val syncProgress = _syncProgress.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime = _lastSyncTime.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val userCount: StateFlow<Int> = repository.getUserCount()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _outdatedUserHandles = repository.getOutdatedUserHandles()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val outdatedUserCount: StateFlow<Int> = _outdatedUserHandles
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        // Track app launch
        viewModelScope.launch(Dispatchers.IO) {
            val launchCount = appPreferences.incrementLaunchCount()
            analyticsService.logMilestoneLaunch(launchCount)
        }

        // Fetch remote config
        viewModelScope.launch(Dispatchers.IO) {
            remoteConfigService.fetchAndActivate()
        }

        // Load last sync time and auto-refresh if needed
        viewModelScope.launch(Dispatchers.IO) {
            _lastSyncTime.value = appPreferences.getUsersInfoLastSyncTime()
            autoRefreshIfNeeded()
        }

        viewModelScope.launch {
            combine(_sortOption, _searchQuery) { sortOption, searchQuery ->
                Pair(sortOption, searchQuery)
            }
                .flatMapLatest { (sortOption, searchQuery) ->
                    repository.getUsersWithLatestRatingChange(sortOption.value, searchQuery)
                }
                .flowOn(Dispatchers.IO)
                .catch {
                    crashlyticsService.logException(it)
                    crashlyticsService.setCustomKey("sort_option", sortOption.value.displayName)
                    crashlyticsService.setCustomKey("search_query", searchQuery.value)
                    crashlyticsService.setCustomKey("operation", "search_and_sort")
                    _uiState.value = UiState.Error(it.message ?: "")
                }
                .collect {
                    _uiState.value = UiState.Success(it)
                }
        }

        viewModelScope.launch {
            var wasRunning = false
            workManager.getWorkInfosForUniqueWorkFlow(SyncUsersWorker.WORK_NAME)
                .collect { workInfos ->
                    val runningWork = workInfos.firstOrNull {
                        it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
                    }
                    val completedWork = workInfos.firstOrNull {
                        it.state == WorkInfo.State.SUCCEEDED || it.state == WorkInfo.State.FAILED
                    }

                    val isRunning = runningWork != null
                    _isSyncing.value = isRunning

                    if (runningWork != null) {
                        val current = runningWork.progress.getInt(SyncUsersWorker.KEY_PROGRESS_CURRENT, 0)
                        val total = runningWork.progress.getInt(SyncUsersWorker.KEY_PROGRESS_TOTAL, 0)
                        _syncProgress.value = if (total > 0) Pair(current, total) else null
                        crashlyticsService.log("UserViewModel: Sync progress: $current/$total")
                        wasRunning = true
                    } else {
                        _syncProgress.value = null

                        // Show completion message when sync finishes
                        if (wasRunning && completedWork != null) {
                            when (completedWork.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    _snackbarMessage.emit("All users synced successfully")
                                }
                                WorkInfo.State.FAILED -> {
                                    _snackbarMessage.emit("Sync failed")
                                }
                                else -> {}
                            }
                            wasRunning = false
                        }
                    }

                    crashlyticsService.log("UserViewModel: Sync status: isRunning=$isRunning")
                }
        }
    }

    private suspend fun autoRefreshIfNeeded() {
        val lastSyncTime = appPreferences.getUsersInfoLastSyncTime()
        val currentTime = System.currentTimeMillis() / 1000
        val refreshIntervalMinutes = remoteConfigService.getUsersInfoRefreshIntervalMinutes()
        val refreshIntervalSeconds = refreshIntervalMinutes * 60

        if (currentTime - lastSyncTime >= refreshIntervalSeconds) {
            refreshUsersInfo()
        }
    }

    fun getRefreshIntervalMinutes(): Long {
        return remoteConfigService.getUsersInfoRefreshIntervalMinutes()
    }

    private fun refreshUsersInfo() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                val handles = repository.getAllUserHandles()
                if (handles.isNotEmpty()) {
                    repository.fetchUsersInfo(handles)
                    val currentTime = System.currentTimeMillis() / 1000
                    appPreferences.setUsersInfoLastSyncTime(currentTime)
                    _lastSyncTime.value = currentTime
                }
            } catch (e: Exception) {
                crashlyticsService.logException(e)
                crashlyticsService.setCustomKey("operation", "refresh_users_info")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun setSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
        analyticsService.logSortChanged(sortOption.displayName)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    suspend fun fetchUser(handle: String) {
        try {
            repository.fetchUser(handle)
            _snackbarMessage.emit("Successfully synced $handle")
            analyticsService.logUserAdded(handle)
        } catch (e: Exception) {
            crashlyticsService.logException(e)
            crashlyticsService.setCustomKey("user_handle", handle)
            crashlyticsService.setCustomKey("operation", "fetchUser")
            _snackbarMessage.emit("Failed to sync $handle: ${e.message}")
            analyticsService.logUserAddFailed(handle, e.message ?: "unknown")
        }
    }

    fun deleteUser(handle: String) {
        viewModelScope.launch {
            try {
                repository.deleteUser(handle)
                _snackbarMessage.emit("Deleted $handle")
                analyticsService.logUserDeleted(handle)
            } catch (e: Exception) {
                crashlyticsService.logException(e)
                crashlyticsService.setCustomKey("user_handle", handle)
                crashlyticsService.setCustomKey("operation", "delete")
                _snackbarMessage.emit("Failed to delete $handle: ${e.message}")
            }
        }
    }

    suspend fun canSyncAllUsers(): Boolean {
        val cooldownMillis = remoteConfigService.getSyncAllCooldownMinutes() * 60 * 1000
        val currentTime = System.currentTimeMillis()
        val lastSyncAllTime = appPreferences.getLastSyncAllTime()
        val timeSinceLastSync = currentTime - lastSyncAllTime
        if (timeSinceLastSync < cooldownMillis) {
            // Still in cooldown, show remaining time
            val nextSyncTime = (lastSyncAllTime + cooldownMillis) / 1000 // Convert to seconds
            val relativeTime = nextSyncTime.toRelativeTime()
            val message = "You can sync again $relativeTime"

            _snackbarMessage.emit(message)
            crashlyticsService.log("UserViewModel: Sync blocked, cooldown active. Next sync: $relativeTime")
            return false
        }
        return true
    }

    fun syncAllUsers() {
        viewModelScope.launch {
            crashlyticsService.log("UserViewModel: syncAllUsers called")
            if (!canSyncAllUsers()) {
                return@launch
            }

            // Get outdated user handles from cached StateFlow
            val outdatedHandles = _outdatedUserHandles.value
            if (outdatedHandles.isEmpty()) {
                _snackbarMessage.emit("No users need syncing")
                return@launch
            }

            // Cooldown has passed, proceed with sync
            analyticsService.logBulkSyncStarted()

            // Update last sync time
            appPreferences.setLastSyncAllTime(System.currentTimeMillis())

            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncUsersWorker>()
                .setInputData(
                    workDataOf(SyncUsersWorker.KEY_USER_HANDLES to outdatedHandles.toTypedArray())
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            workManager.enqueueUniqueWork(
                SyncUsersWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                syncWorkRequest
            )

            crashlyticsService.log("UserViewModel: Work enqueued with ID: ${syncWorkRequest.id}, syncing ${outdatedHandles.size} outdated users")
        }
    }

    // Get user by handle
    fun getUserByHandle(handle: String) = repository.getUserByHandle(handle)

    // Get rating changes by handle
    fun getRatingChangesByHandle(handle: String, searchQuery: String = "") = repository.getRatingChangesByHandle(handle, searchQuery)

    // Remote Config feature flags
    fun isAddUserEnabled() = remoteConfigService.isAddUserEnabled()
    fun isSyncAllUsersEnabled() = remoteConfigService.isSyncAllUsersEnabled()
    fun isSyncUserEnabled() = remoteConfigService.isSyncUserEnabled()

    // Crashlytics logging
    fun logToCrashlytics(message: String) {
        crashlyticsService.log(message)
    }
}