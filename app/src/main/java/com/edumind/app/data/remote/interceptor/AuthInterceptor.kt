package com.edumind.app.data.remote.interceptor
import com.edumind.app.data.local.prefs.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Kiểm tra cờ No-Auth (dành cho API công khai hoặc API refresh)
        if (originalRequest.header("No-Auth") != null) {
            val requestWithoutNoAuth = originalRequest.newBuilder()
                .removeHeader("No-Auth")
                .build()
            return chain.proceed(requestWithoutNoAuth)
        }

        // Tự động gắn Bearer Token nếu người dùng đã đăng nhập
        val accessToken = tokenManager.getAccessToken()
        val requestBuilder = originalRequest.newBuilder()

        if (!accessToken.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}
