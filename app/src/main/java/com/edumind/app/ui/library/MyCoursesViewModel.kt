package com.edumind.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.model.response.MyCourse
import com.edumind.app.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyCoursesViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyCoursesUiState>(MyCoursesUiState.Loading)
    val uiState: StateFlow<MyCoursesUiState> = _uiState.asStateFlow()

    init {
        loadMyCourses()
    }

    fun loadMyCourses() {
        viewModelScope.launch {
            _uiState.value = MyCoursesUiState.Loading

            val coursesResult = courseRepository.getMyCourses()
            val resumeResult = courseRepository.getQuickResumeItem()

            coursesResult.fold(
                onSuccess = { courses ->
                    if (courses.isEmpty()) {
                        _uiState.value = MyCoursesUiState.Empty
                    } else {
                        val inProgress = courses.filter { it.progressPercent < 100 }
                        val completed = courses.filter { it.progressPercent == 100 }
                        val defaultFilter = MyCoursesFilter.ALL

                        _uiState.value = MyCoursesUiState.Success(
                            allCourses = courses,
                            displayedCourses = filterCourses(courses, defaultFilter),
                            quickResumeItem = resumeResult.getOrNull(),
                            selectedFilter = defaultFilter,
                            allCount = courses.size,
                            inProgressCount = inProgress.size,
                            completedCount = completed.size
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = MyCoursesUiState.Error(
                        error.localizedMessage ?: "Không thể tải danh sách khóa học. Vui lòng thử lại sau."
                    )
                }
            )
        }
    }

    fun onFilterChanged(filter: MyCoursesFilter) {
        val current = _uiState.value
        if (current is MyCoursesUiState.Success) {
            _uiState.value = current.copy(
                selectedFilter = filter,
                displayedCourses = filterCourses(current.allCourses, filter)
            )
        }
    }

    private fun filterCourses(courses: List<MyCourse>, filter: MyCoursesFilter): List<MyCourse> {
        return when (filter) {
            MyCoursesFilter.ALL -> courses.sortedWith(compareBy<MyCourse> { it.progressPercent == 100 })
            MyCoursesFilter.IN_PROGRESS -> courses.filter { it.progressPercent < 100 }
            MyCoursesFilter.COMPLETED -> courses.filter { it.progressPercent == 100 }
        }
    }
}
