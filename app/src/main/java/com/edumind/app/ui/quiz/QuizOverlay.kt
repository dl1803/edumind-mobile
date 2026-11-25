package com.edumind.app.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.QuizAnswerResultDto
import com.edumind.app.data.model.response.QuizDto
import com.edumind.app.data.model.response.QuizOptionDto
import com.edumind.app.data.model.response.QuizQuestionDto
import com.edumind.app.data.model.response.QuizQuestionType
import com.edumind.app.ui.common.EduMindButton
import com.edumind.app.ui.theme.BgOverlay
import com.edumind.app.ui.theme.Error100
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.Error700
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral800
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary100
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700
import com.edumind.app.ui.theme.Primary900
import com.edumind.app.ui.theme.Success100
import com.edumind.app.ui.theme.Success700
import com.edumind.app.ui.theme.Warning100
import com.edumind.app.ui.theme.Warning700
import kotlinx.coroutines.launch

@Composable
fun QuizOverlay(
    state: QuizUiState,
    onSelectAnswer: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    onErrorShown: () -> Unit,
    onRewindTheory: (() -> Unit)? = null,
    staticConfettiProgress: Float? = null,
    modifier: Modifier = Modifier
) {
    if (state == QuizUiState.Hidden) return

    BackHandler(onBack = onBack)

    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = (state as? QuizUiState.Question)?.errorMessage

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            val result = snackbarHostState.showSnackbar(
                message = errorMessage,
                actionLabel = "Thử lại",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                onSubmit()
            } else {
                onErrorShown()
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BgOverlay)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        when (state) {
            QuizUiState.Hidden -> Unit
            QuizUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
            is QuizUiState.Active -> QuizCard(
                state = state,
                maxHeight = maxHeight,
                onSelectAnswer = onSelectAnswer,
                onSubmit = onSubmit,
                onNext = onNext,
                onFinish = onFinish,
                onRewindTheory = onRewindTheory ?: onBack
            )
        }

        val feedback = state as? QuizUiState.Feedback
        if (feedback != null && feedback.result.correct) {
            key(state) {
                CelebrationConfettiEffect(
                    staticProgress = staticConfettiProgress,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(8.dp),
                containerColor = Neutral800,
                contentColor = Color.White,
                actionColor = Primary100
            )
        }
    }
}

@Composable
private fun QuizCard(
    state: QuizUiState.Active,
    maxHeight: Dp,
    onSelectAnswer: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    onRewindTheory: () -> Unit
) {
    val isInspection = LocalInspectionMode.current
    val appearState = remember(state.quiz.id) { MutableTransitionState(isInspection).apply { targetState = true } }
    val redFlashAlpha = remember {
        Animatable(if (isInspection && state is QuizUiState.Feedback && !state.result.correct) 0.12f else 0f)
    }

    LaunchedEffect(state) {
        if (state is QuizUiState.Feedback && !state.result.correct) {
            redFlashAlpha.snapTo(0.18f)
            redFlashAlpha.animateTo(0f, animationSpec = tween(650))
        }
    }

    AnimatedVisibility(
        visibleState = appearState,
        enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300))
    ) {
        val cardShape = RoundedCornerShape(28.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .heightIn(max = maxHeight)
                .shadow(elevation = 20.dp, shape = cardShape)
                .clip(cardShape)
                .background(Color.White)
                .border(
                    width = if (redFlashAlpha.value > 0.01f) 1.5.dp else 1.dp,
                    color = if (redFlashAlpha.value > 0.01f) Error600.copy(alpha = redFlashAlpha.value * 3f) else Color.White.copy(alpha = 0.6f),
                    shape = cardShape
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                QuizHeader(state = state)
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = Neutral100)
                Spacer(modifier = Modifier.height(12.dp))

                key(state.question.id) {
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = state.question.content,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Neutral900,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.question.options.forEach { option ->
                                QuizOptionItem(
                                    optionKey = option.key,
                                    content = option.content,
                                    state = optionStateFor(state, option.key),
                                    enabled = state is QuizUiState.Question,
                                    onClick = { onSelectAnswer(option.key) }
                                )
                            }
                        }

                        val feedbackResult = (state as? QuizUiState.Feedback)?.result
                        if (feedbackResult != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            AnimatedVisibility(
                                visible = true,
                                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                                exit = ExitTransition.None
                            ) {
                                if (feedbackResult.correct) {
                                    QuizExplanationCorrect(
                                        explanation = feedbackResult.explanation ?: "Bạn đã chọn câu trả lời hoàn toàn chính xác."
                                    )
                                } else {
                                    QuizExplanationRemedial(
                                        explanation = feedbackResult.explanation ?: "Hãy nghe lại đoạn bài giảng tại mốc thời gian trước đó để nắm vững kiến thức nhé."
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 1.dp, color = Neutral100)
                Spacer(modifier = Modifier.height(12.dp))

                QuizActionButtons(
                    state = state,
                    onSubmit = onSubmit,
                    onNext = onNext,
                    onFinish = onFinish,
                    onRewindTheory = onRewindTheory
                )
            }

            if (redFlashAlpha.value > 0.001f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Error600.copy(alpha = redFlashAlpha.value))
                )
            }
        }
    }
}

@Composable
private fun QuizHeader(state: QuizUiState.Active) {
    val total = state.quiz.questions.size
    val currentIdx = state.index + 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NebulaBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = "Kiểm tra kiến thức nhanh",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900
                )
                Text(
                    text = if (total > 1) "Câu $currentIdx/$total - Mốc 00:30" else "Mốc 00:30",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Neutral500
                )
            }
        }

        if (state is QuizUiState.Feedback) {
            val (badgeText, badgeBg, badgeTextColor) = if (state.result.correct) {
                Triple("Chính xác", Success100, Success700)
            } else {
                Triple("Chưa chính xác", Error100, Error700)
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor
                )
            }
        }
    }
}

@Composable
private fun QuizExplanationCorrect(explanation: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Primary50)
            .border(
                width = 1.dp,
                color = Primary100,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Lightbulb,
                contentDescription = null,
                tint = Primary600,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Giải thích chi tiết",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Primary900
            )
        }
        Text(
            text = explanation,
            fontSize = 11.sp,
            color = Neutral700,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun QuizExplanationRemedial(explanation: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Warning100)
            .border(
                width = 1.dp,
                color = Warning700.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Gợi ý ôn tập: ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Warning700
            )
        }
        Text(
            text = explanation,
            fontSize = 11.sp,
            color = Warning700,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun QuizActionButtons(
    state: QuizUiState.Active,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    onRewindTheory: () -> Unit
) {
    when (state) {
        is QuizUiState.Question -> {
            if (state.selectedKey == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Neutral200),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chọn đáp án để tiếp tục",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral400
                    )
                }
            } else {
                EduMindButton(
                    text = "Gửi câu trả lời",
                    onClick = onSubmit,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        is QuizUiState.Submitting -> {
            EduMindButton(
                text = "Gửi câu trả lời",
                onClick = {},
                loading = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        is QuizUiState.Feedback -> {
            if (state.result.correct) {
                EduMindButton(
                    text = if (state.isLast) "Tiếp tục xem video" else "Câu tiếp theo",
                    onClick = if (state.isLast) onFinish else onNext,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Primary100)
                            .clickable(onClick = onRewindTheory),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Replay,
                                contentDescription = null,
                                tint = Primary700,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Xem lại lý thuyết",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary700
                            )
                        }
                    }

                    EduMindButton(
                        text = if (state.isLast) "Bỏ qua & Tiếp tục xem" else "Câu tiếp theo",
                        onClick = if (state.isLast) onFinish else onNext,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private data class ConfettiParticle(
    val angleRad: Float,
    val speed: Float,
    val startXOffset: Float,
    val widthPx: Float,
    val heightPx: Float,
    val isCircle: Boolean,
    val color: Color,
    val flipSpeedX: Float,
    val flipSpeedY: Float,
    val spinSpeedZ: Float,
    val initialRotationZ: Float,
    val swaySpeed: Float,
    val swayAmpPx: Float,
    val cornerRadiusPx: Float
)

@Composable
private fun CelebrationConfettiEffect(
    staticProgress: Float? = null,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (staticProgress == null) {
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1600, easing = LinearEasing)
            )
        }
    }

    val density = LocalDensity.current

    val particles = remember(density) {
        val colors = listOf(
            Color(0xFF8B5CF6), // Royal Purple
            Color(0xFF10B981), // Emerald
            Color(0xFFEC4899), // Neon Pink
            Color(0xFFF59E0B), // Amber Gold
            Color(0xFF3B82F6), // Blue
            Color(0xFF06B6D4), // Cyan
            Color(0xFFFEF08A), // Soft Yellow
            Color(0xFF6366F1), // Indigo
            Color(0xFFFB7185), // Rose
            Color(0xFF14B8A6)  // Teal
        )
        val rand = kotlin.random.Random(1337)
        with(density) {
            List(100) { i ->
                val angleDeg = 270f + (rand.nextFloat() - 0.5f) * 108f
                val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
                val speed = 0.85f + rand.nextFloat() * 0.38f
                val startXOffset = (rand.nextFloat() - 0.5f) * 0.14f
                val widthPx = (7f + rand.nextFloat() * 9f).dp.toPx()
                val heightPx = (4f + rand.nextFloat() * 6f).dp.toPx()
                val isCircle = (i % 6 == 0)
                val color = colors[i % colors.size]
                val flipSpeedX = 2.6f + rand.nextFloat() * 3.0f
                val flipSpeedY = 2.0f + rand.nextFloat() * 2.4f
                val spinSpeedZ = (rand.nextFloat() - 0.5f) * 140f
                val initialRotationZ = rand.nextFloat() * 360f
                val swaySpeed = 1.4f + rand.nextFloat() * 1.2f
                val swayAmpPx = (10f + rand.nextFloat() * 16f).dp.toPx()
                val cornerRadiusPx = 1.5.dp.toPx()

                ConfettiParticle(
                    angleRad = angleRad,
                    speed = speed,
                    startXOffset = startXOffset,
                    widthPx = widthPx,
                    heightPx = heightPx,
                    isCircle = isCircle,
                    color = color,
                    flipSpeedX = flipSpeedX,
                    flipSpeedY = flipSpeedY,
                    spinSpeedZ = spinSpeedZ,
                    initialRotationZ = initialRotationZ,
                    swaySpeed = swaySpeed,
                    swayAmpPx = swayAmpPx,
                    cornerRadiusPx = cornerRadiusPx
                )
            }
        }
    }

    Canvas(modifier = modifier) {
        val t = staticProgress ?: animProgress.value
        if (staticProgress == null && (t <= 0.001f || t >= 1f)) return@Canvas

        val w = size.width
        val h = size.height
        val originX = w * 0.5f
        val originY = h * 0.62f
        val maxLaunchHeight = h * 0.72f
        val maxHorizontalTravel = w * 0.66f

        val alpha = if (t > 0.82f) ((1f - t) / 0.18f).coerceIn(0f, 1f) else 1f
        if (alpha <= 0.005f) return@Canvas

        particles.forEach { p ->
            val pOriginX = originX + p.startXOffset * w
            val sway = kotlin.math.sin((p.swaySpeed * t * 2.0 * Math.PI + p.initialRotationZ).toDouble()).toFloat() * p.swayAmpPx * (t * (1.2f - 0.2f * t))
            val curX = pOriginX + kotlin.math.cos(p.angleRad) * p.speed * maxHorizontalTravel * (1f - kotlin.math.exp(-2.5f * t)) + sway
            val upwardTravel = -kotlin.math.sin(p.angleRad) * p.speed * maxLaunchHeight * (1f - kotlin.math.exp(-3.5f * t))
            val gravityDrop = h * 0.86f * (t * t * (0.28f + 0.72f * t))
            val curY = originY - upwardTravel + gravityDrop

            val cosX = kotlin.math.cos((p.flipSpeedX * t * 2 * Math.PI).toDouble()).toFloat()
            val cosY = kotlin.math.cos((p.flipSpeedY * t * 2 * Math.PI).toDouble()).toFloat()
            val scaleW = p.widthPx * (0.12f + 0.88f * kotlin.math.abs(cosX))
            val scaleH = p.heightPx * (0.12f + 0.88f * kotlin.math.abs(cosY))
            val rotationZ = p.initialRotationZ + p.spinSpeedZ * t

            rotate(degrees = rotationZ, pivot = Offset(curX, curY)) {
                if (p.isCircle) {
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = scaleW * 0.5f,
                        center = Offset(curX, curY)
                    )
                } else {
                    drawRoundRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(curX - scaleW * 0.5f, curY - scaleH * 0.5f),
                        size = Size(scaleW, scaleH),
                        cornerRadius = CornerRadius(p.cornerRadiusPx, p.cornerRadiusPx)
                    )
                }
            }
        }
    }
}

private fun optionStateFor(state: QuizUiState.Active, key: String): QuizOptionState = when (state) {
    is QuizUiState.Question ->
        if (state.selectedKey == key) QuizOptionState.Selected else QuizOptionState.Default
    is QuizUiState.Submitting ->
        if (state.selectedKey == key) QuizOptionState.Selected else QuizOptionState.Default
    is QuizUiState.Feedback -> when {
        key == state.result.correctAnswer -> QuizOptionState.Correct
        key == state.selectedKey -> QuizOptionState.Wrong
        else -> QuizOptionState.Default
    }
}

@Preview(name = "1. Interactive Playground (Test Click & Effects)", showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun QuizInteractivePlaygroundPreview() {
    val sampleQuiz = QuizDto(
        id = "quiz-interactive",
        questions = listOf(
            QuizQuestionDto(
                id = "q-interactive",
                type = QuizQuestionType.SINGLE_CHOICE,
                content = "Đoạn mã Python sau sẽ in ra giá trị gì tại dòng cuối cùng?",
                options = listOf(
                    QuizOptionDto("A", "3"),
                    QuizOptionDto("B", "4"),
                    QuizOptionDto("C", "Lỗi IndexError"),
                    QuizOptionDto("D", "Lỗi TypeError")
                ),
                order = 1
            )
        ),
        totalQuestions = 1
    )

    var state by remember {
        mutableStateOf<QuizUiState>(
            QuizUiState.Question(
                quiz = sampleQuiz,
                index = 0,
                selectedKey = null
            )
        )
    }

    QuizOverlay(
        state = state,
        onSelectAnswer = { key ->
            state = (state as? QuizUiState.Question)?.copy(selectedKey = key) ?: state
        },
        onSubmit = {
            val q = state as? QuizUiState.Question
            val isCorrect = q?.selectedKey == "B"
            state = QuizUiState.Feedback(
                quiz = sampleQuiz,
                index = 0,
                selectedKey = q?.selectedKey ?: "A",
                result = QuizAnswerResultDto(
                    correct = isCorrect,
                    correctAnswer = "B",
                    explanation = if (isCorrect) {
                        "Trong Python, List là kiểu dữ liệu Mutable. Phép gán values = numbers tạo ra một tham chiếu cùng trỏ về một ô nhớ. Do đó lệnh append trên values cũng thay đổi trực tiếp numbers."
                    } else {
                        "Bạn có thể đang nhầm lẫn rằng phép gán biến tạo ra một vùng nhớ mới độc lập. Hãy nghe lại đoạn giảng về tham chiếu List tại mốc 03:20."
                    }
                )
            )
        },
        onNext = {
            state = QuizUiState.Question(quiz = sampleQuiz, index = 0, selectedKey = null)
        },
        onFinish = {
            state = QuizUiState.Question(quiz = sampleQuiz, index = 0, selectedKey = null)
        },
        onBack = {
            state = QuizUiState.Question(quiz = sampleQuiz, index = 0, selectedKey = null)
        },
        onErrorShown = {},
        onRewindTheory = {
            state = QuizUiState.Question(quiz = sampleQuiz, index = 0, selectedKey = null)
        }
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun QuizOverlayQuestionPreview() {
    val sampleQuiz = QuizDto(
        id = "quiz-1",
        questions = listOf(
            QuizQuestionDto(
                id = "q-1",
                type = QuizQuestionType.SINGLE_CHOICE,
                content = "Đoạn mã Python sau sẽ in ra giá trị gì tại dòng cuối cùng?",
                options = listOf(
                    QuizOptionDto("A", "3"),
                    QuizOptionDto("B", "4"),
                    QuizOptionDto("C", "Lỗi IndexError"),
                    QuizOptionDto("D", "Lỗi TypeError")
                ),
                order = 1
            ),
            QuizQuestionDto(id = "q-2", order = 2)
        ),
        totalQuestions = 2
    )

    QuizOverlay(
        state = QuizUiState.Question(
            quiz = sampleQuiz,
            index = 0,
            selectedKey = null
        ),
        onSelectAnswer = {},
        onSubmit = {},
        onNext = {},
        onFinish = {},
        onBack = {},
        onErrorShown = {}
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun QuizOverlaySelectedPreview() {
    val sampleQuiz = QuizDto(
        id = "quiz-1",
        questions = listOf(
            QuizQuestionDto(
                id = "q-1",
                type = QuizQuestionType.SINGLE_CHOICE,
                content = "Đoạn mã Python sau sẽ in ra giá trị gì tại dòng cuối cùng?",
                options = listOf(
                    QuizOptionDto("A", "3"),
                    QuizOptionDto("B", "4"),
                    QuizOptionDto("C", "Lỗi IndexError"),
                    QuizOptionDto("D", "Lỗi TypeError")
                ),
                order = 1
            ),
            QuizQuestionDto(id = "q-2", order = 2)
        ),
        totalQuestions = 2
    )

    QuizOverlay(
        state = QuizUiState.Question(
            quiz = sampleQuiz,
            index = 0,
            selectedKey = "B"
        ),
        onSelectAnswer = {},
        onSubmit = {},
        onNext = {},
        onFinish = {},
        onBack = {},
        onErrorShown = {}
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun QuizOverlayFeedbackCorrectPreview() {
    val sampleQuiz = QuizDto(
        id = "quiz-1",
        questions = listOf(
            QuizQuestionDto(
                id = "q-1",
                type = QuizQuestionType.SINGLE_CHOICE,
                content = "Đoạn mã Python sau sẽ in ra giá trị gì tại dòng cuối cùng?",
                options = listOf(
                    QuizOptionDto("A", "3"),
                    QuizOptionDto("B", "4"),
                    QuizOptionDto("C", "Lỗi IndexError"),
                    QuizOptionDto("D", "Lỗi TypeError")
                ),
                order = 1
            )
        ),
        totalQuestions = 1
    )

    QuizOverlay(
        state = QuizUiState.Feedback(
            quiz = sampleQuiz,
            index = 0,
            selectedKey = "B",
            result = QuizAnswerResultDto(
                correct = true,
                correctAnswer = "B",
                explanation = "Trong Python, List là kiểu dữ liệu Mutable. Phép gán values = numbers tạo ra một tham chiếu cùng trỏ về một ô nhớ. Do đó lệnh append trên values cũng thay đổi trực tiếp numbers."
            )
        ),
        onSelectAnswer = {},
        onSubmit = {},
        onNext = {},
        onFinish = {},
        onBack = {},
        onErrorShown = {},
        staticConfettiProgress = 0.38f
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun QuizOverlayFeedbackWrongPreview() {
    val sampleQuiz = QuizDto(
        id = "quiz-1",
        questions = listOf(
            QuizQuestionDto(
                id = "q-1",
                type = QuizQuestionType.SINGLE_CHOICE,
                content = "Đoạn mã Python sau sẽ in ra giá trị gì tại dòng cuối cùng?",
                options = listOf(
                    QuizOptionDto("A", "3"),
                    QuizOptionDto("B", "4"),
                    QuizOptionDto("C", "Lỗi IndexError"),
                    QuizOptionDto("D", "Lỗi TypeError")
                ),
                order = 1
            )
        ),
        totalQuestions = 1
    )

    QuizOverlay(
        state = QuizUiState.Feedback(
            quiz = sampleQuiz,
            index = 0,
            selectedKey = "A",
            result = QuizAnswerResultDto(
                correct = false,
                correctAnswer = "B",
                explanation = "Bạn có thể đang nhầm lẫn rằng phép gán biến tạo ra một vùng nhớ mới độc lập. Hãy nghe lại đoạn giảng về tham chiếu List tại mốc 03:20."
            )
        ),
        onSelectAnswer = {},
        onSubmit = {},
        onNext = {},
        onFinish = {},
        onBack = {},
        onErrorShown = {}
    )
}
