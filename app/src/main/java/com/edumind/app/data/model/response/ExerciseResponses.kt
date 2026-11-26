package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

enum class ExerciseQuestionType {
    @SerializedName("single_choice")
    SINGLE_CHOICE,

    @SerializedName("multiple_choice")
    MULTIPLE_CHOICE,

    @SerializedName("true_false")
    TRUE_FALSE,

    @SerializedName("essay")
    ESSAY
}

enum class ExerciseSubmitStatus {
    @SerializedName("graded")
    GRADED,

    @SerializedName("pending_grading")
    PENDING_GRADING
}

data class ExerciseQuestionDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("type") val type: ExerciseQuestionType = ExerciseQuestionType.SINGLE_CHOICE,
    @SerializedName("content") val content: String = "",
    @SerializedName("options") val options: List<QuizOptionDto>? = null,
    @SerializedName("order") val order: Int = 0
)

data class ExerciseDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("lesson_id") val lessonId: String = "",
    @SerializedName("time_limit") val timeLimit: Int = 0,
    @SerializedName("passing_score") val passingScore: Int = 0,
    @SerializedName("max_attempts") val maxAttempts: Int = 0,
    @SerializedName("attempts_used") val attemptsUsed: Int = 0,
    @SerializedName("questions") val questions: List<ExerciseQuestionDto> = emptyList(),
    @SerializedName("total_questions") val totalQuestions: Int = 0
)

data class ExerciseAnswerResultDto(
    @SerializedName("question_id") val questionId: String = "",
    @SerializedName("correct") val correct: Boolean? = null,
    @SerializedName("correct_answer") val correctAnswer: String? = null,
    @SerializedName("explanation") val explanation: String? = null
)

data class ExerciseResultDto(
    @SerializedName("submission_id") val submissionId: String = "",
    @SerializedName("score") val score: Float? = null,
    @SerializedName("passed") val passed: Boolean? = null,
    @SerializedName("status") val status: ExerciseSubmitStatus = ExerciseSubmitStatus.GRADED,
    @SerializedName("attempts_remaining") val attemptsRemaining: Int = 0,
    @SerializedName("details") val details: List<ExerciseAnswerResultDto>? = null
)
