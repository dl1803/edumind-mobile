package com.edumind.app.ui.ai.summary

import com.edumind.app.data.model.response.LectureSummary

sealed interface SummaryUiState {
    data object Empty : SummaryUiState
    data object Loading : SummaryUiState
    data class Success(
        val summary: LectureSummary,
        val expandedSectionIds: Set<String>
    ) : SummaryUiState
    data class Error(val message: String) : SummaryUiState
}

