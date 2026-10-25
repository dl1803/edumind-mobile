package com.edumind.app.ui.profile

import com.edumind.app.util.Validators

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
        get() = Validators.hasMinLength(newPassword)

    val hasUppercase: Boolean
        get() = Validators.hasUppercase(newPassword)

    val hasLowercase: Boolean
        get() = Validators.hasLowercase(newPassword)

    val hasUpperAndLower: Boolean
        get() = hasUppercase && hasLowercase

    val hasDigit: Boolean
        get() = Validators.hasDigit(newPassword)

    val hasSpecialChar: Boolean
        get() = Validators.hasSpecialChar(newPassword)

    val hasLetterAndDigit: Boolean
        get() = (hasUppercase || hasLowercase) && hasDigit

    val isConfirmMatching: Boolean
        get() = confirmPassword.isNotEmpty() && confirmPassword == newPassword

    val isNewPasswordValid: Boolean
        get() = hasMinLength && hasUpperAndLower && hasDigit && hasSpecialChar

    val isSameAsOldPassword: Boolean
        get() = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && currentPassword == newPassword

    val isSaveEnabled: Boolean
        get() = currentPassword.isNotEmpty() &&
                isNewPasswordValid &&
                isConfirmMatching &&
                !isSameAsOldPassword &&
                !isLoading
}
