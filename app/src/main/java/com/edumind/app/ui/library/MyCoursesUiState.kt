package com.edumind.app.ui.library

import com.edumind.app.data.model.response.ContinueLearningItem
import com.edumind.app.data.model.response.MyCourse

enum class MyCoursesFilter {
    ALL,
    IN_PROGRESS,
    COMPLETED
}

sealed interface MyCoursesUiState {
    data object Loading : MyCoursesUiState

    data class Success(
        val allCourses: List<MyCourse>,
        val displayedCourses: List<MyCourse>,
        val quickResumeItem: ContinueLearningItem?,
        val selectedFilter: MyCoursesFilter = MyCoursesFilter.IN_PROGRESS,
        val allCount: Int = 0,
        val inProgressCount: Int = 0,
        val completedCount: Int = 0
    ) : MyCoursesUiState

    data object Empty : MyCoursesUiState

    data class Error(val message: String) : MyCoursesUiState
}
