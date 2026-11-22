package com.edumind.app.repository

import com.edumind.app.data.model.request.SubmitQuizAnswerRequest
import com.edumind.app.data.model.response.QuizAnswerResultDto
import com.edumind.app.data.model.response.QuizDto
import com.edumind.app.data.model.response.QuizMarker

interface QuizRepository {
    suspend fun getLessonQuizMarkers(lessonId: String): Result<List<QuizMarker>>
    suspend fun getQuiz(quizId: String): Result<QuizDto>
    suspend fun submitAnswer(request: SubmitQuizAnswerRequest): Result<QuizAnswerResultDto>
}
