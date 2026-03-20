package com.dush1729.cfseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dush1729.cfseeker.data.local.AppPreferences
import com.dush1729.cfseeker.data.remote.api.CodeforcesApi
import com.dush1729.cfseeker.data.remote.api.safeApiCall
import com.dush1729.cfseeker.data.remote.firestore.FirestoreService
import com.dush1729.cfseeker.platform.ioDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface ProfileState {
    data object NotSignedIn : ProfileState
    data class SignedIn(val handle: String) : ProfileState
    data class Verifying(val handle: String, val verificationCode: String) : ProfileState
    data object Loading : ProfileState
}

sealed interface VerificationResult {
    data object Success : VerificationResult
    data class NameMismatch(val expected: String, val actual: String?) : VerificationResult
    data class NetworkError(val message: String) : VerificationResult
}

class ProfileViewModel(
    private val api: CodeforcesApi,
    private val appPreferences: AppPreferences,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState = _profileState.asStateFlow()

    private val _verificationResult = MutableStateFlow<VerificationResult?>(null)
    val verificationResult = _verificationResult.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying = _isVerifying.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val handle = appPreferences.getSignedInHandle()
            _profileState.value = if (handle != null) {
                ProfileState.SignedIn(handle)
            } else {
                ProfileState.NotSignedIn
            }
        }
    }

    fun startVerification(handle: String) {
        val code = "cfseeker_" + Random.nextInt(100000, 999999)
        _verificationResult.value = null
        _profileState.value = ProfileState.Verifying(handle, code)
    }

    fun cancelVerification() {
        _verificationResult.value = null
        _profileState.value = ProfileState.NotSignedIn
    }

    fun verify() {
        val state = _profileState.value
        if (state !is ProfileState.Verifying) return

        viewModelScope.launch(ioDispatcher) {
            _isVerifying.value = true
            _verificationResult.value = null
            try {
                val response = safeApiCall { api.getUser(state.handle) }
                val user = response.result?.firstOrNull()
                if (user == null) {
                    _verificationResult.value = VerificationResult.NetworkError("User not found")
                } else if (user.firstName == state.verificationCode) {
                    appPreferences.setSignedInHandle(state.handle)
                    try {
                        firestoreService.registerUser(state.handle)
                    } catch (_: Exception) {
                        // Non-blocking: registration is best-effort
                    }
                    _profileState.value = ProfileState.SignedIn(state.handle)
                    _verificationResult.value = VerificationResult.Success
                } else {
                    _verificationResult.value = VerificationResult.NameMismatch(
                        expected = state.verificationCode,
                        actual = user.firstName
                    )
                }
            } catch (e: Exception) {
                _verificationResult.value = VerificationResult.NetworkError(
                    e.message ?: "Unknown error"
                )
            } finally {
                _isVerifying.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch(ioDispatcher) {
            appPreferences.setSignedInHandle(null)
            _profileState.value = ProfileState.NotSignedIn
            _verificationResult.value = null
        }
    }
}
