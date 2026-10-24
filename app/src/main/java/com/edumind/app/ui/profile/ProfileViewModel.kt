package com.edumind.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            profileRepository.getProfile().fold(
                onSuccess = { user ->
                    _uiState.value = ProfileUiState.Success(user = user)
                },
                onFailure = { error ->
                    _uiState.value = ProfileUiState.Error(
                        message = error.localizedMessage ?: "Không thể tải thông tin hồ sơ."
                    )
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is ProfileUiState.Success) {
                _uiState.value = current.copy(isLoggingOut = true)
            }
            profileRepository.logout().fold(
                onSuccess = {
                    _logoutEvent.emit(Unit)
                },
                onFailure = {
                    val fallbackState = _uiState.value
                    if (fallbackState is ProfileUiState.Success) {
                        _uiState.value = fallbackState.copy(isLoggingOut = false)
                    }
                }
            )
        }
    }

    fun retry() {
        loadProfile()
    }
}
