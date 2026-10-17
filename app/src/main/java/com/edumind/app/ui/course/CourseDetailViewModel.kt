package com.edumind.app.ui.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.CourseRepository
import com.edumind.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val courseId: String = savedStateHandle[Screen.ARG_COURSE_ID] ?: ""

    private val _uiState = MutableStateFlow<CourseDetailUiState>(CourseDetailUiState.Loading)
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        if (courseId.isNotBlank()) {
            loadCourseDetail()
        } else {
            _uiState.value = CourseDetailUiState.Error("Không tìm thấy mã khóa học")
        }
    }

    fun loadCourseDetail() {
        viewModelScope.launch {
            _uiState.value = CourseDetailUiState.Loading
            val result = courseRepository.getCourseDetail(courseId)
            result.fold(
                onSuccess = { detail ->
                    val initialExpanded = detail.sections.take(2).map { it.id }.toSet()
                    _uiState.value = CourseDetailUiState.Success(
                        course = detail,
                        expandedSectionIds = initialExpanded
                    )
                },
                onFailure = { error ->
                    _uiState.value = CourseDetailUiState.Error(
                        message = error.localizedMessage ?: "Không thể tải thông tin khóa học"
                    )
                }
            )
        }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { current ->
            if (current is CourseDetailUiState.Success) {
                current.copy(selectedTab = tabIndex)
            } else current
        }
    }

    fun onToggleSection(sectionId: String) {
        _uiState.update { current ->
            if (current is CourseDetailUiState.Success) {
                val updatedSet = current.expandedSectionIds.toMutableSet()
                if (updatedSet.contains(sectionId)) {
                    updatedSet.remove(sectionId)
                } else {
                    updatedSet.add(sectionId)
                }
                current.copy(expandedSectionIds = updatedSet)
            } else current
        }
    }

    fun onToggleExpandAll() {
        _uiState.update { current ->
            if (current is CourseDetailUiState.Success) {
                val allIds = current.course.sections.map { it.id }.toSet()
                val newExpanded = if (current.expandedSectionIds.size == allIds.size) emptySet() else allIds
                current.copy(expandedSectionIds = newExpanded)
            } else current
        }
    }

    fun onEnrollFreeCourse(onSuccess: () -> Unit) {
        val currentState = _uiState.value as? CourseDetailUiState.Success ?: return
        if (currentState.isEnrolling) return

        viewModelScope.launch {
            _uiState.update { (it as CourseDetailUiState.Success).copy(isEnrolling = true) }
            val result = courseRepository.enrollCourse(courseId)
            result.fold(
                onSuccess = {
                    _uiState.update { current ->
                        if (current is CourseDetailUiState.Success) {
                            current.copy(
                                isEnrolling = false,
                                course = current.course.copy(isEnrolled = true)
                            )
                        } else current
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { current ->
                        if (current is CourseDetailUiState.Success) {
                            current.copy(
                                isEnrolling = false,
                                userMessage = error.localizedMessage ?: "Đăng ký thất bại"
                            )
                        } else current
                    }
                }
            )
        }
    }

    fun clearUserMessage() {
        _uiState.update { current ->
            if (current is CourseDetailUiState.Success) {
                current.copy(userMessage = null)
            } else current
        }
    }

    fun retry() {
        loadCourseDetail()
    }
}
