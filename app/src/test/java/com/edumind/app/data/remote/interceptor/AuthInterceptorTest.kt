package com.edumind.app.data.remote.interceptor

import com.edumind.app.data.local.prefs.TokenManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.*
import org.junit.Test

class AuthInterceptorTest {

    private val tokenManager: TokenManager = mockk()
    private val interceptor = AuthInterceptor(tokenManager)
    private val chain: Interceptor.Chain = mockk()

    @Test
    fun intercept_addsBearerHeader_whenAccessTokenAvailable() {
        every { tokenManager.getAccessToken() } returns "my_access_token"

        val request = Request.Builder().url("https://api.edumind.com/courses").build()
        every { chain.request() } returns request

        val requestSlot = slot<Request>()
        every { chain.proceed(capture(requestSlot)) } returns Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        interceptor.intercept(chain)

        assertEquals("Bearer my_access_token", requestSlot.captured.header("Authorization"))
    }

    @Test
    fun intercept_removesNoAuthHeader_andOmitsAuthorization() {
        val request = Request.Builder()
            .url("https://api.edumind.com/auth/login")
            .header("No-Auth", "true")
            .build()
        every { chain.request() } returns request

        val requestSlot = slot<Request>()
        every { chain.proceed(capture(requestSlot)) } returns Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        interceptor.intercept(chain)

        assertNull(requestSlot.captured.header("Authorization"))
        assertNull(requestSlot.captured.header("No-Auth"))
    }
}
