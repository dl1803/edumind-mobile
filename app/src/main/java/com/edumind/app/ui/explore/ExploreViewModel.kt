package com.edumind.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.model.response.Category
import com.edumind.app.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow(ExploreFilter())
    val filter: StateFlow<ExploreFilter> = _filter.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private var currentPage = 1
    private val pageSize = 20
    private var isFetching = false

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            loadCategories()
            fetchCourses(page = 1, isRefresh = true)
        }
    }

    private suspend fun loadCategories() {
        courseRepository.getCategories().onSuccess { catList ->
            _categories.value = catList
        }
    }

    fun onCategorySelected(categoryId: String?) {
        val normalizedId = if (categoryId == "cat_all" || categoryId.isNullOrBlank()) null else categoryId
        if (_filter.value.categoryId == normalizedId) return

        _filter.update { it.copy(categoryId = normalizedId) }
        resetAndReload()
    }

    fun onFilterApplied(newFilter: ExploreFilter) {
        _filter.value = newFilter
        resetAndReload()
    }

    fun onResetFilter() {
        _filter.update {
            ExploreFilter(categoryId = it.categoryId)
        }
        resetAndReload()
    }

    fun onClearAllFilters() {
        _filter.value = ExploreFilter()
        resetAndReload()
    }

    fun onSortChanged(sortOption: SortOption) {
        if (_filter.value.sortOption == sortOption) return
        _filter.update { it.copy(sortOption = sortOption) }
        resetAndReload()
    }

    private fun resetAndReload() {
        currentPage = 1
        _uiState.value = ExploreUiState.Loading
        viewModelScope.launch {
            fetchCourses(page = 1, isRefresh = true)
        }
    }

    fun loadMore() {
        val currentState = _uiState.value as? ExploreUiState.Success ?: return
        if (isFetching || currentState.isEndReached || currentState.isLoadingMore) return

        viewModelScope.launch {
            fetchCourses(page = currentPage + 1, isRefresh = false)
        }
    }

    fun retry() {
        val currentState = _uiState.value
        if (currentState is ExploreUiState.Error || currentState is ExploreUiState.Loading) {
            resetAndReload()
        }
    }

    private suspend fun fetchCourses(page: Int, isRefresh: Boolean) {
        if (isFetching) return
        isFetching = true

        val currentFilter = _filter.value
        val currentState = _uiState.value

        if (!isRefresh && currentState is ExploreUiState.Success) {
            _uiState.value = currentState.copy(isLoadingMore = true)
        }

        val result = courseRepository.getCourses(
            page = page,
            pageSize = pageSize,
            categoryId = currentFilter.categoryId,
            priceMin = currentFilter.priceOption.min,
            priceMax = currentFilter.priceOption.max,
            minRating = currentFilter.ratingOption.minRating,
            sortBy = currentFilter.sortOption.value
        )

        result.fold(
            onSuccess = { paged ->
                currentPage = paged.page
                val newItems = paged.data
                val total = paged.total
                val endReached = currentPage >= paged.totalPages || newItems.size < pageSize

                if (isRefresh) {
                    if (newItems.isEmpty()) {
                        _uiState.value = ExploreUiState.Empty
                    } else {
                        _uiState.value = ExploreUiState.Success(
                            courses = newItems,
                            totalCount = total,
                            isLoadingMore = false,
                            isEndReached = endReached
                        )
                    }
                } else if (currentState is ExploreUiState.Success) {
                    _uiState.value = currentState.copy(
                        courses = currentState.courses + newItems,
                        totalCount = total,
                        isLoadingMore = false,
                        isEndReached = endReached
                    )
                }
            },
            onFailure = { error ->
                if (isRefresh) {
                    _uiState.value = ExploreUiState.Error(error.message ?: "Đã xảy ra lỗi kết nối.")
                } else if (currentState is ExploreUiState.Success) {
                    _uiState.value = currentState.copy(isLoadingMore = false)
                }
            }
        )

        isFetching = false
    }
}
