package com.edumind.app.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.ChatMessage
import com.edumind.app.data.model.response.ChatRole
import com.edumind.app.data.model.response.Citation
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Error100
import com.edumind.app.ui.theme.Error300
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.NebulaGradient
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary200
import com.edumind.app.ui.theme.Primary300
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary700

private const val USER_BUBBLE_WIDTH_FRACTION = 0.85f
private const val AI_BUBBLE_WIDTH_FRACTION = 0.94f

private val UserBubbleShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 2.dp,
    bottomEnd = 16.dp,
    bottomStart = 16.dp
)

internal val AiBubbleShape = RoundedCornerShape(
    topStart = 2.dp,
    topEnd = 16.dp,
    bottomEnd = 16.dp,
    bottomStart = 16.dp
)

@Composable
fun ChatBubble(
    message: ChatMessage,
    onCitationClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (message.role) {
        ChatRole.USER -> UserBubble(message = message, modifier = modifier)
        ChatRole.AI -> AiBubble(message = message, onCitationClick = onCitationClick, modifier = modifier)
    }
}

@Composable
private fun UserBubble(message: ChatMessage, modifier: Modifier) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier.widthIn(max = maxWidth * USER_BUBBLE_WIDTH_FRACTION),
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .clip(UserBubbleShape)
                    .background(Primary100.copy(alpha = 0.95f))
                    .border(1.dp, Primary300, UserBubbleShape)
                    .drawBehind {
                        drawRect(
                            color = Primary700,
                            topLeft = Offset(size.width - 3.dp.toPx(), 0f),
                            size = Size(3.dp.toPx(), size.height)
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .clearAndSetSemantics { contentDescription = "Bạn nói: ${message.text}" }
            ) {
                Text(
                    text = message.text,
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Neutral900
                )
            }
            Text(
                text = "Vừa xong • Bạn",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Neutral400,
                modifier = Modifier.padding(top = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
private fun AiBubble(
    message: ChatMessage,
    onCitationClick: (Long) -> Unit,
    modifier: Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.widthIn(max = maxWidth * AI_BUBBLE_WIDTH_FRACTION),
            horizontalAlignment = Alignment.Start
        ) {
            Column(
                modifier = Modifier
                    .clip(AiBubbleShape)
                    .background(Color.White)
                    .border(2.dp, NebulaGradient, AiBubbleShape)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = Primary700,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "EduAI Trợ giảng",
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Neutral900
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Primary50)
                            .border(1.dp, Primary200, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Trích xuất từ bài giảng",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary700
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = Neutral100,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                )

                Text(
                    text = message.text,
                    fontFamily = EduMindFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Normal,
                    color = Neutral900,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "AI Trợ giảng trả lời: ${message.text}"
                    }
                )

                message.citations.forEach { citation ->
                    CitationCard(
                        citation = citation,
                        onClick = { onCitationClick(citation.timestampMs) }
                    )
                }
            }

            Text(
                text = "Vừa xong",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Neutral400,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun CitationCard(
    citation: Citation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Primary50)
            .border(1.dp, Primary200, RoundedCornerShape(12.dp))
            .drawBehind {
                drawRect(
                    color = Primary700,
                    topLeft = Offset.Zero,
                    size = Size(4.dp.toPx(), size.height)
                )
            }
            .clickable(
                onClickLabel = "Xem tại mốc ${citation.label}",
                role = Role.Button,
                onClick = onClick
            )
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Primary700),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
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
                        text = "📎 Mốc ${citation.label}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Tua video",
                            fontFamily = EduMindFontFamily,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary700
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Primary700,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                Text(
                    text = "Nhấn để phát bài giảng ngay tại đoạn giải thích này",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = Neutral600
                )
            }
        }
    }
}

@Composable
fun ChatErrorBubble(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.widthIn(max = maxWidth * 0.75f),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(AiBubbleShape)
                    .background(Error100)
                    .border(1.dp, Error300, AiBubbleShape)
                    .drawBehind {
                        drawRect(
                            color = Error600,
                            topLeft = Offset.Zero,
                            size = Size(3.dp.toPx(), size.height)
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Error600,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = message,
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Neutral700
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = 4.dp, start = 4.dp)
                    .clickable(
                        onClickLabel = "Gửi lại câu hỏi",
                        role = Role.Button,
                        onClick = onRetry
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Thử lại",
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary700
                )
            }
        }
    }
}
