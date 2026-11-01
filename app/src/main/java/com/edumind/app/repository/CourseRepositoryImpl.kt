package com.edumind.app.repository

import com.edumind.app.data.model.response.Category
import com.edumind.app.data.model.response.ContinueLearningItem
import com.edumind.app.data.model.response.CourseDetail
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.MyCourse
import com.edumind.app.data.model.response.PagedResponse
import com.edumind.app.data.model.response.toDomain
import com.edumind.app.data.remote.api.CourseApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val courseApiService: CourseApiService
) : CourseRepository {

    override suspend fun getCourses(
        page: Int,
        pageSize: Int,
        search: String?,
        categoryId: String?,
        priceMin: Long?,
        priceMax: Long?,
        minRating: Float?,
        sortBy: String
    ): Result<PagedResponse<CourseSummary>> {
        return try {
            val response = courseApiService.getCourses(
                page = page,
                pageSize = pageSize,
                search = search,
                categoryId = categoryId,
                priceMin = priceMin,
                priceMax = priceMax,
                sortBy = sortBy
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Không tìm thấy danh sách khóa học phù hợp."
                    401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
                    else -> "Không thể tải danh sách khóa học lúc này. Vui lòng thử lại sau."
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ. Vui lòng kiểm tra đường truyền mạng."))
        }
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = courseApiService.getCategories()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception("Không thể tải danh mục khóa học lúc này."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng."))
        }
    }

    override suspend fun getCourseDetail(courseId: String): Result<CourseDetail> {
        return try {
            val response = courseApiService.getCourseDetail(courseId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body.toDomain())
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Khóa học này hiện không khả dụng hoặc đã bị gỡ bỏ."
                    403 -> "Bạn chưa có quyền truy cập vào nội dung khóa học này."
                    else -> "Không thể tải thông tin khóa học lúc này. Vui lòng thử lại sau."
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng."))
        }
    }

    override suspend fun enroll(courseId: String): EnrollResult {
        return try {
            val response = courseApiService.enroll(courseId)
            when (response.code()) {
                200, 201 -> {
                    val body = response.body()
                    EnrollResult.Success(body?.firstLessonId)
                }
                409 -> EnrollResult.AlreadyEnrolled
                400, 403 -> EnrollResult.PaymentRequired
                else -> {
                    val msg = when (response.code()) {
                        401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
                        404 -> "Không tìm thấy khóa học để đăng ký."
                        else -> "Ghi danh khóa học thất bại. Vui lòng thử lại sau."
                    }
                    EnrollResult.Failure(msg)
                }
            }
        } catch (e: Exception) {
            EnrollResult.Failure("Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.")
        }
    }

    override suspend fun enrollCourse(courseId: String): Result<Boolean> {
        return when (val res = enroll(courseId)) {
            is EnrollResult.Success, is EnrollResult.AlreadyEnrolled -> Result.success(true)
            is EnrollResult.PaymentRequired -> Result.failure(Exception("Khóa học có phí, vui lòng mua để học"))
            is EnrollResult.Failure -> Result.failure(Exception(res.message))
        }
    }

    override suspend fun getMyCourses(): Result<List<MyCourse>> {
        return try {
            val response = courseApiService.getMyCourses()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val courses = body.data.map { it.toDomain() }
                Result.success(courses)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
                    else -> "Không thể tải danh sách khóa học của bạn."
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng."))
        }
    }

    override suspend fun getQuickResumeItem(): Result<ContinueLearningItem?> {
        return Result.failure(UnsupportedOperationException("Endpoint tiến độ học nhanh sẽ được tích hợp tại Ngày 25"))
    }
}
