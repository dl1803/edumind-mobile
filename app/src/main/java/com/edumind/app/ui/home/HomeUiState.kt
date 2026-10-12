package com.edumind.app.ui.home

import com.edumind.app.data.model.response.ContinueLearningItem
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.FeaturedCourse

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val featuredCourses: List<FeaturedCourse> = emptyList(),
    val continueLearningItems: List<ContinueLearningItem> = emptyList(),
    val recommendedCourses: List<CourseSummary> = emptyList(),
    val newCourses: List<CourseSummary> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val userDisplayName: String = "Nguyễn Văn Minh",
    val streakDays: Int = 5,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null &&
                featuredCourses.isEmpty() &&
                continueLearningItems.isEmpty() &&
                recommendedCourses.isEmpty() &&
                newCourses.isEmpty()
}
