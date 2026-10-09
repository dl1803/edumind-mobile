package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

// Thông tin người dùng cơ bản
data class UserProfile(
    @SerializedName("id")
    val id: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("avatar_url")
    val avatarUrl: String?,
    @SerializedName("role")
    val role: String // "student" hoặc "admin"
)

// Response Đăng nhập
data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("token_type")
    val tokenType: String = "Bearer",
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("user")
    val user: UserProfile
)

// Response Đăng ký
data class RegisterResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("otp_expires_in")
    val otpExpiresIn: Int = 300
)

// Response Xác thực OTP
data class VerifyOtpResponse(
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("refresh_token")
    val refreshToken: String?,
    @SerializedName("token_type")
    val tokenType: String = "Bearer",
    @SerializedName("expires_in")
    val expiresIn: Int?,
    @SerializedName("reset_token")
    val resetToken: String?
)

// Response Gửi lại OTP
data class ResendOtpResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("otp_expires_in")
    val otpExpiresIn: Int
)

// Response thông báo chung
data class MessageResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("otp_expires_in")
    val otpExpiresIn: Int? = null
)

// Cấu trúc lỗi chuẩn hệ thống từ Backend (FastAPI)
data class ApiErrorWrapper(
    @SerializedName("error")
    val error: ApiErrorDetail
)

data class ApiErrorDetail(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("details")
    val details: Map<String, Any>? = null
)
