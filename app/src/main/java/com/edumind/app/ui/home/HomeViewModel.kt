package com.edumind.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            fetchDataInternal()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            fetchDataInternal()
        }
    }

    private suspend fun fetchDataInternal() = coroutineScope {
        try {
            // Tải song song 5 luồng độc lập
            val featuredDeferred = async { homeRepository.getFeaturedCourses() }
            val continueDeferred = async { homeRepository.getContinueLearningItems() }
            val recommendedDeferred = async { homeRepository.getRecommendedCourses() }
            val newCoursesDeferred = async { homeRepository.getNewCourses(page = 1) }
            val unreadDeferred = async { homeRepository.getUnreadNotificationCount() }

            val featuredResult = featuredDeferred.await()
            val continueResult = continueDeferred.await()
            val recommendedResult = recommendedDeferred.await()
            val newCoursesResult = newCoursesDeferred.await()
            val unreadResult = unreadDeferred.await()

            // Chỉ kiểm tra lỗi trên các phân đoạn cốt lõi của trang chủ:
            val primaryFailure = listOf(featuredResult, recommendedResult, newCoursesResult)
                .firstOrNull { it.isFailure }

            if (primaryFailure != null && _uiState.value.featuredCourses.isEmpty()) {
                val errorMsg = primaryFailure.exceptionOrNull()?.localizedMessage
                    ?: "Không thể tải dữ liệu trang chủ. Vui lòng thử lại."
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = errorMsg
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        featuredCourses = featuredResult.getOrDefault(it.featuredCourses),
                        // Tiếp tục học nhận danh sách thật hoặc rỗng an toàn:
                        continueLearningItems = continueResult.getOrDefault(emptyList()),
                        recommendedCourses = recommendedResult.getOrDefault(it.recommendedCourses),
                        newCourses = newCoursesResult.getOrDefault(it.newCourses),
                        unreadNotificationCount = unreadResult.getOrDefault(0),
                        errorMessage = null
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = e.localizedMessage ?: "Đã có lỗi xảy ra"
                )
            }
        }
    }
}
