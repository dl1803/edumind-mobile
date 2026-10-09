package com.edumind.app.data.model.request

import com.google.gson.annotations.SerializedName

// Đăng ký
data class RegisterRequest(
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

// Xác thực OTP
data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp_code")
    val otpCode: String,
    @SerializedName("purpose")
    val purpose: String // "registration" hoặc "password_reset"
)

// Gửi lại OTP
data class ResendOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("purpose")
    val purpose: String // "registration" hoặc "password_reset"
)

// Đăng nhập
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

// Đăng xuất
data class LogoutRequest(
    @SerializedName("refresh_token")
    val refreshToken: String? = null
)

// Quên mật khẩu
data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)

// Đặt lại mật khẩu mới
data class ResetPasswordRequest(
    @SerializedName("reset_token")
    val resetToken: String,
    @SerializedName("new_password")
    val newPassword: String
)
