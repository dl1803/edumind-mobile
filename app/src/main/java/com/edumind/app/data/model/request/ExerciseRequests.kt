package com.edumind.app.data.model.request

import com.google.gson.annotations.SerializedName

data class ExerciseAnswerRequest(
    @SerializedName("question_id") val questionId: String,
    @SerializedName("answer") val answer: String
)

data class SubmitExerciseRequest(
    @SerializedName("answers") val answers: List<ExerciseAnswerRequest>,
    @SerializedName("time_spent") val timeSpent: Int? = null
)

object ExerciseAnswerFormat {
    private const val DELIMITER = ","

    fun encode(keys: Set<String>): String = keys.sorted().joinToString(DELIMITER)

    fun decode(raw: String?): Set<String> =
        raw.orEmpty()
            .split(DELIMITER)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
}
