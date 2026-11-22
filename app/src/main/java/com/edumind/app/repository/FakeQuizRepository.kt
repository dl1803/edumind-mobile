package com.edumind.app.repository

import com.edumind.app.data.model.request.SubmitQuizAnswerRequest
import com.edumind.app.data.model.response.QuizAnswerResultDto
import com.edumind.app.data.model.response.QuizDto
import com.edumind.app.data.model.response.QuizMarker
import com.edumind.app.data.model.response.QuizOptionDto
import com.edumind.app.data.model.response.QuizQuestionDto
import com.edumind.app.data.model.response.QuizQuestionType
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeQuizRepository @Inject constructor() : QuizRepository {

    override suspend fun getLessonQuizMarkers(lessonId: String): Result<List<QuizMarker>> {
        delay(NETWORK_DELAY_MS)
        return Result.success(MARKERS)
    }

    override suspend fun getQuiz(quizId: String): Result<QuizDto> {
        delay(NETWORK_DELAY_MS)
        val quiz = QUIZZES.firstOrNull { it.id == quizId }
            ?: return Result.failure(NoSuchElementException("Không tìm thấy quiz $quizId"))
        return Result.success(quiz)
    }

    override suspend fun submitAnswer(request: SubmitQuizAnswerRequest): Result<QuizAnswerResultDto> {
        delay(NETWORK_DELAY_MS)
        val key = ANSWER_KEYS[request.questionId]
            ?: return Result.failure(NoSuchElementException("Không tìm thấy câu hỏi ${request.questionId}"))
        return Result.success(
            QuizAnswerResultDto(
                correct = request.answer == key.correctAnswer,
                correctAnswer = key.correctAnswer,
                explanation = key.explanation
            )
        )
    }

    private data class AnswerKey(val correctAnswer: String, val explanation: String?)

    companion object {
        private const val NETWORK_DELAY_MS = 400L

        private val MARKERS = listOf(
            QuizMarker(quizId = "quiz-mock-01", timestampSec = 30f, isAnswered = false),
            QuizMarker(quizId = "quiz-mock-02", timestampSec = 90f, isAnswered = false)
        )

        private val QUIZZES = listOf(
            QuizDto(
                id = "quiz-mock-01",
                questions = listOf(
                    QuizQuestionDto(
                        id = "quiz-mock-01-q1",
                        type = QuizQuestionType.SINGLE_CHOICE,
                        content = "Trong Python, hàm nào dùng để in nội dung ra màn hình?",
                        options = listOf(
                            QuizOptionDto("A", "print()"),
                            QuizOptionDto("B", "echo()"),
                            QuizOptionDto("C", "console.log()"),
                            QuizOptionDto("D", "printf()")
                        ),
                        order = 1
                    )
                ),
                totalQuestions = 1
            ),
            QuizDto(
                id = "quiz-mock-02",
                questions = listOf(
                    QuizQuestionDto(
                        id = "quiz-mock-02-q1",
                        type = QuizQuestionType.TRUE_FALSE,
                        content = "Python yêu cầu khai báo kiểu dữ liệu trước khi gán giá trị cho biến.",
                        options = listOf(
                            QuizOptionDto("A", "Đúng"),
                            QuizOptionDto("B", "Sai")
                        ),
                        order = 1
                    ),
                    QuizQuestionDto(
                        id = "quiz-mock-02-q2",
                        type = QuizQuestionType.SINGLE_CHOICE,
                        content = "Giá trị 3.14 trong Python thuộc kiểu dữ liệu nào?",
                        options = listOf(
                            QuizOptionDto("A", "int"),
                            QuizOptionDto("B", "float"),
                            QuizOptionDto("C", "str"),
                            QuizOptionDto("D", "bool")
                        ),
                        order = 2
                    )
                ),
                totalQuestions = 2
            )
        )

        private val ANSWER_KEYS = mapOf(
            "quiz-mock-01-q1" to AnswerKey("A", "Python dùng hàm print() để in nội dung ra màn hình."),
            "quiz-mock-02-q1" to AnswerKey("B", "Python tự xác định kiểu dữ liệu khi gán giá trị nên không cần khai báo trước."),
            "quiz-mock-02-q2" to AnswerKey("B", "3.14 là số thực nên có kiểu float.")
        )
    }
}
