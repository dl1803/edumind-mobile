package com.edumind.app.ui.explore

import com.edumind.app.data.model.response.CourseSummary

enum class PriceFilterOption(val label: String, val min: Long?, val max: Long?) {
    ALL("Tất cả", null, null),
    FREE("Miễn phí", 0L, 0L),
    PAID("Có phí", 1L, null),
    UNDER_500K("< 500.000đ", 1L, 499999L),
    FROM_500K_TO_1M("500.000đ – 1.000.000đ", 500000L, 1000000L)
}

enum class RatingFilterOption(val label: String, val minRating: Float?) {
    ALL("Tất cả", null),
    RATING_3_0("≥ 3.0", 3.0f),
    RATING_3_5("≥ 3.5", 3.5f),
    RATING_4_0("≥ 4.0", 4.0f),
    RATING_4_5("≥ 4.5", 4.5f)
}

enum class SortOption(val value: String, val label: String) {
    NEWEST("newest", "Mới nhất (Mặc định)"),
    POPULAR("recommended", "Phổ biến nhất"),
    PRICE_ASC("price_asc", "Giá tăng dần"),
    PRICE_DESC("price_desc", "Giá giảm dần")
}

data class ExploreFilter(
    val categoryId: String? = null,
    val priceOption: PriceFilterOption = PriceFilterOption.ALL,
    val ratingOption: RatingFilterOption = RatingFilterOption.ALL,
    val sortOption: SortOption = SortOption.NEWEST
) {
    val activeCount: Int
        get() {
            var count = 0
            if (priceOption != PriceFilterOption.ALL) count++
            if (ratingOption != RatingFilterOption.ALL) count++
            if (sortOption != SortOption.NEWEST) count++
            return count
        }

    val isDefault: Boolean
        get() = categoryId == null && activeCount == 0
}

sealed interface ExploreUiState {
    data object InitialLoading : ExploreUiState
    data object Loading : ExploreUiState
    data class Success(
        val courses: List<CourseSummary>,
        val totalCount: Int,
        val isLoadingMore: Boolean = false,
        val isEndReached: Boolean = false
    ) : ExploreUiState
    data object Empty : ExploreUiState
    data class Error(val message: String) : ExploreUiState
}
