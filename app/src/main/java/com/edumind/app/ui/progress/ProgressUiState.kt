package com.edumind.app.ui.progress

import com.edumind.app.data.model.response.LearningProgressResponse

sealed interface ProgressUiState {
    data object Loading : ProgressUiState

    data class Success(
        val data: LearningProgressResponse,
        val overallPercent: Int,
        val isGoalSheetVisible: Boolean = false
    ) : ProgressUiState

    data object Empty : ProgressUiState

    data class Error(val message: String) : ProgressUiState
}
