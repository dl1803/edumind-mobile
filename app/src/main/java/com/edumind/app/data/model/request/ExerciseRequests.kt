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
