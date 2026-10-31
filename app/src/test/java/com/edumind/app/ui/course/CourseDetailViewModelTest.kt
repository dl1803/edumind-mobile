package com.edumind.app.ui.course

import androidx.lifecycle.SavedStateHandle
import com.edumind.app.data.model.response.CourseDetail
import com.edumind.app.data.model.response.CourseLesson
import com.edumind.app.data.model.response.CourseSection
import com.edumind.app.data.model.response.InstructorSummary
import com.edumind.app.data.model.response.LessonType
import com.edumind.app.repository.CourseRepository
import com.edumind.app.repository.EnrollResult
import com.edumind.app.ui.navigation.Screen
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
class CourseDetailViewModelTest {

    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private val sampleCourseId = "c_python_01"

    private val sampleCourseDetail = CourseDetail(
        id = sampleCourseId,
        title = "Nhập môn Lập trình Python: Từ Zero đến Chuyên sâu",
        categoryName = "Lập trình",
        thumbnailUrl = "thumb",
        priceVnd = 499_000L,
        originalPriceVnd = 799_000L,
        rating = 4.8f,
        reviewCount = 142,
        studentCount = 1520,
        totalHours = 14,
        description = "Mô tả khóa học Python",
        instructor = InstructorSummary(
            id = "ins_1",
            name = "TS. Lê Hoàng Nam",
            title = "Giảng viên CNTT",
            rating = 4.9f,
            courseCount = 12,
            studentCount = 1520
        ),
        sections = listOf(
            CourseSection(
                id = "sec_1",
                title = "Chương 1: Cài đặt môi trường",
                durationSummary = "3 bài • 45 phút",
                lessons = listOf(
                    CourseLesson("l1", "Bài 1", "15p", LessonType.VIDEO, isFreePreview = true)
                )
            ),
            CourseSection(
                id = "sec_2",
                title = "Chương 2: Cú pháp cơ bản",
                durationSummary = "4 bài • 1h 20p",
                lessons = listOf(
                    CourseLesson("l2", "Bài 2", "20p", LessonType.VIDEO, isFreePreview = false)
                )
            ),
            CourseSection(
                id = "sec_3",
                title = "Chương 3: Cấu trúc điều khiển",
                durationSummary = "2 bài • 40 phút",
                lessons = listOf(
                    CourseLesson("l3", "Bài 3", "25p", LessonType.VIDEO, isFreePreview = false)
                )
            )
        ),
        isEnrolled = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { courseRepository.getCourseDetail(sampleCourseId) } returns Result.success(sampleCourseDetail)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCourseDetail success updates state to Success and expands initial 2 sections`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to sampleCourseId))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CourseDetailUiState.Success)
        val success = state as CourseDetailUiState.Success
        assertEquals(sampleCourseId, success.course.id)
        assertEquals(2, success.expandedSectionIds.size)
        assertTrue(success.expandedSectionIds.contains("sec_1"))
        assertTrue(success.expandedSectionIds.contains("sec_2"))
        assertFalse(success.expandedSectionIds.contains("sec_3"))
    }

    @Test
    fun `loadCourseDetail failure updates state to Error`() = runTest {
        coEvery { courseRepository.getCourseDetail("invalid_id") } returns Result.failure(Exception("Khóa học không tồn tại"))

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to "invalid_id"))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CourseDetailUiState.Error)
        assertEquals("Khóa học không tồn tại", (state as CourseDetailUiState.Error).message)
    }

    @Test
    fun `onTabSelected switches selected tab index`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to sampleCourseId))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)
        advanceUntilIdle()

        viewModel.onTabSelected(1)
        val state1 = viewModel.uiState.value as CourseDetailUiState.Success
        assertEquals(1, state1.selectedTab)

        viewModel.onTabSelected(0)
        val state0 = viewModel.uiState.value as CourseDetailUiState.Success
        assertEquals(0, state0.selectedTab)
    }

    @Test
    fun `onToggleSection toggles specific section expansion`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to sampleCourseId))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)
        advanceUntilIdle()

        viewModel.onToggleSection("sec_1")
        val state1 = viewModel.uiState.value as CourseDetailUiState.Success
        assertFalse(state1.expandedSectionIds.contains("sec_1"))

        viewModel.onToggleSection("sec_1")
        val state2 = viewModel.uiState.value as CourseDetailUiState.Success
        assertTrue(state2.expandedSectionIds.contains("sec_1"))
    }

    @Test
    fun `onToggleExpandAll collapses all when all are expanded and expands all otherwise`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to sampleCourseId))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)
        advanceUntilIdle()

        viewModel.onToggleExpandAll()
        val allExpandedState = viewModel.uiState.value as CourseDetailUiState.Success
        assertEquals(3, allExpandedState.expandedSectionIds.size)

        viewModel.onToggleExpandAll()
        val allCollapsedState = viewModel.uiState.value as CourseDetailUiState.Success
        assertTrue(allCollapsedState.expandedSectionIds.isEmpty())
    }

    @Test
    fun `onEnrollFreeCourse success marks course as enrolled and invokes callback`() = runTest {
        coEvery { courseRepository.enroll(sampleCourseId) } returns EnrollResult.Success("l_1_1")

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to sampleCourseId))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)
        advanceUntilIdle()

        var callbackInvoked = false
        viewModel.onEnrollFreeCourse {
            callbackInvoked = true
        }

        advanceUntilIdle()

        assertTrue(callbackInvoked)
        val state = viewModel.uiState.value as CourseDetailUiState.Success
        assertTrue(state.course.isEnrolled)
        assertFalse(state.isEnrolling)
    }

    @Test
    fun `onEnrollFreeCourse failure sets userMessage and resets isEnrolling`() = runTest {
        coEvery { courseRepository.enroll(sampleCourseId) } returns EnrollResult.Failure("Ghi danh thất bại")

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to sampleCourseId))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)
        advanceUntilIdle()

        viewModel.onEnrollFreeCourse {}
        advanceUntilIdle()

        val state = viewModel.uiState.value as CourseDetailUiState.Success
        assertFalse(state.isEnrolling)
        assertEquals("Ghi danh thất bại", state.userMessage)

        viewModel.clearUserMessage()
        val clearedState = viewModel.uiState.value as CourseDetailUiState.Success
        assertNull(clearedState.userMessage)
    }

    @Test
    fun `onEnrollClick with EnrollResult Success updates enrolled state and invokes onSuccess`() = runTest {
        coEvery { courseRepository.enroll(sampleCourseId) } returns EnrollResult.Success(firstLessonId = "l_1_1")

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to sampleCourseId))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)
        advanceUntilIdle()

        var receivedLessonId: String? = null
        var successCallbackInvoked = false
        viewModel.onEnrollClick(
            onSuccess = { lessonId ->
                successCallbackInvoked = true
                receivedLessonId = lessonId
            }
        )
        advanceUntilIdle()

        assertTrue(successCallbackInvoked)
        assertEquals("l_1_1", receivedLessonId)

        val state = viewModel.uiState.value as CourseDetailUiState.Success
        assertFalse(state.isEnrolling)
        assertTrue(state.course.isEnrolled)
        assertEquals("l_1_1", state.course.lastLessonId)
        assertEquals("Đăng ký khóa học thành công!", state.userMessage)
    }

    @Test
    fun `onEnrollClick with EnrollResult AlreadyEnrolled handles 409 conflict gracefully`() = runTest {
        coEvery { courseRepository.enroll(sampleCourseId) } returns EnrollResult.AlreadyEnrolled

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to sampleCourseId))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)
        advanceUntilIdle()

        var successCallbackInvoked = false
        viewModel.onEnrollClick(
            onSuccess = {
                successCallbackInvoked = true
            }
        )
        advanceUntilIdle()

        assertTrue(successCallbackInvoked)
        val state = viewModel.uiState.value as CourseDetailUiState.Success
        assertFalse(state.isEnrolling)
        assertTrue(state.course.isEnrolled)
        assertEquals("Bạn đã đăng ký khóa học này trước đó.", state.userMessage)
    }

    @Test
    fun `onEnrollClick with EnrollResult PaymentRequired invokes onPaymentRequired callback`() = runTest {
        coEvery { courseRepository.enroll(sampleCourseId) } returns EnrollResult.PaymentRequired

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to sampleCourseId))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)
        advanceUntilIdle()

        var paymentCallbackInvoked = false
        var successCallbackInvoked = false
        viewModel.onEnrollClick(
            onSuccess = { successCallbackInvoked = true },
            onPaymentRequired = { paymentCallbackInvoked = true }
        )
        advanceUntilIdle()

        assertTrue(paymentCallbackInvoked)
        assertFalse(successCallbackInvoked)

        val state = viewModel.uiState.value as CourseDetailUiState.Success
        assertFalse(state.isEnrolling)
        assertFalse(state.course.isEnrolled)
        assertEquals("Khóa học có phí, vui lòng thanh toán để mở khóa.", state.userMessage)
    }

    @Test
    fun `onEnrollClick with EnrollResult Failure sets error message and resets isEnrolling`() = runTest {
        val errorMessage = "Không tìm thấy khóa học để đăng ký."
        coEvery { courseRepository.enroll(sampleCourseId) } returns EnrollResult.Failure(errorMessage)

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to sampleCourseId))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)
        advanceUntilIdle()

        viewModel.onEnrollClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value as CourseDetailUiState.Success
        assertFalse(state.isEnrolling)
        assertFalse(state.course.isEnrolled)
        assertEquals(errorMessage, state.userMessage)
    }

    @Test
    fun `onEnrollClick prevents duplicate requests when already enrolling`() = runTest {
        coEvery { courseRepository.enroll(sampleCourseId) } coAnswers {
            kotlinx.coroutines.delay(500)
            EnrollResult.Success("l_1_1")
        }

        val savedStateHandle = SavedStateHandle(mapOf(Screen.ARG_COURSE_ID to sampleCourseId))
        val viewModel = CourseDetailViewModel(savedStateHandle, courseRepository)
        advanceUntilIdle()

        viewModel.onEnrollClick()
        viewModel.onEnrollClick()

        advanceUntilIdle()

        coVerify(exactly = 1) { courseRepository.enroll(sampleCourseId) }
    }
}
