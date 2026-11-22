package com.edumind.app.data.remote.api

import com.edumind.app.data.model.request.SubmitQuizAnswerRequest
import com.edumind.app.data.model.response.QuizAnswerResultDto
import com.edumind.app.data.model.response.QuizDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface QuizApiService {

    @GET("quizzes/{quiz_id}")
    suspend fun getQuiz(
        @Path("quiz_id") quizId: String
    ): Response<QuizDto>

    @POST("quiz-responses")
    suspend fun submitAnswer(
        @Body request: SubmitQuizAnswerRequest
    ): Response<QuizAnswerResultDto>
}
