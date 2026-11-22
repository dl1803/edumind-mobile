package com.edumind.app.repository

import com.edumind.app.data.model.request.SubmitQuizAnswerRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeQuizRepositoryTest {

    private val repository = FakeQuizRepository()

    @Test
    fun markersMatchQuizzes() {
        runBlocking {
            val markers = repository.getLessonQuizMarkers("lesson-any").getOrThrow()
            assertEquals(listOf(30f, 90f), markers.map { it.timestampSec })
            markers.forEach { marker ->
                val quiz = repository.getQuiz(marker.quizId).getOrThrow()
                assertEquals(marker.quizId, quiz.id)
                assertEquals(quiz.questions.size, quiz.totalQuestions)
            }
        }
    }

    @Test
    fun submitAnswerGradesAndRejectsUnknownQuestion() {
        runBlocking {
            val questionId = "quiz-mock-01-q1"
            val right = repository.submitAnswer(SubmitQuizAnswerRequest("quiz-mock-01", questionId, "A")).getOrThrow()
            assertTrue(right.correct)
            val wrong = repository.submitAnswer(SubmitQuizAnswerRequest("quiz-mock-01", questionId, "B")).getOrThrow()
            assertTrue(!wrong.correct)
            assertEquals("A", wrong.correctAnswer)
            assertTrue(repository.submitAnswer(SubmitQuizAnswerRequest("quiz-mock-01", "unknown", "A")).isFailure)
        }
    }
}
