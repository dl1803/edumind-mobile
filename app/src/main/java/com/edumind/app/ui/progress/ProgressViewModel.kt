package com.edumind.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgress()
    }

    fun loadProgress() {
        viewModelScope.launch {
            _uiState.value = ProgressUiState.Loading
            progressRepository.getProgressOverview().fold(
                onSuccess = { response ->
                    if (response.courses.isEmpty() && response.totalHoursWatched == 0f) {
                        _uiState.value = ProgressUiState.Empty
                    } else {
                        val overall = if (response.courses.isNotEmpty()) {
                            response.courses.map { it.progressPercent }.average().roundToInt()
                        } else 0

                        _uiState.value = ProgressUiState.Success(
                            data = response,
                            overallPercent = overall.coerceIn(0, 100)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = ProgressUiState.Error(
                        message = error.localizedMessage ?: "Đã xảy ra lỗi khi tải tiến độ học tập."
                    )
                }
            )
        }
    }

    fun retry() {
        loadProgress()
    }

    fun toggleGoalSheet(show: Boolean) {
        _uiState.update { current ->
            if (current is ProgressUiState.Success) {
                current.copy(isGoalSheetVisible = show)
            } else current
        }
    }

    fun updateDailyGoal(newMinutes: Int) {
        viewModelScope.launch {
            progressRepository.updateDailyGoal(newMinutes).fold(
                onSuccess = {
                    toggleGoalSheet(false)
                    loadProgress()
                },
                onFailure = { error ->
                    _uiState.value = ProgressUiState.Error(
                        message = error.localizedMessage ?: "Không thể cập nhật mục tiêu học tập."
                    )
                }
            )
        }
    }
}
