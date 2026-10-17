package com.edumind.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.ui.common.CourseCard
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral50
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val keywordInput by viewModel.keywordInput.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
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
                    .background(Neutral100)
                    .border(
                        width = 1.dp,
                        color = if (keywordInput.isNotEmpty()) Primary600 else Neutral200,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = if (keywordInput.isNotEmpty()) Primary700 else Neutral400,
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
        }

        when (val state = uiState) {
            is SearchUiState.Idle -> {
                SearchIdleContent(
                    recentSearches = state.recentSearches,
                    trendingKeywords = state.trendingKeywords,
                    onKeywordClick = viewModel::onSelectSuggestedKeyword,
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
                    onCourseClick = onCourseClick
                )
            }

            is SearchUiState.Empty -> {
                SearchEmptyContent(keyword = state.keyword)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchIdleContent(
    recentSearches: List<String>,
    trendingKeywords: List<String>,
    onKeywordClick: (String) -> Unit,
    onClearRecent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (recentSearches.isNotEmpty()) {
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

            Spacer(modifier = Modifier.height(8.dp))

            recentSearches.forEach { keyword ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onKeywordClick(keyword) }
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
                            text = keyword,
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

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Neutral100)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

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
}

@Composable
private fun SearchSuccessContent(
    keyword: String,
    courses: List<CourseSummary>,
    onCourseClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf("Tất cả (${courses.size})", "Đánh giá 4.5★+", "Dưới 500k", "Miễn phí", "Mới nhất")
            chips.forEachIndexed { index, title ->
                val isSelected = index == 0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Primary700 else Neutral100)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Neutral700
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tìm thấy ${courses.size} kết quả cho \"$keyword\"",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                color = Neutral700
            )
            Text(
                text = "Sắp xếp: Mới nhất",
                fontFamily = EduMindFontFamily,
                fontSize = 11.sp,
                color = Neutral400
            )
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

@Composable
private fun SearchEmptyContent(keyword: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Primary100)
                .border(6.dp, Primary100.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Primary700,
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
            text = "Rất tiếc, chúng tôi không tìm thấy khóa học nào phù hợp với từ khóa \"$keyword\".",
            fontFamily = EduMindFontFamily,
            fontSize = 13.sp,
            color = Neutral700,
            textAlign = TextAlign.Center
        )
    }
}
