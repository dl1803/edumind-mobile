package com.edumind.app.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.AuthRepository
import com.edumind.app.util.AppException
import com.edumind.app.util.NetworkException
import com.edumind.app.util.Validators
import com.edumind.app.util.ClockProvider
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val authRepository: AuthRepository,
    private val clockProvider: ClockProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LoginUiEvent>()
    val eventFlow: SharedFlow<LoginUiEvent> = _eventFlow.asSharedFlow()

    private var failedAttempts: Int = 0
    private var lockedUntilTimestamp: Long = 0L

    fun onEmailChanged(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                emailError = null,
                generalErrorMessage = null
            )
        }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = null,
                generalErrorMessage = null
            )
        }
    }

    fun login() {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        val now = clockProvider.currentTimeMillis()
        if (currentState.isLocked) {
            if (now >= lockedUntilTimestamp) {
                failedAttempts = 0
                lockedUntilTimestamp = 0L
                _uiState.update {
                    it.copy(
                        isLocked = false,
                        lockRemainingSeconds = 0L,
                        generalErrorMessage = null
                    )
                }
            } else {
                val remainingSeconds = ((lockedUntilTimestamp - now) / 1000).coerceAtLeast(1)
                _uiState.update { it.copy(lockRemainingSeconds = remainingSeconds) }
                viewModelScope.launch {
                    _eventFlow.emit(LoginUiEvent.TriggerShake)
                }
                return
            }
        }

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
                    failedAttempts = 0
                    lockedUntilTimestamp = 0L
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = true,
                            isLocked = false,
                            lockRemainingSeconds = 0L
                        )
                    }
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
                        failedAttempts++
                        _eventFlow.emit(LoginUiEvent.TriggerShake)

                        if (failedAttempts >= 5) {
                            lockedUntilTimestamp = clockProvider.currentTimeMillis() + 15 * 60 * 1000L
                            _uiState.update {
                                it.copy(
                                    isLocked = true,
                                    lockRemainingSeconds = 900L,
                                    generalErrorMessage = "Tài khoản bị khóa tạm thời 15 phút do nhập sai 5 lần."
                                )
                            }
                        } else {
                            val errorMsg = "Tài khoản hoặc mật khẩu không chính xác."
                            _uiState.update {
                                it.copy(generalErrorMessage = errorMsg)
                            }
                        }
                    }
                    403 -> {
                        when (error.errorCode) {
                            "EMAIL_NOT_VERIFIED" -> {
                                _uiState.update { it.copy(showUnverifiedSnackbar = true) }
                            }
                            "ACCOUNT_LOCKED" -> {
                                _uiState.update { it.copy(showAccountLockedDialog = true) }
                            }
                            else -> {
                                val msg = error.message
                                _eventFlow.emit(LoginUiEvent.ShowToast(msg))
                                _uiState.update { it.copy(generalErrorMessage = msg) }
                            }
                        }
                    }
                    429 -> {
                        failedAttempts = 5
                        lockedUntilTimestamp = clockProvider.currentTimeMillis() + 15 * 60 * 1000L
                        val msg = error.message.ifBlank { "Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 15 phút." }
                        _eventFlow.emit(LoginUiEvent.TriggerShake)
                        _uiState.update {
                            it.copy(
                                isLocked = true,
                                lockRemainingSeconds = 900L,
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
