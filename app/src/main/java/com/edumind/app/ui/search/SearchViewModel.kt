package com.edumind.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _keywordInput = MutableStateFlow("")
    val keywordInput: StateFlow<String> = _keywordInput.asStateFlow()

    private val _recentSearches = MutableStateFlow(
        listOf("Python cơ bản", "Machine Learning cho người mới", "Data Science")
    )

    val uiState: StateFlow<SearchUiState> = _keywordInput
        .debounce(300)
        .map { it.trim() }
        .distinctUntilChanged()
        .flatMapLatest { trimmed ->
            if (trimmed.isEmpty()) {
                flowOf<SearchUiState>(SearchUiState.Idle(recentSearches = _recentSearches.value))
            } else {
                flow<SearchUiState> {
                    emit(SearchUiState.Loading)
                    val result = courseRepository.getCourses(search = trimmed)
                    result.fold(
                        onSuccess = { response ->
                            val items = response.data
                            if (items.isEmpty()) {
                                emit(SearchUiState.Empty(keyword = trimmed))
                            } else {
                                emit(SearchUiState.Success(keyword = trimmed, courses = items))
                            }
                        },
                        onFailure = { error ->
                            emit(SearchUiState.Error(message = error.localizedMessage ?: "Đã xảy ra lỗi khi tìm kiếm"))
                        }
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchUiState.Idle()
        )

    fun onKeywordChange(newText: String) {
        if (newText.length <= 100) {
            _keywordInput.value = newText
        }
    }

    fun onClearKeyword() {
        _keywordInput.value = ""
    }

    fun onSelectSuggestedKeyword(keyword: String) {
        _keywordInput.value = keyword
    }

    fun onClearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun retry() {
        val current = _keywordInput.value
        _keywordInput.value = ""
        _keywordInput.value = current
    }
}
