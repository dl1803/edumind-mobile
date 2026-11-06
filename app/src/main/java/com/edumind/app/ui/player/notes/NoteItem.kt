package com.edumind.app.ui.player.notes

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.Note
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.EduMindTheme
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral800
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary700
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteItem(
    note: Note,
    onSeek: (Long) -> Unit,
    onEditClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .animateContentSize()
    ) {
        // Hàng trên: Badge nhãn phân loại (bên trái) + Nút sửa/xóa phẳng (bên phải) theo Frame 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (note.tagLabel != null) {
                NoteTagBadge(tag = note.tagLabel!!)
            } else {
                // Nếu không có nhãn thì hiển thị mốc thời gian Zero-Pill ở góc trên
                ZeroPillTimestamp(
                    formattedTimestamp = note.formattedTimestamp,
                    onClick = { onSeek(note.timestampMs) }
                )
            }

            // Nút sửa và xóa phẳng (không nền card) với touch target 36-40dp
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onEditClick(note) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sửa ghi chú",
                        tint = Neutral400,
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(
                    onClick = { onDeleteClick(note) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa ghi chú",
                        tint = Error600.copy(alpha = 0.8f),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nội dung văn bản ghi chú (sử dụng cleanContent đã bỏ tiền tố tag)
        Text(
            text = note.cleanContent,
            fontFamily = EduMindFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Neutral800,
            lineHeight = 19.sp,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis
        )

        // Nút "Xem thêm" / "Thu gọn" nếu nội dung dài
        if (note.cleanContent.length > 120 || note.cleanContent.count { it == '\n' } >= 3) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isExpanded) "Thu gọn" else "Xem thêm",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary700,
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Neutral100, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // Hàng dưới (Footer chuẩn Frame 3): Nguồn (bên trái) + Mốc thời gian & ngày tạo (bên phải)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Neutral400,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Viết tay",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    color = Neutral500
                )
            }

            // Mốc thời gian bấm tua video (Font monospace màu tím gạch chân) + ngày tạo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onSeek(note.timestampMs) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Mốc ",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary700
                )
                Text(
                    text = note.formattedTimestamp,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary700,
                    modifier = Modifier
                        .padding(bottom = 1.dp)
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Primary700,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "- ${formatNoteDate(note.createdAt)}",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    color = Neutral400
                )
            }
        }
    }
}

@Composable
fun ZeroPillTimestamp(
    formattedTimestamp: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Primary50)
            .border(1.dp, Primary100, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Tua đến mốc này",
                tint = Primary700,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "Mốc $formattedTimestamp",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Primary700
            )
        }
    }
}

@Composable
fun NoteTagBadge(tag: String) {
    val (bgColor, borderColor, textColor) = when {
        tag == "Cú pháp" -> Triple(Color(0xFFF5F3FF), Color(0xFFDDD6FE), Color(0xFF7C3AED))
        tag == "Lỗi thường gặp" -> Triple(Color(0xFFFFFBEB), Color(0xFFFDE68A), Color(0xFFCA8A04))
        else -> Triple(Color(0xFFEFF6FF), Color(0xFFBFDBFE), Color(0xFF1D4ED8))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = tag,
            fontFamily = EduMindFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

private fun formatNoteDate(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (_: Exception) {
        ""
    }
}

@Preview(name = "M-15 NoteItem Samples", showBackground = true)
@Composable
private fun NoteItemPreview() {
    EduMindTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFAFAFA))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NoteItem(
                note = Note(
                    id = "1",
                    lessonId = "lesson_1",
                    content = "[Cú pháp] Quy tắc đặt tên biến: Luôn dùng cú pháp snake_case theo PEP 8. Không bắt đầu bằng số.",
                    timestampSeconds = 135f,
                    createdAt = System.currentTimeMillis() - 3600000,
                    updatedAt = System.currentTimeMillis() - 3600000
                ),
                onSeek = {},
                onEditClick = {},
                onDeleteClick = {}
            )

            NoteItem(
                note = Note(
                    id = "2",
                    lessonId = "lesson_1",
                    content = "[Lỗi thường gặp] Cơ chế Dynamic Typing: Python tự nhận diện kiểu dữ liệu dựa trên giá trị gán, có thể dùng hàm type() để kiểm tra. Tránh nhầm lẫn khi ép kiểu dữ liệu từ chuỗi nhập vào bằng input().",
                    timestampSeconds = 332f,
                    createdAt = System.currentTimeMillis() - 7200000,
                    updatedAt = System.currentTimeMillis() - 7200000
                ),
                onSeek = {},
                onEditClick = {},
                onDeleteClick = {}
            )

            NoteItem(
                note = Note(
                    id = "3",
                    lessonId = "lesson_1",
                    content = "[Lưu ý quan trọng] Khi ép float sang int bằng int(3.99) kết quả sẽ là 3 (bị cắt phần thập phân chứ không làm tròn).",
                    timestampSeconds = 710f,
                    createdAt = System.currentTimeMillis() - 86400000,
                    updatedAt = System.currentTimeMillis() - 86400000
                ),
                onSeek = {},
                onEditClick = {},
                onDeleteClick = {}
            )
        }
    }
}
