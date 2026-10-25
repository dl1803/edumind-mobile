package com.edumind.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.R
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.ui.common.CourseCard
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.explore.ExploreFilter
import com.edumind.app.ui.explore.SortOption
import com.edumind.app.ui.explore.components.FilterBottomSheet
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral300
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral50
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary200
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary500
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToExplore: () -> Unit = onNavigateBack,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val keywordInput by viewModel.keywordInput.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val filter by viewModel.filter.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x668B5CF6),
                        0.30f to Color(0x47EC4899),
                        0.65f to Color(0x1F8B5CF6),
                        1.0f to Color.Transparent
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Neutral900
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            color = if (keywordInput.isNotEmpty()) Primary200 else Neutral200,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Primary600,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BasicTextField(
                        value = keywordInput,
                        onValueChange = viewModel::onKeywordChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(
                            fontFamily = EduMindFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Neutral900
                        ),
                        cursorBrush = SolidColor(Primary700),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                viewModel.onSearchSubmit()
                            }
                        ),
                        decorationBox = { innerTextField ->
                            if (keywordInput.isEmpty()) {
                                Text(
                                    text = "Tìm kiếm khóa học...",
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 14.sp,
                                    color = Neutral400
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (keywordInput.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Neutral200)
                                .clickable { viewModel.onClearKeyword() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Xóa",
                                tint = Neutral700,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                if (uiState is SearchUiState.Success) {
                    Spacer(modifier = Modifier.width(8.dp))
                    val isFilterActive = filter.activeCount > 0
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isFilterActive) Primary50 else Color(0xFFFAF5FF))
                                .border(
                                    width = 1.dp,
                                    color = if (isFilterActive) Primary500 else Color(0xFFE9D5FF),
                                    shape = CircleShape
                                )
                                .clickable { showFilterSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_filter),
                                contentDescription = "Bộ lọc nâng cao",
                                tint = Primary700,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (isFilterActive) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(NebulaBrush)
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter.activeCount.toString(),
                                    fontSize = 10.sp,
                                    fontFamily = EduMindFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    SearchIdleContent(
                        keyword = state.keyword,
                        suggestions = state.suggestions,
                        recentSearches = state.recentSearches,
                        trendingKeywords = state.trendingKeywords,
                        onRecentKeywordClick = viewModel::onSelectSuggestedKeyword,
                        onSuggestionClick = { course ->
                            viewModel.onSearchSubmit(course.title)
                        },
                        onTrendingKeywordClick = viewModel::onSelectSuggestedKeyword,
                        onClearRecent = viewModel::onClearRecentSearches
                    )
                }

                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Primary700,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                is SearchUiState.Success -> {
                    SearchSuccessContent(
                        keyword = state.keyword,
                        courses = state.courses,
                        filter = filter,
                        onSortOptionSelected = viewModel::onSortOptionSelected,
                        onCourseClick = onCourseClick
                    )
                }

                is SearchUiState.Empty -> {
                    SearchEmptyContent(
                        keyword = state.keyword,
                        suggestedKeywords = state.suggestedKeywords,
                        onKeywordClick = viewModel::onSelectSuggestedKeyword,
                        onExploreAllClick = onNavigateToExplore
                    )
                }

                is SearchUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = viewModel::retry
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = filter,
            onDismiss = { showFilterSheet = false },
            onApply = { newFilter ->
                viewModel.onApplyFilter(newFilter)
                showFilterSheet = false
            },
            onReset = {
                viewModel.onResetFilter()
                showFilterSheet = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchIdleContent(
    keyword: String,
    suggestions: List<CourseSummary>,
    recentSearches: List<String>,
    trendingKeywords: List<String>,
    onRecentKeywordClick: (String) -> Unit,
    onSuggestionClick: (CourseSummary) -> Unit,
    onTrendingKeywordClick: (String) -> Unit,
    onClearRecent: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (recentSearches.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TÌM KIẾM GẦN ĐÂY",
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral400,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Xóa tất cả",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary700,
                        modifier = Modifier.clickable(onClick = onClearRecent)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(recentSearches) { itemKeyword ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecentKeywordClick(itemKeyword) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Neutral400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = itemKeyword,
                            fontFamily = EduMindFontFamily,
                            fontSize = 14.sp,
                            color = Neutral700
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.NorthWest,
                        contentDescription = null,
                        tint = Neutral400,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Neutral100)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (keyword.isNotEmpty() && suggestions.isNotEmpty()) {
            item {
                Text(
                    text = "KHÓA HỌC GỢI Ý KHỚP TỪ KHÓA",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral400,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(suggestions, key = { it.id }) { course ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(course) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Neutral400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        HighlightedText(
                            fullText = course.title,
                            highlight = keyword
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Khóa học nền tảng · 14 giờ",
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            color = Neutral500
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Neutral300,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Neutral100)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Text(
                text = "TỪ KHÓA NỔI BẬT",
                fontFamily = EduMindFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral400,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                trendingKeywords.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Neutral100)
                            .clickable { onTrendingKeywordClick(tag) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Neutral700
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HighlightedText(
    fullText: String,
    highlight: String,
    modifier: Modifier = Modifier
) {
    val annotatedString = remember(fullText, highlight) {
        buildAnnotatedString {
            if (highlight.isBlank()) {
                append(fullText)
                return@buildAnnotatedString
            }
            val index = fullText.indexOf(highlight, ignoreCase = true)
            if (index == -1) {
                append(fullText)
            } else {
                append(fullText.substring(0, index))
                withStyle(
                    SpanStyle(
                        color = Primary700,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(fullText.substring(index, index + highlight.length))
                }
                append(fullText.substring(index + highlight.length))
            }
        }
    }

    Text(
        text = annotatedString,
        fontFamily = EduMindFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Neutral900,
        modifier = modifier
    )
}

@Composable
private fun SearchSuccessContent(
    keyword: String,
    courses: List<CourseSummary>,
    filter: ExploreFilter,
    onSortOptionSelected: (SortOption) -> Unit,
    onCourseClick: (String) -> Unit
) {
    var showSortDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Tìm thấy ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Neutral900)) {
                        append("${courses.size} kết quả")
                    }
                    append(" cho \"")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Primary700)) {
                        append(keyword)
                    }
                    append("\"")
                },
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                color = Neutral700
            )

            val sortLabel = when (filter.sortOption) {
                SortOption.NEWEST -> "Mới nhất"
                SortOption.POPULAR -> "Phổ biến"
                SortOption.PRICE_ASC -> "Giá tăng"
                SortOption.PRICE_DESC -> "Giá giảm"
            }

            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSortDropdown = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Sắp xếp: $sortLabel",
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        color = Primary700,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Chọn sắp xếp",
                        tint = Primary700,
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSortDropdown,
                    onDismissRequest = { showSortDropdown = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    SortOption.entries.forEach { option ->
                        val isSelected = option == filter.sortOption
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Primary700 else Neutral900
                                )
                            },
                            onClick = {
                                onSortOptionSelected(option)
                                showSortDropdown = false
                            }
                        )
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(Neutral50)
        ) {
            items(courses, key = { it.id }) { course ->
                CourseCard(
                    course = course,
                    onClick = { onCourseClick(course.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchEmptyContent(
    keyword: String,
    suggestedKeywords: List<String>,
    onKeywordClick: (String) -> Unit,
    onExploreAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFEFF6FF), Color(0xFFF5F3FF), Color(0xFFFDF2F8))
                    )
                )
                .border(8.dp, Color(0xFFEDE9FE).copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Primary600,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Không tìm thấy kết quả",
            fontFamily = EduMindFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral900
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = buildAnnotatedString {
                append("Rất tiếc, chúng tôi không tìm thấy khóa học nào phù hợp với từ khóa ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Neutral900)) {
                    append("\"$keyword\"")
                }
                append(".")
            },
            fontFamily = EduMindFontFamily,
            fontSize = 13.sp,
            color = Neutral500,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Neutral50)
                .border(1.dp, Neutral200.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Gợi ý tìm kiếm cho bạn:",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Neutral700
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedKeywords.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                            .clickable { onKeywordClick(tag) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Neutral700
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899))
                    )
                )
                .clickable { onExploreAllClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Khám phá tất cả khóa học",
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
