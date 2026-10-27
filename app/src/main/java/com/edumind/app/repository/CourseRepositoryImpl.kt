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
                Result.failure(Exception("Lỗi tải danh sách khóa học (${response.code()}): ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = courseApiService.getCategories()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Lỗi tải danh mục (${response.code()}): ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCourseDetail(courseId: String): Result<CourseDetail> {
        return try {
            val response = courseApiService.getCourseDetail(courseId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body.toDomain())
            } else {
                when (response.code()) {
                    404 -> Result.failure(Exception("Khóa học không tồn tại hoặc đã bị gỡ bỏ"))
                    403 -> Result.failure(Exception("Bạn không có quyền truy cập khóa học này"))
                    else -> Result.failure(Exception("Không thể tải thông tin khóa học (Mã ${response.code()})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun enrollCourse(courseId: String): Result<Boolean> {
        return Result.failure(UnsupportedOperationException("Endpoint đăng ký khóa học sẽ được tích hợp tại Ngày 24"))
    }

    override suspend fun getMyCourses(): Result<List<MyCourse>> {
        return Result.failure(UnsupportedOperationException("Endpoint khóa học của tôi sẽ được tích hợp tại Ngày 24"))
    }

    override suspend fun getQuickResumeItem(): Result<ContinueLearningItem?> {
        return Result.failure(UnsupportedOperationException("Endpoint tiến độ học nhanh sẽ được tích hợp tại Ngày 25"))
    }
}
