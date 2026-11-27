package com.edumind.app.ui.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.edumind.app.data.model.request.ExerciseAnswerFormat
import com.edumind.app.data.model.response.ExerciseQuestionDto
import com.edumind.app.data.model.response.ExerciseQuestionType
import com.edumind.app.data.model.response.ExerciseSubmitStatus
import com.edumind.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ExerciseResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    resultStore: ExerciseResultStore
) : ViewModel() {

    val uiState: ExerciseResultUiState = run {
        val exerciseId = savedStateHandle.get<String>(Screen.ARG_EXERCISE_ID).orEmpty()
        val submission = resultStore.get(exerciseId)
        if (submission == null) {
            ExerciseResultUiState.Missing
        } else {
            ExerciseResultUiState.Content(submission.toResultContent(exerciseId))
        }
    }
}

internal fun ExerciseSubmission.toResultContent(exerciseId: String): ExerciseResultContent {
    val isGraded = result.status == ExerciseSubmitStatus.GRADED
    val detailsByQuestion = result.details.orEmpty().associateBy { it.questionId }

    val items = exercise.questions.mapIndexed { index, question ->
        val answer = answers[question.id]
        val isAnswered = answer?.isAnswered(question.type) == true
        val detail = detailsByQuestion[question.id]
        val verdict = when {
            !isAnswered -> ExerciseVerdict.SKIPPED
            !isGraded -> ExerciseVerdict.UNGRADED
            detail?.correct == true -> ExerciseVerdict.CORRECT
            detail?.correct == false -> ExerciseVerdict.WRONG
            else -> ExerciseVerdict.UNGRADED
        }
        ExerciseReviewItem(
            id = question.id,
            number = index + 1,
            content = question.content,
            verdict = verdict,
            yourAnswerLines = if (isAnswered && answer != null) {
                answer.toDisplayLines(question)
            } else {
                emptyList()
            },
            correctAnswerLines = if (verdict == ExerciseVerdict.WRONG || verdict == ExerciseVerdict.SKIPPED) {
                question.correctAnswerLines(detail?.correctAnswer)
            } else {
                emptyList()
            },
            explanation = detail?.explanation?.takeIf { it.isNotBlank() }
        )
    }

    val outcome = when {
        !isGraded -> ExerciseOutcome.PENDING
        result.passed == true -> ExerciseOutcome.PASSED
        else -> ExerciseOutcome.FAILED
    }

    return ExerciseResultContent(
        exerciseId = exerciseId,
        outcome = outcome,
        scorePercent = result.score?.roundToInt()?.coerceIn(0, 100),
        passingScore = exercise.passingScore,
        correctCount = items.count { it.verdict == ExerciseVerdict.CORRECT },
        wrongCount = items.count { it.verdict == ExerciseVerdict.WRONG },
        answeredCount = items.count { it.verdict != ExerciseVerdict.SKIPPED },
        skippedCount = items.count { it.verdict == ExerciseVerdict.SKIPPED },
        timeSpentSeconds = timeSpentSeconds,
        canRetry = outcome == ExerciseOutcome.FAILED &&
            (exercise.maxAttempts == 0 || result.attemptsRemaining > 0),
        reviewItems = if (isGraded) items else emptyList()
    )
}

private fun ExerciseAnswer.toDisplayLines(question: ExerciseQuestionDto): List<String> =
    if (question.type == ExerciseQuestionType.ESSAY) {
        listOf(essayText.trim())
    } else {
        question.optionLines(selectedKeys)
    }

private fun ExerciseQuestionDto.correctAnswerLines(rawCorrectAnswer: String?): List<String> {
    val keys = ExerciseAnswerFormat.decode(rawCorrectAnswer)
    if (keys.isEmpty()) return emptyList()
    val matched = optionLines(keys)
    if (matched.isNotEmpty()) return matched
    val mappedKeys = keys.mapNotNull { key ->
        when (key.uppercase()) {
            "TRUE" -> options.orEmpty().getOrNull(0)?.key
            "FALSE" -> options.orEmpty().getOrNull(1)?.key
            else -> null
        }
    }.toSet()
    val mapped = optionLines(mappedKeys)
    if (mapped.isNotEmpty()) return mapped
    return listOfNotNull(rawCorrectAnswer?.trim()?.takeIf { it.isNotEmpty() })
}

private fun ExerciseQuestionDto.optionLines(keys: Set<String>): List<String> =
    options.orEmpty()
        .filter { it.key in keys }
        .map { "${it.key}. ${it.content}" }
