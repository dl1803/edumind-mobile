package com.edumind.app.repository

import com.edumind.app.data.model.response.Category
import com.edumind.app.data.model.response.CourseDetail
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.PagedResponse

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

    suspend fun enrollCourse(courseId: String): Result<Boolean>
}

