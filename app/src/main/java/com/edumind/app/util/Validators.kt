package com.edumind.app.util

import java.util.regex.Pattern

object Validators {
    private val EMAIL_PATTERN = Pattern.compile(
        "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"
    )

    // Regex mật khẩu: Tối thiểu 8 ký tự, gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt
    private val STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    )

    fun validateFullName(fullName: String): ValidationResult {
        val trimmed = fullName.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "Vui lòng nhập họ và tên.")
            trimmed.length < 2 -> ValidationResult(false, "Họ và tên phải có ít nhất 2 ký tự.")
            trimmed.length > 100 -> ValidationResult(false, "Họ và tên không được vượt quá 100 ký tự.")
            else -> ValidationResult(true)
        }
    }


    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "Vui lòng nhập địa chỉ email.")
            !EMAIL_PATTERN.matcher(trimmed).matches() -> ValidationResult(false, "Định dạng email không hợp lệ.")
            else -> ValidationResult(true)
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult(false, "Vui lòng nhập mật khẩu.")
            !STRONG_PASSWORD_PATTERN.matcher(password).matches() -> ValidationResult(
                false,
                "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt."
            )
            else -> ValidationResult(true)
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isEmpty() -> ValidationResult(false, "Vui lòng nhập lại mật khẩu.")
            password != confirmPassword -> ValidationResult(false, "Mật khẩu xác nhận không trùng khớp.")
            else -> ValidationResult(true)
        }
    }
    // Các hàm kiểm tra tiêu chí con để phục vụ hiển thị Checklist trực quan
    fun hasMinLength(password: String): Boolean = password.length >= 8
    fun hasUppercase(password: String): Boolean = password.any { it.isUpperCase() }
    fun hasLowercase(password: String): Boolean = password.any { it.isLowerCase() }
    fun hasDigit(password: String): Boolean = password.any { it.isDigit() }
    fun hasSpecialChar(password: String): Boolean = password.any { "@$!%*?&".contains(it) }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
