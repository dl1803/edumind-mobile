package com.edumind.app.ui.player.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.Note
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.EduMindTheme
import com.edumind.app.ui.theme.Error100
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.NebulaGradient
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700

@Composable
fun NotesTab(
    viewModel: NotesViewModel,
    currentPositionMs: Long,
    @Suppress("UNUSED_PARAMETER") lessonTitle: String,
    onSeek: (Long) -> Unit,
    onPauseVideo: () -> Unit,
    onResumeVideo: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val noteToDelete by viewModel.noteToDelete.collectAsState()
    val selectedFilterTag by viewModel.selectedFilterTag.collectAsState()

    NotesTabContent(
        uiState = uiState,
        noteToDelete = noteToDelete,
        selectedFilterTag = selectedFilterTag,
        currentPositionMs = currentPositionMs,
        onFilterTagSelected = { viewModel.setFilterTag(it) },
        onAddClick = {
            onPauseVideo()
            viewModel.openCreateSheet(currentPositionMs)
        },
        onSeek = onSeek,
        onEditClick = {
            onPauseVideo()
            viewModel.openEditSheet(it)
        },
        onDeleteClick = {
            onPauseVideo()
            viewModel.requestDelete(it)
        },
        onConfirmDelete = {
            viewModel.confirmDelete(onSuccess = onResumeVideo)
        },
        onCancelDelete = {
            viewModel.cancelDelete()
            onResumeVideo?.invoke()
        },
        onRetry = { viewModel.loadNotes() },
        modifier = modifier
    )
}

@Composable
fun NotesTabContent(
    uiState: NotesUiState,
    noteToDelete: Note?,
    selectedFilterTag: String,
    currentPositionMs: Long,
    onFilterTagSelected: (String) -> Unit,
    onAddClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onEditClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPosSeconds = (currentPositionMs.coerceAtLeast(0L) / 1000).toInt()
    val formattedCurrentPos = String.format("%02d:%02d", currentPosSeconds / 60, currentPosSeconds % 60)
    val filterTags = listOf("Tất cả", "Cú pháp", "Lỗi thường gặp", "Lưu ý quan trọng")

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {
        // TRƯỜNG HỢP 1: Trạng thái Rỗng hoàn toàn (Empty State) theo đúng Frame 3B
        // KHÔNG hiển thị thanh nhãn và KHÔNG hiển thị header "Ghi chú trong bài / Thêm tại..."
        if (uiState is NotesUiState.Empty) {
            NotesEmptyState(
                onAddClick = onAddClick,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // TRƯỜNG HỢP 2: Có ghi chú hoặc đang tải/lỗi/lọc rỗng
            Column(modifier = Modifier.fillMaxSize()) {
                // Thanh lọc nhãn Tag (Full-width nền trắng với viền đáy border-neutral-100 chuẩn Frame 3)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Nhãn:",
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Neutral400,
                            modifier = Modifier.padding(end = 2.dp)
                        )

                        filterTags.forEach { tag ->
                            val isSelected = selectedFilterTag == tag
                            val countText = if (tag == "Tất cả" && uiState is NotesUiState.Success) {
                                " (${uiState.notes.size})"
                            } else ""

                            if (isSelected) {
                                // Chip đang chọn: Nổi bật với dải màu chủ đạo 3 màu (Nebula Gradient) + chữ trắng đậm
                                Box(
                                    modifier = Modifier
                                        .shadow(elevation = 1.dp, shape = RoundedCornerShape(50))
                                        .clip(RoundedCornerShape(50))
                                        .background(NebulaGradient)
                                        .clickable { onFilterTagSelected(tag) }
                                        .padding(horizontal = 12.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$tag$countText",
                                        fontFamily = EduMindFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                // Các chip không chọn: Màu trắng bình thường + viền mờ Neutral200
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White)
                                        .border(
                                            width = 1.dp,
                                            color = Neutral200,
                                            shape = RoundedCornerShape(50)
                                        )
                                        .clickable { onFilterTagSelected(tag) }
                                        .padding(horizontal = 12.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$tag$countText",
                                        fontFamily = EduMindFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Neutral700
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Neutral100, thickness = 1.dp)
                }

                // Vùng nội dung ghi chú bên dưới (padding 16dp)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Dòng tiêu đề section: "Ghi chú trong bài (x)" + Nút gradient "+ Thêm tại mm:ss"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    val countText = when (uiState) {
                        is NotesUiState.Success -> " (${uiState.notes.size})"
                        else -> ""
                    }
                    Text(
                        text = "Ghi chú trong bài$countText",
                        fontFamily = EduMindFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral900
                    )

                    Box(
                        modifier = Modifier
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(NebulaGradient)
                            .clickable(onClick = onAddClick)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Thêm tại $formattedCurrentPos",
                                fontFamily = EduMindFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                when (uiState) {
                    is NotesUiState.Loading -> {
                        NotesSkeletonLoading()
                    }

                    is NotesUiState.Error -> {
                        ErrorView(
                            message = uiState.message,
                            onRetry = onRetry,
                            retryText = "Thử lại ngay"
                        )
                    }

                    is NotesUiState.Success -> {
                        if (uiState.notes.isEmpty()) {
                            // Khi có ghi chú nhưng lọc theo nhãn không có kết quả
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không có ghi chú nào thuộc nhãn '$selectedFilterTag'.",
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 13.sp,
                                    color = Neutral500
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.notes, key = { it.id }) { note ->
                                    NoteItem(
                                        note = note,
                                        onSeek = onSeek,
                                        onEditClick = onEditClick,
                                        onDeleteClick = onDeleteClick
                                    )
                                }
                            }
                        }
                    }

                    is NotesUiState.Empty -> {
                        // Handled above
                    }
                }
            }
        }
    }

        // Hộp thoại xác nhận xóa ghi chú (Modal Dialog)
        if (noteToDelete != null) {
            val target = noteToDelete
            AlertDialog(
                onDismissRequest = onCancelDelete,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Error100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Error600,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Xác nhận xóa ghi chú?",
                        fontFamily = EduMindFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral900,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Ghi chú tại mốc ${target.formattedTimestamp} sẽ bị xóa vĩnh viễn khỏi bài học này.",
                        fontFamily = EduMindFontFamily,
                        fontSize = 13.sp,
                        color = Neutral600,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Error600),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "Xóa ngay",
                            fontFamily = EduMindFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onCancelDelete,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "Hủy bỏ",
                            fontFamily = EduMindFontFamily,
                            fontSize = 13.sp,
                            color = Neutral700
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
private fun NotesSkeletonLoading() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Neutral100)
            )
        }
    }
}

/**
 * Minh họa Empty State cuốn sổ có bút và nút + chuẩn 99% theo Frame 3B của Prototype M-15 HTML.
 */
@Composable
private fun NotesEmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Minh họa SVG cuốn sổ có bút và huy hiệu '+' (Khung ngoài 128x128dp)
        Box(
            modifier = Modifier.size(128.dp),
            contentAlignment = Alignment.Center
        ) {
            // Hộp nền vuông bo góc 28dp với gradient siêu nhẹ grad-nebula-soft và viền tím mờ
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFEFF6FF), // Blue 50
                                Color(0xFFF5F3FF), // Purple 50
                                Color(0xFFFDF2F8)  // Pink 50
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFDDD6FE).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Tờ giấy trắng 80x96dp, bo góc 12dp, đổ bóng, viền, xoay nghiêng nhẹ 2.5 độ
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 96.dp)
                        .graphicsLayer { rotationZ = 2.5f }
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Thanh header tím trên cuốn sổ: 32x6dp
                            Box(
                                modifier = Modifier
                                    .size(width = 32.dp, height = 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Primary600)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Dòng kẻ ngang 1
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFE5E7EB))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Dòng kẻ ngang 2 (ngắn 70%)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFE5E7EB))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Dòng kẻ ngang 3
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFE5E7EB))
                            )
                        }

                        // Icon cây bút chì tím nhỏ ở góc dưới phải của tờ giấy
                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .size(18.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color(0xFFF5F3FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Primary700,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }

            // Huy hiệu tròn nổi '+' gradient Nebula ở góc dưới phải của khung ngoài 128dp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
                    .size(32.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(NebulaGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tiêu đề
        Text(
            text = "Chưa có ghi chú nào",
            fontFamily = EduMindFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral900
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Mô tả
        Text(
            text = "Ghi chú giúp bạn lưu lại các đoạn code hay và mốc thời gian bài giảng để ôn tập trước kỳ kiểm tra.",
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            color = Neutral500,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Nút CTA "+ Tạo ghi chú đầu tiên" bo tròn full pill theo Frame 3B
        Box(
            modifier = Modifier
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(NebulaGradient)
                .clickable(onClick = onAddClick)
                .padding(horizontal = 22.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tạo ghi chú đầu tiên",
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(name = "M-15 NotesTab - Có dữ liệu (Success)", showBackground = true)
@Composable
private fun NotesTabSuccessPreview() {
    val sampleNotes = listOf(
        Note(
            id = "1",
            lessonId = "lesson_1",
            content = "[Cú pháp] Khai báo biến với từ khóa val (bất biến) và var (khả biến).",
            timestampSeconds = 45f,
            createdAt = System.currentTimeMillis() - 3600000,
            updatedAt = System.currentTimeMillis() - 3600000
        ),
        Note(
            id = "2",
            lessonId = "lesson_1",
            content = "[Lỗi thường gặp] Tránh gọi hàm blocking trên Main Thread vì sẽ gây giật lag hoặc ANR. Luôn dùng Dispatchers.IO.",
            timestampSeconds = 125f,
            createdAt = System.currentTimeMillis() - 7200000,
            updatedAt = System.currentTimeMillis() - 7200000
        ),
        Note(
            id = "3",
            lessonId = "lesson_1",
            content = "[Lưu ý quan trọng] Luôn nhớ đóng kết nối Stream sau khi hoàn tất đọc tệp.",
            timestampSeconds = 3680f,
            createdAt = System.currentTimeMillis() - 86400000,
            updatedAt = System.currentTimeMillis() - 86400000
        )
    )

    EduMindTheme {
        NotesTabContent(
            uiState = NotesUiState.Success(sampleNotes),
            noteToDelete = null,
            selectedFilterTag = "Tất cả",
            currentPositionMs = 90000L,
            onFilterTagSelected = {},
            onAddClick = {},
            onSeek = {},
            onEditClick = {},
            onDeleteClick = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onRetry = {}
        )
    }
}

@Preview(name = "M-15 NotesTab - Rỗng (Empty State)", showBackground = true)
@Composable
private fun NotesTabEmptyPreview() {
    EduMindTheme {
        NotesTabContent(
            uiState = NotesUiState.Empty,
            noteToDelete = null,
            selectedFilterTag = "Tất cả",
            currentPositionMs = 0L,
            onFilterTagSelected = {},
            onAddClick = {},
            onSeek = {},
            onEditClick = {},
            onDeleteClick = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onRetry = {}
        )
    }
}

@Preview(name = "M-15 NotesTab - Đang tải (Loading Skeleton)", showBackground = true)
@Composable
private fun NotesTabLoadingPreview() {
    EduMindTheme {
        NotesTabContent(
            uiState = NotesUiState.Loading,
            noteToDelete = null,
            selectedFilterTag = "Tất cả",
            currentPositionMs = 0L,
            onFilterTagSelected = {},
            onAddClick = {},
            onSeek = {},
            onEditClick = {},
            onDeleteClick = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onRetry = {}
        )
    }
}
