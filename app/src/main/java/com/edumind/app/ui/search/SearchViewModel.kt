package com.edumind.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.CourseRepository
import com.edumind.app.ui.explore.ExploreFilter
import com.edumind.app.ui.explore.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _keywordInput = MutableStateFlow("")
    val keywordInput: StateFlow<String> = _keywordInput.asStateFlow()

    private val _filter = MutableStateFlow(ExploreFilter())
    val filter: StateFlow<ExploreFilter> = _filter.asStateFlow()

    private val _recentSearches = MutableStateFlow(
        listOf("Python cơ bản", "Machine Learning cho người mới", "Data Science")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    val trendingKeywords: List<String> = listOf("Python", "AI Trợ giảng", "SQL Database", "React Native", "Kotlin Android")

    private val _uiState = MutableStateFlow<SearchUiState>(
        SearchUiState.Idle(
            keyword = "",
            suggestions = emptyList(),
            recentSearches = _recentSearches.value,
            trendingKeywords = trendingKeywords
        )
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var suggestionJob: Job? = null
    private var searchJob: Job? = null

    fun onKeywordChange(newText: String) {
        if (newText.length > 100) return
        _keywordInput.value = newText

        suggestionJob?.cancel()
        searchJob?.cancel()

        val trimmed = newText.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = SearchUiState.Idle(
                keyword = "",
                suggestions = emptyList(),
                recentSearches = _recentSearches.value,
                trendingKeywords = trendingKeywords
            )
            return
        }

        suggestionJob = viewModelScope.launch {
            delay(300)
            val result = courseRepository.getCourses(search = trimmed, pageSize = 4)
            result.fold(
                onSuccess = { response ->
                    if (_keywordInput.value == newText && _uiState.value !is SearchUiState.Success) {
                        _uiState.value = SearchUiState.Idle(
                            keyword = newText,
                            suggestions = response.data,
                            recentSearches = _recentSearches.value,
                            trendingKeywords = trendingKeywords
                        )
                    }
                },
                onFailure = {
                }
            )
        }
    }

    fun onSearchSubmit(explicitKeyword: String? = null) {
        suggestionJob?.cancel()
        searchJob?.cancel()

        val query = (explicitKeyword ?: _keywordInput.value).trim()
        if (query.isEmpty()) return

        _keywordInput.value = query
        saveRecentSearch(query)

        _uiState.value = SearchUiState.Loading

        searchJob = viewModelScope.launch {
            val f = _filter.value
            val result = courseRepository.getCourses(
                search = query,
                categoryId = f.categoryId,
                priceMin = f.priceOption.min,
                priceMax = f.priceOption.max,
                minRating = f.ratingOption.minRating,
                sortBy = f.sortOption.value
            )
            result.fold(
                onSuccess = { response ->
                    val items = response.data
                    if (items.isEmpty()) {
                        _uiState.value = SearchUiState.Empty(keyword = query)
                    } else {
                        _uiState.value = SearchUiState.Success(keyword = query, courses = items)
                    }
                },
                onFailure = { error ->
                    _uiState.value = SearchUiState.Error(
                        message = error.localizedMessage ?: "Đã xảy ra lỗi khi tìm kiếm"
                    )
                }
            )
        }
    }

    fun onApplyFilter(newFilter: ExploreFilter) {
        _filter.value = newFilter
        val currentKeyword = _keywordInput.value.trim()
        if (currentKeyword.isNotEmpty()) {
            onSearchSubmit(currentKeyword)
        }
    }

    fun onSortOptionSelected(sortOption: SortOption) {
        onApplyFilter(_filter.value.copy(sortOption = sortOption))
    }

    fun onToggleSort() {
        val nextSort = when (_filter.value.sortOption) {
            SortOption.NEWEST -> SortOption.POPULAR
            SortOption.POPULAR -> SortOption.PRICE_ASC
            SortOption.PRICE_ASC -> SortOption.PRICE_DESC
            SortOption.PRICE_DESC -> SortOption.NEWEST
        }
        onApplyFilter(_filter.value.copy(sortOption = nextSort))
    }

    fun onResetFilter() {
        onApplyFilter(ExploreFilter())
    }

    fun onClearKeyword() {
        suggestionJob?.cancel()
        searchJob?.cancel()
        _keywordInput.value = ""
        _uiState.value = SearchUiState.Idle(
            keyword = "",
            suggestions = emptyList(),
            recentSearches = _recentSearches.value,
            trendingKeywords = trendingKeywords
        )
    }

    fun onSelectSuggestedKeyword(keyword: String) {
        onSearchSubmit(keyword)
    }

    fun onClearRecentSearches() {
        _recentSearches.value = emptyList()
        val current = _uiState.value
        if (current is SearchUiState.Idle) {
            _uiState.value = current.copy(recentSearches = emptyList())
        }
    }

    fun retry() {
        onSearchSubmit()
    }

    private fun saveRecentSearch(keyword: String) {
        val updated = _recentSearches.value.toMutableList().apply {
            remove(keyword)
            add(0, keyword)
        }
        _recentSearches.value = updated.take(10)
    }
}
