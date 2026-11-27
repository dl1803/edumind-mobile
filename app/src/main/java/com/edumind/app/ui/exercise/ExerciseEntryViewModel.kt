package com.edumind.app.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.model.response.ExerciseEntry
import com.edumind.app.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseEntryViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _entry = MutableStateFlow<ExerciseEntry?>(null)
    val entry: StateFlow<ExerciseEntry?> = _entry.asStateFlow()

    private var loadedLessonId: String? = null
    private var loadJob: Job? = null

    fun initLessonId(lessonId: String) {
        if (lessonId.isBlank()) {
            loadedLessonId = null
            loadJob?.cancel()
            _entry.value = null
            return
        }
        if (lessonId == loadedLessonId) return
        loadedLessonId = lessonId
        _entry.value = null
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val result = exerciseRepository.getLessonExercise(lessonId).getOrNull()
            if (loadedLessonId == lessonId) {
                _entry.value = result
            }
        }
    }
}
