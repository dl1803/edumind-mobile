package com.edumind.app.ui.profile

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val currentPasswordError: String? = null,
    val errorMessage: String? = null
) {
    val hasMinLength: Boolean
        get() = newPassword.length >= 8

    val hasLetterAndDigit: Boolean
        get() = newPassword.any { it.isLetter() } && newPassword.any { it.isDigit() }

    val isConfirmMatching: Boolean
        get() = confirmPassword.isNotEmpty() && confirmPassword == newPassword

    val isNewPasswordValid: Boolean
        get() = hasMinLength && hasLetterAndDigit

    val isSaveEnabled: Boolean
        get() = currentPassword.isNotEmpty() &&
                isNewPasswordValid &&
                isConfirmMatching &&
                currentPassword != newPassword &&
                !isLoading
}
