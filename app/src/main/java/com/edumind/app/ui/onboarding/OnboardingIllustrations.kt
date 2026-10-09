package com.edumind.app.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.EduMindFontFamily

/**
 * Minh họa Slide 1: Xem bài giảng mọi lúc
 * Màn hình thiết bị phát video trực tuyến, nút play phát sáng, thanh tiến độ gradient và badge chương.
 */
@Composable
fun OnboardingIllustration1(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(280.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = 45.dp)
                .size(width = 240.dp, height = 160.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1E1B4B))
        )

        Box(
            modifier = Modifier
                .offset(x = 28.dp, y = 53.dp)
                .size(width = 224.dp, height = 132.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F0D2E))
        )

        Box(
            modifier = Modifier
                .offset(x = 36.dp, y = 65.dp)
                .size(width = 95.dp, height = 22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color.White.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chương 01: Video",
                fontFamily = EduMindFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF7C3AED)
            )
        }

        Box(
            modifier = Modifier
                .offset(x = (140 - 28).dp, y = (119 - 28).dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF8B5CF6).copy(alpha = 0.35f))
        )

        Box(
            modifier = Modifier
                .offset(x = (140 - 20).dp, y = (119 - 20).dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(width = 15.dp, height = 18.dp)) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path = path, color = Color(0xFF7C3AED))
            }
        }

        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = 171.dp)
                .size(width = 200.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF334155))
        )

        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = 171.dp)
                .size(width = 90.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF3B82F6),
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .offset(x = (130 - 4).dp, y = (173 - 4).dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/**
 * Minh họa Slide 2: Hỏi AI Trợ giảng
 * Robot AI trợ giảng thông minh với bong bóng hội thoại ngữ cảnh bài giảng.
 * Đổ bóng mềm mại khuếch tán không bị viền đen đậm.
 */
@Composable
fun OnboardingIllustration2(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(280.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = (140 - 38).dp, y = (95 - 38).dp)
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFF8B5CF6))
        )

        Box(
            modifier = Modifier
                .offset(x = 124.dp, y = 86.dp)
                .size(width = 10.dp, height = 5.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(Color.White)
        )

        Box(
            modifier = Modifier
                .offset(x = 146.dp, y = 86.dp)
                .size(width = 10.dp, height = 5.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(Color.White)
        )

        Canvas(
            modifier = Modifier
                .offset(x = 133.dp, y = 103.dp)
                .size(width = 14.dp, height = 7.dp)
        ) {
            val smilePath = Path().apply {
                moveTo(0f, 0f)
                quadraticBezierTo(size.width / 2f, size.height, size.width, 0f)
            }
            drawPath(
                path = smilePath,
                color = Color.White,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 30.dp, y = 146.dp)
                .size(width = 175.dp, height = 48.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = false,
                    ambientColor = Color(0x10000000),
                    spotColor = Color(0x18000000)
                )
                .background(Color(0xFF4C1D95), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Column {
                Text(
                    text = "Giải thích đoạn code ở 04:25?",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Hỏi trực tiếp từ bài giảng",
                    fontFamily = EduMindFontFamily,
                    fontSize = 9.sp,
                    color = Color(0xFFDDD6FE),
                    maxLines = 1
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(x = 75.dp, y = 200.dp)
                .size(width = 175.dp, height = 48.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = false,
                    ambientColor = Color(0x208B5CF6),
                    spotColor = Color(0x358B5CF6)
                )
                .background(Color.White, RoundedCornerShape(10.dp))
                .border(width = 1.5.dp, color = Color(0xFFEDE9FE), shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column {
                Text(
                    text = "AI Trợ giảng",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Đoạn code sử dụng hàm map()...",
                    fontFamily = EduMindFontFamily,
                    fontSize = 9.5.sp,
                    color = Color(0xFF525252),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Minh họa Slide 3: Ghi chú thông minh
 * Sổ tay ghi chú gắn timestamp bài giảng kèm thẻ trắc nghiệm Quiz pop-up.
 * Bóng đổ thanh thoát, loại bỏ hoàn toàn viền đen đậm.
 */
@Composable
fun OnboardingIllustration3(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(280.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = 40.dp)
                .size(width = 170.dp, height = 200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(width = 1.5.dp, color = Color(0xFFE5E5E5), shape = RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .size(width = 170.dp, height = 32.dp)
                    .background(Color(0xFFFAF5FF))
                    .padding(start = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Ghi chú & Câu hỏi",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF404040)
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = 12.dp, y = 45.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFF3E8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏱ 02:45",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )
                }

                Text(
                    text = "Khái niệm OOP",
                    fontFamily = EduMindFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF171717),
                    modifier = Modifier.offset(x = 54.dp, y = (-1).dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(x = 90.dp, y = 125.dp)
                .size(width = 150.dp, height = 100.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = false,
                    ambientColor = Color(0x15000000),
                    spotColor = Color(0x258B5CF6)
                )
                .background(Color.White, RoundedCornerShape(10.dp))
                .border(width = 1.5.dp, color = Color(0xFFDDD6FE), shape = RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "Quiz giữa video",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C3AED)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Từ khóa kế thừa?",
                    fontFamily = EduMindFontFamily,
                    fontSize = 9.5.sp,
                    color = Color(0xFF171717)
                )
                Spacer(modifier = Modifier.height(7.dp))
                Box(
                    modifier = Modifier
                        .size(width = 126.dp, height = 20.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "A. extends ✓",
                        fontFamily = EduMindFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF15803D)
                    )
                }
            }
        }
    }
}

/**
 * Minh họa Slide 4: Sẵn sàng học tập
 * Vầng hào quang Nebula tròn, mũ cử nhân tri thức và huy hiệu "Sẵn sàng học".
 */
@Composable
fun OnboardingIllustration4(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(280.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = (140 - 60).dp, y = (120 - 60).dp)
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF3B82F6),
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899)
                        )
                    )
                )
        )

        Canvas(
            modifier = Modifier
                .offset(x = 80.dp, y = 60.dp)
                .size(120.dp)
        ) {
            val capDiamond = Path().apply {
                moveTo(60.dp.toPx(), 35.dp.toPx())
                lineTo(90.dp.toPx(), 50.dp.toPx())
                lineTo(60.dp.toPx(), 65.dp.toPx())
                lineTo(30.dp.toPx(), 50.dp.toPx())
                close()
            }
            drawPath(path = capDiamond, color = Color.White)

            val capBody = Path().apply {
                moveTo(40.dp.toPx(), 61.dp.toPx())
                lineTo(40.dp.toPx(), 78.dp.toPx())
                cubicTo(
                    40.dp.toPx(), 86.dp.toPx(),
                    80.dp.toPx(), 86.dp.toPx(),
                    80.dp.toPx(), 78.dp.toPx()
                )
                lineTo(80.dp.toPx(), 61.dp.toPx())
            }
            drawPath(
                path = capBody,
                color = Color.White,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 85.dp, y = 195.dp)
                .size(width = 110.dp, height = 30.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(15.dp),
                    clip = false,
                    ambientColor = Color(0x258B5CF6),
                    spotColor = Color(0x358B5CF6)
                )
                .background(Color.White, RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sẵn sàng học",
                fontFamily = EduMindFontFamily,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7C3AED)
            )
        }
    }
}
