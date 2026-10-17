package com.edumind.app.ui.course

import com.edumind.app.data.model.response.CourseDetail

sealed interface CourseDetailUiState {
    data object Loading : CourseDetailUiState

    data class Success(
        val course: CourseDetail,
        val selectedTab: Int = 0,
        val expandedSectionIds: Set<String> = emptySet(),
        val isEnrolling: Boolean = false,
        val userMessage: String? = null
    ) : CourseDetailUiState

    data class Error(
        val message: String
    ) : CourseDetailUiState
}
