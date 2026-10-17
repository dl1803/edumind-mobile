package com.edumind.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.ui.common.CourseCard
import com.edumind.app.ui.common.EduMindSearchBar
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.explore.components.CategoryChipsRow
import com.edumind.app.ui.explore.components.ExploreEmptyState
import com.edumind.app.ui.explore.components.ExploreFilterBar
import com.edumind.app.ui.explore.components.ExploreSkeletonGrid
import com.edumind.app.ui.explore.components.FilterBottomSheet
import com.edumind.app.ui.theme.BgCanvasMobile
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary600
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ExploreScreen(
    onCourseClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState) {
        snapshotFlow {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 3
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) {
                    viewModel.loadMore()
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvasMobile)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x1A8B5CF6),
                            Color(0x0DFC4899),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Khám phá",
                fontFamily = EduMindFontFamily,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral950,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            EduMindSearchBar(
                onSearchClick = onSearchClick,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            CategoryChipsRow(
                categories = categories,
                selectedCategoryId = filter.categoryId,
                onCategoryClick = { viewModel.onCategorySelected(it) },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val totalItems = (uiState as? ExploreUiState.Success)?.totalCount ?: 0
            ExploreFilterBar(
                totalCount = totalItems,
                activeFilterCount = filter.activeCount,
                currentSort = filter.sortOption,
                onOpenFilterSheet = { showFilterSheet = true },
                onToggleSort = {
                    val nextSort = when (filter.sortOption) {
                        SortOption.NEWEST -> SortOption.POPULAR
                        SortOption.POPULAR -> SortOption.PRICE_ASC
                        SortOption.PRICE_ASC -> SortOption.PRICE_DESC
                        SortOption.PRICE_DESC -> SortOption.NEWEST
                    }
                    viewModel.onSortChanged(nextSort)
                }
            )

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is ExploreUiState.Loading -> {
                        ExploreSkeletonGrid()
                    }

                    is ExploreUiState.Empty -> {
                        ExploreEmptyState(
                            onClearFilters = { viewModel.onClearAllFilters() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is ExploreUiState.Error -> {
                        ErrorView(
                            message = state.message,
                            onRetry = { viewModel.retry() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is ExploreUiState.Success -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.courses,
                                key = { it.id }
                            ) { course ->
                                CourseCard(
                                    course = course,
                                    onClick = { onCourseClick(course.id) }
                                )
                            }

                            if (state.isLoadingMore) {
                                item(span = { GridItemSpan(2) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Primary600,
                                            strokeWidth = 2.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showFilterSheet) {
            FilterBottomSheet(
                currentFilter = filter,
                onDismiss = { showFilterSheet = false },
                onApply = { newFilter ->
                    showFilterSheet = false
                    viewModel.onFilterApplied(newFilter)
                },
                onReset = {
                    viewModel.onResetFilter()
                }
            )
        }
    }
}
