package com.edumind.app.ui.player.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.EduMindTheme
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.NebulaGradient
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral300
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorSheet(
    editorState: NoteEditorUiState,
    lessonTitle: String,
    onContentChange: (String) -> Unit,
    onSelectTag: (String) -> Unit,
    onSyncCurrentPosition: () -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!editorState.isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White,
        modifier = modifier
    ) {
        NoteEditorContent(
            editorState = editorState,
            lessonTitle = lessonTitle,
            onContentChange = onContentChange,
            onSelectTag = onSelectTag,
            onSyncCurrentPosition = onSyncCurrentPosition,
            onSaveClick = onSaveClick,
            onDismiss = onDismiss,
            onDeleteClick = onDeleteClick,
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
        )
    }
}

@Composable
fun NoteEditorContent(
    editorState: NoteEditorUiState,
    lessonTitle: String,
    onContentChange: (String) -> Unit,
    onSelectTag: (String) -> Unit,
    onSyncCurrentPosition: () -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val totalSeconds = editorState.timestampSeconds.toInt().coerceAtLeast(0)
    val formattedTimestamp = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    val availableTags = listOf("Cú pháp", "Lỗi thường gặp", "Lưu ý quan trọng")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        // Thanh kéo Drag Handle phía trên cùng
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Neutral300)
            )
        }

        // Header Sheet: Thoáng hơn, loại bỏ hoàn toàn card nền của nút X và nút Thùng rác
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Primary50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = Primary700,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (editorState.isEditing) "Chỉnh sửa ghi chú" else "Tạo ghi chú mới",
                        fontFamily = EduMindFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral900
                    )
                    Text(
                        text = lessonTitle.ifBlank { "Bài giảng hiện tại" },
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        color = Neutral500
                    )
                }
            }

            // Nút đóng 'X' hoặc 'Thùng rác': Phẳng hoàn toàn, không có card/box nền bao quanh
            if (editorState.isEditing && onDeleteClick != null) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa ghi chú này",
                        tint = Error600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Neutral500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        androidx.compose.material3.HorizontalDivider(color = Neutral100, thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        // Hộp hiển thị mốc thời gian video kèm nút "Đổi mốc hiện tại"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Primary50.copy(alpha = 0.6f))
                .border(1.dp, Primary100, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mốc thời gian:",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    color = Neutral700
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedTimestamp,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary700
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onSyncCurrentPosition)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Đổi mốc hiện tại",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary700
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Phân loại nhãn: Màu sắc semantic đồng bộ 100% với NoteItem khi được chọn
        Column {
            Text(
                text = "Phân loại nhãn (Tùy chọn):",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Neutral700,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                availableTags.forEach { tag ->
                    val isSelected = editorState.selectedTag == tag

                    val (bgColor, borderColor, textColor) = when {
                        isSelected && tag == "Cú pháp" -> Triple(Color(0xFFF5F3FF), Color(0xFF7C3AED), Color(0xFF7C3AED))
                        isSelected && tag == "Lỗi thường gặp" -> Triple(Color(0xFFFFFBEB), Color(0xFFCA8A04), Color(0xFFCA8A04))
                        isSelected -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), Color(0xFF1D4ED8))
                        else -> Triple(Color.White, Neutral200, Neutral700)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .border(
                                width = 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectTag(tag) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tag,
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Ô nhập liệu văn bản ghi chú (tối đa 500 ký tự)
        OutlinedTextField(
            value = editorState.content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            placeholder = {
                Text(
                    text = "Nhập suy nghĩ, công thức hoặc lưu ý quan trọng...",
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    color = Neutral400
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary700,
                unfocusedBorderColor = Neutral300,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Neutral100.copy(alpha = 0.35f)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!editorState.error.isNullOrBlank()) {
                Text(
                    text = editorState.error,
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    color = Error600
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Text(
                text = "${editorState.characterCount}/${NoteEditorUiState.MAX_CONTENT_LENGTH}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = if (editorState.characterCount >= NoteEditorUiState.MAX_CONTENT_LENGTH) Error600 else Neutral500
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hàng nút bấm: Hủy & Lưu ghi chú (Chiều cao 48dp chuẩn accessibility)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Hủy",
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Neutral700
                )
            }

            Button(
                onClick = onSaveClick,
                enabled = editorState.isValid,
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary700,
                    disabledContainerColor = Neutral200
                )
            ) {
                Text(
                    text = if (editorState.isSaving) "Đang lưu..." else if (editorState.isEditing) "Cập nhật thay đổi" else "Lưu ghi chú",
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (editorState.isValid) Color.White else Neutral500
                )
            }
        }
    }
}

@Preview(name = "M-16 NoteEditorSheet - Tạo mới", showBackground = true)
@Composable
private fun NoteEditorSheetCreatePreview() {
    EduMindTheme {
        NoteEditorContent(
            editorState = NoteEditorUiState(
                isOpen = true,
                isEditing = false,
                timestampSeconds = 150f,
                content = "",
                selectedTag = null
            ),
            lessonTitle = "Bài 03: Biến và Kiểu dữ liệu trong Kotlin",
            onContentChange = {},
            onSelectTag = {},
            onSyncCurrentPosition = {},
            onSaveClick = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "M-16 NoteEditorSheet - Chỉnh sửa", showBackground = true)
@Composable
private fun NoteEditorSheetEditPreview() {
    EduMindTheme {
        NoteEditorContent(
            editorState = NoteEditorUiState(
                isOpen = true,
                isEditing = true,
                noteId = "note_1",
                timestampSeconds = 75f,
                content = "Khai báo val là bất biến (immutable), không thể gán lại giá trị sau khi đã khởi tạo.",
                selectedTag = "Cú pháp"
            ),
            lessonTitle = "Bài 03: Biến và Kiểu dữ liệu trong Kotlin",
            onContentChange = {},
            onSelectTag = {},
            onSyncCurrentPosition = {},
            onSaveClick = {},
            onDismiss = {},
            onDeleteClick = {}
        )
    }
}
