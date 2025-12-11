package com.dush1729.cfseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dush1729.cfseeker.data.local.entity.UserRatingChanges
import com.dush1729.cfseeker.data.repository.UserRepository
import com.dush1729.cfseeker.ui.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<UserRatingChanges>>>(
        UiState.Success(emptyList()))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository
                .getAllUserRatingChanges()
                .flowOn(Dispatchers.IO)
                .catch {
                    _uiState.value = UiState.Error(it.message ?: "")
                }
                .collect {
                    _uiState.value = UiState.Success(it)
                }
        }
    }

    suspend fun fetchUser(handle: String) {
        repository.fetchUser(handle)
    }

    fun deleteUser(handle: String) {
        viewModelScope.launch {
            repository.deleteUser(handle)
        }
    }
}