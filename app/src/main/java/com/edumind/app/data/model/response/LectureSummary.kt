package com.edumind.app.data.model.response

data class SummarySection(
    val id: String,
    val title: String,
    val content: String,
    val startMs: Long? = null
)

data class LectureSummary(
    val lessonId: String,
    val keyPoints: List<String>,
    val sections: List<SummarySection>,
    val rawSummary: String = "",
    val isCached: Boolean = false,
    val generatedAt: String
) {
    val hasContent: Boolean
        get() = keyPoints.isNotEmpty() || sections.isNotEmpty() || rawSummary.isNotBlank()
}

