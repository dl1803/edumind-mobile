package com.edumind.app.data.remote.mock

import com.edumind.app.data.model.request.ForgotPasswordRequest
import com.edumind.app.data.model.request.LoginRequest
import com.edumind.app.data.model.request.LogoutRequest
import com.edumind.app.data.model.request.RefreshRequest
import com.edumind.app.data.model.request.RegisterRequest
import com.edumind.app.data.model.request.ResendOtpRequest
import com.edumind.app.data.model.request.ResetPasswordRequest
import com.edumind.app.data.model.request.VerifyOtpRequest
import com.edumind.app.data.model.response.LoginResponse
import com.edumind.app.data.model.response.MessageResponse
import com.edumind.app.data.model.response.RefreshResponse
import com.edumind.app.data.model.response.RegisterResponse
import com.edumind.app.data.model.response.ResendOtpResponse
import com.edumind.app.data.model.response.UserProfile
import com.edumind.app.data.model.response.VerifyOtpResponse
import com.edumind.app.data.remote.api.AuthApiService
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Call
import retrofit2.Response

class FakeAuthApiService : AuthApiService {

    override fun refresh(body: RefreshRequest): Call<RefreshResponse> {
        throw UnsupportedOperationException("Sử dụng bản thật trong OkHttp Authenticator")
    }

    override suspend fun register(body: RegisterRequest): Response<RegisterResponse> {
        delay(800)
        if (body.email == "exists@test.com") {
            return Response.error(
                409,
                """{"error":{"code":"EMAIL_ALREADY_EXISTS","message":"Email này đã được đăng ký. Vui lòng đăng nhập."}}"""
                    .toResponseBody("application/json".toMediaTypeOrNull())
            )
        }
        return Response.success(
            RegisterResponse(
                message = "Đăng ký thành công. Mã OTP đã được gửi đến email.",
                email = body.email,
                otpExpiresIn = 300
            )
        )
    }

    override suspend fun verifyOtp(body: VerifyOtpRequest): Response<VerifyOtpResponse> {
        delay(600)
        return when (body.otpCode) {
            "123456" -> {
                Response.success(
                    VerifyOtpResponse(
                        accessToken = "mock_access_token_xyz",
                        refreshToken = "mock_refresh_token_xyz",
                        tokenType = "Bearer",
                        expiresIn = 900,
                        resetToken = if (body.purpose == "password_reset") "mock_reset_token_123" else null
                    )
                )
            }
            "999999" -> {
                Response.error(
                    429,
                    """{"error":{"code":"OTP_ATTEMPTS_EXCEEDED","message":"Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 15 phút."}}"""
                        .toResponseBody("application/json".toMediaTypeOrNull())
                )
            }
            else -> {
                Response.error(
                    400,
                    """{"error":{"code":"INVALID_OTP","message":"Mã xác nhận không đúng."}}"""
                        .toResponseBody("application/json".toMediaTypeOrNull())
                )
            }
        }
    }

    override suspend fun resendOtp(body: ResendOtpRequest): Response<ResendOtpResponse> {
        delay(500)
        return Response.success(
            ResendOtpResponse(
                message = "Mã OTP mới đã được gửi.",
                otpExpiresIn = 300
            )
        )
    }

    override suspend fun login(body: LoginRequest): Response<LoginResponse> {
        delay(800)
        return when {
            body.email == "user@test.com" && body.password == "Test@1234" -> {
                Response.success(
                    LoginResponse(
                        accessToken = "mock_valid_access_token",
                        refreshToken = "mock_valid_refresh_token",
                        tokenType = "Bearer",
                        expiresIn = 900,
                        user = UserProfile(
                            id = "c3f87b8d-8a21-4a41-b8ef-1f19d3fbc9a1",
                            fullName = "Nguyễn Văn Học Viên",
                            email = body.email,
                            avatarUrl = null,
                            role = "student"
                        )
                    )
                )
            }
            body.email == "unverified@test.com" -> {
                Response.error(
                    403,
                    """{"error":{"code":"EMAIL_NOT_VERIFIED","message":"Tài khoản chưa được xác thực email."}}"""
                        .toResponseBody("application/json".toMediaTypeOrNull())
                )
            }
            body.email == "locked@test.com" -> {
                Response.error(
                    403,
                    """{"error":{"code":"ACCOUNT_LOCKED","message":"Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên."}}"""
                        .toResponseBody("application/json".toMediaTypeOrNull())
                )
            }
            body.email == "rate_limit@test.com" -> {
                Response.error(
                    429,
                    """{"error":{"code":"RATE_LIMIT_EXCEEDED","message":"Quá nhiều yêu cầu. Vui lòng thử lại sau 15 phút."}}"""
                        .toResponseBody("application/json".toMediaTypeOrNull())
                )
            }
            else -> {
                Response.error(
                    401,
                    """{"error":{"code":"INVALID_CREDENTIALS","message":"Email hoặc mật khẩu không đúng."}}"""
                        .toResponseBody("application/json".toMediaTypeOrNull())
                )
            }
        }
    }

    override suspend fun refreshAsync(body: RefreshRequest): Response<RefreshResponse> {
        delay(500)
        return Response.success(
            RefreshResponse(
                accessToken = "mock_new_access_token",
                refreshToken = "mock_new_refresh_token",
                expiresIn = 900
            )
        )
    }

    override suspend fun logout(body: LogoutRequest): Response<MessageResponse> {
        delay(300)
        return Response.success(MessageResponse("Đăng xuất thành công"))
    }

    override suspend fun forgotPassword(body: ForgotPasswordRequest): Response<MessageResponse> {
        delay(600)
        return Response.success(
            MessageResponse(
                message = "Nếu email tồn tại, mã xác nhận sẽ được gửi.",
                otpExpiresIn = 300
            )
        )
    }

    override suspend fun resetPassword(body: ResetPasswordRequest): Response<MessageResponse> {
        delay(800)
        return if (body.resetToken == "mock_reset_token_123") {
            Response.success(MessageResponse("Mật khẩu đã được đặt lại thành công."))
        } else {
            Response.error(
                400,
                """{"error":{"code":"INVALID_RESET_TOKEN","message":"Liên kết đặt lại đã hết hạn. Vui lòng thử lại."}}"""
                    .toResponseBody("application/json".toMediaTypeOrNull())
            )
        }
    }
}
