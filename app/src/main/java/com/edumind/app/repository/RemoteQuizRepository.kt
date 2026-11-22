package com.edumind.app.repository

import com.edumind.app.data.model.request.SubmitQuizAnswerRequest
import com.edumind.app.data.model.response.QuizAnswerResultDto
import com.edumind.app.data.model.response.QuizDto
import com.edumind.app.data.model.response.QuizMarker
import com.edumind.app.data.remote.api.LessonApiService
import com.edumind.app.data.remote.api.QuizApiService
import com.edumind.app.util.ApiErrorParser
import com.edumind.app.util.NetworkException
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteQuizRepository @Inject constructor(
    private val quizApiService: QuizApiService,
    private val lessonApiService: LessonApiService
) : QuizRepository {

    override suspend fun getLessonQuizMarkers(lessonId: String): Result<List<QuizMarker>> {
        return execute("Không thể tải danh sách quiz của bài giảng.") {
            lessonApiService.getLessonDetail(lessonId)
        }.map { detail ->
            detail.quizMarkers.map { QuizMarker(it.quizId, it.timestamp, it.isAnswered) }
        }
    }

    override suspend fun getQuiz(quizId: String): Result<QuizDto> {
        return execute("Không thể tải câu hỏi quiz.") { quizApiService.getQuiz(quizId) }
    }

    override suspend fun submitAnswer(request: SubmitQuizAnswerRequest): Result<QuizAnswerResultDto> {
        return execute("Không thể gửi đáp án quiz.") { quizApiService.submitAnswer(request) }
    }

    private suspend fun <T> execute(
        defaultMessage: String,
        call: suspend () -> Response<T>
    ): Result<T> {
        return try {
            val response = call()
            val body = response.body()
            when {
                response.isSuccessful && body != null -> Result.success(body)
                response.isSuccessful -> Result.failure(Exception(defaultMessage))
                else -> Result.failure(ApiErrorParser.parseError(response.code(), response.errorBody()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(NetworkException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
