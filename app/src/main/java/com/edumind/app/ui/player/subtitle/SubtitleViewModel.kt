package com.edumind.app.ui.player.subtitle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.local.datastore.PlayerPreferences
import com.edumind.app.data.local.datastore.SubtitleFontSize
import com.edumind.app.data.model.response.SubtitleSegment
import com.edumind.app.repository.SubtitleRepository
import com.edumind.app.ui.navigation.Screen
import com.edumind.app.util.subtitle.SubtitleSyncHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubtitleUiState(
    val segments: List<SubtitleSegment> = emptyList(),
    val currentIndex: Int = -1,
    val enabled: Boolean = true,
    val fontSize: SubtitleFontSize = SubtitleFontSize.MEDIUM
) {
    val hasSubtitles: Boolean
        get() = segments.isNotEmpty()

    val currentText: String?
        get() = segments.getOrNull(currentIndex)?.text
}

@HiltViewModel
class SubtitleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val subtitleRepository: SubtitleRepository,
    private val playerPreferences: PlayerPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubtitleUiState())
    val uiState: StateFlow<SubtitleUiState> = _uiState.asStateFlow()

    private var currentLessonId = ""
    private var lastPositionMs = 0L
    private var loadJob: Job? = null

    init {
        loadPreferences()
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
            val index = SubtitleSyncHelper.findCueIndex(state.segments, positionMs)
            if (index == state.currentIndex) state else state.copy(currentIndex = index)
        }
    }

    fun setEnabled(enabled: Boolean) {
        _uiState.update { it.copy(enabled = enabled) }
        viewModelScope.launch { playerPreferences.setSubtitleEnabled(enabled) }
    }

    fun setFontSize(fontSize: SubtitleFontSize) {
        _uiState.update { it.copy(fontSize = fontSize) }
        viewModelScope.launch { playerPreferences.setSubtitleFontSize(fontSize) }
    }

    fun toggleEnabled() {
        setEnabled(!_uiState.value.enabled)
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val enabled = playerPreferences.subtitleEnabled.first()
            val fontSize = playerPreferences.subtitleFontSize.first()
            _uiState.update { it.copy(enabled = enabled, fontSize = fontSize) }
        }
    }

    private fun loadSegments(lessonId: String) {
        currentLessonId = lessonId
        loadJob?.cancel()
        _uiState.update { it.copy(segments = emptyList(), currentIndex = -1) }
        loadJob = viewModelScope.launch {
            val segments = try {
                subtitleRepository.getSegments(lessonId).sortedBy { it.startMs }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            }
            _uiState.update {
                it.copy(
                    segments = segments,
                    currentIndex = SubtitleSyncHelper.findCueIndex(segments, lastPositionMs)
                )
            }
        }
    }
}
