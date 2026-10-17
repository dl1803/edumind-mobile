package com.edumind.app.ui.explore

import com.edumind.app.data.model.response.Category
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.PagedResponse
import com.edumind.app.repository.CourseRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ExploreViewModel

    private val sampleCategories = listOf(
        Category(id = "cat_it", name = "Công nghệ thông tin", courseCount = 12),
        Category(id = "cat_design", name = "Thiết kế & UI/UX", courseCount = 8)
    )

    private val sampleCourses = listOf(
        CourseSummary(
            id = "c1",
            title = "Lập trình Kotlin cơ bản",
            thumbnailUrl = "thumb1",
            instructorName = "Nguyễn Văn A",
            rating = 4.8f,
            reviewCount = 120,
            priceVnd = 199_000L
        ),
        CourseSummary(
            id = "c2",
            title = "Jetpack Compose nâng cao",
            thumbnailUrl = "thumb2",
            instructorName = "Trần Thị B",
            rating = 4.9f,
            reviewCount = 85,
            priceVnd = 0L
        )
    )

    private val defaultPagedResponse = PagedResponse(
        data = sampleCourses,
        total = 2,
        page = 1,
        pageSize = 20,
        totalPages = 1
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { courseRepository.getCategories() } returns Result.success(sampleCategories)
        coEvery {
            courseRepository.getCourses(
                page = any(),
                pageSize = any(),
                search = any(),
                categoryId = any(),
                priceMin = any(),
                priceMax = any(),
                minRating = any(),
                sortBy = any()
            )
        } returns Result.success(defaultPagedResponse)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_success_loadsCategoriesAndFirstPageCourses() = runTest(testDispatcher) {
        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.categories.value.size)
        val state = viewModel.uiState.value
        assertTrue(state is ExploreUiState.Success)
        val successState = state as ExploreUiState.Success
        assertEquals(2, successState.courses.size)
        assertEquals(2, successState.totalCount)
        assertFalse(successState.isLoadingMore)
        assertTrue(successState.isEndReached)
    }

    @Test
    fun init_emptyResult_emitsEmptyState() = runTest(testDispatcher) {
        val emptyPaged = PagedResponse<CourseSummary>(
            data = emptyList(),
            total = 0,
            page = 1,
            pageSize = 20,
            totalPages = 0
        )
        coEvery {
            courseRepository.getCourses(
                page = any(),
                pageSize = any(),
                search = any(),
                categoryId = any(),
                priceMin = any(),
                priceMax = any(),
                minRating = any(),
                sortBy = any()
            )
        } returns Result.success(emptyPaged)

        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ExploreUiState.Empty)
    }

    @Test
    fun init_failure_emitsErrorState() = runTest(testDispatcher) {
        coEvery {
            courseRepository.getCourses(
                page = any(),
                pageSize = any(),
                search = any(),
                categoryId = any(),
                priceMin = any(),
                priceMax = any(),
                minRating = any(),
                sortBy = any()
            )
        } returns Result.failure(RuntimeException("Lỗi kết nối mạng"))

        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ExploreUiState.Error)
        assertEquals("Lỗi kết nối mạng", (state as ExploreUiState.Error).message)
    }

    @Test
    fun onCategorySelected_updatesFilterAndReloadsPage1() = runTest(testDispatcher) {
        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCategorySelected("cat_it")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("cat_it", viewModel.filter.value.categoryId)
        coVerify {
            courseRepository.getCourses(
                page = 1,
                pageSize = 20,
                search = null,
                categoryId = "cat_it",
                priceMin = null,
                priceMax = null,
                minRating = null,
                sortBy = "newest"
            )
        }
    }

    @Test
    fun onCategorySelected_allCategory_resetsCategoryIdToNull() = runTest(testDispatcher) {
        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCategorySelected("cat_it")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("cat_it", viewModel.filter.value.categoryId)

        viewModel.onCategorySelected("cat_all")
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.filter.value.categoryId)
    }

    @Test
    fun onFilterApplied_updatesFilterAndReloads() = runTest(testDispatcher) {
        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val newFilter = ExploreFilter(
            priceOption = PriceFilterOption.UNDER_500K,
            ratingOption = RatingFilterOption.RATING_4_0,
            sortOption = SortOption.PRICE_DESC
        )
        viewModel.onFilterApplied(newFilter)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(PriceFilterOption.UNDER_500K, viewModel.filter.value.priceOption)
        assertEquals(RatingFilterOption.RATING_4_0, viewModel.filter.value.ratingOption)
        assertEquals(SortOption.PRICE_DESC, viewModel.filter.value.sortOption)

        coVerify {
            courseRepository.getCourses(
                page = 1,
                pageSize = 20,
                search = null,
                categoryId = null,
                priceMin = 1L,
                priceMax = 499999L,
                minRating = 4.0f,
                sortBy = "price_desc"
            )
        }
    }

    @Test
    fun onSortChanged_updatesSortOptionAndReloads() = runTest(testDispatcher) {
        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSortChanged(SortOption.PRICE_ASC)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SortOption.PRICE_ASC, viewModel.filter.value.sortOption)
        coVerify {
            courseRepository.getCourses(
                page = 1,
                pageSize = 20,
                search = null,
                categoryId = null,
                priceMin = null,
                priceMax = null,
                minRating = null,
                sortBy = "price_asc"
            )
        }
    }

    @Test
    fun onResetFilter_keepsCategoryAndResetsOtherOptions() = runTest(testDispatcher) {
        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCategorySelected("cat_design")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFilterApplied(
            ExploreFilter(
                categoryId = "cat_design",
                priceOption = PriceFilterOption.PAID,
                ratingOption = RatingFilterOption.RATING_4_5
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onResetFilter()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("cat_design", viewModel.filter.value.categoryId)
        assertEquals(PriceFilterOption.ALL, viewModel.filter.value.priceOption)
        assertEquals(RatingFilterOption.ALL, viewModel.filter.value.ratingOption)
        assertEquals(SortOption.NEWEST, viewModel.filter.value.sortOption)
    }

    @Test
    fun onClearAllFilters_resetsAllFilterOptions() = runTest(testDispatcher) {
        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFilterApplied(
            ExploreFilter(
                categoryId = "cat_it",
                priceOption = PriceFilterOption.FREE,
                ratingOption = RatingFilterOption.RATING_4_0,
                sortOption = SortOption.POPULAR
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onClearAllFilters()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.filter.value.categoryId)
        assertEquals(PriceFilterOption.ALL, viewModel.filter.value.priceOption)
        assertEquals(RatingFilterOption.ALL, viewModel.filter.value.ratingOption)
        assertEquals(SortOption.NEWEST, viewModel.filter.value.sortOption)
    }

    @Test
    fun loadMore_success_appendsCoursesToExistingList() = runTest(testDispatcher) {
        val page1Courses = List(20) { index -> sampleCourses[0].copy(id = "c1_$index") }
        val page1Response = PagedResponse(
            data = page1Courses,
            total = 21,
            page = 1,
            pageSize = 20,
            totalPages = 2
        )
        val page2Response = PagedResponse(
            data = listOf(sampleCourses[1]),
            total = 21,
            page = 2,
            pageSize = 20,
            totalPages = 2
        )
        coEvery {
            courseRepository.getCourses(
                page = 1,
                pageSize = any(),
                search = any(),
                categoryId = any(),
                priceMin = any(),
                priceMax = any(),
                minRating = any(),
                sortBy = any()
            )
        } returns Result.success(page1Response)

        coEvery {
            courseRepository.getCourses(
                page = 2,
                pageSize = any(),
                search = any(),
                categoryId = any(),
                priceMin = any(),
                priceMax = any(),
                minRating = any(),
                sortBy = any()
            )
        } returns Result.success(page2Response)

        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterPage1 = viewModel.uiState.value as ExploreUiState.Success
        assertEquals(20, stateAfterPage1.courses.size)
        assertFalse(stateAfterPage1.isEndReached)

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterPage2 = viewModel.uiState.value as ExploreUiState.Success
        assertEquals(21, stateAfterPage2.courses.size)
        assertTrue(stateAfterPage2.isEndReached)
        assertFalse(stateAfterPage2.isLoadingMore)
    }

    @Test
    fun loadMore_failure_keepsCurrentCoursesAndResetsLoadingMore() = runTest(testDispatcher) {
        val page1Courses = List(20) { index -> sampleCourses[0].copy(id = "c1_$index") }
        val page1Response = PagedResponse(
            data = page1Courses,
            total = 21,
            page = 1,
            pageSize = 20,
            totalPages = 2
        )
        coEvery {
            courseRepository.getCourses(
                page = 1,
                pageSize = any(),
                search = any(),
                categoryId = any(),
                priceMin = any(),
                priceMax = any(),
                minRating = any(),
                sortBy = any()
            )
        } returns Result.success(page1Response)

        coEvery {
            courseRepository.getCourses(
                page = 2,
                pageSize = any(),
                search = any(),
                categoryId = any(),
                priceMin = any(),
                priceMax = any(),
                minRating = any(),
                sortBy = any()
            )
        } returns Result.failure(RuntimeException("Lỗi tải trang 2"))

        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as ExploreUiState.Success
        assertEquals(20, state.courses.size)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun retry_fromError_reloadsCoursesSuccessfully() = runTest(testDispatcher) {
        coEvery {
            courseRepository.getCourses(
                page = any(),
                pageSize = any(),
                search = any(),
                categoryId = any(),
                priceMin = any(),
                priceMax = any(),
                minRating = any(),
                sortBy = any()
            )
        } returns Result.failure(RuntimeException("Lỗi kết nối"))

        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ExploreUiState.Error)

        coEvery {
            courseRepository.getCourses(
                page = any(),
                pageSize = any(),
                search = any(),
                categoryId = any(),
                priceMin = any(),
                priceMax = any(),
                minRating = any(),
                sortBy = any()
            )
        } returns Result.success(defaultPagedResponse)

        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ExploreUiState.Success)
        val successState = viewModel.uiState.value as ExploreUiState.Success
        assertEquals(2, successState.courses.size)
    }

    @Test
    fun initialState_isInitialLoading() = runTest(testDispatcher) {
        viewModel = ExploreViewModel(courseRepository)
        assertEquals(ExploreUiState.InitialLoading, viewModel.uiState.value)
    }

    @Test
    fun onCategorySelected_emitsLoadingStateNotInitialLoading() = runTest(testDispatcher) {
        viewModel = ExploreViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ExploreUiState.Success)

        viewModel.onCategorySelected("cat_it")
        assertEquals(ExploreUiState.Loading, viewModel.uiState.value)
    }
}
