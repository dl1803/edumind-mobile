package com.edumind.app.ui.auth.forgot

/**
 * Định nghĩa 3 bước trong quy trình Quên mật khẩu
 */
enum class ForgotPasswordStep(val stepIndex: Int, val title: String) {
    EMAIL(1, "Nhập email"),
    OTP(2, "Xác thực mã"),
    NEW_PASSWORD(3, "Mật khẩu mới")
}

data class ForgotPasswordUiState(
    val currentStep: ForgotPasswordStep = ForgotPasswordStep.EMAIL,
    val email: String = "",
    val emailError: String? = null,
    val otp: String = "",
    val otpError: String? = null,
    val resetToken: String? = null,
    val newPassword: String = "",
    val newPasswordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val countdownSeconds: Int = 300,
    val resendCooldownSeconds: Int = 60,
    val isResendEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val isSuccessDialogOpen: Boolean = false,
    val isCancelDialogOpen: Boolean = false,
    val shakeTrigger: Long = 0L
)
