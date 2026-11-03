package com.edumind.app.repository

import com.edumind.app.data.local.db.dao.PlaybackPositionDao
import com.edumind.app.data.local.db.entity.PlaybackPositionEntity
import com.edumind.app.data.model.response.SaveProgressResponse
import com.edumind.app.data.remote.api.ProgressApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ProgressRepositoryImplTest {

    private val progressApiService: ProgressApiService = mockk(relaxed = true)
    private val playbackPositionDao: PlaybackPositionDao = mockk(relaxed = true)
    private lateinit var repository: ProgressRepositoryImpl

    @Before
    fun setup() {
        repository = ProgressRepositoryImpl(progressApiService, playbackPositionDao)
    }

    @Test
    fun saveProgress_networkSuccess_savesLocalAndMarksSynced() = runTest {
        val lessonId = "lesson_01"
        val positionMs = 60_000L
        val durationMs = 120_000L

        coEvery { progressApiService.saveProgress(any()) } returns Response.success(
            SaveProgressResponse(message = "OK", courseProgressPercent = 50f)
        )

        val result = repository.saveProgress(lessonId, positionMs, durationMs)

        assertTrue(result.isSuccess)
        assertEquals(50f, result.getOrNull())

        // Xác nhận luôn lưu vào Room trước với synced = false
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            playbackPositionDao.upsert(match {
                it.lessonId == lessonId && it.positionMs == positionMs && !it.synced
            })
            progressApiService.saveProgress(any())
            playbackPositionDao.markSynced(lessonId)
        }
    }

    @Test
    fun saveProgress_networkFailure_keepsLocalDataAndReturnsEstimatedPercent() = runTest {
        val lessonId = "lesson_02"
        val positionMs = 30_000L
        val durationMs = 60_000L

        // Giả lập ngắt kết nối mạng (IOException)
        coEvery { progressApiService.saveProgress(any()) } throws IOException("No internet connection")

        val result = repository.saveProgress(lessonId, positionMs, durationMs)

        // Không ném lỗi ra ngoài làm sập app
        assertTrue(result.isSuccess)
        assertEquals(50f, result.getOrNull() ?: 0f, 0.1f)

        // Xác nhận đã lưu vào Room, nhưng KHÔNG gọi markSynced vì mạng lỗi
        coVerify(exactly = 1) {
            playbackPositionDao.upsert(match {
                it.lessonId == lessonId && !it.synced
            })
        }
        coVerify(exactly = 0) {
            playbackPositionDao.markSynced(lessonId)
        }
    }

    @Test
    fun syncPending_sendsUnsyncedItemsAndMarksSynced() = runTest {
        val pendingEntity = PlaybackPositionEntity(
            lessonId = "lesson_offline",
            positionMs = 45_000L,
            durationMs = 90_000L,
            completed = false,
            updatedAt = 1000L,
            synced = false
        )

        coEvery { playbackPositionDao.getUnsynced() } returns listOf(pendingEntity)
        coEvery { progressApiService.saveProgress(any()) } returns Response.success(
            SaveProgressResponse(message = "OK", courseProgressPercent = 50f)
        )

        val result = repository.syncPending()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            progressApiService.saveProgress(match {
                it.lessonId == "lesson_offline" && it.currentPosition == 45f
            })
            playbackPositionDao.markSynced("lesson_offline")
        }
    }

    @Test
    fun getPlaybackPosition_returnsDaoEntity() = runTest {
        val entity = PlaybackPositionEntity(
            lessonId = "lesson_resume",
            positionMs = 15_000L,
            durationMs = 60_000L,
            completed = false,
            updatedAt = 2000L,
            synced = true
        )

        coEvery { playbackPositionDao.getByLessonId("lesson_resume") } returns entity

        val result = repository.getPlaybackPosition("lesson_resume")

        assertNotNull(result)
        assertEquals(15_000L, result?.positionMs)
    }
}

