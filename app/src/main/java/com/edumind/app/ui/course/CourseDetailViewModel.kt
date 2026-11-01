package com.edumind.app.ui.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.CourseRepository
import com.edumind.app.repository.EnrollResult
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

    fun toggleEnrolledMode() {
        _uiState.update { current ->
            if (current is CourseDetailUiState.Success) {
                val newEnrolled = !current.course.isEnrolled
                current.copy(
                    course = current.course.copy(
                        isEnrolled = newEnrolled,
                        progressPercent = if (newEnrolled) 35 else 0,
                        lastLessonId = if (newEnrolled) "l_2_2" else null,
                        lastLessonTitle = if (newEnrolled) "Bài 5: Vòng lặp For và While" else null
                    )
                )
            } else current
        }
    }

    fun onEnrollClick(
        onSuccess: (firstLessonId: String?) -> Unit = {},
        onPaymentRequired: () -> Unit = {}
    ) {
        val currentState = _uiState.value as? CourseDetailUiState.Success ?: return
        if (currentState.isEnrolling) return

        _uiState.update { (it as CourseDetailUiState.Success).copy(isEnrolling = true) }

        viewModelScope.launch {
            val result = try {
                courseRepository.enroll(courseId)
            } catch (e: Exception) {
                EnrollResult.Failure(e.localizedMessage ?: "Ghi danh thất bại")
            }

            when (result) {
                is EnrollResult.Success -> {
                    _uiState.update { current ->
                        if (current is CourseDetailUiState.Success) {
                            current.copy(
                                isEnrolling = false,
                                course = current.course.copy(
                                    isEnrolled = true,
                                    progressPercent = current.course.progressPercent,
                                    lastLessonId = result.firstLessonId ?: current.course.lastLessonId ?: "l_1_1",
                                    lastLessonTitle = current.course.lastLessonTitle ?: "Bài 1: Bắt đầu bài giảng"
                                ),
                                userMessage = "Đăng ký khóa học thành công!"
                            )
                        } else current
                    }
                    onSuccess(result.firstLessonId)
                }
                is EnrollResult.AlreadyEnrolled -> {
                    _uiState.update { current ->
                        if (current is CourseDetailUiState.Success) {
                            current.copy(
                                isEnrolling = false,
                                course = current.course.copy(isEnrolled = true),
                                userMessage = "Bạn đã đăng ký khóa học này trước đó."
                            )
                        } else current
                    }
                    onSuccess(null)
                }
                is EnrollResult.PaymentRequired -> {
                    _uiState.update { current ->
                        if (current is CourseDetailUiState.Success) {
                            current.copy(
                                isEnrolling = false,
                                userMessage = "Khóa học có phí, vui lòng thanh toán để mở khóa."
                            )
                        } else current
                    }
                    onPaymentRequired()
                }
                is EnrollResult.Failure -> {
                    _uiState.update { current ->
                        if (current is CourseDetailUiState.Success) {
                            current.copy(
                                isEnrolling = false,
                                userMessage = result.message
                            )
                        } else current
                    }
                }
            }
        }
    }

    fun onEnrollFreeCourse(
        onSuccess: (firstLessonId: String?) -> Unit = {},
        onPaymentRequired: () -> Unit = {}
    ) {
        onEnrollClick(onSuccess, onPaymentRequired)
    }

    fun onEnrollFreeCourse(onSuccess: () -> Unit) {
        onEnrollClick(onSuccess = { onSuccess() })
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
