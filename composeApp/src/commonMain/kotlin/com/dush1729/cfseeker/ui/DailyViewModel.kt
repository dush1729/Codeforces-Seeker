package com.dush1729.cfseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.remote.firestore.DailyData
import com.dush1729.cfseeker.data.remote.firestore.FirestoreService
import com.dush1729.cfseeker.platform.ioDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

sealed interface DailyUiState {
    data object Loading : DailyUiState
    data class Success(val data: DailyData, val signedInHandle: String?) : DailyUiState
    data class Error(val message: String) : DailyUiState
}

class DailyViewModel(
    private val firestoreService: FirestoreService,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyUiState>(DailyUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadDaily()
    }

    fun refresh() {
        loadDaily()
    }

    private fun loadDaily() {
        viewModelScope.launch(ioDispatcher) {
            try {
                val today = Clock.System.todayIn(TimeZone.UTC).toString()
                val data = firestoreService.getDailyLeaderboard(today)
                val handle = appPreferences.getSignedInHandle()
                _uiState.value = DailyUiState.Success(data, handle)
            } catch (e: Exception) {
                _uiState.value = DailyUiState.Error(e.message ?: "Failed to load daily data")
            }
        }
    }
}
