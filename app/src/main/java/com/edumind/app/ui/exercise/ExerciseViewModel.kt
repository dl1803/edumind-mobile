package com.edumind.app.ui.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.model.response.ExerciseDto
import com.edumind.app.data.model.response.ExerciseQuestionType
import com.edumind.app.repository.ExerciseRepository
import com.edumind.app.ui.navigation.Screen
import com.edumind.app.util.ClockProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val clockProvider: ClockProvider
) : ViewModel() {

    private val exerciseId: String =
        savedStateHandle.get<String>(Screen.ARG_EXERCISE_ID).orEmpty()

    private val _uiState = MutableStateFlow<ExerciseUiState>(ExerciseUiState.Loading)
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun start() {
        val intro = _uiState.value as? ExerciseUiState.Intro ?: return
        if (intro.isOutOfAttempts) return
        setInProgress(
            ExerciseUiState.InProgress(
                exercise = intro.exercise,
                currentIndex = 0,
                answers = emptyMap(),
                startedAtMs = clockProvider.currentTimeMillis()
            )
        )
    }

    fun selectSingle(questionId: String, key: String) {
        updateInProgress { state ->
            val question = state.questions.firstOrNull { it.id == questionId }
                ?: return@updateInProgress state
            val isSingleType = question.type == ExerciseQuestionType.SINGLE_CHOICE ||
                question.type == ExerciseQuestionType.TRUE_FALSE
            if (!isSingleType || question.options.orEmpty().none { it.key == key }) {
                return@updateInProgress state
            }
            state.copy(
                answers = state.answers + (questionId to ExerciseAnswer(selectedKeys = setOf(key)))
            )
        }
    }

    fun toggleMultiple(questionId: String, key: String) {
        updateInProgress { state ->
            val question = state.questions.firstOrNull { it.id == questionId }
                ?: return@updateInProgress state
            if (question.type != ExerciseQuestionType.MULTIPLE_CHOICE ||
                question.options.orEmpty().none { it.key == key }
            ) {
                return@updateInProgress state
            }
            val current = state.answerOf(question).selectedKeys
            val next = if (key in current) current - key else current + key
            state.copy(
                answers = state.answers + (questionId to ExerciseAnswer(selectedKeys = next))
            )
        }
    }

    fun updateEssay(questionId: String, text: String) {
        updateInProgress { state ->
            val question = state.questions.firstOrNull { it.id == questionId }
                ?: return@updateInProgress state
            if (question.type != ExerciseQuestionType.ESSAY) return@updateInProgress state
            state.copy(
                answers = state.answers +
                    (questionId to ExerciseAnswer(essayText = text.take(ESSAY_MAX_LENGTH)))
            )
        }
    }

    fun goTo(index: Int) {
        updateInProgress { state ->
            if (index in state.questions.indices) state.copy(currentIndex = index) else state
        }
    }

    fun next() {
        moveBy(1)
    }

    fun previous() {
        moveBy(-1)
    }

    private fun moveBy(delta: Int) {
        val state = _uiState.value as? ExerciseUiState.InProgress ?: return
        goTo(state.currentIndex + delta)
    }

    private fun load() {
        if (exerciseId.isBlank()) {
            _uiState.value = ExerciseUiState.Error(LOAD_ERROR_MESSAGE)
            return
        }
        _uiState.value = ExerciseUiState.Loading
        viewModelScope.launch {
            _uiState.value = exerciseRepository.getExercise(exerciseId).fold(
                onSuccess = { toLoadedState(it) },
                onFailure = { ExerciseUiState.Error(LOAD_ERROR_MESSAGE) }
            )
        }
    }

    private fun toLoadedState(raw: ExerciseDto): ExerciseUiState {
        val exercise = raw.copy(questions = raw.questions.sortedBy { it.order })
        if (!exercise.isPlayable()) return ExerciseUiState.Error(INVALID_EXERCISE_MESSAGE)
        val startedAtMs = savedStateHandle.get<Long>(KEY_STARTED_AT_MS)
            ?: return ExerciseUiState.Intro(exercise)
        val validIds = exercise.questions.map { it.id }.toSet()
        val savedAnswers = savedStateHandle
            .get<HashMap<String, ExerciseAnswer>>(KEY_ANSWERS)
            .orEmpty()
            .filterKeys { it in validIds }
        val index = (savedStateHandle.get<Int>(KEY_CURRENT_INDEX) ?: 0)
            .coerceIn(0, exercise.questions.lastIndex)
        return ExerciseUiState.InProgress(
            exercise = exercise,
            currentIndex = index,
            answers = savedAnswers,
            startedAtMs = startedAtMs
        )
    }

    private fun ExerciseDto.isPlayable(): Boolean =
        questions.isNotEmpty() && questions.all { question ->
            question.type == ExerciseQuestionType.ESSAY || !question.options.isNullOrEmpty()
        }

    private fun setInProgress(state: ExerciseUiState.InProgress) {
        _uiState.value = state
        savedStateHandle[KEY_STARTED_AT_MS] = state.startedAtMs
        savedStateHandle[KEY_CURRENT_INDEX] = state.currentIndex
        savedStateHandle[KEY_ANSWERS] = HashMap(state.answers)
    }

    private inline fun updateInProgress(
        transform: (ExerciseUiState.InProgress) -> ExerciseUiState.InProgress
    ) {
        val current = _uiState.value as? ExerciseUiState.InProgress ?: return
        setInProgress(transform(current))
    }

    private companion object {
        const val KEY_STARTED_AT_MS = "exercise_started_at_ms"
        const val KEY_CURRENT_INDEX = "exercise_current_index"
        const val KEY_ANSWERS = "exercise_answers"
        const val LOAD_ERROR_MESSAGE = "Không thể tải bài tập. Vui lòng thử lại."
        const val INVALID_EXERCISE_MESSAGE = "Bài tập chưa có nội dung hợp lệ để làm."
    }
}
