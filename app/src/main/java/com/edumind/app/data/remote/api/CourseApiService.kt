package com.edumind.app.data.remote.api

import com.edumind.app.data.model.response.CategoryListResponse
import com.edumind.app.data.model.response.CourseDetailResponse
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.PagedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CourseApiService {

    @GET("courses")
    suspend fun getCourses(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: String? = null,
        @Query("price_min") priceMin: Long? = null,
        @Query("price_max") priceMax: Long? = null,
        @Query("sort_by") sortBy: String? = "newest"
    ): Response<PagedResponse<CourseSummary>>

    @GET("categories")
    suspend fun getCategories(): Response<CategoryListResponse>

    @GET("courses/{course_id}")
    suspend fun getCourseDetail(
        @Path("course_id") courseId: String
    ): Response<CourseDetailResponse>
}
