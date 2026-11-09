package com.edumind.app.ui.ai.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.model.response.LectureSummary
import com.edumind.app.repository.SummaryRepository
import com.edumind.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val GENERATE_ERROR_MESSAGE = "Không thể tạo bản tóm tắt. Vui lòng thử lại."

@HiltViewModel
class SummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SummaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SummaryUiState>(SummaryUiState.Empty)
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    private var currentLessonId = savedStateHandle.get<String>(Screen.ARG_LESSON_ID).orEmpty()
    private var loadJob: Job? = null

    init {
        loadCached()
    }

    fun initLessonId(lessonId: String) {
        if (lessonId.isBlank() || lessonId == currentLessonId) return
        loadJob?.cancel()
        currentLessonId = lessonId
        _uiState.value = SummaryUiState.Empty
        loadCached()
    }

    fun onGenerate() {
        if (currentLessonId.isBlank() || _uiState.value is SummaryUiState.Loading) return
        loadJob?.cancel()
        _uiState.value = SummaryUiState.Loading
        val lessonId = currentLessonId
        loadJob = viewModelScope.launch {
            repository.generateSummary(lessonId).fold(
                onSuccess = { _uiState.value = it.toUiState() },
                onFailure = { _uiState.value = SummaryUiState.Error(GENERATE_ERROR_MESSAGE) }
            )
        }
    }

    fun onRetry() = onGenerate()

    fun onToggleSection(sectionId: String) {
        _uiState.update { current ->
            if (current !is SummaryUiState.Success) {
                current
            } else {
                val expanded = current.expandedSectionIds
                current.copy(
                    expandedSectionIds = if (sectionId in expanded) expanded - sectionId else expanded + sectionId
                )
            }
        }
    }

    private fun loadCached() {
        if (currentLessonId.isBlank()) return
        loadJob?.cancel()
        val lessonId = currentLessonId
        loadJob = viewModelScope.launch {
            repository.getSummary(lessonId).getOrNull()
                ?.takeIf { it.hasContent }
                ?.let { _uiState.value = it.toUiState() }
        }
    }

    private fun LectureSummary.toUiState(): SummaryUiState =
        if (hasContent) {
            SummaryUiState.Success(summary = this, expandedSectionIds = sections.map { it.id }.toSet())
        } else {
            SummaryUiState.Empty
        }
}

