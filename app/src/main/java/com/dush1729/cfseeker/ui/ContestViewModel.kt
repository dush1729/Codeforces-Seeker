package com.dush1729.cfseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.local.ContestCacheInfo
import com.dush1729.cfseeker.data.local.entity.ContestEntity
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.data.repository.ContestRepository
import com.dush1729.cfseeker.ui.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ContestPhase(val displayName: String, val phase: String?) {
    UPCOMING("Upcoming", "BEFORE"),
    ONGOING("Ongoing", null),  // null = everything else (not BEFORE, not FINISHED)
    PAST("Past", "FINISHED")
}

@HiltViewModel
class ContestViewModel @Inject constructor(
    private val repository: ContestRepository,
    private val crashlyticsService: CrashlyticsService,
    private val remoteConfigService: RemoteConfigService
): ViewModel() {

    private val _selectedPhase = MutableStateFlow(ContestPhase.UPCOMING)
    val selectedPhase = _selectedPhase.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<List<ContestEntity>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime = _lastSyncTime.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_selectedPhase, _searchQuery) { phase, query ->
                Pair(phase, query)
            }
                .flatMapLatest { (contestPhase, searchQuery) ->
                    when (contestPhase) {
                        ContestPhase.UPCOMING -> repository.getUpcomingContests()
                        ContestPhase.ONGOING -> repository.getOngoingContests()
                        ContestPhase.PAST -> repository.getPastContests(searchQuery)
                    }
                }
                .flowOn(Dispatchers.IO)
                .catch { exception ->
                    crashlyticsService.logException(exception)
                    crashlyticsService.setCustomKey("operation", "observe_contests")
                    _uiState.value = UiState.Error(exception.message ?: "Failed to load contests")
                }
                .collect { contests ->
                    _uiState.value = UiState.Success(contests)
                }
        }

        // Load last sync time and auto-refresh if needed
        viewModelScope.launch(Dispatchers.IO) {
            _lastSyncTime.value = repository.getLastSyncTime()
            autoRefreshIfNeeded()
        }
    }

    private suspend fun autoRefreshIfNeeded() {
        val lastSyncTime = repository.getLastSyncTime()
        val currentTime = System.currentTimeMillis() / 1000
        val refreshIntervalMinutes = remoteConfigService.getContestRefreshIntervalMinutes()
        val refreshIntervalSeconds = refreshIntervalMinutes * 60

        if (lastSyncTime == null || (currentTime - lastSyncTime) >= refreshIntervalSeconds) {
            // Auto-refresh if never synced or interval has passed
            refreshContests()
        }
    }

    fun setSelectedPhase(phase: ContestPhase) {
        _selectedPhase.value = phase
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getRefreshIntervalMinutes(): Long {
        return remoteConfigService.getContestRefreshIntervalMinutes()
    }

    fun refreshContests() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                repository.fetchContests()
                _lastSyncTime.value = repository.getLastSyncTime()
                _snackbarMessage.emit("Contests refreshed successfully")
            } catch (e: Exception) {
                crashlyticsService.logException(e)
                crashlyticsService.setCustomKey("operation", "refresh_contests")
                _snackbarMessage.emit("Failed to refresh contests: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    suspend fun getCacheInfo(): ContestCacheInfo {
        return repository.getCacheInfo()
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                repository.clearCache()
                _snackbarMessage.emit("Cache cleared successfully")
            } catch (e: Exception) {
                crashlyticsService.logException(e)
                crashlyticsService.setCustomKey("operation", "clear_contest_cache")
                _snackbarMessage.emit("Failed to clear cache: ${e.message}")
            }
        }
    }
}
