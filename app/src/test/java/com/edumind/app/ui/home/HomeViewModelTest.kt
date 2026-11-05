package com.edumind.app.ui.home

import com.edumind.app.data.model.response.ContinueLearningItem
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.FeaturedCourse
import com.edumind.app.repository.HomeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val homeRepository: HomeRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HomeViewModel

    private val sampleFeatured = listOf(
        FeaturedCourse(
            id = "c1",
            title = "Khóa học 1",
            subtitle = "Mô tả 1",
            bannerUrl = "url",
            priceVnd = 100_000L
        )
    )

    private val sampleContinue = listOf(
        ContinueLearningItem(
            courseId = "c1",
            lessonId = "l1",
            courseTitle = "Khóa 1",
            currentLessonTitle = "Bài 1",
            thumbnailUrl = "thumb",
            progressPercent = 50,
            remainingTimeText = "10 phút"
        )
    )

    private val sampleCourses = listOf(
        CourseSummary(
            id = "c2",
            title = "Khóa học 2",
            thumbnailUrl = "thumb",
            instructorName = "Giảng viên A",
            priceVnd = 0L
        )
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
    fun loadHomeData_success_updatesUiStateWithData() = runTest(testDispatcher) {
        coEvery { homeRepository.getFeaturedCourses() } returns Result.success(sampleFeatured)
        coEvery { homeRepository.getContinueLearningItems() } returns Result.success(sampleContinue)
        coEvery { homeRepository.getRecommendedCourses() } returns Result.success(sampleCourses)
        coEvery { homeRepository.getNewCourses(any()) } returns Result.success(sampleCourses)
        coEvery { homeRepository.getUnreadNotificationCount() } returns Result.success(5)

        viewModel = HomeViewModel(homeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertNull(state.errorMessage)
        assertEquals(1, state.featuredCourses.size)
        assertEquals(1, state.continueLearningItems.size)
        assertEquals(1, state.recommendedCourses.size)
        assertEquals(5, state.unreadNotificationCount)
    }

    @Test
    fun loadHomeData_notificationFailure_doesNotBreakMainContent() = runTest(testDispatcher) {
        coEvery { homeRepository.getFeaturedCourses() } returns Result.success(sampleFeatured)
        coEvery { homeRepository.getContinueLearningItems() } returns Result.success(sampleContinue)
        coEvery { homeRepository.getRecommendedCourses() } returns Result.success(sampleCourses)
        coEvery { homeRepository.getNewCourses(any()) } returns Result.success(sampleCourses)
        // Notification API bị lỗi
        coEvery { homeRepository.getUnreadNotificationCount() } returns Result.failure(RuntimeException("Lỗi mạng"))

        viewModel = HomeViewModel(homeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage) // Không bị coi là lỗi chung
        assertEquals(0, state.unreadNotificationCount) // Fallback về 0
        assertEquals(1, state.featuredCourses.size)
    }

    @Test
    fun loadHomeData_continueLearningFailure_doesNotBreakMainContent() = runTest(testDispatcher) {
        coEvery { homeRepository.getFeaturedCourses() } returns Result.success(sampleFeatured)
        // Mục Tiếp tục học bị lỗi hoặc rớt mạng
        coEvery { homeRepository.getContinueLearningItems() } returns Result.failure(RuntimeException("Lỗi mạng tiếp tục học"))
        coEvery { homeRepository.getRecommendedCourses() } returns Result.success(sampleCourses)
        coEvery { homeRepository.getNewCourses(any()) } returns Result.success(sampleCourses)
        coEvery { homeRepository.getUnreadNotificationCount() } returns Result.success(2)

        viewModel = HomeViewModel(homeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage) // Không bị coi là lỗi chung toàn trang
        assertEquals(0, state.continueLearningItems.size) // Fallback về danh sách rỗng an toàn
        assertEquals(1, state.featuredCourses.size)
        assertEquals(1, state.recommendedCourses.size)
    }

    @Test
    fun loadHomeData_mainContentFailure_setsErrorMessage() = runTest(testDispatcher) {
        coEvery { homeRepository.getFeaturedCourses() } returns Result.failure(RuntimeException("Lỗi máy chủ"))
        coEvery { homeRepository.getContinueLearningItems() } returns Result.success(emptyList())
        coEvery { homeRepository.getRecommendedCourses() } returns Result.success(emptyList())
        coEvery { homeRepository.getNewCourses(any()) } returns Result.success(emptyList())
        coEvery { homeRepository.getUnreadNotificationCount() } returns Result.success(0)

        viewModel = HomeViewModel(homeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertTrue(state.featuredCourses.isEmpty())
    }

    @Test
    fun refresh_triggersLoadingAndMaintainsData() = runTest(testDispatcher) {
        coEvery { homeRepository.getFeaturedCourses() } returns Result.success(sampleFeatured)
        coEvery { homeRepository.getContinueLearningItems() } returns Result.success(sampleContinue)
        coEvery { homeRepository.getRecommendedCourses() } returns Result.success(sampleCourses)
        coEvery { homeRepository.getNewCourses(any()) } returns Result.success(sampleCourses)
        coEvery { homeRepository.getUnreadNotificationCount() } returns Result.success(2)

        viewModel = HomeViewModel(homeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(1, state.featuredCourses.size)
    }

    @Test
    fun init_isLoadingUntilDataArrives_thenSuccess() = runTest(testDispatcher) {
        coEvery { homeRepository.getFeaturedCourses() } returns Result.success(sampleFeatured)
        coEvery { homeRepository.getContinueLearningItems() } returns Result.success(sampleContinue)
        coEvery { homeRepository.getRecommendedCourses() } returns Result.success(sampleCourses)
        coEvery { homeRepository.getNewCourses(any()) } returns Result.success(sampleCourses)
        coEvery { homeRepository.getUnreadNotificationCount() } returns Result.success(1)

        viewModel = HomeViewModel(homeRepository)
        assertTrue(viewModel.uiState.value.isLoading)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.featuredCourses.size)
    }

    @Test
    fun refresh_setsIsRefreshingThenReplacesData() = runTest(testDispatcher) {
        val refreshedFeatured = listOf(sampleFeatured[0].copy(id = "c9", title = "Khóa học làm mới"))
        coEvery { homeRepository.getFeaturedCourses() } returns Result.success(sampleFeatured)
        coEvery { homeRepository.getContinueLearningItems() } returns Result.success(sampleContinue)
        coEvery { homeRepository.getRecommendedCourses() } returns Result.success(sampleCourses)
        coEvery { homeRepository.getNewCourses(any()) } returns Result.success(sampleCourses)
        coEvery { homeRepository.getUnreadNotificationCount() } returns Result.success(1)

        viewModel = HomeViewModel(homeRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("c1", viewModel.uiState.value.featuredCourses[0].id)

        coEvery { homeRepository.getFeaturedCourses() } coAnswers {
            delay(1_000)
            Result.success(refreshedFeatured)
        }
        viewModel.refresh()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isRefreshing)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals("c9", viewModel.uiState.value.featuredCourses[0].id)
    }
}
