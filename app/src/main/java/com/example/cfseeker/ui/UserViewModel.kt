package com.example.cfseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cfseeker.data.local.entity.RatingChangeEntity
import com.example.cfseeker.data.local.entity.UserEntity
import com.example.cfseeker.data.repository.UserRepository
import com.example.cfseeker.ui.base.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserViewModel @Inject constructor(
    private val repository: UserRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Pair<UserEntity, RatingChangeEntity?>>>>(
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

    fun fetchUser(handle: String) {
        viewModelScope.launch {
            repository.fetchUser(handle)
        }
    }
}