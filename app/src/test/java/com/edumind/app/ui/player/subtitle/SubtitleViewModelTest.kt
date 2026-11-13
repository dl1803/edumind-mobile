package com.edumind.app.ui.player.subtitle

import androidx.lifecycle.SavedStateHandle
import com.edumind.app.data.local.datastore.PlayerPreferences
import com.edumind.app.data.local.datastore.SubtitleFontSize
import com.edumind.app.data.model.response.SubtitleSegment
import com.edumind.app.repository.SubtitleRepository
import com.edumind.app.ui.navigation.Screen
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubtitleViewModelTest {

    private val repository: SubtitleRepository = mockk()
    private val playerPreferences: PlayerPreferences = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private val lessonId = "lesson_sub_01"

    private val unsortedSegments = listOf(
        SubtitleSegment(startMs = 10_000L, endMs = 15_000L, text = "Câu thứ hai"),
        SubtitleSegment(startMs = 0L, endMs = 5_000L, text = "Câu thứ nhất")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getSegments(lessonId) } returns unsortedSegments
        coEvery { repository.getSegments("lesson_empty") } returns emptyList()
        every { playerPreferences.subtitleEnabled } returns flowOf(true)
        every { playerPreferences.subtitleFontSize } returns flowOf(SubtitleFontSize.MEDIUM)
        coEvery { playerPreferences.setSubtitleEnabled(any()) } just Runs
        coEvery { playerPreferences.setSubtitleFontSize(any()) } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(navLessonId: String? = lessonId): SubtitleViewModel {
        val handle = if (navLessonId == null) {
            SavedStateHandle()
        } else {
            SavedStateHandle(mapOf(Screen.ARG_LESSON_ID to navLessonId))
        }
        return SubtitleViewModel(handle, repository, playerPreferences)
    }

    @Test
    fun init_loadsSegmentsSortedByStart() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasSubtitles)
        assertEquals(listOf("Câu thứ nhất", "Câu thứ hai"), state.segments.map { it.text })
        assertTrue(state.enabled)
    }

    @Test
    fun onPositionChanged_insideSegment_selectsThatSegment() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPositionChanged(2_000L)
        assertEquals(0, viewModel.uiState.value.currentIndex)
        assertEquals("Câu thứ nhất", viewModel.uiState.value.currentText)

        viewModel.onPositionChanged(12_000L)
        assertEquals(1, viewModel.uiState.value.currentIndex)
        assertEquals("Câu thứ hai", viewModel.uiState.value.currentText)
    }

    @Test
    fun onPositionChanged_boundaries_startInclusiveEndExclusive() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPositionChanged(4_999L)
        assertEquals(0, viewModel.uiState.value.currentIndex)

        viewModel.onPositionChanged(5_000L)
        assertEquals(-1, viewModel.uiState.value.currentIndex)

        viewModel.onPositionChanged(10_000L)
        assertEquals(1, viewModel.uiState.value.currentIndex)

        viewModel.onPositionChanged(15_000L)
        assertEquals(-1, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun onPositionChanged_inGap_hasNoCurrentText() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPositionChanged(7_000L)

        assertEquals(-1, viewModel.uiState.value.currentIndex)
        assertNull(viewModel.uiState.value.currentText)
    }

    @Test
    fun lessonWithoutSubtitles_hasNoSubtitlesAndNoCurrentText() = runTest(testDispatcher) {
        val viewModel = createViewModel("lesson_empty")
        advanceUntilIdle()

        viewModel.onPositionChanged(2_000L)

        assertFalse(viewModel.uiState.value.hasSubtitles)
        assertEquals(-1, viewModel.uiState.value.currentIndex)
        assertNull(viewModel.uiState.value.currentText)
    }

    @Test
    fun setEnabled_updatesFlagKeepsCurrentIndexAndPersists() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onPositionChanged(2_000L)

        viewModel.setEnabled(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.enabled)
        assertEquals(0, viewModel.uiState.value.currentIndex)
        coVerify(exactly = 1) { playerPreferences.setSubtitleEnabled(false) }

        viewModel.setEnabled(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.enabled)
        coVerify(exactly = 1) { playerPreferences.setSubtitleEnabled(true) }
    }

    @Test
    fun setFontSize_updatesStateAndPersists() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFontSize(SubtitleFontSize.LARGE)
        advanceUntilIdle()
        assertEquals(SubtitleFontSize.LARGE, viewModel.uiState.value.fontSize)
        coVerify(exactly = 1) { playerPreferences.setSubtitleFontSize(SubtitleFontSize.LARGE) }
    }

    @Test
    fun toggleEnabled_flipsFlagAndPersists() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleEnabled()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.enabled)
        coVerify(exactly = 1) { playerPreferences.setSubtitleEnabled(false) }

        viewModel.toggleEnabled()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.enabled)
        coVerify(exactly = 1) { playerPreferences.setSubtitleEnabled(true) }
    }

    @Test
    fun positionReportedBeforeLoad_isAppliedAfterLoad() = runTest(testDispatcher) {
        val viewModel = createViewModel(navLessonId = null)
        viewModel.onPositionChanged(12_000L)

        viewModel.initLessonId(lessonId)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun initLessonId_sameLesson_doesNotReload() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.initLessonId(lessonId)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.getSegments(lessonId) }
    }
}
