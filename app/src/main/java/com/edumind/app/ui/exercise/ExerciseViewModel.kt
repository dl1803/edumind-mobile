package com.edumind.app.ui.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.model.request.ExerciseAnswerFormat
import com.edumind.app.data.model.request.ExerciseAnswerRequest
import com.edumind.app.data.model.request.SubmitExerciseRequest
import com.edumind.app.data.model.response.ExerciseDto
import com.edumind.app.data.model.response.ExerciseQuestionType
import com.edumind.app.data.model.response.ExerciseResultDto
import com.edumind.app.repository.ExerciseRepository
import com.edumind.app.ui.navigation.Screen
import com.edumind.app.util.ClockProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val clockProvider: ClockProvider,
    private val resultStore: ExerciseResultStore
) : ViewModel() {

    private val exerciseId: String =
        savedStateHandle.get<String>(Screen.ARG_EXERCISE_ID).orEmpty()

    private val _uiState = MutableStateFlow<ExerciseUiState>(ExerciseUiState.Loading)
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    private val _remainingSeconds = MutableStateFlow<Int?>(null)
    val remainingSeconds: StateFlow<Int?> = _remainingSeconds.asStateFlow()

    private var timerJob: Job? = null

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun start() {
        val intro = _uiState.value as? ExerciseUiState.Intro ?: return
        if (intro.isOutOfAttempts) return
        val state = ExerciseUiState.InProgress(
            exercise = intro.exercise,
            currentIndex = 0,
            answers = emptyMap(),
            startedAtMs = clockProvider.currentTimeMillis()
        )
        setInProgress(state)
        startTimer(state)
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

    fun submit(auto: Boolean = false) {
        val state = _uiState.value as? ExerciseUiState.InProgress ?: return
        if (state.submitPhase != ExerciseSubmitPhase.Idle) return
        val timeSpentSeconds = elapsedSeconds(state)
        _uiState.value = state.copy(
            submitPhase = ExerciseSubmitPhase.Submitting(auto),
            submitError = null
        )
        viewModelScope.launch {
            exerciseRepository.submitExercise(exerciseId, state.toRequest(timeSpentSeconds)).fold(
                onSuccess = { result -> onSubmitSuccess(state, auto, timeSpentSeconds, result) },
                onFailure = { error -> onSubmitFailure(error, auto) }
            )
        }
    }

    fun retrySubmit() {
        val state = _uiState.value as? ExerciseUiState.InProgress ?: return
        submit(auto = state.submitError?.isAuto == true || state.isTimeUp)
    }

    fun onSubmitErrorShown() {
        val state = _uiState.value as? ExerciseUiState.InProgress ?: return
        if (state.submitError == null) return
        _uiState.value = state.copy(submitError = null)
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
            val loaded = exerciseRepository.getExercise(exerciseId).fold(
                onSuccess = { toLoadedState(it) },
                onFailure = { ExerciseUiState.Error(LOAD_ERROR_MESSAGE) }
            )
            _uiState.value = loaded
            (loaded as? ExerciseUiState.InProgress)?.let { startTimer(it) }
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
        if (current.isLocked) return
        setInProgress(transform(current))
    }

    private fun startTimer(state: ExerciseUiState.InProgress) {
        timerJob?.cancel()
        val expiresAtMs = state.expiresAtMs
        if (expiresAtMs == null) {
            _remainingSeconds.value = null
            return
        }
        timerJob = viewModelScope.launch {
            ticks()
                .map { nowMs -> remainingSecondsUntil(expiresAtMs, nowMs) }
                .onEach { _remainingSeconds.value = it }
                .first { it <= 0 }
            markTimeUp()
            submit(auto = true)
        }
    }

    private fun ticks(): Flow<Long> = flow {
        while (true) {
            emit(clockProvider.currentTimeMillis())
            delay(TICK_INTERVAL_MS)
        }
    }

    private fun markTimeUp() {
        val state = _uiState.value as? ExerciseUiState.InProgress ?: return
        _uiState.value = state.copy(isTimeUp = true)
    }

    private fun elapsedSeconds(state: ExerciseUiState.InProgress): Int {
        val elapsed = ((clockProvider.currentTimeMillis() - state.startedAtMs) / 1000L)
            .toInt()
            .coerceAtLeast(0)
        val limit = state.exercise.timeLimit
        return if (limit > 0) elapsed.coerceAtMost(limit) else elapsed
    }

    private fun onSubmitSuccess(
        submitted: ExerciseUiState.InProgress,
        auto: Boolean,
        timeSpentSeconds: Int,
        result: ExerciseResultDto
    ) {
        timerJob?.cancel()
        resultStore.put(
            exerciseId,
            ExerciseSubmission(
                exercise = submitted.exercise,
                answers = submitted.answers,
                timeSpentSeconds = timeSpentSeconds,
                result = result
            )
        )
        clearSavedAttempt()
        val latest = _uiState.value as? ExerciseUiState.InProgress ?: submitted
        _uiState.value = latest.copy(
            submitPhase = ExerciseSubmitPhase.Submitted(exerciseId, auto),
            submitError = null
        )
    }

    private fun onSubmitFailure(error: Throwable, auto: Boolean) {
        val latest = _uiState.value as? ExerciseUiState.InProgress ?: return
        _uiState.value = latest.copy(
            submitPhase = ExerciseSubmitPhase.Idle,
            submitError = ExerciseSubmitError(
                message = error.message?.takeIf { it.isNotBlank() } ?: SUBMIT_ERROR_MESSAGE,
                isAuto = auto
            )
        )
    }

    private fun clearSavedAttempt() {
        savedStateHandle.remove<Any>(KEY_STARTED_AT_MS)
        savedStateHandle.remove<Any>(KEY_CURRENT_INDEX)
        savedStateHandle.remove<Any>(KEY_ANSWERS)
    }

    private companion object {
        const val KEY_STARTED_AT_MS = "exercise_started_at_ms"
        const val KEY_CURRENT_INDEX = "exercise_current_index"
        const val KEY_ANSWERS = "exercise_answers"
        const val TICK_INTERVAL_MS = 1000L
        const val LOAD_ERROR_MESSAGE = "Không thể tải bài tập. Vui lòng thử lại."
        const val INVALID_EXERCISE_MESSAGE = "Bài tập chưa có nội dung hợp lệ để làm."
        const val SUBMIT_ERROR_MESSAGE = "Không thể nộp bài. Vui lòng thử lại."
    }
}

private fun ExerciseUiState.InProgress.toRequest(timeSpentSeconds: Int): SubmitExerciseRequest =
    SubmitExerciseRequest(
        answers = questions.filter { isAnswered(it) }.map { question ->
            val answer = answerOf(question)
            ExerciseAnswerRequest(
                questionId = question.id,
                answer = if (question.type == ExerciseQuestionType.ESSAY) {
                    answer.essayText.trim()
                } else {
                    ExerciseAnswerFormat.encode(answer.selectedKeys)
                }
            )
        },
        timeSpent = timeSpentSeconds
    )
