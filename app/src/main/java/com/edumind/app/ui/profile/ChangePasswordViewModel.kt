package com.edumind.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onCurrentPasswordChange(value: String) {
        _uiState.update { it.copy(currentPassword = value, errorMessage = null) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun toggleCurrentPasswordVisibility() {
        _uiState.update { it.copy(isCurrentPasswordVisible = !it.isCurrentPasswordVisible) }
    }

    fun toggleNewPasswordVisibility() {
        _uiState.update { it.copy(isNewPasswordVisible = !it.isNewPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun changePassword() {
        val state = _uiState.value
        if (!state.isSaveEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            profileRepository.changePassword(
                currentPassword = state.currentPassword,
                newPassword = state.newPassword
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            currentPassword = "",
                            newPassword = "",
                            confirmPassword = ""
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Đổi mật khẩu thất bại. Vui lòng thử lại."
                        )
                    }
                }
            )
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
