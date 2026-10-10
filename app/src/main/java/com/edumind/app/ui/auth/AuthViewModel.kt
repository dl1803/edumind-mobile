package com.edumind.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.AuthRepository
import com.edumind.app.util.AppException
import com.edumind.app.util.NetworkException
import com.edumind.app.util.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val generalErrorMessage: String? = null,
    val isLocked: Boolean = false,
    val lockRemainingSeconds: Long = 0L,
    val showUnverifiedSnackbar: Boolean = false,
    val showAccountLockedDialog: Boolean = false,
    val isLoginSuccess: Boolean = false
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && emailError == null && passwordError == null && !isLocked && !isLoading

    val lockCountdownFormatted: String
        get() {
            val minutes = lockRemainingSeconds / 60
            val seconds = lockRemainingSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}

sealed interface LoginUiEvent {
    data object TriggerShake : LoginUiEvent
    data class ShowToast(val message: String) : LoginUiEvent
    data class ShowSnackbar(val message: String) : LoginUiEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LoginUiEvent>()
    val eventFlow: SharedFlow<LoginUiEvent> = _eventFlow.asSharedFlow()

    /**
     * Khi người dùng đang nhập email: chỉ cập nhật giá trị và xóa lỗi đang hiển thị.
     * Không validate ngay khi đang gõ để tránh báo lỗi làm phiền người dùng.
     */
    fun onEmailChanged(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                emailError = null,
                generalErrorMessage = null
            )
        }
    }

    /**
     * Khi người dùng đang nhập mật khẩu: chỉ cập nhật giá trị và xóa lỗi đang hiển thị.
     * Không validate ngay khi đang gõ để tránh báo lỗi làm phiền người dùng.
     */
    fun onPasswordChanged(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = null,
                generalErrorMessage = null
            )
        }
    }

    /**
     * Xử lý đăng nhập: Chỉ validate khi người dùng bấm Đăng nhập.
     */
    fun login() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isLocked) return

        val emailValidation = Validators.validateEmail(currentState.email)
        val passwordValidation = Validators.validatePassword(currentState.password)

        if (!emailValidation.isValid || !passwordValidation.isValid) {
            viewModelScope.launch {
                _eventFlow.emit(LoginUiEvent.TriggerShake)
            }
            _uiState.update {
                it.copy(
                    emailError = emailValidation.errorMessage,
                    passwordError = passwordValidation.errorMessage,
                    generalErrorMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    emailError = null,
                    passwordError = null,
                    generalErrorMessage = null
                )
            }

            val result = authRepository.login(currentState.email.trim(), currentState.password)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                },
                onFailure = { error ->
                    handleLoginError(error)
                }
            )
        }
    }

    private suspend fun handleLoginError(error: Throwable) {
        _uiState.update { it.copy(isLoading = false) }

        when (error) {
            is AppException -> {
                when (error.httpCode) {
                    401 -> {
                        // Kích hoạt rung form, không phát Toast đáy màn hình
                        _eventFlow.emit(LoginUiEvent.TriggerShake)
                        val errorMsg = "Tài khoản hoặc mật khẩu không chính xác."
                        _uiState.update {
                            it.copy(
                                generalErrorMessage = errorMsg
                            )
                        }
                    }
                    403 -> {
                        if (error.errorCode == "EMAIL_NOT_VERIFIED") {
                            _uiState.update { it.copy(showUnverifiedSnackbar = true) }
                        } else if (error.errorCode == "ACCOUNT_LOCKED") {
                            _uiState.update { it.copy(showAccountLockedDialog = true) }
                        } else {
                            val msg = error.message
                            _eventFlow.emit(LoginUiEvent.ShowToast(msg))
                            _uiState.update { it.copy(generalErrorMessage = msg) }
                        }
                    }
                    429 -> {
                        val msg = error.message
                        _eventFlow.emit(LoginUiEvent.TriggerShake)
                        _uiState.update {
                            it.copy(
                                showAccountLockedDialog = true,
                                generalErrorMessage = msg
                            )
                        }
                    }
                    else -> {
                        val msg = error.message
                        _eventFlow.emit(LoginUiEvent.ShowToast(msg))
                        _uiState.update { it.copy(generalErrorMessage = msg) }
                    }
                }
            }
            is NetworkException -> {
                val msg = error.message
                _eventFlow.emit(LoginUiEvent.ShowToast(msg))
                _uiState.update { it.copy(generalErrorMessage = msg) }
            }
            else -> {
                val msg = error.localizedMessage ?: "Đã xảy ra lỗi không xác định."
                _eventFlow.emit(LoginUiEvent.ShowToast(msg))
                _uiState.update { it.copy(generalErrorMessage = msg) }
            }
        }
    }

    fun dismissAccountLockedDialog() {
        _uiState.update { it.copy(showAccountLockedDialog = false) }
    }

    fun dismissUnverifiedSnackbar() {
        _uiState.update { it.copy(showUnverifiedSnackbar = false) }
    }

    fun clearErrors() {
        _uiState.update {
            it.copy(
                emailError = null,
                passwordError = null,
                generalErrorMessage = null
            )
        }
    }
}
