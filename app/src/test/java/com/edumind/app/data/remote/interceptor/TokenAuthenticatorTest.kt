package com.edumind.app.data.remote.interceptor

import com.edumind.app.data.local.prefs.TokenManager
import com.edumind.app.data.model.request.RefreshRequest
import com.edumind.app.data.model.response.RefreshResponse
import com.edumind.app.data.remote.api.AuthApiService
import com.edumind.app.util.SessionEventBus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response as RetrofitResponse

class TokenAuthenticatorTest {

    private val tokenManager: TokenManager = mockk(relaxed = true)
    private val authApiService: AuthApiService = mockk()
    private val sessionEventBus: SessionEventBus = mockk(relaxed = true)
    private lateinit var authenticator: TokenAuthenticator

    @Before
    fun setup() {
        authenticator = TokenAuthenticator(tokenManager, authApiService, sessionEventBus)
    }

    @Test
    fun authenticate_returnsNull_whenNoAuthorizationHeaderPresent() {
        val request = Request.Builder().url("https://api.edumind.com/auth/login").build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        val result = authenticator.authenticate(null, response)
        assertNull(result)
    }

    @Test
    fun authenticate_refreshesAndRetries_whenRefreshIsSuccessful() {
        val request = Request.Builder()
            .url("https://api.edumind.com/user/profile")
            .header("Authorization", "Bearer old_token")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        every { tokenManager.getAccessToken() } returns "old_token"
        every { tokenManager.getRefreshToken() } returns "valid_refresh"

        val call: Call<RefreshResponse> = mockk()
        val refreshDto = RefreshResponse("new_access_token", "new_refresh_token", 900)
        every { authApiService.refresh(RefreshRequest("valid_refresh")) } returns call
        every { call.execute() } returns RetrofitResponse.success(refreshDto)

        val newRequest = authenticator.authenticate(null, response)

        assertNotNull(newRequest)
        assertEquals("Bearer new_access_token", newRequest?.header("Authorization"))
        verify { tokenManager.saveTokens("new_access_token", "new_refresh_token") }
    }

    @Test
    fun authenticate_clearsTokensAndEmitsExpired_whenRefreshFails() {
        val request = Request.Builder()
            .url("https://api.edumind.com/user/profile")
            .header("Authorization", "Bearer old_token")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        every { tokenManager.getAccessToken() } returns "old_token"
        every { tokenManager.getRefreshToken() } returns "expired_refresh"

        val call: Call<RefreshResponse> = mockk()
        every { authApiService.refresh(RefreshRequest("expired_refresh")) } returns call
        every { call.execute() } returns RetrofitResponse.error(401, mockk(relaxed = true))

        val result = authenticator.authenticate(null, response)

        assertNull(result)
        verify { tokenManager.clearTokens() }
        verify { sessionEventBus.emitSessionExpired() }
    }

    @Test
    fun authenticate_concurrentCallsWithSameOldToken_refreshesOnlyOnce() {
        val request = Request.Builder()
            .url("https://api.edumind.com/user/profile")
            .header("Authorization", "Bearer old_token")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        var currentAccess = "old_token"
        every { tokenManager.getAccessToken() } answers { currentAccess }
        every { tokenManager.getRefreshToken() } returns "valid_refresh"
        every { tokenManager.saveTokens(any(), any()) } answers {
            currentAccess = firstArg()
        }

        val call: Call<RefreshResponse> = mockk()
        val refreshDto = RefreshResponse("new_access_token", "new_refresh_token", 900)
        every { authApiService.refresh(RefreshRequest("valid_refresh")) } returns call
        every { call.execute() } returns RetrofitResponse.success(refreshDto)

        val threads = 3
        val executor = java.util.concurrent.Executors.newFixedThreadPool(threads)
        val latch = java.util.concurrent.CountDownLatch(threads)
        val results = java.util.concurrent.CopyOnWriteArrayList<Request?>()

        for (i in 0 until threads) {
            executor.submit {
                val res = authenticator.authenticate(null, response)
                results.add(res)
                latch.countDown()
            }
        }
        latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
        executor.shutdown()

        assertEquals(3, results.size)
        results.forEach { req ->
            assertNotNull(req)
            assertEquals("Bearer new_access_token", req?.header("Authorization"))
        }
        verify(exactly = 1) { authApiService.refresh(any()) }
    }
}
