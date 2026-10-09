package com.edumind.app.repository

import com.edumind.app.data.local.prefs.TokenManager
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
import com.edumind.app.data.remote.api.AuthApiService
import com.edumind.app.util.ApiErrorParser
import com.edumind.app.util.NetworkException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun register(fullName: String, email: String, pass: String): Result<RegisterResponse>
    suspend fun verifyOtp(email: String, otp: String, purpose: String): Result<VerifyOtpResponse>
    suspend fun resendOtp(email: String, purpose: String): Result<ResendOtpResponse>
    suspend fun login(email: String, pass: String): Result<LoginResponse>
    suspend fun logout(): Result<Unit>
    suspend fun forgotPassword(email: String): Result<MessageResponse>
    suspend fun resetPassword(token: String, newPass: String): Result<MessageResponse>
    suspend fun refreshToken(): Result<RefreshResponse>
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun register(fullName: String, email: String, pass: String): Result<RegisterResponse> {
        return try {
            val response = authApiService.register(RegisterRequest(fullName, email, pass))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(ApiErrorParser.parseError(response.code(), response.errorBody()))
            }
        } catch (e: IOException) {
            Result.failure(NetworkException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(email: String, otp: String, purpose: String): Result<VerifyOtpResponse> {
        return try {
            val response = authApiService.verifyOtp(VerifyOtpRequest(email, otp, purpose))
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                // Nếu xác thực đăng ký -> Auto lưu token đăng nhập luôn
                if (!data.accessToken.isNullOrBlank() && !data.refreshToken.isNullOrBlank()) {
                    tokenManager.saveTokens(data.accessToken, data.refreshToken)
                }
                Result.success(data)
            } else {
                Result.failure(ApiErrorParser.parseError(response.code(), response.errorBody()))
            }
        } catch (e: IOException) {
            Result.failure(NetworkException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resendOtp(email: String, purpose: String): Result<ResendOtpResponse> {
        return try {
            val response = authApiService.resendOtp(ResendOtpRequest(email, purpose))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(ApiErrorParser.parseError(response.code(), response.errorBody()))
            }
        } catch (e: IOException) {
            Result.failure(NetworkException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, pass: String): Result<LoginResponse> {
        return try {
            val response = authApiService.login(LoginRequest(email, pass))
            if (response.isSuccessful && response.body() != null) {
                val loginData = response.body()!!
                tokenManager.saveTokens(loginData.accessToken, loginData.refreshToken)
                Result.success(loginData)
            } else {
                Result.failure(ApiErrorParser.parseError(response.code(), response.errorBody()))
            }
        } catch (e: IOException) {
            Result.failure(NetworkException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
            authApiService.logout(LogoutRequest(refreshToken))
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            // Best-effort: Nếu server lỗi hoặc mất mạng, client vẫn xóa token local
            tokenManager.clearTokens()
            Result.success(Unit)
        }
    }

    override suspend fun forgotPassword(email: String): Result<MessageResponse> {
        return try {
            val response = authApiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(ApiErrorParser.parseError(response.code(), response.errorBody()))
            }
        } catch (e: IOException) {
            Result.failure(NetworkException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(token: String, newPass: String): Result<MessageResponse> {
        return try {
            val response = authApiService.resetPassword(ResetPasswordRequest(token, newPass))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(ApiErrorParser.parseError(response.code(), response.errorBody()))
            }
        } catch (e: IOException) {
            Result.failure(NetworkException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun refreshToken(): Result<RefreshResponse> {
    return try {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            return Result.failure(Exception("No refresh token"))
        }
        val response = authApiService.refreshAsync(RefreshRequest(refreshToken))
        if (response.isSuccessful && response.body() != null) {
            val data = response.body()!!
            tokenManager.saveTokens(data.accessToken, data.refreshToken)
            Result.success(data)
        } else {
            Result.failure(ApiErrorParser.parseError(response.code(), response.errorBody()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
}
