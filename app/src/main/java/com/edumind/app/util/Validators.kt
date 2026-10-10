package com.edumind.app.util

import java.util.regex.Pattern

object Validators {
    private val EMAIL_PATTERN = Pattern.compile(
        "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"
    )

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
            password.length < 8 -> ValidationResult(false, "Mật khẩu phải có ít nhất 8 ký tự.")
            else -> ValidationResult(true)
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
