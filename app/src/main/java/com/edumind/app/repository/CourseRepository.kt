package com.edumind.app.repository

import com.edumind.app.data.model.response.Category
import com.edumind.app.data.model.response.ContinueLearningItem
import com.edumind.app.data.model.response.CourseDetail
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.MyCourse
import com.edumind.app.data.model.response.PagedResponse

sealed interface EnrollResult {
    data class Success(val firstLessonId: String? = null) : EnrollResult
    data object AlreadyEnrolled : EnrollResult
    data object PaymentRequired : EnrollResult
    data class Failure(val message: String) : EnrollResult
}

interface CourseRepository {
    suspend fun getCourses(
        page: Int = 1,
        pageSize: Int = 20,
        search: String? = null,
        categoryId: String? = null,
        priceMin: Long? = null,
        priceMax: Long? = null,
        minRating: Float? = null,
        sortBy: String = "newest"
    ): Result<PagedResponse<CourseSummary>>

    suspend fun getCategories(): Result<List<Category>>

    suspend fun getCourseDetail(courseId: String): Result<CourseDetail>

    suspend fun enroll(courseId: String): EnrollResult

    suspend fun enrollCourse(courseId: String): Result<Boolean> = when (val res = enroll(courseId)) {
        is EnrollResult.Success, is EnrollResult.AlreadyEnrolled -> Result.success(true)
        is EnrollResult.PaymentRequired -> Result.failure(Exception("Khóa học có phí, vui lòng mua để học"))
        is EnrollResult.Failure -> Result.failure(Exception(res.message))
    }

    suspend fun getMyCourses(): Result<List<MyCourse>>

    suspend fun getQuickResumeItem(): Result<ContinueLearningItem?>
}
