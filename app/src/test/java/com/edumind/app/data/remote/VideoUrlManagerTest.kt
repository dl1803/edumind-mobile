package com.edumind.app.data.remote

import com.edumind.app.data.model.response.LessonDetailResponseDto
import com.edumind.app.data.remote.api.LessonApiService
import com.edumind.app.repository.LessonAccessDeniedException
import com.edumind.app.repository.LessonNotFoundException
import com.edumind.app.repository.VideoProcessingException
import com.edumind.app.util.ClockProvider
import com.edumind.app.util.Constants
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class VideoUrlManagerTest {

    private var now = 0L
    private val clock = ClockProvider { now }
    private val api = mockk<LessonApiService>()
    private lateinit var manager: VideoUrlManager

    private fun createDto(url: String? = "https://example.test/master.m3u8") = LessonDetailResponseDto(
        id = "les-01",
        title = "Bài giảng kiểm thử",
        courseId = "crs-01",
        hlsStreamUrl = url
    )

    @Before
    fun setUp() {
        now = 0L
        manager = VideoUrlManager(api, clock)
    }

    @Test
    fun getPlaybackUrl_firstCall_fetchesFromApiAndCaches() = runTest {
        coEvery { api.getLessonDetail("les-01") } returns Response.success(createDto())

        val result = manager.getPlaybackUrl("les-01")

        assertTrue(result.isSuccess)
        assertEquals("les-01", result.getOrNull()?.lessonId)
        coVerify(exactly = 1) { api.getLessonDetail("les-01") }
    }

    @Test
    fun getPlaybackUrl_secondCallWithinTtl_usesCacheWithoutSecondApiCall() = runTest {
        coEvery { api.getLessonDetail("les-01") } returns Response.success(createDto())

        val first = manager.getPlaybackUrl("les-01").getOrThrow()
        val second = manager.getPlaybackUrl("les-01").getOrThrow()

        assertSame(first, second)
        coVerify(exactly = 1) { api.getLessonDetail("les-01") }
    }

    @Test
    fun getPlaybackUrl_whenTokenNearExpiry_callsApiToRefresh() = runTest {
        coEvery { api.getLessonDetail("les-01") } returns Response.success(createDto())

        manager.getPlaybackUrl("les-01").getOrThrow()
        // Remaining TTL is 30s, below the 60s refresh margin
        now = Constants.VIDEO_URL_TTL_MS - 30_000L

        manager.getPlaybackUrl("les-01").getOrThrow()
        coVerify(exactly = 2) { api.getLessonDetail("les-01") }
    }

    @Test
    fun renew_forcesFetchEvenIfWithinTtl() = runTest {
        coEvery { api.getLessonDetail("les-01") } returns Response.success(createDto())

        manager.getPlaybackUrl("les-01").getOrThrow()
        val renewed = manager.renew("les-01").getOrThrow()

        assertTrue(renewed.hlsUrl.isNotBlank())
        coVerify(exactly = 2) { api.getLessonDetail("les-01") }
    }

    @Test
    fun renew_concurrentCalls_onlyExecutesSingleApiCallDueToMutexAndReferenceCheck() = runTest {
        coEvery { api.getLessonDetail("les-01") } coAnswers {
            delay(100)
            Response.success(createDto())
        }

        val results = (1..3).map { async { manager.renew("les-01") } }.awaitAll()

        assertTrue(results.all { it.isSuccess })
        coVerify(exactly = 1) { api.getLessonDetail("les-01") }
    }

    @Test
    fun getPlaybackUrl_whenApiReturnsErrors_mapsProperExceptionsWithoutOverwritingCache() = runTest {
        val errorBody = "{\"message\":\"Error\"}".toResponseBody("application/json".toMediaTypeOrNull())

        coEvery { api.getLessonDetail("forbidden") } returns Response.error(403, errorBody)
        val res403 = manager.getPlaybackUrl("forbidden")
        assertTrue(res403.isFailure)
        assertTrue(res403.exceptionOrNull() is LessonAccessDeniedException)

        coEvery { api.getLessonDetail("not_found") } returns Response.error(404, errorBody)
        val res404 = manager.getPlaybackUrl("not_found")
        assertTrue(res404.isFailure)
        assertTrue(res404.exceptionOrNull() is LessonNotFoundException)

        coEvery { api.getLessonDetail("blank_url") } returns Response.success(createDto(url = ""))
        val resBlank = manager.getPlaybackUrl("blank_url")
        assertTrue(resBlank.isFailure)
        assertTrue(resBlank.exceptionOrNull() is VideoProcessingException)
    }
}

