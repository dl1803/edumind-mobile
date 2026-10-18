package com.edumind.app.ui.library

import com.edumind.app.data.model.response.ContinueLearningItem
import com.edumind.app.data.model.response.MyCourse
import com.edumind.app.repository.CourseRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyCoursesViewModelTest {

    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MyCoursesViewModel

    private val sampleCourses = listOf(
        MyCourse(
            id = "c1",
            title = "Lập trình Python",
            thumbnailUrl = "thumb1",
            instructorName = "TS. Lê Hoàng Nam",
            progressPercent = 45,
            totalLessons = 20,
            completedLessons = 9,
            nextLessonTitle = "Bài 04: Cấu trúc điều kiện"
        ),
        MyCourse(
            id = "c2",
            title = "Cấu trúc dữ liệu & Giải thuật",
            thumbnailUrl = "thumb2",
            instructorName = "ThS. Đặng Minh Tuấn",
            progressPercent = 12,
            totalLessons = 25,
            completedLessons = 3,
            nextLessonTitle = "Bài 02: Big-O"
        ),
        MyCourse(
            id = "c3",
            title = "Thiết kế Web với Tailwind CSS",
            thumbnailUrl = "thumb3",
            instructorName = "ThS. Hoàng Thùy Linh",
            progressPercent = 100,
            totalLessons = 16,
            completedLessons = 16,
            nextLessonTitle = "Bài 16: Tối ưu hiệu năng"
        ),
        MyCourse(
            id = "c4",
            title = "Machine Learning cơ bản",
            thumbnailUrl = "thumb4",
            instructorName = "ThS. Lê Hoàng Long",
            progressPercent = 0,
            totalLessons = 15,
            completedLessons = 0,
            nextLessonTitle = "Bài 01: Giới thiệu"
        )
    )

    private val sampleResumeItem = ContinueLearningItem(
        courseId = "c1",
        lessonId = "l4",
        courseTitle = "Lập trình Python",
        currentLessonTitle = "Bài 04: Cấu trúc điều kiện",
        thumbnailUrl = "thumb1",
        progressPercent = 45,
        remainingTimeText = "12:45 / 24:30"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMyCourses thanh cong - mac dinh chon filter ALL va dua khoa hoc hoan thanh xuong cuoi`() = runTest {
        coEvery { courseRepository.getMyCourses() } returns Result.success(sampleCourses)
        coEvery { courseRepository.getQuickResumeItem() } returns Result.success(sampleResumeItem)

        viewModel = MyCoursesViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MyCoursesUiState.Success)
        val successState = state as MyCoursesUiState.Success

        assertEquals(MyCoursesFilter.ALL, successState.selectedFilter)
        assertEquals(4, successState.allCount)
        assertEquals(3, successState.inProgressCount)
        assertEquals(1, successState.completedCount)
        assertEquals(4, successState.displayedCourses.size)
        assertEquals(100, successState.displayedCourses.last().progressPercent)
        assertNotNull(successState.quickResumeItem)
    }

    @Test
    fun `onFilterChanged sang IN_PROGRESS - chi hien thi cac khoa hoc dang hoc`() = runTest {
        coEvery { courseRepository.getMyCourses() } returns Result.success(sampleCourses)
        coEvery { courseRepository.getQuickResumeItem() } returns Result.success(sampleResumeItem)

        viewModel = MyCoursesViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFilterChanged(MyCoursesFilter.IN_PROGRESS)

        val state = viewModel.uiState.value as MyCoursesUiState.Success
        assertEquals(MyCoursesFilter.IN_PROGRESS, state.selectedFilter)
        assertEquals(3, state.displayedCourses.size)
        assertTrue(state.displayedCourses.all { it.progressPercent < 100 })
    }

    @Test
    fun `onFilterChanged sang COMPLETED - chi hien thi cac khoa hoc da hoan thanh`() = runTest {
        coEvery { courseRepository.getMyCourses() } returns Result.success(sampleCourses)
        coEvery { courseRepository.getQuickResumeItem() } returns Result.success(sampleResumeItem)

        viewModel = MyCoursesViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFilterChanged(MyCoursesFilter.COMPLETED)

        val state = viewModel.uiState.value as MyCoursesUiState.Success
        assertEquals(MyCoursesFilter.COMPLETED, state.selectedFilter)
        assertEquals(1, state.displayedCourses.size)
        assertEquals(100, state.displayedCourses.first().progressPercent)
        assertEquals("c3", state.displayedCourses.first().id)
    }

    @Test
    fun `onFilterChanged sang ALL - hien thi toan bo danh sach khoa hoc`() = runTest {
        coEvery { courseRepository.getMyCourses() } returns Result.success(sampleCourses)
        coEvery { courseRepository.getQuickResumeItem() } returns Result.success(sampleResumeItem)

        viewModel = MyCoursesViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFilterChanged(MyCoursesFilter.ALL)

        val state = viewModel.uiState.value as MyCoursesUiState.Success
        assertEquals(MyCoursesFilter.ALL, state.selectedFilter)
        assertEquals(4, state.displayedCourses.size)
    }

    @Test
    fun `loadMyCourses khi danh sach rong - chuyen sang trang thai Empty`() = runTest {
        coEvery { courseRepository.getMyCourses() } returns Result.success(emptyList())
        coEvery { courseRepository.getQuickResumeItem() } returns Result.success(null)

        viewModel = MyCoursesViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MyCoursesUiState.Empty)
    }

    @Test
    fun `loadMyCourses khi xay ra loi - chuyen sang trang thai Error`() = runTest {
        val errorMessage = "Lỗi kết nối mạng"
        coEvery { courseRepository.getMyCourses() } returns Result.failure(Exception(errorMessage))
        coEvery { courseRepository.getQuickResumeItem() } returns Result.success(null)

        viewModel = MyCoursesViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MyCoursesUiState.Error)
        assertEquals(errorMessage, (state as MyCoursesUiState.Error).message)
    }

    @Test
    fun `retry sau khi loi - nap lai du lieu thanh cong`() = runTest {
        coEvery { courseRepository.getMyCourses() } returns Result.failure(Exception("Lỗi tạm thời"))
        coEvery { courseRepository.getQuickResumeItem() } returns Result.success(null)

        viewModel = MyCoursesViewModel(courseRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is MyCoursesUiState.Error)

        coEvery { courseRepository.getMyCourses() } returns Result.success(sampleCourses)
        viewModel.loadMyCourses()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is MyCoursesUiState.Success)
    }
}
