package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

enum class QuizQuestionType {
    @SerializedName("single_choice")
    SINGLE_CHOICE,

    @SerializedName("true_false")
    TRUE_FALSE
}

data class QuizOptionDto(
    @SerializedName("key") val key: String = "",
    @SerializedName("content") val content: String = ""
)

data class QuizQuestionDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("type") val type: QuizQuestionType = QuizQuestionType.SINGLE_CHOICE,
    @SerializedName("content") val content: String = "",
    @SerializedName("options") val options: List<QuizOptionDto> = emptyList(),
    @SerializedName("order") val order: Int = 0
)

data class QuizDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("questions") val questions: List<QuizQuestionDto> = emptyList(),
    @SerializedName("total_questions") val totalQuestions: Int = 0
)

data class QuizAnswerResultDto(
    @SerializedName("correct") val correct: Boolean = false,
    @SerializedName("correct_answer") val correctAnswer: String = "",
    @SerializedName("explanation") val explanation: String? = null
)
