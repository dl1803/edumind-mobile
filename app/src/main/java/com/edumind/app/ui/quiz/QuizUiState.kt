package com.edumind.app.ui.quiz

import com.edumind.app.data.model.response.QuizAnswerResultDto
import com.edumind.app.data.model.response.QuizDto
import com.edumind.app.data.model.response.QuizQuestionDto

sealed interface QuizUiState {
    data object Hidden : QuizUiState

    data object Loading : QuizUiState

    sealed interface Active : QuizUiState {
        val quiz: QuizDto
        val index: Int
        val selectedKey: String?
        val question: QuizQuestionDto get() = quiz.questions[index]
        val isLast: Boolean get() = index == quiz.questions.lastIndex
    }

    data class Question(
        override val quiz: QuizDto,
        override val index: Int,
        override val selectedKey: String? = null,
        val errorMessage: String? = null
    ) : Active

    data class Submitting(
        override val quiz: QuizDto,
        override val index: Int,
        override val selectedKey: String
    ) : Active

    data class Feedback(
        override val quiz: QuizDto,
        override val index: Int,
        override val selectedKey: String,
        val result: QuizAnswerResultDto
    ) : Active
}

data class QuizClosedEvent(
    val quizId: String,
    val answered: Boolean,
    val message: String? = null
)
