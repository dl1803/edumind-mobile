package com.edumind.app.ui.ai

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.model.response.ChatMessage
import com.edumind.app.data.model.response.ChatMessageStatus
import com.edumind.app.data.model.response.ChatRole
import com.edumind.app.repository.AIRepository
import com.edumind.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isTyping: Boolean = false,
    val error: String? = null
) {
    val canSend: Boolean
        get() = input.isNotBlank() && !isTyping
}

@HiltViewModel
class ChatAIViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentLessonId = savedStateHandle.get<String>(Screen.ARG_LESSON_ID).orEmpty()
    private var sendJob: Job? = null

    fun initLessonId(lessonId: String) {
        if (lessonId.isBlank() || lessonId == currentLessonId) return
        currentLessonId = lessonId
        sendJob?.cancel()
        _uiState.value = ChatUiState()
    }

    fun onInputChange(value: String) {
        _uiState.update { it.copy(input = value) }
    }

    fun onSend() {
        val state = _uiState.value
        if (!state.canSend) return
        val question = state.input.trim()
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatRole.USER,
            text = question,
            status = ChatMessageStatus.SENDING
        )
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                input = "",
                isTyping = true,
                error = null
            )
        }
        ask(userMessage.id, question)
    }

    fun onRetry() {
        val state = _uiState.value
        if (state.isTyping) return
        val failed = state.messages.lastOrNull {
            it.role == ChatRole.USER && it.status == ChatMessageStatus.ERROR
        } ?: return
        _uiState.update {
            it.copy(
                messages = it.messages.withStatus(failed.id, ChatMessageStatus.SENDING),
                isTyping = true,
                error = null
            )
        }
        ask(failed.id, failed.text)
    }

    private fun ask(userMessageId: String, question: String) {
        sendJob?.cancel()
        sendJob = viewModelScope.launch {
            aiRepository.ask(currentLessonId, question).fold(
                onSuccess = { reply ->
                    _uiState.update {
                        it.copy(
                            messages = it.messages.withStatus(userMessageId, ChatMessageStatus.SENT) + reply,
                            isTyping = false,
                            error = null
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            messages = it.messages.withStatus(userMessageId, ChatMessageStatus.ERROR),
                            isTyping = false,
                            error = ERROR_MESSAGE
                        )
                    }
                }
            )
        }
    }

    private fun List<ChatMessage>.withStatus(id: String, status: ChatMessageStatus): List<ChatMessage> =
        map { if (it.id == id) it.copy(status = status) else it }

    companion object {
        private const val ERROR_MESSAGE = "AI đang gặp sự cố"
    }
}

