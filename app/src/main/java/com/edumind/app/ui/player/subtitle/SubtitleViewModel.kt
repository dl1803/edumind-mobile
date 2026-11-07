package com.edumind.app.ui.player.subtitle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.model.response.SubtitleSegment
import com.edumind.app.data.model.response.indexAt
import com.edumind.app.repository.SubtitleRepository
import com.edumind.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubtitleUiState(
    val segments: List<SubtitleSegment> = emptyList(),
    val currentIndex: Int = -1,
    val enabled: Boolean = true
) {
    val hasSubtitles: Boolean
        get() = segments.isNotEmpty()

    val currentText: String?
        get() = segments.getOrNull(currentIndex)?.text
}

@HiltViewModel
class SubtitleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val subtitleRepository: SubtitleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubtitleUiState())
    val uiState: StateFlow<SubtitleUiState> = _uiState.asStateFlow()

    private var currentLessonId = ""
    private var lastPositionMs = 0L
    private var loadJob: Job? = null

    init {
        val navLessonId = savedStateHandle.get<String>(Screen.ARG_LESSON_ID).orEmpty()
        if (navLessonId.isNotBlank()) {
            loadSegments(navLessonId)
        }
    }

    fun initLessonId(lessonId: String) {
        if (lessonId.isNotBlank() && lessonId != currentLessonId) {
            loadSegments(lessonId)
        }
    }

    fun onPositionChanged(positionMs: Long) {
        lastPositionMs = positionMs
        _uiState.update { state ->
            val index = state.segments.indexAt(positionMs)
            if (index == state.currentIndex) state else state.copy(currentIndex = index)
        }
    }

    fun toggleEnabled() {
        _uiState.update { it.copy(enabled = !it.enabled) }
    }

    private fun loadSegments(lessonId: String) {
        currentLessonId = lessonId
        loadJob?.cancel()
        _uiState.update { it.copy(segments = emptyList(), currentIndex = -1) }
        loadJob = viewModelScope.launch {
            val segments = subtitleRepository.getSegments(lessonId).sortedBy { it.startMs }
            _uiState.update {
                it.copy(segments = segments, currentIndex = segments.indexAt(lastPositionMs))
            }
        }
    }
}

