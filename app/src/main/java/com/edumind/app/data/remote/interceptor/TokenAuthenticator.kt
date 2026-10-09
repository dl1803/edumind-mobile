package com.edumind.app.data.remote.interceptor

import com.edumind.app.data.local.prefs.TokenManager
import com.edumind.app.data.model.request.RefreshRequest
import com.edumind.app.data.remote.api.AuthApiService
import com.edumind.app.util.SessionEventBus
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    @Named("refresh") private val authApiService: AuthApiService,
    private val sessionEventBus: SessionEventBus
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Chống lặp vô hạn (chỉ cho phép retry tối đa 1 lần tiếp theo)
        if (responseCount(response) >= 2) {
            return null
        }

        // Không xử lý nếu request bị 401 không có header Authorization (ví dụ đăng nhập sai)
        val currentHeader = response.request.header("Authorization") ?: return null
        val currentToken = currentHeader.removePrefix("Bearer ").trim()

        synchronized(lock) {
            val latestAccessToken = tokenManager.getAccessToken()

            // Nếu luồng khác đã vừa refresh xong và lưu token mới -> retry ngay lập tức
            if (!latestAccessToken.isNullOrBlank() && latestAccessToken != currentToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latestAccessToken")
                    .build()
            }

            // Nếu chưa có luồng nào refresh, tiến hành gọi API refresh
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                handleSessionExpired()
                return null
            }

            return try {
                val refreshCall = authApiService.refresh(RefreshRequest(refreshToken))
                val refreshResponse = refreshCall.execute()

                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    val newTokens = refreshResponse.body()!!
                    tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()
                } else {
                    handleSessionExpired()
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun handleSessionExpired() {
        tokenManager.clearTokens()
        sessionEventBus.emitSessionExpired()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
