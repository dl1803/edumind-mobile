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

enum class RegisterStep {
    FORM,   // Điền biểu mẫu đăng ký
    OTP     // Nhập mã xác nhận 6 số
}

data class RegisterUiState(
    val currentStep: RegisterStep = RegisterStep.FORM,

    // Dữ liệu Form
    val fullName: String = "",
    val fullNameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,

    // Tiêu chí mật khẩu để cập nhật Checklist trực quan
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasDigit: Boolean = false,
    val hasSpecialChar: Boolean = false,

    // Dữ liệu OTP
    val otpCode: String = "",
    val otpError: String? = null,
    val countdownSeconds: Int = 300,
    val isResendEnabled: Boolean = false,
    val isOtpLocked: Boolean = false,
    val lockRemainingSeconds: Long = 0L,

    // Trạng thái chung
    val isLoading: Boolean = false,
    val showExitOtpDialog: Boolean = false,
    val isRegisterSuccess: Boolean = false
) {
    // Nút "Đăng ký" chỉ enable khi các trường không rỗng và không có lỗi validation
    val isFormValid: Boolean
        get() = fullName.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                fullNameError == null &&
                emailError == null &&
                passwordError == null &&
                confirmPasswordError == null &&
                !isLoading

    // Định dạng mm:ss cho bộ đếm 300s
    val countdownFormatted: String
        get() {
            val minutes = countdownSeconds / 60
            val seconds = countdownSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}

sealed interface RegisterUiEvent {
    data object TriggerShake : RegisterUiEvent
    data class ShowToast(val message: String) : RegisterUiEvent
    data class ShowSnackbar(val message: String) : RegisterUiEvent
    data object NavigateToHome : RegisterUiEvent
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<RegisterUiEvent>()
    val eventFlow: SharedFlow<RegisterUiEvent> = _eventFlow.asSharedFlow()

    private var countdownJob: Job? = null
    private var lockJob: Job? = null

    // --- XỬ LÝ NHẬP LIỆU FORM ---

    fun onFullNameChanged(name: String) {
        _uiState.update { it.copy(fullName = name, fullNameError = null) }
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, emailError = null) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = null,
                hasMinLength = Validators.hasMinLength(newPassword),
                hasUppercase = Validators.hasUppercase(newPassword),
                hasLowercase = Validators.hasLowercase(newPassword),
                hasDigit = Validators.hasDigit(newPassword),
                hasSpecialChar = Validators.hasSpecialChar(newPassword)
            )
        }
        // Kiểm tra lại confirm password nếu đã nhập
        if (_uiState.value.confirmPassword.isNotEmpty()) {
            val confirmValidation = Validators.validateConfirmPassword(newPassword, _uiState.value.confirmPassword)
            _uiState.update { it.copy(confirmPasswordError = confirmValidation.errorMessage) }
        }
    }

    fun onConfirmPasswordChanged(newConfirm: String) {
        _uiState.update {
            it.copy(
                confirmPassword = newConfirm,
                confirmPasswordError = null
            )
        }
    }

    fun submitRegisterForm() {
        val state = _uiState.value
        if (state.isLoading) return

        val nameVal = Validators.validateFullName(state.fullName)
        val emailVal = Validators.validateEmail(state.email)
        val passVal = Validators.validatePassword(state.password)
        val confirmVal = Validators.validateConfirmPassword(state.password, state.confirmPassword)

        if (!nameVal.isValid || !emailVal.isValid || !passVal.isValid || !confirmVal.isValid) {
            viewModelScope.launch { _eventFlow.emit(RegisterUiEvent.TriggerShake) }
            _uiState.update {
                it.copy(
                    fullNameError = nameVal.errorMessage,
                    emailError = emailVal.errorMessage,
                    passwordError = passVal.errorMessage,
                    confirmPasswordError = confirmVal.errorMessage
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, emailError = null) }

            val result = authRepository.register(
                fullName = state.fullName.trim(),
                email = state.email.trim(),
                pass = state.password
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentStep = RegisterStep.OTP,
                            otpCode = "",
                            otpError = null
                        )
                    }
                    startCountdownTimer(300)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    handleRegisterError(error)
                }
            )
        }
    }

    private suspend fun handleRegisterError(error: Throwable) {
        when (error) {
            is AppException -> {
                if (error.httpCode == 409 || error.errorCode == "EMAIL_ALREADY_EXISTS") {
                    _eventFlow.emit(RegisterUiEvent.TriggerShake)
                    _uiState.update { it.copy(emailError = "Email này đã được đăng ký. Vui lòng đăng nhập.") }
                } else {
                    _eventFlow.emit(RegisterUiEvent.ShowToast(error.message))
                }
            }
            is NetworkException -> {
                _eventFlow.emit(RegisterUiEvent.ShowToast(error.message))
            }
            else -> {
                val msg = error.localizedMessage ?: "Đã xảy ra lỗi trong quá trình đăng ký."
                _eventFlow.emit(RegisterUiEvent.ShowToast(msg))
            }
        }
    }

    // --- XỬ LÝ XÁC THỰC OTP ---

    fun onOtpCodeChanged(newOtp: String) {
        _uiState.update { it.copy(otpCode = newOtp, otpError = null) }
    }

    fun verifyOtp(otp: String = _uiState.value.otpCode) {
        val state = _uiState.value
        if (state.isLoading || state.isOtpLocked || otp.length != 6) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, otpError = null) }

            val result = authRepository.verifyOtp(
                email = state.email.trim(),
                otp = otp.trim(),
                purpose = "register"
            )

            result.fold(
                onSuccess = {
                    countdownJob?.cancel()
                    _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
                    _eventFlow.emit(RegisterUiEvent.ShowToast("Đăng ký tài khoản thành công!"))
                    _eventFlow.emit(RegisterUiEvent.NavigateToHome)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    handleOtpError(error)
                }
            )
        }
    }

    fun resendOtp() {
        val state = _uiState.value
        if (state.isLoading || !state.isResendEnabled || state.isOtpLocked) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = authRepository.resendOtp(
                email = state.email.trim(),
                purpose = "register"
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, otpCode = "", otpError = null) }
                    _eventFlow.emit(RegisterUiEvent.ShowToast("Mã xác nhận mới đã được gửi đến email."))
                    startCountdownTimer(300)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    val msg = error.localizedMessage ?: "Không thể gửi lại mã OTP."
                    _eventFlow.emit(RegisterUiEvent.ShowToast(msg))
                }
            )
        }
    }

    private suspend fun handleOtpError(error: Throwable) {
        _eventFlow.emit(RegisterUiEvent.TriggerShake)
        when (error) {
            is AppException -> {
                when (error.httpCode) {
                    400 -> {
                        _uiState.update { it.copy(otpError = "Mã xác nhận không đúng. Vui lòng thử lại.") }
                    }
                    429 -> {
                        val msg = error.message.ifBlank { "Bạn đã nhập sai quá số lần cho phép. Vui lòng thử lại sau 15 phút." }
                        _uiState.update {
                            it.copy(
                                isOtpLocked = true,
                                lockRemainingSeconds = 900L,
                                otpError = msg
                            )
                        }
                        startLockTimer(900L)
                    }
                    else -> {
                        _uiState.update { it.copy(otpError = error.message) }
                    }
                }
            }
            is NetworkException -> {
                _eventFlow.emit(RegisterUiEvent.ShowToast(error.message))
            }
            else -> {
                _uiState.update { it.copy(otpError = error.localizedMessage ?: "Mã xác thực không hợp lệ.") }
            }
        }
    }

    private fun startCountdownTimer(seconds: Int) {
        countdownJob?.cancel()
        _uiState.update { it.copy(countdownSeconds = seconds, isResendEnabled = false) }

        countdownJob = viewModelScope.launch {
            var current = seconds
            while (current > 0) {
                delay(1000)
                current--
                _uiState.update { it.copy(countdownSeconds = current) }
            }
            _uiState.update { it.copy(isResendEnabled = true) }
        }
    }

    private fun startLockTimer(seconds: Long) {
        lockJob?.cancel()
        lockJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(lockRemainingSeconds = remaining) }
            }
            _uiState.update { it.copy(isOtpLocked = false, otpError = null) }
        }
    }

    // --- ĐIỀU HƯỚNG & XÁC NHẬN THOÁT ---

    fun requestExitOtp() {
        _uiState.update { it.copy(showExitOtpDialog = true) }
    }

    fun dismissExitOtpDialog() {
        _uiState.update { it.copy(showExitOtpDialog = false) }
    }

    fun confirmExitOtp() {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                currentStep = RegisterStep.FORM,
                showExitOtpDialog = false,
                otpCode = "",
                otpError = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        lockJob?.cancel()
    }
}
