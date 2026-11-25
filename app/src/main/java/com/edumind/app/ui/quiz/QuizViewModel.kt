package com.edumind.app.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.model.request.SubmitQuizAnswerRequest
import com.edumind.app.data.model.response.QuizDto
import com.edumind.app.data.model.response.QuizMarker
import com.edumind.app.repository.QuizRepository
import com.edumind.app.util.ClockProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val clockProvider: ClockProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Hidden)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QuizClosedEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<QuizClosedEvent> = _events.asSharedFlow()

    private var currentQuizId: String? = null
    private var questionShownAtMs = 0L
    private var recordedTimeTaken: Int? = null
    private var requestJob: Job? = null

    fun onQuizTriggered(marker: QuizMarker) {
        if (_uiState.value != QuizUiState.Hidden) return
        currentQuizId = marker.quizId
        _uiState.value = QuizUiState.Loading
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            quizRepository.getQuiz(marker.quizId).fold(
                onSuccess = { quiz -> showQuiz(quiz) },
                onFailure = { error ->
                    close(answered = false, message = error.message ?: LOAD_ERROR_MESSAGE)
                }
            )
        }
    }

    fun selectAnswer(key: String) {
        _uiState.update { state ->
            if (state is QuizUiState.Question) {
                state.copy(selectedKey = key, errorMessage = null)
            } else {
                state
            }
        }
    }

    fun submit() {
        val state = _uiState.value as? QuizUiState.Question ?: return
        val key = state.selectedKey ?: return
        _uiState.value = QuizUiState.Submitting(state.quiz, state.index, key)
        val timeTaken = recordedTimeTaken ?: ((clockProvider.currentTimeMillis() - questionShownAtMs) / 1000L)
            .toInt()
            .coerceAtLeast(0)
            .also { recordedTimeTaken = it }
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            quizRepository.submitAnswer(
                SubmitQuizAnswerRequest(
                    quizId = state.quiz.id,
                    questionId = state.question.id,
                    answer = key,
                    timeTaken = timeTaken
                )
            ).fold(
                onSuccess = { result ->
                    _uiState.value = QuizUiState.Feedback(state.quiz, state.index, key, result)
                },
                onFailure = { error ->
                    _uiState.value = state.copy(errorMessage = error.message ?: SUBMIT_ERROR_MESSAGE)
                }
            )
        }
    }

    fun onErrorShown() {
        _uiState.update { state ->
            if (state is QuizUiState.Question && state.errorMessage != null) {
                state.copy(errorMessage = null)
            } else {
                state
            }
        }
    }

    fun next() {
        val state = _uiState.value as? QuizUiState.Feedback ?: return
        if (state.isLast) return
        showQuestion(state.quiz, state.index + 1)
    }

    fun finish() {
        val state = _uiState.value as? QuizUiState.Feedback ?: return
        if (!state.isLast) return
        close(answered = true)
    }

    fun rewind() {
        if (_uiState.value !is QuizUiState.Feedback) return
        close(answered = false)
    }

    fun onBack() {
        when (val state = _uiState.value) {
            QuizUiState.Hidden, is QuizUiState.Submitting -> Unit
            QuizUiState.Loading -> {
                requestJob?.cancel()
                close(answered = false)
            }
            is QuizUiState.Question -> close(answered = false)
            is QuizUiState.Feedback -> close(answered = state.isLast)
        }
    }

    fun reset() {
        requestJob?.cancel()
        currentQuizId = null
        recordedTimeTaken = null
        _uiState.value = QuizUiState.Hidden
    }

    private fun showQuiz(quiz: QuizDto) {
        val ordered = quiz.copy(questions = quiz.questions.sortedBy { it.order })
        if (ordered.questions.isEmpty()) {
            close(answered = false, message = LOAD_ERROR_MESSAGE)
            return
        }
        showQuestion(ordered, 0)
    }

    private fun showQuestion(quiz: QuizDto, index: Int) {
        questionShownAtMs = clockProvider.currentTimeMillis()
        recordedTimeTaken = null
        _uiState.value = QuizUiState.Question(quiz, index)
    }

    private fun close(answered: Boolean, message: String? = null) {
        val quizId = currentQuizId
        currentQuizId = null
        recordedTimeTaken = null
        _uiState.value = QuizUiState.Hidden
        if (quizId != null) {
            _events.tryEmit(QuizClosedEvent(quizId, answered, message))
        }
    }

    private companion object {
        const val LOAD_ERROR_MESSAGE = "Không thể tải câu hỏi quiz."
        const val SUBMIT_ERROR_MESSAGE = "Không thể gửi đáp án. Vui lòng thử lại."
    }
}
