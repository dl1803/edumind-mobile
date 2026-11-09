package com.edumind.app.ui.ai.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.LectureSummary
import com.edumind.app.data.model.response.SummarySection
import com.edumind.app.ui.player.PocPlayerState
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.EduMindTheme
import com.edumind.app.ui.theme.Error200
import com.edumind.app.ui.theme.Error50
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.NebulaGradient
import com.edumind.app.ui.theme.NebulaPurple
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral300
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral50
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary200
import com.edumind.app.ui.theme.Primary300
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Primary900
import com.edumind.app.ui.theme.Primary950
import com.edumind.app.ui.theme.Success600
import com.edumind.app.ui.theme.Warning600

private const val SHEET_HEIGHT_FRACTION = 0.82f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    state: SummaryUiState,
    lessonTitle: String = "Bài 03: Biến & Kiểu dữ liệu cơ bản",
    onGenerate: () -> Unit,
    onRetry: () -> Unit,
    onToggleSection: (String) -> Unit = {},
    onTimestampClick: (Long) -> Unit,
    onSaveToNotes: () -> Unit = {},
    onAskEduAI: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = null,
        windowInsets = WindowInsets(0)
    ) {
        SummarySheetContent(
            state = state,
            lessonTitle = lessonTitle,
            onGenerate = onGenerate,
            onRetry = onRetry,
            onToggleSection = onToggleSection,
            onTimestampClick = onTimestampClick,
            onSaveToNotes = onSaveToNotes,
            onAskEduAI = onAskEduAI,
            onClose = onDismiss,
            modifier = Modifier
                .fillMaxHeight(SHEET_HEIGHT_FRACTION)
                .fillMaxWidth()
        )
    }
}

@Composable
fun SummarySheetContent(
    state: SummaryUiState,
    lessonTitle: String = "Bài 03: Biến & Kiểu dữ liệu cơ bản",
    onGenerate: () -> Unit,
    onRetry: () -> Unit,
    onToggleSection: (String) -> Unit = {},
    onTimestampClick: (Long) -> Unit,
    onSaveToNotes: () -> Unit = {},
    onAskEduAI: () -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveModal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SummaryDragHandle(onClose = onClose)
            SummaryHeader(
                isLoading = state is SummaryUiState.Loading
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Neutral200.copy(alpha = 0.8f)
            )

            when (state) {
                SummaryUiState.Empty -> {
                    SummaryEmptyBody(onGenerate = onGenerate)
                }

                SummaryUiState.Loading -> {
                    SummaryLoadingBody()
                }

                is SummaryUiState.Error -> {
                    SummaryErrorBody(
                        message = state.message,
                        onRetry = onRetry
                    )
                }

                is SummaryUiState.Success -> {
                    SummarySuccessBody(
                        state = state,
                        lessonTitle = lessonTitle,
                        onToggleSection = onToggleSection,
                        onTimestampClick = onTimestampClick,
                        onRegenerate = onGenerate,
                        onOpenSaveNotes = { showSaveModal = true },
                        onAskEduAI = onAskEduAI
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showSaveModal,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SaveToNotesModal(
                lessonTitle = lessonTitle,
                onClose = { showSaveModal = false },
                onSave = {
                    showSaveModal = false
                    onSaveToNotes()
                }
            )
        }
    }
}

@Composable
private fun SummaryDragHandle(onClose: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClose)
            .padding(top = 10.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Neutral300)
        )
    }
}

@Composable
private fun SummaryHeader(
    isLoading: Boolean,
    remainingQuota: String = "Còn 8/10 lượt hôm nay"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NebulaGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(
                text = if (isLoading) "EduAI đang tóm tắt..." else "EduAI Tóm tắt bài giảng",
                fontFamily = EduMindFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Primary950
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Primary50)
                .border(1.dp, Primary200, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = remainingQuota,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary700
            )
        }
    }
}

@Composable
private fun SummarySuccessBody(
    state: SummaryUiState.Success,
    lessonTitle: String,
    onToggleSection: (String) -> Unit = {},
    onTimestampClick: (Long) -> Unit,
    onRegenerate: () -> Unit,
    onOpenSaveNotes: () -> Unit,
    onAskEduAI: () -> Unit
) {
    var selectedVersion by remember { mutableIntStateOf(1) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Neutral50.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = lessonTitle,
                fontFamily = EduMindFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Neutral600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Bản:",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Neutral500
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (selectedVersion == 1) Modifier.background(NebulaGradient)
                            else Modifier.background(Neutral200.copy(alpha = 0.7f))
                        )
                        .clickable { selectedVersion = 1 }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Bản 1",
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (selectedVersion == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedVersion == 1) Color.White else Neutral700
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (selectedVersion == 2) Modifier.background(NebulaGradient)
                            else Modifier.background(Neutral200.copy(alpha = 0.7f))
                        )
                        .clickable { selectedVersion = 2 }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Bản 2",
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (selectedVersion == 2) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedVersion == 2) Color.White else Neutral700
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Neutral200.copy(alpha = 0.8f)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Success600,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Trích xuất từ 1.420 từ transcript",
                                fontFamily = EduMindFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary700
                            )
                        }
                        Text(
                            text = "Độ tin cậy: 98%",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Neutral500
                        )
                    }
                }

                if (selectedVersion == 1) {
                    itemsIndexed(state.summary.sections) { index, section ->
                        SummaryCardItem(
                            index = index,
                            section = section,
                            onToggle = { onToggleSection(section.id) },
                            onTimestampClick = onTimestampClick
                        )
                    }

                    item {
                        CalloutCard(
                            title = "Quy ước chuẩn PEP 8",
                            content = "Nên dùng quy cách đặt tên snake_case cho biến và hàm trong Python (VD: total_count thay vì viết hoa)."
                        )
                    }
                } else {
                    itemsIndexed(Version2Sections) { index, section ->
                        SummaryCardItem(
                            index = index,
                            section = section,
                            onToggle = { onToggleSection(section.id) },
                            onTimestampClick = onTimestampClick
                        )
                    }

                    item {
                        CodeSnippetBox()
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NebulaGradient)
                    .clickable(onClick = onAskEduAI),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "Hỏi EduAI",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Neutral200)
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Primary700, RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable(onClick = onRegenerate),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = Primary700,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tạo lại bản mới",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1.6f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NebulaGradient)
                    .clickable(onClick = onOpenSaveNotes),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Lưu vào ghi chú",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCardItem(
    index: Int,
    section: SummarySection,
    onToggle: () -> Unit = {},
    onTimestampClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Neutral50.copy(alpha = 0.8f))
            .border(1.dp, Primary200.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Primary100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }
                Text(
                    text = section.title,
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900
                )
            }
            section.startMs?.let { startMs ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Primary100)
                        .clickable(role = Role.Button) { onTimestampClick(startMs) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Xem tại mốc thời gian",
                            tint = Primary700,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = PocPlayerState.formatTime(startMs),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary700
                        )
                    }
                }
            }
        }
        Text(
            text = section.content,
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = Neutral600
        )
    }
}

@Composable
private fun CalloutCard(title: String, content: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Primary50)
            .border(1.dp, Primary200.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Lightbulb,
            contentDescription = null,
            tint = Primary700,
            modifier = Modifier.size(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontFamily = EduMindFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Primary950
            )
            Text(
                text = content,
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Primary900.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun CodeSnippetBox() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Neutral950)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "# Code mẫu ôn tập nhanh:",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Neutral400
        )
        Text(
            text = "score = 100  # int",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = NebulaPurple
        )
        Text(
            text = "rate = 4.5  # float",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Warning600
        )
        Text(
            text = "msg = f\"Điểm: {score}\"  # f-string",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Success600
        )
    }
}

@Composable
private fun SaveToNotesModal(
    lessonTitle: String,
    onClose: () -> Unit,
    onSave: () -> Unit
) {
    val selectedItems = remember {
        mutableStateMapOf(
            0 to true,
            1 to true,
            2 to false,
            3 to true
        )
    }

    val saveNotesItems = listOf(
        Triple("Mục 1: Quy tắc đặt tên biến", "02:15", "Chỉ bắt đầu bằng chữ cái hoặc gạch dưới, không trùng 35 từ khóa."),
        Triple("Mục 2: 4 Kiểu dữ liệu nguyên thủy", "07:40", "Gồm int (số nguyên), float (số thực), str (chuỗi) và bool (True/False)."),
        Triple("Mục 3: Ép kiểu dữ liệu Casting", "11:50", "Chuyển đổi type() tường minh bằng hàm int(), str()."),
        Triple("Lưu ý thực chiến PEP 8", "Quy chuẩn", "Quy ước đặt tên snake_case cho biến và hàm trong Python.")
    )

    val selectedCount = selectedItems.values.count { it }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SummaryDragHandle()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Primary100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = null,
                            tint = Primary700,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Lưu vào Sổ ghi chú",
                            fontFamily = EduMindFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Neutral900
                        )
                        Text(
                            text = lessonTitle,
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            color = Neutral500
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Primary50.copy(alpha = 0.7f))
                    .border(1.dp, Primary200)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "THƯ MỤC ĐÍCH LƯU TRỮ:",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary700
                )
                Text(
                    text = "Lập trình Python > Chương 1 > Bài 03: Biến",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary950
                )
                Text(
                    text = "Xem lại bất cứ lúc nào tại Sổ ghi chú.",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    color = Neutral500
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Chọn các mục muốn lưu ($selectedCount/4):",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral700
                )
                Text(
                    text = "Chọn tất cả",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary700,
                    modifier = Modifier.clickable {
                        for (i in 0..3) selectedItems[i] = true
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(saveNotesItems) { index, item ->
                    val isChecked = selectedItems[index] == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isChecked) Primary50.copy(alpha = 0.5f) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isChecked) Primary300 else Neutral200.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedItems[index] = !isChecked }
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isChecked) 0.dp else 1.5.dp,
                                    color = if (isChecked) Color.Transparent else Neutral300,
                                    shape = CircleShape
                                )
                                .then(
                                    if (isChecked) Modifier.background(NebulaGradient)
                                    else Modifier.background(Color.White)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChecked) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.first,
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) Neutral900 else Neutral700
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isChecked) Primary100 else Neutral100)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.second,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isChecked) Primary700 else Neutral600
                                    )
                                }
                            }
                            Text(
                                text = item.third,
                                fontFamily = EduMindFontFamily,
                                fontSize = 11.sp,
                                color = Neutral600
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Neutral200)
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Neutral100)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hủy",
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral700
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NebulaGradient)
                        .clickable(onClick = onSave),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Lưu $selectedCount mục đã chọn",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryEmptyBody(onGenerate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Primary50)
                .border(1.dp, Primary200, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Primary700,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Chưa có bản tóm tắt bài giảng",
            fontFamily = EduMindFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral900
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "EduAI sẽ trích xuất ý chính của bài giảng và gắn mốc thời gian để bạn xem lại video nhanh chóng.",
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = Neutral600,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(280.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .width(270.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NebulaGradient)
                .clickable(onClick = onGenerate),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Tạo tóm tắt bài giảng",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SummaryLoadingBody() {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary50.copy(alpha = 0.9f))
                .border(1.dp, Primary200)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Primary700,
                strokeWidth = 2.dp
            )
            Text(
                text = "EduAI đang tổng hợp ý chính và gắn mốc video...",
                fontFamily = EduMindFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Primary950
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            repeat(3) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Neutral50)
                        .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 160.dp, height = 16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Neutral200)
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 48.dp, height = 14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Neutral200)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Neutral100)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Neutral100)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Neutral200)
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Neutral200)
            )
            Box(
                modifier = Modifier
                    .weight(1.6f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Neutral300)
            )
        }
    }
}

@Composable
private fun SummaryErrorBody(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Error50)
                .border(1.dp, Error200, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Error600,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Chưa thể tạo bản tóm tắt tự động",
            fontFamily = EduMindFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral900
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message.ifBlank { "Hệ thống đang hoàn thiện trích xuất transcript chi tiết cho video bài học này hoặc kết nối mạng tạm thời bị gián đoạn." },
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = Neutral600,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(260.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .width(270.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NebulaGradient)
                .clickable(onClick = onRetry),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Thử lại ngay",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private val Version1Summary = LectureSummary(
    lessonId = "lesson_03",
    keyPoints = listOf(
        "Quy tắc đặt tên biến",
        "4 Kiểu dữ liệu nguyên thủy cơ bản",
        "Ép kiểu tường minh"
    ),
    sections = listOf(
        SummarySection(
            id = "sec_1",
            title = "Quy tắc đặt tên biến (Identifier)",
            content = "Tên biến chỉ được bắt đầu bằng chữ cái (a-z, A-Z) hoặc dấu gạch dưới (_). Tuyệt đối không bắt đầu bằng số và không trùng 35 từ khóa bảo lưu của Python.",
            startMs = 135_000L
        ),
        SummarySection(
            id = "sec_2",
            title = "4 Kiểu dữ liệu nguyên thủy cơ bản",
            content = "Bao gồm: int (số nguyên), float (số thực), str (chuỗi ký tự nháy đơn/kép), và bool (True/False).",
            startMs = 460_000L
        ),
        SummarySection(
            id = "sec_3",
            title = "Ép kiểu tường minh (Type Casting)",
            content = "Dùng hàm type() để xem kiểu dữ liệu. Chuyển đổi qua lại bằng hàm int('20'), str(100).",
            startMs = 710_000L
        )
    ),
    generatedAt = "2026-11-07T09:00:00Z"
)

private val Version2Sections = listOf(
    SummarySection(
        id = "sec_v2_1",
        title = "Khởi tạo biến đa hình & Kiểm tra ID bộ nhớ",
        content = "Mỗi biến trỏ tới một object trong bộ nhớ. Sử dụng hàm id(biến) để kiểm tra địa chỉ RAM khi gán lại giá trị mới.",
        startMs = 270_000L
    ),
    SummarySection(
        id = "sec_v2_2",
        title = "Lỗi thường gặp: TypeError khi cộng chuỗi và số",
        content = "Trong Python, lệnh \"Tuổi: \" + 20 sẽ văng lỗi TypeError. Bắt buộc dùng f-string: f\"Tuổi: {20}\".",
        startMs = 555_000L
    ),
    SummarySection(
        id = "sec_v2_3",
        title = "Câu hỏi phỏng vấn: Mutable vs Immutable",
        content = "Số nguyên (int), số thực (float) và chuỗi (str) là dạng bất biến (Immutable). Thay đổi giá trị thực chất là tạo ra ô nhớ mới.",
        startMs = 800_000L
    )
)

@Preview(name = "1. M-14 Bản 1 - Lý thuyết & Khái niệm", showBackground = true, heightDp = 720)
@Composable
private fun SummaryVersion1Preview() {
    EduMindTheme {
        SummarySheetContent(
            state = SummaryUiState.Success(Version1Summary, emptySet()),
            lessonTitle = "Bài 03: Biến & Kiểu dữ liệu cơ bản",
            onGenerate = {},
            onRetry = {},
            onTimestampClick = {},
            onClose = {}
        )
    }
}

@Preview(name = "2. M-14 Đang tổng hợp (Loading Skeleton)", showBackground = true, heightDp = 720)
@Composable
private fun SummaryLoadingPreview() {
    EduMindTheme {
        SummarySheetContent(
            state = SummaryUiState.Loading,
            lessonTitle = "Bài 03: Biến & Kiểu dữ liệu cơ bản",
            onGenerate = {},
            onRetry = {},
            onTimestampClick = {},
            onClose = {}
        )
    }
}

@Preview(name = "3. M-14 Lỗi tạo tóm tắt (Error State)", showBackground = true, heightDp = 720)
@Composable
private fun SummaryErrorPreview() {
    EduMindTheme {
        SummarySheetContent(
            state = SummaryUiState.Error("Hệ thống đang hoàn thiện trích xuất transcript chi tiết cho video bài học này hoặc kết nối mạng tạm thời bị gián đoạn."),
            lessonTitle = "Bài 03: Biến & Kiểu dữ liệu cơ bản",
            onGenerate = {},
            onRetry = {},
            onTimestampClick = {},
            onClose = {}
        )
    }
}

@Preview(name = "4. M-14 Chưa có tóm tắt (Empty State)", showBackground = true, heightDp = 720)
@Composable
private fun SummaryEmptyPreview() {
    EduMindTheme {
        SummarySheetContent(
            state = SummaryUiState.Empty,
            lessonTitle = "Bài 03: Biến & Kiểu dữ liệu cơ bản",
            onGenerate = {},
            onRetry = {},
            onTimestampClick = {},
            onClose = {}
        )
    }
}

@Preview(name = "5. M-15 Lưu vào Ghi chú (Modal)", showBackground = true, heightDp = 720)
@Composable
private fun SummarySaveToNotesPreview() {
    EduMindTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            SaveToNotesModal(
                lessonTitle = "Bài 03: Biến & Kiểu dữ liệu cơ bản",
                onClose = {},
                onSave = {}
            )
        }
    }
}
