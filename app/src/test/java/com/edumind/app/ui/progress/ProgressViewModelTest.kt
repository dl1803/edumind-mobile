package com.edumind.app.ui.progress

import com.edumind.app.data.model.response.CourseProgress
import com.edumind.app.data.model.response.DailyStudyTime
import com.edumind.app.data.model.response.LearningProgressResponse
import com.edumind.app.repository.ProgressRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {

    private val progressRepository: ProgressRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ProgressViewModel

    private val sampleCourses = listOf(
        CourseProgress(
            courseId = "course-1",
            courseTitle = "Khóa học Jetpack Compose",
            thumbnailUrl = "https://example.com/thumb1.jpg",
            progressPercent = 40f,
            completedLessons = 8,
            totalLessons = 20
        ),
        CourseProgress(
            courseId = "course-2",
            courseTitle = "Khóa học Kotlin Cơ bản",
            thumbnailUrl = "https://example.com/thumb2.jpg",
            progressPercent = 80f,
            completedLessons = 16,
            totalLessons = 20
        )
    )

    private val sampleWeeklyStudy = listOf(
        DailyStudyTime(dayOfWeek = "T2", minutes = 30),
        DailyStudyTime(dayOfWeek = "T3", minutes = 45),
        DailyStudyTime(dayOfWeek = "T4", minutes = 60, isToday = true)
    )

    private val sampleProgressResponse = LearningProgressResponse(
        totalEnrolledCourses = 2,
        totalHoursWatched = 12.5f,
        totalLessonsCompleted = 24,
        courses = sampleCourses,
        streakDays = 5,
        totalExercisesPassed = 18,
        totalExercises = 20,
        dailyGoalMinutes = 45,
        weeklyStudy = sampleWeeklyStudy
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadProgress_success_emitsSuccessStateWithCalculatedOverallPercent() = runTest(testDispatcher) {
        coEvery { progressRepository.getProgressOverview() } returns Result.success(sampleProgressResponse)

        viewModel = ProgressViewModel(progressRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProgressUiState.Success)
        val successState = state as ProgressUiState.Success
        assertEquals(sampleProgressResponse, successState.data)
        assertEquals(60, successState.overallPercent)
        assertFalse(successState.isGoalSheetVisible)
    }

    @Test
    fun loadProgress_emptyData_emitsEmptyState() = runTest(testDispatcher) {
        val emptyResponse = LearningProgressResponse(
            courses = emptyList(),
            totalHoursWatched = 0f
        )
        coEvery { progressRepository.getProgressOverview() } returns Result.success(emptyResponse)

        viewModel = ProgressViewModel(progressRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProgressUiState.Empty)
    }

    @Test
    fun loadProgress_failure_emitsErrorState() = runTest(testDispatcher) {
        val errorMessage = "Không thể tải dữ liệu tiến độ học tập."
        coEvery { progressRepository.getProgressOverview() } returns Result.failure(Exception(errorMessage))

        viewModel = ProgressViewModel(progressRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProgressUiState.Error)
        val errorState = state as ProgressUiState.Error
        assertEquals(errorMessage, errorState.message)
    }

    @Test
    fun retry_afterFailure_reloadsDataSuccessfully() = runTest(testDispatcher) {
        coEvery { progressRepository.getProgressOverview() } returns Result.failure(Exception("Lỗi mạng"))

        viewModel = ProgressViewModel(progressRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ProgressUiState.Error)

        coEvery { progressRepository.getProgressOverview() } returns Result.success(sampleProgressResponse)
        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProgressUiState.Success)
        assertEquals(60, (state as ProgressUiState.Success).overallPercent)
    }

    @Test
    fun toggleGoalSheet_whenSuccessState_updatesVisibilityCorrectly() = runTest(testDispatcher) {
        coEvery { progressRepository.getProgressOverview() } returns Result.success(sampleProgressResponse)

        viewModel = ProgressViewModel(progressRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleGoalSheet(true)
        val openState = viewModel.uiState.value as ProgressUiState.Success
        assertTrue(openState.isGoalSheetVisible)

        viewModel.toggleGoalSheet(false)
        val closedState = viewModel.uiState.value as ProgressUiState.Success
        assertFalse(closedState.isGoalSheetVisible)
    }

    @Test
    fun toggleGoalSheet_whenNonSuccessState_keepsCurrentState() = runTest(testDispatcher) {
        coEvery { progressRepository.getProgressOverview() } returns Result.failure(Exception("Lỗi"))

        viewModel = ProgressViewModel(progressRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleGoalSheet(true)
        assertTrue(viewModel.uiState.value is ProgressUiState.Error)
    }

    @Test
    fun updateDailyGoal_success_closesSheetAndReloadsProgress() = runTest(testDispatcher) {
        val updatedResponse = sampleProgressResponse.copy(dailyGoalMinutes = 60)
        coEvery { progressRepository.getProgressOverview() } returnsMany listOf(
            Result.success(sampleProgressResponse),
            Result.success(updatedResponse)
        )
        coEvery { progressRepository.updateDailyGoal(60) } returns Result.success(Unit)

        viewModel = ProgressViewModel(progressRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleGoalSheet(true)
        assertTrue((viewModel.uiState.value as ProgressUiState.Success).isGoalSheetVisible)

        viewModel.updateDailyGoal(60)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { progressRepository.updateDailyGoal(60) }
        val state = viewModel.uiState.value as ProgressUiState.Success
        assertFalse(state.isGoalSheetVisible)
        assertEquals(60, state.data.dailyGoalMinutes)
    }

    @Test
    fun updateDailyGoal_failure_emitsErrorState() = runTest(testDispatcher) {
        coEvery { progressRepository.getProgressOverview() } returns Result.success(sampleProgressResponse)
        val updateError = "Mạng không ổn định"
        coEvery { progressRepository.updateDailyGoal(any()) } returns Result.failure(Exception(updateError))

        viewModel = ProgressViewModel(progressRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateDailyGoal(90)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProgressUiState.Error)
        assertEquals(updateError, (state as ProgressUiState.Error).message)
    }
}
