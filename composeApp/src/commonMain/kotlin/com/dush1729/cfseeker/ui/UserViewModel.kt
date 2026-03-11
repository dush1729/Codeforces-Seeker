package com.dush1729.cfseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.local.view.UserWithLatestRatingChangeView
import com.dush1729.cfseeker.data.repository.UserRepository
import com.dush1729.cfseeker.platform.BackgroundSyncScheduler
import com.dush1729.cfseeker.platform.SyncStatus
import com.dush1729.cfseeker.ui.base.UiState
import com.dush1729.cfseeker.utils.toRelativeTime
import com.dush1729.cfseeker.platform.ioDispatcher
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
import kotlinx.datetime.Clock

class UserViewModel(
    private val repository: UserRepository,
    private val backgroundSyncScheduler: BackgroundSyncScheduler,
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
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _outdatedUserHandles = repository.getOutdatedUserHandles()
        .flowOn(ioDispatcher)
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
        viewModelScope.launch(ioDispatcher) {
            val launchCount = appPreferences.incrementLaunchCount()
            analyticsService.logMilestoneLaunch(launchCount)
        }

        viewModelScope.launch(ioDispatcher) {
            remoteConfigService.fetchAndActivate()
        }

        viewModelScope.launch(ioDispatcher) {
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
                .flowOn(ioDispatcher)
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
            backgroundSyncScheduler.observeSyncStatus()
                .collect { status ->
                    when (status) {
                        is SyncStatus.Running -> {
                            _isSyncing.value = true
                            _syncProgress.value = if (status.total > 0) Pair(status.current, status.total) else null
                            crashlyticsService.log("UserViewModel: Sync progress: ${status.current}/${status.total}")
                            wasRunning = true
                        }
                        is SyncStatus.Succeeded -> {
                            _isSyncing.value = false
                            _syncProgress.value = null
                            if (wasRunning) {
                                _snackbarMessage.emit("All users synced successfully")
                                wasRunning = false
                            }
                        }
                        is SyncStatus.Failed -> {
                            _isSyncing.value = false
                            _syncProgress.value = null
                            if (wasRunning) {
                                _snackbarMessage.emit("Sync failed")
                                wasRunning = false
                            }
                        }
                        is SyncStatus.Idle -> {
                            _isSyncing.value = false
                            _syncProgress.value = null
                        }
                    }
                    crashlyticsService.log("UserViewModel: Sync status: $status")
                }
        }
    }

    private suspend fun autoRefreshIfNeeded() {
        val lastSyncTime = appPreferences.getUsersInfoLastSyncTime()
        val currentTime = Clock.System.now().epochSeconds
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
                    val currentTime = Clock.System.now().epochSeconds
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
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val lastSyncAllTime = appPreferences.getLastSyncAllTime()
        val timeSinceLastSync = currentTime - lastSyncAllTime
        if (timeSinceLastSync < cooldownMillis) {
            val nextSyncTime = (lastSyncAllTime + cooldownMillis) / 1000
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

            val outdatedHandles = _outdatedUserHandles.value
            if (outdatedHandles.isEmpty()) {
                _snackbarMessage.emit("No users need syncing")
                return@launch
            }

            analyticsService.logBulkSyncStarted()
            appPreferences.setLastSyncAllTime(Clock.System.now().toEpochMilliseconds())

            backgroundSyncScheduler.scheduleSyncUsers(outdatedHandles)

            crashlyticsService.log("UserViewModel: Sync scheduled for ${outdatedHandles.size} outdated users")
        }
    }

    fun getUserByHandle(handle: String) = repository.getUserByHandle(handle)
    fun getRatingChangesByHandle(handle: String, searchQuery: String = "") = repository.getRatingChangesByHandle(handle, searchQuery)

    fun isAddUserEnabled() = remoteConfigService.isAddUserEnabled()
    fun isSyncAllUsersEnabled() = remoteConfigService.isSyncAllUsersEnabled()
    fun isSyncUserEnabled() = remoteConfigService.isSyncUserEnabled()

    fun logToCrashlytics(message: String) {
        crashlyticsService.log(message)
    }
}
