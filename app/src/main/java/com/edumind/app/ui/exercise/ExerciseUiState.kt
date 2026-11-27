package com.edumind.app.ui.exercise

import com.edumind.app.data.model.response.ExerciseDto
import com.edumind.app.data.model.response.ExerciseQuestionDto
import com.edumind.app.data.model.response.ExerciseQuestionType
import java.io.Serializable

const val ESSAY_MIN_LENGTH = 10
const val ESSAY_MAX_LENGTH = 5000

data class ExerciseAnswer(
    val selectedKeys: Set<String> = emptySet(),
    val essayText: String = ""
) : Serializable {

    fun isAnswered(type: ExerciseQuestionType): Boolean = when (type) {
        ExerciseQuestionType.ESSAY -> essayText.trim().length >= ESSAY_MIN_LENGTH
        else -> selectedKeys.isNotEmpty()
    }
}

sealed interface ExerciseSubmitPhase {
    data object Idle : ExerciseSubmitPhase
    data class Submitting(val isAuto: Boolean) : ExerciseSubmitPhase
    data class Submitted(val exerciseId: String, val isAuto: Boolean) : ExerciseSubmitPhase
}

data class ExerciseSubmitError(val message: String, val isAuto: Boolean)

sealed interface ExerciseUiState {

    data object Loading : ExerciseUiState

    data class Error(val message: String) : ExerciseUiState

    data class Intro(val exercise: ExerciseDto) : ExerciseUiState {
        val isOutOfAttempts: Boolean
            get() = exercise.maxAttempts > 0 && exercise.attemptsUsed >= exercise.maxAttempts
    }

    data class InProgress(
        val exercise: ExerciseDto,
        val currentIndex: Int,
        val answers: Map<String, ExerciseAnswer>,
        val startedAtMs: Long,
        val submitPhase: ExerciseSubmitPhase = ExerciseSubmitPhase.Idle,
        val submitError: ExerciseSubmitError? = null,
        val isTimeUp: Boolean = false
    ) : ExerciseUiState {

        val expiresAtMs: Long?
            get() = if (exercise.timeLimit > 0) startedAtMs + exercise.timeLimit * 1000L else null

        val isLocked: Boolean
            get() = submitPhase != ExerciseSubmitPhase.Idle || isTimeUp

        val isBackLocked: Boolean
            get() = submitPhase !is ExerciseSubmitPhase.Idle

        val questions: List<ExerciseQuestionDto>
            get() = exercise.questions

        val currentQuestion: ExerciseQuestionDto
            get() = questions[currentIndex]

        val answeredCount: Int
            get() = questions.count { isAnswered(it) }

        fun answerOf(question: ExerciseQuestionDto): ExerciseAnswer =
            answers[question.id] ?: ExerciseAnswer()

        fun isAnswered(question: ExerciseQuestionDto): Boolean =
            answerOf(question).isAnswered(question.type)
    }
}
