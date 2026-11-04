package com.edumind.app.ui.player

import android.content.Context
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.edumind.app.data.model.response.CourseLesson
import com.edumind.app.data.model.response.CourseSection
import com.edumind.app.data.model.response.LessonDetail
import com.edumind.app.data.model.response.LessonType
import com.edumind.app.data.model.response.PlayerChapter
import com.edumind.app.data.model.response.VideoChapter
import com.edumind.app.data.model.response.toPlayerChapters
import com.edumind.app.repository.CourseRepository
import com.edumind.app.repository.LessonRepository
import com.edumind.app.repository.ProgressRepository
import com.edumind.app.ui.navigation.Screen
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Method

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val context: Context = mockk(relaxed = true)
    private val lessonRepository: LessonRepository = mockk(relaxed = true)
    private val progressRepository: ProgressRepository = mockk(relaxed = true)
    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private val mockExoPlayer: ExoPlayer = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    private val sampleLessonId = "lesson_01"
    private val sampleCourseId = "course_python_01"

    private val sampleVideoChapters = listOf(
        VideoChapter(title = "1. Giới thiệu tổng quan", startTimeSec = 0f),
        VideoChapter(title = "2. Khái niệm biến và kiểu dữ liệu", startTimeSec = 135f), // 02:15 = 135_000L
        VideoChapter(title = "3. Kiểu số nguyên int & float", startTimeSec = 255f),     // 04:15 = 255_000L
        VideoChapter(title = "4. Chuỗi ký tự (str) & Logic (bool)", startTimeSec = 380f) // 06:20 = 380_000L
    )

    private val sampleLessonDetail = LessonDetail(
        id = sampleLessonId,
        title = "Bài 01: Nhập môn Python",
        description = "Mô tả bài học",
        courseId = sampleCourseId,
        chapterId = "chap_01",
        hlsStreamUrl = "https://example.com/video.m3u8",
        durationSec = 600,
        lastPositionSec = 0f,
        transcript = emptyList(),
        chapters = sampleVideoChapters,
        quizMarkers = emptyList(),
        nextLessonId = "lesson_02",
        prevLessonId = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Looper::class)
        val mockLooper = mockk<Looper>(relaxed = true)
        every { Looper.myLooper() } returns mockLooper
        every { Looper.getMainLooper() } returns mockLooper

        mockkStatic(Uri::class)
        val mockUri = mockk<Uri>(relaxed = true)
        every { Uri.parse(any()) } returns mockUri

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        mockkConstructor(ExoPlayer.Builder::class)
        every { anyConstructed<ExoPlayer.Builder>().setMediaSourceFactory(any()) } answers { self as ExoPlayer.Builder }
        every { anyConstructed<ExoPlayer.Builder>().build() } returns mockExoPlayer

        every { mockExoPlayer.currentPosition } returns 0L
        every { mockExoPlayer.duration } returns 600_000L
        every { mockExoPlayer.playbackState } returns Player.STATE_READY
        every { mockExoPlayer.playerError } returns null

        coEvery { lessonRepository.getLessonDetail(sampleLessonId) } returns Result.success(sampleLessonDetail)
        coEvery { progressRepository.getPlaybackPosition(any()) } returns null
        coEvery { progressRepository.saveProgress(any(), any(), any(), any()) } returns Result.success(10f)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Looper::class)
        unmockkConstructor(ExoPlayer.Builder::class)
        unmockkStatic(Uri::class)
        unmockkStatic(Log::class)
    }

    private fun callUpdateCurrentChapterIndex(viewModel: PlayerViewModel, positionMs: Long) {
        val method: Method = PlayerViewModel::class.java.getDeclaredMethod("updateCurrentChapterIndex", Long::class.java)
        method.isAccessible = true
        method.invoke(viewModel, positionMs)
    }

    @Test
    fun `init without lessonId loads sample fallback chapters`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = PlayerViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            lessonRepository = lessonRepository,
            progressRepository = progressRepository,
            courseRepository = courseRepository
        )

        val chapters = viewModel.chapters.value
        assertEquals(4, chapters.size)
        assertEquals(0, viewModel.currentChapterIndex.value)
        assertEquals("1. Giới thiệu tổng quan bài học", chapters[0].title)
    }

    @Test
    fun `fetchLesson populates chapters and sets currentChapterIndex based on position`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_LESSON_ID to sampleLessonId))
        val viewModel = PlayerViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            lessonRepository = lessonRepository,
            progressRepository = progressRepository,
            courseRepository = courseRepository
        )

        advanceUntilIdle()

        val chapters = viewModel.chapters.value
        assertEquals(4, chapters.size)
        assertEquals(0L, chapters[0].startMs)
        assertEquals(135_000L, chapters[1].startMs)
        assertEquals(255_000L, chapters[2].startMs)
        assertEquals(380_000L, chapters[3].startMs)
    }

    @Test
    fun `updateCurrentChapterIndex correctly selects chapter across time boundaries`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_LESSON_ID to sampleLessonId))
        val viewModel = PlayerViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            lessonRepository = lessonRepository,
            progressRepository = progressRepository,
            courseRepository = courseRepository
        )
        advanceUntilIdle()

        // Mốc 150s (giữa 135s và 255s) -> Chapter 1
        callUpdateCurrentChapterIndex(viewModel, 150_000L)
        assertEquals(1, viewModel.currentChapterIndex.value)
        assertTrue(viewModel.chapters.value[1].isCurrent)
        assertFalse(viewModel.chapters.value[0].isCurrent)

        // Mốc 260s (giữa 255s và 380s) -> Chapter 2
        callUpdateCurrentChapterIndex(viewModel, 260_000L)
        assertEquals(2, viewModel.currentChapterIndex.value)
        assertTrue(viewModel.chapters.value[2].isCurrent)

        // Mốc 400s (sau 380s) -> Chapter 3
        callUpdateCurrentChapterIndex(viewModel, 400_000L)
        assertEquals(3, viewModel.currentChapterIndex.value)
        assertTrue(viewModel.chapters.value[3].isCurrent)
    }

    @Test
    fun `onChapterSelected with timestamp marker seeks exoPlayer and starts playback`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_LESSON_ID to sampleLessonId))
        val viewModel = PlayerViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            lessonRepository = lessonRepository,
            progressRepository = progressRepository,
            courseRepository = courseRepository
        )
        advanceUntilIdle()

        val targetChapter = viewModel.chapters.value[1] // Mốc 135_000L
        viewModel.onChapterSelected(targetChapter)

        verify { mockExoPlayer.seekTo(135_000L) }
        verify { mockExoPlayer.play() }
    }

    @Test
    fun `onChapterSelected with locked chapter emits user message and blocks seeking`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_LESSON_ID to sampleLessonId))
        val viewModel = PlayerViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            lessonRepository = lessonRepository,
            progressRepository = progressRepository,
            courseRepository = courseRepository
        )
        advanceUntilIdle()

        val lockedChapter = PlayerChapter(
            id = "lesson_locked",
            title = "Bài học nâng cao có phí",
            startMs = null,
            durationText = "15:00",
            lessonId = "lesson_locked",
            isLocked = true
        )

        viewModel.onChapterSelected(lockedChapter)

        assertNotNull(viewModel.userMessage.value)
        assertTrue(viewModel.userMessage.value!!.contains("đăng ký khóa học"))
        verify(exactly = 0) { mockExoPlayer.seekTo(any()) }
    }

    @Test
    fun `onChapterSelected with unlocked different lesson saves progress and opens new lesson`() = runTest {
        val newLessonId = "lesson_02"
        val newLessonDetail = sampleLessonDetail.copy(id = newLessonId, title = "Bài 02")
        coEvery { lessonRepository.getLessonDetail(newLessonId) } returns Result.success(newLessonDetail)

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_LESSON_ID to sampleLessonId))
        val viewModel = PlayerViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            lessonRepository = lessonRepository,
            progressRepository = progressRepository,
            courseRepository = courseRepository
        )
        advanceUntilIdle()

        val nextLessonChapter = PlayerChapter(
            id = newLessonId,
            title = "Bài 02: Cú pháp nâng cao",
            startMs = null,
            durationText = "20:00",
            lessonId = newLessonId,
            isLocked = false
        )

        viewModel.onChapterSelected(nextLessonChapter)
        advanceUntilIdle()

        coVerify { lessonRepository.getLessonDetail(newLessonId) }
    }

    @Test
    fun `clearUserMessage resets message state to null`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_LESSON_ID to sampleLessonId))
        val viewModel = PlayerViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            lessonRepository = lessonRepository,
            progressRepository = progressRepository,
            courseRepository = courseRepository
        )
        advanceUntilIdle()

        val lockedChapter = PlayerChapter(
            id = "l_lock",
            title = "Khóa",
            startMs = null,
            durationText = "10p",
            lessonId = "l_lock",
            isLocked = true
        )
        viewModel.onChapterSelected(lockedChapter)
        assertNotNull(viewModel.userMessage.value)

        viewModel.clearUserMessage()
        assertNull(viewModel.userMessage.value)
    }

    @Test
    fun `VideoChapter toPlayerChapters mapper converts seconds to milliseconds accurately`() {
        val rawChapters = listOf(
            VideoChapter("Mở đầu", 0f),
            VideoChapter("Phần 1", 90.5f) // 90.5 * 1000 = 90500L
        )

        val playerChapters = rawChapters.toPlayerChapters(currentLessonId = "lesson_test")

        assertEquals(2, playerChapters.size)
        assertEquals(0L, playerChapters[0].startMs)
        assertEquals("00:00", playerChapters[0].durationText)
        assertEquals(90_500L, playerChapters[1].startMs)
        assertEquals("01:30", playerChapters[1].durationText)
        assertTrue(playerChapters[0].isTimestampMarker)
    }

    @Test
    fun `CourseSection toPlayerChapters mapper transforms curriculum tree to flattened chapters`() {
        val sections = listOf(
            CourseSection(
                id = "sec_1",
                title = "Chương 1",
                durationSummary = "2 bài",
                lessons = listOf(
                    CourseLesson("l1", "Bài 1", "10p", LessonType.VIDEO, isFreePreview = true, isCompleted = true, isLocked = false),
                    CourseLesson("l2", "Bài 2", "15p", LessonType.VIDEO, isFreePreview = false, isCompleted = false, isLocked = true)
                )
            )
        )

        val playerChapters = sections.toPlayerChapters(currentLessonId = "l1")

        assertEquals(2, playerChapters.size)
        assertEquals("l1", playerChapters[0].lessonId)
        assertTrue(playerChapters[0].isCurrent)
        assertTrue(playerChapters[0].isCompleted)
        assertFalse(playerChapters[0].isLocked)

        assertEquals("l2", playerChapters[1].lessonId)
        assertFalse(playerChapters[1].isCurrent)
        assertFalse(playerChapters[1].isCompleted)
        assertTrue(playerChapters[1].isLocked) // Vì không free preview và chưa hoàn thành
        assertFalse(playerChapters[1].isTimestampMarker)
    }

    @Test
    fun isFullscreen_defaultState_isFalse() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_LESSON_ID to sampleLessonId))
        val viewModel = PlayerViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            lessonRepository = lessonRepository,
            progressRepository = progressRepository,
            courseRepository = courseRepository
        )

        assertFalse(viewModel.isFullscreen.value)
    }

    @Test
    fun toggleFullscreen_togglesStateCorrectly() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_LESSON_ID to sampleLessonId))
        val viewModel = PlayerViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            lessonRepository = lessonRepository,
            progressRepository = progressRepository,
            courseRepository = courseRepository
        )

        viewModel.toggleFullscreen()
        assertTrue(viewModel.isFullscreen.value)

        viewModel.toggleFullscreen()
        assertFalse(viewModel.isFullscreen.value)
    }

    @Test
    fun exitFullscreen_setsStateToFalse() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                Screen.ARG_LESSON_ID to sampleLessonId,
                "is_fullscreen" to true
            )
        )
        val viewModel = PlayerViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            lessonRepository = lessonRepository,
            progressRepository = progressRepository,
            courseRepository = courseRepository
        )

        assertTrue(viewModel.isFullscreen.value)

        viewModel.exitFullscreen()
        assertFalse(viewModel.isFullscreen.value)
    }
}
