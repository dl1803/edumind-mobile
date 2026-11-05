package com.edumind.app.repository

import com.edumind.app.data.model.response.Category
import com.edumind.app.data.model.response.CategoryListResponse
import com.edumind.app.data.model.response.CourseDetailResponse
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.EnrolledCourseDto
import com.edumind.app.data.model.response.EnrollmentResponseDto
import com.edumind.app.data.model.response.MyCoursesResponseDto
import com.edumind.app.data.model.response.PagedResponse
import com.edumind.app.data.remote.api.CourseApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class CourseRepositoryTest {

    private val courseApiService: CourseApiService = mockk()
    private lateinit var repository: CourseRepositoryImpl

    private val pagedResponse = PagedResponse(
        data = listOf(CourseSummary(id = "c1", title = "Lập trình Kotlin cơ bản", priceVnd = 199_000L)),
        total = 21,
        page = 2,
        pageSize = 20,
        totalPages = 2
    )

    @Before
    fun setUp() {
        repository = CourseRepositoryImpl(courseApiService)
    }

    @Test
    fun getCourses_success_forwardsPagingAndFilterParamsAndReturnsBody() = runTest {
        coEvery {
            courseApiService.getCourses(
                page = 2,
                pageSize = 20,
                search = "python",
                categoryId = "cat_it",
                priceMin = 1L,
                priceMax = 499_999L,
                sortBy = "price_asc"
            )
        } returns Response.success(pagedResponse)

        val result = repository.getCourses(
            page = 2,
            pageSize = 20,
            search = "python",
            categoryId = "cat_it",
            priceMin = 1L,
            priceMax = 499_999L,
            minRating = 4.0f,
            sortBy = "price_asc"
        )

        assertTrue(result.isSuccess)
        assertEquals(pagedResponse, result.getOrThrow())
        coVerify(exactly = 1) {
            courseApiService.getCourses(
                page = 2,
                pageSize = 20,
                search = "python",
                categoryId = "cat_it",
                priceMin = 1L,
                priceMax = 499_999L,
                sortBy = "price_asc"
            )
        }
    }

    @Test
    fun getCourses_http404_returnsNotFoundMessage() = runTest {
        stubGetCoursesError(404)

        val result = repository.getCourses()

        assertTrue(result.isFailure)
        assertEquals("Không tìm thấy danh sách khóa học phù hợp.", result.exceptionOrNull()?.message)
    }

    @Test
    fun getCourses_http401_returnsSessionExpiredMessage() = runTest {
        stubGetCoursesError(401)

        val result = repository.getCourses()

        assertTrue(result.isFailure)
        assertEquals("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", result.exceptionOrNull()?.message)
    }

    @Test
    fun getCourses_http500_returnsGenericMessage() = runTest {
        stubGetCoursesError(500)

        val result = repository.getCourses()

        assertTrue(result.isFailure)
        assertEquals(
            "Không thể tải danh sách khóa học lúc này. Vui lòng thử lại sau.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun getCourses_ioException_returnsConnectionMessage() = runTest {
        coEvery {
            courseApiService.getCourses(any(), any(), any(), any(), any(), any(), any())
        } throws IOException("timeout")

        val result = repository.getCourses()

        assertTrue(result.isFailure)
        assertEquals(
            "Không thể kết nối đến máy chủ. Vui lòng kiểm tra đường truyền mạng.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun getCategories_success_returnsCategoryList() = runTest {
        val sampleCategories = listOf(
            Category(id = "cat_it", name = "Công nghệ thông tin", courseCount = 10),
            Category(id = "cat_biz", name = "Kinh doanh", courseCount = 5)
        )
        coEvery { courseApiService.getCategories() } returns Response.success(
            CategoryListResponse(data = sampleCategories)
        )

        val result = repository.getCategories()

        assertTrue(result.isSuccess)
        val categories = result.getOrThrow()
        assertEquals(2, categories.size)
        assertEquals("cat_it", categories[0].id)
    }

    @Test
    fun getCategories_networkError_returnsFailure() = runTest {
        coEvery { courseApiService.getCategories() } throws IOException("network error")

        val result = repository.getCategories()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("máy chủ") == true)
    }

    @Test
    fun getCourseDetail_success_returnsMappedDomain() = runTest {
        val sampleDetail = CourseDetailResponse(
            id = "course_01",
            title = "Kotlin căn bản",
            price = 0L
        )
        coEvery { courseApiService.getCourseDetail("course_01") } returns Response.success(sampleDetail)

        val result = repository.getCourseDetail("course_01")

        assertTrue(result.isSuccess)
        val detail = result.getOrThrow()
        assertEquals("course_01", detail.id)
        assertEquals("Kotlin căn bản", detail.title)
    }

    @Test
    fun getCourseDetail_http404_returnsFailure() = runTest {
        coEvery { courseApiService.getCourseDetail("course_not_found") } returns Response.error(
            404,
            "".toResponseBody(null)
        )

        val result = repository.getCourseDetail("course_not_found")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("không khả dụng") == true)
    }

    @Test
    fun enroll_success200_returnsEnrollResultSuccess() = runTest {
        coEvery { courseApiService.enroll("course_01") } returns Response.success(
            EnrollmentResponseDto(
                enrollmentId = "e1",
                courseId = "course_01",
                firstLessonId = "lesson_01"
            )
        )

        val result = repository.enroll("course_01")

        assertTrue(result is EnrollResult.Success)
        assertEquals("lesson_01", (result as EnrollResult.Success).firstLessonId)
    }

    @Test
    fun enroll_conflict409_returnsAlreadyEnrolled() = runTest {
        coEvery { courseApiService.enroll("course_01") } returns Response.error(409, "".toResponseBody(null))

        val result = repository.enroll("course_01")

        assertTrue(result is EnrollResult.AlreadyEnrolled)
    }

    @Test
    fun enroll_paymentRequired403_returnsPaymentRequired() = runTest {
        coEvery { courseApiService.enroll("course_paid") } returns Response.error(403, "".toResponseBody(null))

        val result = repository.enroll("course_paid")

        assertTrue(result is EnrollResult.PaymentRequired)
    }

    @Test
    fun getMyCourses_success_returnsMappedDomainList() = runTest {
        val sampleMyCourses = listOf(
            EnrolledCourseDto(
                courseId = "my_01",
                title = "Android Căn bản",
                progressPercent = 45f
            )
        )
        coEvery { courseApiService.getMyCourses() } returns Response.success(
            MyCoursesResponseDto(data = sampleMyCourses)
        )

        val result = repository.getMyCourses()

        assertTrue(result.isSuccess)
        val list = result.getOrThrow()
        assertEquals(1, list.size)
        assertEquals("my_01", list[0].id)
        assertEquals(45, list[0].progressPercent)
    }

    private fun stubGetCoursesError(code: Int) {
        coEvery {
            courseApiService.getCourses(any(), any(), any(), any(), any(), any(), any())
        } returns Response.error(code, "".toResponseBody(null))
    }
}

