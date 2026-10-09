package com.edumind.app.data.remote.api

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
import com.edumind.app.data.model.response.VerifyOtpResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApiService {
    // Refresh token đồng bộ (dành riêng cho TokenAuthenticator)
    @Headers("No-Auth: true")
    @POST("auth/refresh")
    fun refresh(@Body body: RefreshRequest): Call<RefreshResponse>

    // Đăng ký
    @Headers("No-Auth: true")
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    // Xác thực OTP
    @Headers("No-Auth: true")
    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): Response<VerifyOtpResponse>

    // Gửi lại OTP
    @Headers("No-Auth: true")
    @POST("auth/resend-otp")
    suspend fun resendOtp(@Body body: ResendOtpRequest): Response<ResendOtpResponse>

    // Đăng nhập
    @Headers("No-Auth: true")
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    // Refresh token bất đồng bộ
    @Headers("No-Auth: true")
    @POST("auth/refresh")
    suspend fun refreshAsync(@Body body: RefreshRequest): Response<RefreshResponse>

    // Đăng xuất (Yêu cầu Bearer Token)
    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<MessageResponse>

    // Quên mật khẩu
    @Headers("No-Auth: true")
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<MessageResponse>

    // Đặt lại mật khẩu
    @Headers("No-Auth: true")
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<MessageResponse>
}
