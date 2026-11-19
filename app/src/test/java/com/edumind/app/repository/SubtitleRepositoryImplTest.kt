package com.edumind.app.repository

import com.edumind.app.data.local.db.dao.SubtitleCacheDao
import com.edumind.app.data.local.db.entity.SubtitleCacheEntity
import com.edumind.app.data.model.response.LessonDetailResponseDto
import com.edumind.app.data.model.response.SubtitleSegment
import com.edumind.app.data.model.response.TranscriptSegmentDto
import com.edumind.app.data.model.response.toSubtitleSegment
import com.edumind.app.data.remote.api.LessonApiService
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class SubtitleRepositoryImplTest {

    private val lessonApiService: LessonApiService = mockk()
    private val subtitleCacheDao: SubtitleCacheDao = mockk(relaxed = true)
    private lateinit var repository: SubtitleRepositoryImpl
    private val gson = Gson()
    private val testLessonId = "lesson_test_101"

    @Before
    fun setUp() {
        repository = SubtitleRepositoryImpl(lessonApiService, subtitleCacheDao)
    }

    @Test
    fun getSegments_onSuccess_savesToCacheAndReturnsMappedSegments() = runTest {
        val dtos = listOf(
            TranscriptSegmentDto(start = 1.5f, end = 4.2f, text = "  Xin chào các bạn  "),
            TranscriptSegmentDto(start = 5.0f, end = 7.0f, text = "")
        )
        val responseDto = LessonDetailResponseDto(
            id = testLessonId,
            title = "Test Lesson",
            courseId = "c1",
            transcript = dtos
        )
        coEvery { lessonApiService.getLessonDetail(testLessonId) } returns Response.success(responseDto)

        val result = repository.getSegments(testLessonId)

        assertEquals(1, result.size)
        assertEquals(1500L, result[0].startMs)
        assertEquals(4200L, result[0].endMs)
        assertEquals("Xin chào các bạn", result[0].text)

        val slot = slot<SubtitleCacheEntity>()
        coVerify(exactly = 1) { subtitleCacheDao.upsert(capture(slot)) }
        assertEquals(testLessonId, slot.captured.lessonId)
        assertTrue(slot.captured.segmentsJson.contains("Xin chào các bạn"))
    }

    @Test
    fun getSegments_onEmptyTranscript_savesEmptyJsonAndReturnsEmptyList() = runTest {
        val responseDto = LessonDetailResponseDto(
            id = testLessonId,
            title = "Empty Lesson",
            courseId = "c1",
            transcript = emptyList()
        )
        coEvery { lessonApiService.getLessonDetail(testLessonId) } returns Response.success(responseDto)

        val result = repository.getSegments(testLessonId)

        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { subtitleCacheDao.upsert(match { it.segmentsJson == "[]" }) }
    }

    @Test
    fun getSegments_onHttpError_returnsEmptyListWithoutReadingOrWritingCache() = runTest {
        val errorBody = "{}".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { lessonApiService.getLessonDetail(testLessonId) } returns Response.error(404, errorBody)

        val result = repository.getSegments(testLessonId)

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { subtitleCacheDao.upsert(any()) }
        coVerify(exactly = 0) { subtitleCacheDao.getByLessonId(any()) }
    }

    @Test
    fun getSegments_onIOException_fallsBackToCache() = runTest {
        val cachedSegments = listOf(
            SubtitleSegment(startMs = 2000L, endMs = 5000L, text = "Nội dung trong cache")
        )
        val entity = SubtitleCacheEntity(testLessonId, gson.toJson(cachedSegments))
        coEvery { lessonApiService.getLessonDetail(testLessonId) } throws IOException("Mất kết nối mạng")
        coEvery { subtitleCacheDao.getByLessonId(testLessonId) } returns entity

        val result = repository.getSegments(testLessonId)

        assertEquals(1, result.size)
        assertEquals("Nội dung trong cache", result[0].text)
        coVerify(exactly = 1) { subtitleCacheDao.getByLessonId(testLessonId) }
    }

    @Test
    fun getSegments_onIOExceptionWithNoCache_returnsEmptyList() = runTest {
        coEvery { lessonApiService.getLessonDetail(testLessonId) } throws IOException("Timeout")
        coEvery { subtitleCacheDao.getByLessonId(testLessonId) } returns null

        val result = repository.getSegments(testLessonId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun toSubtitleSegment_mapsCorrectlyAndSafely() {
        val normalDto = TranscriptSegmentDto(start = 12.345f, end = 15.678f, text = "  Đoạn thoại  ")
        val normalMapped = normalDto.toSubtitleSegment()
        assertEquals(12345L, normalMapped.startMs)
        assertEquals(15678L, normalMapped.endMs)
        assertEquals("Đoạn thoại", normalMapped.text)

        val edgeDto = TranscriptSegmentDto(start = -1.0f, end = -0.5f, text = "   ")
        val edgeMapped = edgeDto.toSubtitleSegment()
        assertEquals(0L, edgeMapped.startMs)
        assertEquals(0L, edgeMapped.endMs)
        assertEquals("", edgeMapped.text)
    }
}
