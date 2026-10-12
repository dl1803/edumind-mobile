package com.edumind.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.NebulaTextBrush
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Success600
import com.edumind.app.ui.theme.Warning600
import com.edumind.app.util.toVnd

@Composable
fun CourseCard(
    course: CourseSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral200.copy(alpha = 0.6f))
    ) {
        Column {
            // Thumbnail 16:9 hỗ trợ cả JPG và SVG
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Neutral100)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(course.thumbnailUrl)
                        .decoderFactory(SvgDecoder.Factory())
                        .crossfade(true)
                        .build(),
                    contentDescription = course.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }

            // Nội dung Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Tiêu đề khóa học: cố định đúng 2 dòng, nếu lố thì dùng '...'
                Text(
                    text = course.title,
                    fontFamily = EduMindFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Neutral900,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tên giảng viên
                Text(
                    text = course.instructorName,
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    color = Neutral500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Hàng đánh giá: bên trái là điểm sao, bên phải là số lượt đánh giá
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Warning600,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = course.rating.toString(),
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            color = Neutral700,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "${course.reviewCount} đánh giá",
                        fontFamily = EduMindFontFamily,
                        fontSize = 11.sp,
                        color = Neutral500,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Dấu gạch ngang nhạt ngăn cách giá tiền với phần trên
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF1F1F4))
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Giá tiền (Gradient 3 dải màu Nebula) hoặc Chữ Miễn phí (Màu xanh nổi bật Success600) nằm góc phải
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (course.priceVnd <= 0L) {
                        Text(
                            text = "Miễn phí",
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Success600
                        )
                    } else {
                        Text(
                            text = course.priceVnd.toVnd(),
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            style = TextStyle(brush = NebulaTextBrush)
                        )
                    }
                }
            }
        }
    }
}
