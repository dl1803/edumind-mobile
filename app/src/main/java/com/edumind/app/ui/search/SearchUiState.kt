package com.edumind.app.ui.search

import com.edumind.app.data.model.response.CourseSummary

sealed interface SearchUiState {
    data class Idle(
        val keyword: String = "",
        val suggestions: List<CourseSummary> = emptyList(),
        val recentSearches: List<String> = listOf("Python cơ bản", "Machine Learning cho người mới", "Data Science"),
        val trendingKeywords: List<String> = listOf("Python", "AI Trợ giảng", "SQL Database", "React Native", "Kotlin Android")
    ) : SearchUiState

    data object Loading : SearchUiState

    data class Success(
        val keyword: String,
        val courses: List<CourseSummary>
    ) : SearchUiState

    data class Empty(
        val keyword: String,
        val suggestedKeywords: List<String> = listOf(
            "Python",
            "Lập trình Web",
            "Cơ sở dữ liệu SQL",
            "Machine Learning",
            "UI/UX Design"
        )
    ) : SearchUiState

    data class Error(
        val message: String
    ) : SearchUiState
}
