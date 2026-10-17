package com.edumind.app.ui.search

import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.PagedResponse
import com.edumind.app.repository.CourseRepository
import com.edumind.app.ui.explore.ExploreFilter
import com.edumind.app.ui.explore.PriceFilterOption
import com.edumind.app.ui.explore.RatingFilterOption
import com.edumind.app.ui.explore.SortOption
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SearchViewModel

    private val sampleCourses = listOf(
        CourseSummary(
            id = "c1",
            title = "Nhập môn Python cơ bản",
            thumbnailUrl = "thumb1",
            instructorName = "Nguyễn Văn A",
            rating = 4.8f,
            reviewCount = 120,
            priceVnd = 499_000L
        ),
        CourseSummary(
            id = "c2",
            title = "Python chuyên sâu & Automation",
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
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
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
        viewModel = SearchViewModel(courseRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle with recent searches and trending keywords`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Idle)
        val idle = state as SearchUiState.Idle
        assertTrue(idle.recentSearches.isNotEmpty())
        assertTrue(idle.trendingKeywords.isNotEmpty())
    }

    @Test
    fun `debounce 300ms triggers suggestion fetch when typing`() = runTest {
        viewModel.onKeywordChange("py")
        advanceTimeBy(100)
        viewModel.onKeywordChange("pyth")
        advanceTimeBy(100)
        viewModel.onKeywordChange("python")

        coVerify(exactly = 0) { courseRepository.getCourses(search = any(), pageSize = 4) }

        advanceTimeBy(350)
        advanceUntilIdle()

        coVerify(exactly = 1) { courseRepository.getCourses(search = "python", pageSize = 4) }
        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Idle)
        val idle = state as SearchUiState.Idle
        assertEquals("python", idle.keyword)
        assertEquals(2, idle.suggestions.size)
    }

    @Test
    fun `onSearchSubmit triggers search and updates state to Success`() = runTest {
        viewModel.onSearchSubmit("python")
        advanceUntilIdle()

        coVerify { courseRepository.getCourses(search = "python") }
        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Success)
        val success = state as SearchUiState.Success
        assertEquals("python", success.keyword)
        assertEquals(2, success.courses.size)
    }

    @Test
    fun `search with keyword yielding empty result sets state to Empty`() = runTest {
        val emptyResponse = PagedResponse<CourseSummary>(
            data = emptyList(),
            total = 0,
            page = 1,
            pageSize = 20,
            totalPages = 0
        )
        coEvery { courseRepository.getCourses(search = "blockchain") } returns Result.success(emptyResponse)

        viewModel.onSearchSubmit("blockchain")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Empty)
        assertEquals("blockchain", (state as SearchUiState.Empty).keyword)
    }

    @Test
    fun `search with API failure sets state to Error`() = runTest {
        coEvery { courseRepository.getCourses(search = "network_error") } returns Result.failure(Exception("Lỗi kết nối máy chủ"))

        viewModel.onSearchSubmit("network_error")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Error)
        assertEquals("Lỗi kết nối máy chủ", (state as SearchUiState.Error).message)
    }

    @Test
    fun `empty search keyword returns to Idle state without API call`() = runTest {
        viewModel.onKeywordChange("")
        advanceTimeBy(350)
        advanceUntilIdle()

        coVerify(exactly = 0) { courseRepository.getCourses(search = any()) }
        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
    }

    @Test
    fun `onClearKeyword resets input and triggers Idle state`() = runTest {
        viewModel.onSearchSubmit("python")
        advanceUntilIdle()

        viewModel.onClearKeyword()
        advanceUntilIdle()

        assertEquals("", viewModel.keywordInput.value)
        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
    }

    @Test
    fun `onSelectSuggestedKeyword updates input and triggers search`() = runTest {
        coEvery { courseRepository.getCourses(search = "Kotlin Android") } returns Result.success(defaultPagedResponse)

        viewModel.onSelectSuggestedKeyword("Kotlin Android")
        advanceUntilIdle()

        assertEquals("Kotlin Android", viewModel.keywordInput.value)
        coVerify { courseRepository.getCourses(search = "Kotlin Android") }
        assertTrue(viewModel.uiState.value is SearchUiState.Success)
    }

    @Test
    fun `onClearRecentSearches clears recent searches list`() = runTest {
        viewModel.onClearRecentSearches()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Idle)
        assertTrue((state as SearchUiState.Idle).recentSearches.isEmpty())
        assertTrue(viewModel.recentSearches.value.isEmpty())
    }

    @Test
    fun `onApplyFilter updates filter and triggers new search with applied parameters`() = runTest {
        viewModel.onSearchSubmit("python")
        advanceUntilIdle()

        val filter = ExploreFilter(
            priceOption = PriceFilterOption.FREE,
            ratingOption = RatingFilterOption.RATING_4_5,
            sortOption = SortOption.POPULAR
        )
        viewModel.onApplyFilter(filter)
        advanceUntilIdle()

        assertEquals(filter, viewModel.filter.value)
        coVerify {
            courseRepository.getCourses(
                search = "python",
                priceMin = 0L,
                priceMax = 0L,
                minRating = 4.5f,
                sortBy = "popular"
            )
        }
    }

    @Test
    fun `onToggleSort cycles through sort options and updates search`() = runTest {
        viewModel.onSearchSubmit("python")
        advanceUntilIdle()

        assertEquals(SortOption.NEWEST, viewModel.filter.value.sortOption)

        viewModel.onToggleSort()
        advanceUntilIdle()
        assertEquals(SortOption.POPULAR, viewModel.filter.value.sortOption)

        viewModel.onToggleSort()
        advanceUntilIdle()
        assertEquals(SortOption.PRICE_ASC, viewModel.filter.value.sortOption)

        viewModel.onToggleSort()
        advanceUntilIdle()
        assertEquals(SortOption.PRICE_DESC, viewModel.filter.value.sortOption)

        viewModel.onToggleSort()
        advanceUntilIdle()
        assertEquals(SortOption.NEWEST, viewModel.filter.value.sortOption)
    }

    @Test
    fun `onResetFilter resets filter back to default`() = runTest {
        viewModel.onSearchSubmit("python")
        advanceUntilIdle()

        viewModel.onApplyFilter(
            ExploreFilter(
                priceOption = PriceFilterOption.FREE,
                sortOption = SortOption.PRICE_DESC
            )
        )
        advanceUntilIdle()
        assertEquals(PriceFilterOption.FREE, viewModel.filter.value.priceOption)

        viewModel.onResetFilter()
        advanceUntilIdle()
        assertTrue(viewModel.filter.value.isDefault)
    }
}
