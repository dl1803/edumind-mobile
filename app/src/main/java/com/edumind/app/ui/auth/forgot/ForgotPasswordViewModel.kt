package com.edumind.app.ui.auth.forgot

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.AuthRepository
import com.edumind.app.util.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ForgotPasswordUiState(
            email = savedStateHandle.get<String>(KEY_EMAIL) ?: "",
            currentStep = savedStateHandle.get<String>(KEY_STEP)?.let {
                runCatching { ForgotPasswordStep.valueOf(it) }.getOrNull()
            } ?: ForgotPasswordStep.EMAIL
        )
    )
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    companion object {
        private const val KEY_EMAIL = "forgot_password_email"
        private const val KEY_STEP = "forgot_password_step"
        private const val OTP_COUNTDOWN_TIME = 300
    }

    // --- Xử lý Cập nhật Input ---
    fun onEmailChanged(value: String) {
        savedStateHandle[KEY_EMAIL] = value
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onOtpChanged(value: String) {
        if (value.length <= 6) {
            _uiState.update { it.copy(otp = value, otpError = null, generalError = null) }
        }
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.update { current ->
            val updatedConfirmError = if (current.confirmPassword.isNotEmpty() && current.confirmPassword.length >= value.length) {
                if (current.confirmPassword != value) "Mật khẩu xác nhận không trùng khớp." else null
            } else current.confirmPasswordError

            current.copy(
                newPassword = value,
                newPasswordError = null,
                confirmPasswordError = updatedConfirmError,
                generalError = null
            )
        }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { current ->
            val error = if (value.isNotEmpty() && current.newPassword.isNotEmpty() && value.length >= current.newPassword.length) {
                if (value != current.newPassword) "Mật khẩu xác nhận không trùng khớp." else null
            } else null

            current.copy(
                confirmPassword = value,
                confirmPasswordError = error,
                generalError = null
            )
        }
    }

    // --- Gửi yêu cầu Quên mật khẩu ---
    fun submitEmail() {
        val email = _uiState.value.email.trim()
        val emailValidation = Validators.validateEmail(email)
        if (!emailValidation.isValid) {
            _uiState.update { it.copy(emailError = emailValidation.errorMessage, generalError = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, emailError = null, generalError = null) }
            val result = authRepository.forgotPassword(email)
            result.onSuccess {
                setStep(ForgotPasswordStep.OTP)
                startCountdown()
                _uiState.update { it.copy(isLoading = false) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        emailError = error.message ?: "Email không tồn tại trong hệ thống.",
                        generalError = null
                    )
                }
            }
        }
    }

    // --- Xác thực mã OTP ---
    fun verifyOtp(otpCode: String = _uiState.value.otp) {
        val state = _uiState.value
        if (state.isLoading || otpCode.length != 6) {
            if (otpCode.length != 6) {
                _uiState.update { it.copy(otpError = "Vui lòng nhập đủ 6 chữ số mã OTP.") }
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null, otpError = null) }
            val result = authRepository.verifyOtp(
                email = _uiState.value.email.trim(),
                otp = otpCode,
                purpose = "password_reset"
            )
            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resetToken = response.resetToken ?: "mock_reset_token",
                        otpError = null
                    )
                }
                stopCountdown()
                setStep(ForgotPasswordStep.NEW_PASSWORD)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        otpError = error.message ?: "Mã xác thực không hợp lệ hoặc đã hết hạn.",
                        shakeTrigger = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    fun resendOtp() {
        if (!_uiState.value.isResendEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val result = authRepository.resendOtp(
                email = _uiState.value.email.trim(),
                purpose = "password_reset"
            )
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, otp = "", otpError = null) }
                startCountdown()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = error.message ?: "Không thể gửi lại mã, vui lòng thử lại sau."
                    )
                }
            }
        }
    }

    // --- Đặt lại Mật khẩu mới ---
    fun submitNewPassword() {
        val pass = _uiState.value.newPassword
        val confirm = _uiState.value.confirmPassword
        val token = _uiState.value.resetToken ?: ""

        val passValidation = Validators.validatePassword(pass)
        if (!passValidation.isValid) {
            _uiState.update { it.copy(newPasswordError = passValidation.errorMessage) }
            return
        }

        val confirmValidation = Validators.validateConfirmPassword(pass, confirm)
        if (!confirmValidation.isValid) {
            _uiState.update { it.copy(confirmPasswordError = confirmValidation.errorMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val result = authRepository.resetPassword(token, pass)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccessDialogOpen = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = error.message ?: "Không thể đặt lại mật khẩu. Vui lòng thực hiện lại từ đầu."
                    )
                }
            }
        }
    }

    // --- Điều hướng Back ---
    fun handleBackNavigation(onExitToLogin: () -> Unit) {
        when (_uiState.value.currentStep) {
            ForgotPasswordStep.EMAIL -> onExitToLogin()
            ForgotPasswordStep.OTP -> {
                _uiState.update { it.copy(isCancelDialogOpen = true) }
            }
            ForgotPasswordStep.NEW_PASSWORD -> {
                setStep(ForgotPasswordStep.OTP)
            }
        }
    }

    fun confirmCancelOtp() {
        stopCountdown()
        _uiState.update { it.copy(isCancelDialogOpen = false, otp = "", otpError = null, resendCooldownSeconds = 60) }
        setStep(ForgotPasswordStep.EMAIL)
    }

    fun dismissCancelDialog() {
        _uiState.update { it.copy(isCancelDialogOpen = false) }
    }

    private fun setStep(step: ForgotPasswordStep) {
        savedStateHandle[KEY_STEP] = step.name
        _uiState.update { it.copy(currentStep = step, generalError = null) }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                countdownSeconds = OTP_COUNTDOWN_TIME,
                resendCooldownSeconds = 60,
                isResendEnabled = false
            )
        }
        countdownJob = viewModelScope.launch {
            var timeRemaining = OTP_COUNTDOWN_TIME
            var resendTime = 60
            while (timeRemaining > 0) {
                delay(1000L)
                timeRemaining--
                if (resendTime > 0) {
                    resendTime--
                }
                _uiState.update {
                    it.copy(
                        countdownSeconds = timeRemaining,
                        resendCooldownSeconds = resendTime,
                        isResendEnabled = (resendTime == 0)
                    )
                }
            }
            _uiState.update { it.copy(isResendEnabled = true) }
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopCountdown()
    }
}
