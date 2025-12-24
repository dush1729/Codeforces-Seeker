package com.dush1729.cfseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.dush1729.cfseeker.data.local.entity.UserRatingChanges
import com.dush1729.cfseeker.data.repository.UserRepository
import com.dush1729.cfseeker.ui.base.UiState
import com.dush1729.cfseeker.worker.SyncUsersWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class SortOption(val value: String, val displayName: String) {
    LAST_RATING_UPDATE("LAST_RATING_UPDATE", "default"),
    HANDLE("HANDLE", "handle"),
    RATING("RATING", "rating"),
    LAST_SYNC("LAST_SYNC", "last sync"),
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
    private val workManager: WorkManager
): ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<UserRatingChanges>>>(
        UiState.Success(emptyList()))
    val uiState = _uiState.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.LAST_RATING_UPDATE)
    val sortOption = _sortOption.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val syncProgress = _syncProgress.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_sortOption, _searchQuery) { sortOption, searchQuery ->
                Pair(sortOption, searchQuery)
            }
                .flatMapLatest { (sortOption, searchQuery) ->
                    repository.getAllUserRatingChanges(sortOption.value, searchQuery)
                }
                .flowOn(Dispatchers.IO)
                .catch {
                    _uiState.value = UiState.Error(it.message ?: "")
                }
                .collect {
                    _uiState.value = UiState.Success(it)
                }
        }

        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(SyncUsersWorker.WORK_NAME)
                .collect { workInfos ->
                    val runningWork = workInfos.firstOrNull {
                        it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
                    }

                    val isRunning = runningWork != null
                    _isSyncing.value = isRunning

                    if (runningWork != null) {
                        val current = runningWork.progress.getInt(SyncUsersWorker.KEY_PROGRESS_CURRENT, 0)
                        val total = runningWork.progress.getInt(SyncUsersWorker.KEY_PROGRESS_TOTAL, 0)
                        _syncProgress.value = if (total > 0) Pair(current, total) else null
                        android.util.Log.d("UserViewModel", "Sync progress: $current/$total")
                    } else {
                        _syncProgress.value = null
                    }

                    android.util.Log.d("UserViewModel", "Sync status: isRunning=$isRunning")
                }
        }
    }

    fun setSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    suspend fun fetchUser(handle: String) {
        repository.fetchUser(handle)
    }

    fun deleteUser(handle: String) {
        viewModelScope.launch {
            repository.deleteUser(handle)
        }
    }

    fun syncAllUsers() {
        android.util.Log.d("UserViewModel", "syncAllUsers called")
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncUsersWorker>()
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

        android.util.Log.d("UserViewModel", "Work enqueued with ID: ${syncWorkRequest.id}")
    }
}