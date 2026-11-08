package com.edumind.app.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.ChatMessage
import com.edumind.app.data.model.response.ChatMessageStatus
import com.edumind.app.data.model.response.ChatRole
import com.edumind.app.data.model.response.Citation
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.EduMindTheme
import com.edumind.app.ui.theme.NebulaGradient
import com.edumind.app.ui.theme.NebulaPurple
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary200
import com.edumind.app.ui.theme.Primary400
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Success100
import com.edumind.app.ui.theme.Success600

private const val SHEET_HEIGHT_FRACTION = 0.94f
private const val TYPING_ITEM_KEY = "typing"
private const val ERROR_ITEM_KEY = "error"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAIScreen(
    state: ChatUiState,
    currentPositionMs: Long = 0L,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onRetry: () -> Unit,
    onCitationClick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = Color.Transparent,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = null
    ) {
        ChatAISheetContent(
            state = state,
            currentPositionMs = currentPositionMs,
            onInputChange = onInputChange,
            onSend = onSend,
            onRetry = onRetry,
            onCitationClick = onCitationClick,
            onClose = onDismiss,
            modifier = Modifier
                .fillMaxHeight(SHEET_HEIGHT_FRACTION)
                .fillMaxWidth()
                .imePadding()
        )
    }
}

@Composable
fun ChatAISheetContent(
    state: ChatUiState,
    currentPositionMs: Long = 0L,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onRetry: () -> Unit,
    onCitationClick: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .border(1.dp, Primary200.copy(alpha = 0.8f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            ChatHeader()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                if (state.messages.isEmpty()) {
                    ChatEmptyState(onSuggestionClick = onInputChange)
                } else {
                    ChatMessageList(
                        state = state,
                        onRetry = onRetry,
                        onCitationClick = onCitationClick
                    )
                }
            }

            ChatInputBar(
                input = state.input,
                canSend = state.canSend,
                currentPositionMs = currentPositionMs,
                onInputChange = onInputChange,
                onSend = onSend
            )
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(elevation = 12.dp, shape = CircleShape, spotColor = NebulaPurple)
                .clip(CircleShape)
                .background(NebulaGradient)
                .border(4.dp, Color.White, CircleShape)
                .clickable(
                    onClickLabel = "Đóng modal quay về bài giảng",
                    role = Role.Button,
                    onClick = onClose
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Đóng modal quay về bài giảng",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ChatHeader() {
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "EduAI Trợ giảng",
                    fontFamily = EduMindFontFamily,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral950
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Success600)
                        .border(2.dp, Success100, CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary50)
                    .border(1.dp, Primary200, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "50/50 câu hôm nay",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary700
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Neutral200)
        )
    }
}

@Composable
private fun ChatEmptyState(onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Chào bạn, Nguyễn Văn A! 👋",
            fontFamily = EduMindFontFamily,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral900
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Mình là EduAI Trợ giảng nắm vững bài giảng của ThS. Nguyễn Hoàng Nam. Hãy đặt câu hỏi theo ngữ cảnh bài học!",
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Neutral500,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            ) {
                Text(
                    text = "GỢI Ý CÂU HỎI NHANH:",
                    fontFamily = EduMindFontFamily,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary700
                )
            }

            SuggestionCard(
                icon = Icons.Default.Lightbulb,
                text = "Tại sao Python không cần khai báo int, float?",
                onClick = { onSuggestionClick("Tại sao Python không cần khai báo int, float?") }
            )

            SuggestionCard(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                text = "Tóm tắt 3 quy tắc đặt tên biến PEP 8",
                onClick = { onSuggestionClick("Tóm tắt 3 quy tắc đặt tên biến PEP 8") }
            )

            SuggestionCard(
                icon = Icons.Default.Code,
                text = "Cho ví dụ về ép kiểu type casting",
                onClick = { onSuggestionClick("Cho ví dụ về ép kiểu type casting") }
            )
        }
    }
}

@Composable
private fun SuggestionCard(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Primary50)
            .border(1.dp, Primary200, RoundedCornerShape(12.dp))
            .clickable(
                onClickLabel = text,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = text,
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Neutral900
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Primary400,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatMessageList(
    state: ChatUiState,
    onRetry: () -> Unit,
    onCitationClick: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    val isImeVisible = WindowInsets.isImeVisible
    val itemCount = state.messages.size +
        (if (state.isTyping) 1 else 0) +
        (if (state.error != null) 1 else 0)

    LaunchedEffect(itemCount, isImeVisible) {
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = state.messages, key = { it.id }) { message ->
            ChatBubble(message = message, onCitationClick = onCitationClick)
        }
        if (state.isTyping) {
            item(key = TYPING_ITEM_KEY) {
                TypingIndicator()
            }
        }
        state.error?.let { errorMessage ->
            item(key = ERROR_ITEM_KEY) {
                ChatErrorBubble(message = errorMessage, onRetry = onRetry)
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    input: String,
    canSend: Boolean,
    currentPositionMs: Long,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val totalSeconds = (currentPositionMs.coerceAtLeast(0L) / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Neutral200)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Neutral100)
                    .border(1.dp, Neutral200, RoundedCornerShape(24.dp))
                    .clickable { keyboardController?.show() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Primary700,
                    modifier = Modifier.size(18.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { keyboardController?.show() }
                ) {
                    if (input.isEmpty()) {
                        Text(
                            text = "Hỏi EduAI về bài học tại $formattedTime...",
                            fontFamily = EduMindFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Neutral400
                        )
                    }
                    BasicTextField(
                        value = input,
                        onValueChange = onInputChange,
                        textStyle = TextStyle(
                            fontFamily = EduMindFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Neutral900
                        ),
                        cursorBrush = SolidColor(Primary700),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (canSend) {
                                    onSend()
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (canSend) NebulaGradient else SolidColor(Neutral200))
                        .clickable(
                            enabled = canSend,
                            onClickLabel = "Gửi câu hỏi",
                            role = Role.Button,
                            onClick = {
                                onSend()
                                keyboardController?.hide()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi câu hỏi",
                        tint = if (canSend) Color.White else Neutral400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "M-13 AI Chat - Trạng thái hội thoại", showBackground = true, heightDp = 680)
@Composable
private fun ChatAIScreenPreview() {
    EduMindTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            ChatAISheetContent(
                state = ChatUiState(
                    messages = listOf(
                        ChatMessage(
                            id = "1",
                            role = ChatRole.USER,
                            text = "Tại sao trong Python mình gán x = 10 thì x là số nguyên, mà không cần khai báo từ khóa như JavaScript vậy AI?",
                            status = ChatMessageStatus.SENT
                        ),
                        ChatMessage(
                            id = "2",
                            role = ChatRole.AI,
                            text = "Chào bạn! Đó chính là nguyên lý Dynamic Typing (Định kiểu động) trong Python: trình thông dịch tự suy luận kiểu dữ liệu tại thời điểm runtime dựa vào giá trị gán.",
                            citations = listOf(
                                Citation(timestampMs = 135_000L, label = "02:15 – 02:48")
                            ),
                            status = ChatMessageStatus.SENT
                        ),
                        ChatMessage(
                            id = "3",
                            role = ChatRole.USER,
                            text = "Giải thích thêm về ép kiểu float sang int giúp tôi với!",
                            status = ChatMessageStatus.ERROR
                        )
                    ),
                    input = "Toán tử chia lấy dư trong Python dùng ký hiệu nào?",
                    isTyping = true,
                    error = "AI đang gặp sự cố"
                ),
                onInputChange = {},
                onSend = {},
                onRetry = {},
                onCitationClick = {},
                onClose = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "M-13 AI Chat - Trạng thái rỗng (Chuẩn Prototype)", showBackground = true, heightDp = 680)
@Composable
private fun ChatAIEmptyPreview() {
    EduMindTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            ChatAISheetContent(
                state = ChatUiState(),
                onInputChange = {},
                onSend = {},
                onRetry = {},
                onCitationClick = {},
                onClose = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
