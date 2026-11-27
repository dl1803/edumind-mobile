package com.edumind.app.ui.exercise

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.data.model.response.ExerciseDto
import com.edumind.app.data.model.response.ExerciseQuestionDto
import com.edumind.app.data.model.response.ExerciseQuestionType
import com.edumind.app.data.model.response.QuizOptionDto
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.common.LoadingOverlay
import com.edumind.app.ui.common.SkeletonBox
import com.edumind.app.ui.common.SkeletonLines
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Error600
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral50
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral800
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary300
import com.edumind.app.ui.theme.Primary700

private val ExerciseSnackbarBottomOffset = 88.dp

@Composable
fun ExerciseScreen(
    onBackClick: () -> Unit,
    onViewResult: (String) -> Unit = {},
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()

    ExerciseScreenContent(
        state = state,
        onBackClick = onBackClick,
        onRetry = viewModel::retry,
        onStart = viewModel::start,
        onSelectSingle = viewModel::selectSingle,
        onToggleMultiple = viewModel::toggleMultiple,
        onEssayChange = viewModel::updateEssay,
        onPrevious = viewModel::previous,
        onNext = viewModel::next,
        onGoTo = viewModel::goTo,
        remainingSeconds = remainingSeconds,
        onViewResult = onViewResult,
        onSubmit = { viewModel.submit() },
        onRetrySubmit = viewModel::retrySubmit,
        onSubmitErrorShown = viewModel::onSubmitErrorShown
    )
}

@Composable
fun ExerciseScreenContent(
    state: ExerciseUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onStart: () -> Unit,
    onSelectSingle: (String, String) -> Unit,
    onToggleMultiple: (String, String) -> Unit,
    onEssayChange: (String, String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onGoTo: (Int) -> Unit,
    remainingSeconds: Int? = null,
    onViewResult: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onRetrySubmit: () -> Unit = {},
    onSubmitErrorShown: () -> Unit = {}
) {
    var isLeaveDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isSubmitConfirmDialogVisible by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val inProgressState = state as? ExerciseUiState.InProgress
    val isInProgress = inProgressState != null
    val isLocked = inProgressState?.isLocked == true
    val isBackLocked = inProgressState?.isBackLocked == true
    val submitPhase = inProgressState?.submitPhase
    val submitError = inProgressState?.submitError

    BackHandler(enabled = isInProgress) {
        if (!isBackLocked) isLeaveDialogVisible = true
    }

    LaunchedEffect(isLocked) {
        if (isLocked) isSubmitConfirmDialogVisible = false
    }

    LaunchedEffect(isBackLocked) {
        if (isBackLocked) isLeaveDialogVisible = false
    }

    LaunchedEffect(submitPhase) {
        if (submitPhase is ExerciseSubmitPhase.Submitted && !submitPhase.isAuto) {
            onViewResult(submitPhase.exerciseId)
        }
    }

    LaunchedEffect(submitError) {
        if (submitError != null) {
            val result = snackbarHostState.showSnackbar(
                message = submitError.message,
                actionLabel = "Thử lại",
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                onRetrySubmit()
            } else {
                onSubmitErrorShown()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Neutral50)
        ) {
            ExerciseTopBar(
                title = inProgressState?.exercise?.headerTitle(),
                subtitle = inProgressState?.exercise?.summaryText(),
                showDivider = isInProgress,
                containerColor = if (isInProgress) Color.White else Neutral50,
                remainingSeconds = if (isInProgress) remainingSeconds else null,
                onBackClick = if (isInProgress) {
                    { if (!isBackLocked) isLeaveDialogVisible = true }
                } else {
                    onBackClick
                }
            )

            when (state) {
                ExerciseUiState.Loading -> ExerciseLoadingContent(modifier = Modifier.weight(1f))

                is ExerciseUiState.Error -> ErrorView(
                    message = state.message,
                    onRetry = onRetry,
                    onClose = onBackClick,
                    modifier = Modifier.weight(1f)
                )

                is ExerciseUiState.Intro -> ExerciseIntroContent(
                    exercise = state.exercise,
                    isOutOfAttempts = state.isOutOfAttempts,
                    onStart = onStart,
                    modifier = Modifier.weight(1f)
                )

                is ExerciseUiState.InProgress -> if (state.submitPhase is ExerciseSubmitPhase.Submitted && !state.submitPhase.isAuto) {
                    ExerciseLoadingContent(modifier = Modifier.weight(1f))
                } else {
                    ExerciseQuestionContent(
                        state = state,
                        onSelectSingle = onSelectSingle,
                        onToggleMultiple = onToggleMultiple,
                        onEssayChange = onEssayChange,
                        onPrevious = onPrevious,
                        onNext = onNext,
                        onGoTo = onGoTo,
                        onSubmitClick = { isSubmitConfirmDialogVisible = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        LoadingOverlay(
            visible = submitPhase is ExerciseSubmitPhase.Submitting,
            message = "Đang nộp bài..."
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = ExerciseSnackbarBottomOffset)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(8.dp),
                containerColor = Neutral800,
                contentColor = Color.White,
                actionColor = Primary300
            )
        }
    }

    if (isLeaveDialogVisible && isInProgress) {
        ExerciseLeaveConfirmDialog(
            onConfirmLeave = {
                isLeaveDialogVisible = false
                onBackClick()
            },
            onDismiss = { isLeaveDialogVisible = false }
        )
    }

    if (isSubmitConfirmDialogVisible && inProgressState != null) {
        val totalQuestions = inProgressState.questions.size
        val answeredCount = inProgressState.answeredCount
        val unansweredCount = totalQuestions - answeredCount
        val unansweredListText = inProgressState.questions
            .filter { q -> !inProgressState.isAnswered(q) }
            .take(4)
            .joinToString(", ") { "Câu " + (inProgressState.questions.indexOf(it) + 1).toString().padStart(2, '0') }

        ExerciseSubmitConfirmDialog(
            unansweredCount = unansweredCount,
            unansweredListText = unansweredListText,
            onConfirm = {
                isSubmitConfirmDialogVisible = false
                onSubmit()
            },
            onDismiss = { isSubmitConfirmDialogVisible = false }
        )
    }

    if (submitPhase is ExerciseSubmitPhase.Submitted && submitPhase.isAuto) {
        var isNavigating by remember(submitPhase) { mutableStateOf(false) }
        ExerciseTimeOutDialog(
            onViewResult = {
                if (!isNavigating) {
                    isNavigating = true
                    onViewResult(submitPhase.exerciseId)
                }
            }
        )
    }
}

@Composable
private fun ExerciseTopBar(
    title: String?,
    subtitle: String?,
    showDivider: Boolean,
    containerColor: Color,
    remainingSeconds: Int?,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(containerColor)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Neutral700,
                    modifier = Modifier.size(22.dp)
                )
            }
            if (!title.isNullOrBlank()) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = EduMindFontFamily,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Neutral950,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Neutral500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (remainingSeconds != null) {
                ExerciseTimerBadge(remainingSeconds = remainingSeconds)
            }
        }
        if (showDivider) {
            HorizontalDivider(color = Neutral100)
        }
    }
}

@Composable
private fun ExerciseLoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(8.dp))
        SkeletonLines(lines = 4)
    }
}

internal fun formatTimeLimit(timeLimitSeconds: Int): String =
    if (timeLimitSeconds <= 0) "Không giới hạn" else "${(timeLimitSeconds + 59) / 60} phút"

private fun ExerciseDto.headerTitle(): String {
    val trimmed = title.trimStart()
    val detail = if (trimmed.startsWith("Bài tập cuối bài", ignoreCase = true)) {
        trimmed.drop("Bài tập cuối bài".length)
            .trimStart()
            .removePrefix(":")
            .trim()
    } else {
        title.trim()
    }
    return detail.ifEmpty { title }
}

private fun ExerciseDto.summaryText(): String {
    val count = "${questions.size} câu"
    return if (timeLimit > 0) "$count - Thời gian ${formatTimeLimit(timeLimit)}" else count
}

private val previewExercise = ExerciseDto(
    id = "exercise-preview",
    title = "Bài tập cuối bài: Biến và kiểu dữ liệu",
    lessonId = "lesson-preview",
    timeLimit = 900,
    passingScore = 70,
    maxAttempts = 3,
    attemptsUsed = 1,
    totalQuestions = 2,
    questions = listOf(
        ExerciseQuestionDto(
            id = "q1",
            type = ExerciseQuestionType.SINGLE_CHOICE,
            content = "Kiểu dữ liệu nào dưới đây là bất biến (immutable) trong Python?",
            options = listOf(
                QuizOptionDto("A", "List"),
                QuizOptionDto("B", "Tuple"),
                QuizOptionDto("C", "Dict"),
                QuizOptionDto("D", "Set")
            ),
            order = 1
        ),
        ExerciseQuestionDto(
            id = "q2",
            type = ExerciseQuestionType.ESSAY,
            content = "Trình bày sự khác biệt giữa kiểu dữ liệu mutable và immutable trong Python.",
            order = 2
        )
    )
)

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseIntroPreview() {
    ExerciseScreenContent(
        state = ExerciseUiState.Intro(previewExercise),
        onBackClick = {},
        onRetry = {},
        onStart = {},
        onSelectSingle = { _, _ -> },
        onToggleMultiple = { _, _ -> },
        onEssayChange = { _, _ -> },
        onPrevious = {},
        onNext = {},
        onGoTo = {}
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseQuestionSingleChoicePreview() {
    ExerciseScreenContent(
        state = ExerciseUiState.InProgress(
            exercise = previewExercise,
            currentIndex = 0,
            answers = mapOf("q1" to ExerciseAnswer(selectedKeys = setOf("B"))),
            startedAtMs = 0L
        ),
        onBackClick = {},
        onRetry = {},
        onStart = {},
        onSelectSingle = { _, _ -> },
        onToggleMultiple = { _, _ -> },
        onEssayChange = { _, _ -> },
        onPrevious = {},
        onNext = {},
        onGoTo = {}
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseQuestionEssayPreview() {
    ExerciseScreenContent(
        state = ExerciseUiState.InProgress(
            exercise = previewExercise,
            currentIndex = 1,
            answers = mapOf("q2" to ExerciseAnswer(essayText = "Tuple bất biến")),
            startedAtMs = 0L
        ),
        onBackClick = {},
        onRetry = {},
        onStart = {},
        onSelectSingle = { _, _ -> },
        onToggleMultiple = { _, _ -> },
        onEssayChange = { _, _ -> },
        onPrevious = {},
        onNext = {},
        onGoTo = {}
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseQuestionGridSheetPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        ExerciseScreenContent(
            state = ExerciseUiState.InProgress(
                exercise = previewExercise,
                currentIndex = 0,
                answers = mapOf("q1" to ExerciseAnswer(selectedKeys = setOf("B"))),
                startedAtMs = 0L
            ),
            onBackClick = {},
            onRetry = {},
            onStart = {},
            onSelectSingle = { _, _ -> },
            onToggleMultiple = { _, _ -> },
            onEssayChange = { _, _ -> },
            onPrevious = {},
            onNext = {},
            onGoTo = {}
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White
            ) {
                ExerciseQuestionGridBody(
                    state = ExerciseUiState.InProgress(
                        exercise = previewExercise,
                        currentIndex = 0,
                        answers = mapOf("q1" to ExerciseAnswer(selectedKeys = setOf("B"))),
                        startedAtMs = 0L
                    ),
                    onSelect = {},
                    onSubmit = {},
                    modifier = Modifier.fillMaxHeight(0.72f)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseSubmitConfirmDialogPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        ExerciseScreenContent(
            state = ExerciseUiState.InProgress(
                exercise = previewExercise,
                currentIndex = 0,
                answers = mapOf("q1" to ExerciseAnswer(selectedKeys = setOf("B"))),
                startedAtMs = 0L
            ),
            onBackClick = {},
            onRetry = {},
            onStart = {},
            onSelectSingle = { _, _ -> },
            onToggleMultiple = { _, _ -> },
            onEssayChange = { _, _ -> },
            onPrevious = {},
            onNext = {},
            onGoTo = {}
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            ExerciseSubmitConfirmCard(
                unansweredCount = 4,
                unansweredListText = "Câu 07, Câu 08, Câu 09, Câu 10",
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseTimeOutDialogPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        ExerciseScreenContent(
            state = ExerciseUiState.InProgress(
                exercise = previewExercise,
                currentIndex = 0,
                answers = mapOf("q1" to ExerciseAnswer(selectedKeys = setOf("B"))),
                startedAtMs = 0L
            ),
            onBackClick = {},
            onRetry = {},
            onStart = {},
            onSelectSingle = { _, _ -> },
            onToggleMultiple = { _, _ -> },
            onEssayChange = { _, _ -> },
            onPrevious = {},
            onNext = {},
            onGoTo = {}
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            ExerciseTimeOutCard(
                onViewResult = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseLeaveConfirmDialogPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        ExerciseScreenContent(
            state = ExerciseUiState.InProgress(
                exercise = previewExercise,
                currentIndex = 0,
                answers = mapOf("q1" to ExerciseAnswer(selectedKeys = setOf("B"))),
                startedAtMs = 0L
            ),
            onBackClick = {},
            onRetry = {},
            onStart = {},
            onSelectSingle = { _, _ -> },
            onToggleMultiple = { _, _ -> },
            onEssayChange = { _, _ -> },
            onPrevious = {},
            onNext = {},
            onGoTo = {}
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            ExerciseLeaveConfirmCard(
                onConfirmLeave = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseLoadingPreview() {
    ExerciseScreenContent(
        state = ExerciseUiState.Loading,
        onBackClick = {},
        onRetry = {},
        onStart = {},
        onSelectSingle = { _, _ -> },
        onToggleMultiple = { _, _ -> },
        onEssayChange = { _, _ -> },
        onPrevious = {},
        onNext = {},
        onGoTo = {}
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 890)
@Composable
private fun ExerciseErrorPreview() {
    ExerciseScreenContent(
        state = ExerciseUiState.Error("Không thể tải bài tập. Vui lòng thử lại."),
        onBackClick = {},
        onRetry = {},
        onStart = {},
        onSelectSingle = { _, _ -> },
        onToggleMultiple = { _, _ -> },
        onEssayChange = { _, _ -> },
        onPrevious = {},
        onNext = {},
        onGoTo = {}
    )
}
