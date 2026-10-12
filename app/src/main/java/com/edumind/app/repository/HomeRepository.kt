package com.edumind.app.repository

import com.edumind.app.data.model.response.ContinueLearningItem
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.FeaturedCourse

interface HomeRepository {
    suspend fun getFeaturedCourses(): Result<List<FeaturedCourse>>
    suspend fun getContinueLearningItems(): Result<List<ContinueLearningItem>>
    suspend fun getRecommendedCourses(): Result<List<CourseSummary>>
    suspend fun getNewCourses(page: Int = 1): Result<List<CourseSummary>>
    suspend fun getUnreadNotificationCount(): Result<Int>
}
