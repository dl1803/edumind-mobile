package com.edumind.app.data.model.request

import com.google.gson.annotations.SerializedName

data class SubmitQuizAnswerRequest(
    @SerializedName("quiz_id") val quizId: String,
    @SerializedName("question_id") val questionId: String,
    @SerializedName("answer") val answer: String,
    @SerializedName("time_taken") val timeTaken: Int? = null
)
