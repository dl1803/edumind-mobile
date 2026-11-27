package com.edumind.app.ui.exercise

enum class ExerciseVerdict { CORRECT, WRONG, SKIPPED, UNGRADED }

enum class ExerciseOutcome { PASSED, FAILED, PENDING }

data class ExerciseReviewItem(
    val id: String,
    val number: Int,
    val content: String,
    val verdict: ExerciseVerdict,
    val yourAnswerLines: List<String>,
    val correctAnswerLines: List<String>,
    val explanation: String?
)

data class ExerciseResultContent(
    val exerciseId: String,
    val exerciseTitle: String,
    val mcqAnsweredCount: Int,
    val mcqTotalCount: Int,
    val essayAnsweredCount: Int,
    val essayTotalCount: Int,
    val outcome: ExerciseOutcome,
    val scorePercent: Int?,
    val passingScore: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val answeredCount: Int,
    val skippedCount: Int,
    val timeSpentSeconds: Int,
    val canRetry: Boolean,
    val reviewItems: List<ExerciseReviewItem>
)

sealed interface ExerciseResultUiState {
    data object Missing : ExerciseResultUiState
    data class Content(val content: ExerciseResultContent) : ExerciseResultUiState
}
