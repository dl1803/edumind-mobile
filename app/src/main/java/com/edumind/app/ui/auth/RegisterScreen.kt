package com.edumind.app.ui.auth

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.ui.auth.components.AuthTextField
import com.edumind.app.ui.auth.components.GradientButton
import com.edumind.app.ui.auth.components.OtpInput
import com.edumind.app.ui.auth.components.PasswordField
import com.edumind.app.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Hiệu ứng Rung khi gặp lỗi validation hoặc OTP sai
    val shakeOffsetX = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is RegisterUiEvent.TriggerShake -> {
                    shakeOffsetX.snapTo(0f)
                    shakeOffsetX.animateTo(
                        targetValue = 0f,
                        animationSpec = keyframes {
                            durationMillis = 200
                            0f at 0
                            -8f at 25
                            8f at 50
                            -6f at 75
                            6f at 100
                            -4f at 125
                            4f at 150
                            -2f at 175
                            0f at 200
                        }
                    )
                }
                is RegisterUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is RegisterUiEvent.ShowSnackbar -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is RegisterUiEvent.NavigateToHome -> {
                    onRegisterSuccess()
                }
            }
        }
    }

    // Xử lý phím Back cứng trên điện thoại
    BackHandler {
        if (uiState.currentStep == RegisterStep.OTP) {
            viewModel.requestExitOtp()
        } else {
            onNavigateBack()
        }
    }

    // Hộp thoại xác nhận hủy đăng ký khi đang ở bước OTP
    if (uiState.showExitOtpDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissExitOtpDialog() },
            title = {
                Text(
                    text = "Hủy xác nhận email?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Quá trình đăng ký chưa hoàn tất. Bạn có chắc chắn muốn quay lại biểu mẫu?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmExitOtp() }) {
                    Text(text = "Quay lại", color = Error600, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExitOtpDialog() }) {
                    Text(text = "Tiếp tục nhập", color = Primary700, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.currentStep == RegisterStep.FORM) "Đăng ký" else "Xác nhận email",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Neutral950
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.currentStep == RegisterStep.OTP) {
                                viewModel.requestExitOtp()
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Neutral950
                        )
                    }
                },
                actions = {
                    // Cân đối khoảng trống phía bên phải TopAppBar
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState == RegisterStep.OTP) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "RegisterStepTransition"
            ) { step ->
                when (step) {
                    RegisterStep.FORM -> {
                        RegisterFormContent(
                            uiState = uiState,
                            shakeOffset = shakeOffsetX.value,
                            onFullNameChange = viewModel::onFullNameChanged,
                            onEmailChange = viewModel::onEmailChanged,
                            onPasswordChange = viewModel::onPasswordChanged,
                            onConfirmPasswordChange = viewModel::onConfirmPasswordChanged,
                            onSubmit = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                viewModel.submitRegisterForm()
                            },
                            onNavigateToLogin = onNavigateToLogin
                        )
                    }
                    RegisterStep.OTP -> {
                        RegisterOtpContent(
                            uiState = uiState,
                            shakeOffset = shakeOffsetX.value,
                            onOtpChange = viewModel::onOtpCodeChanged,
                            onVerify = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                viewModel.verifyOtp()
                            },
                            onResendOtp = viewModel::resendOtp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Giao diện Bước 1: Form Đăng ký
 */
@Composable
private fun RegisterFormContent(
    uiState: RegisterUiState,
    shakeOffset: Float,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = shakeOffset.dp)
        ) {
            // Họ và tên
            AuthTextField(
                value = uiState.fullName,
                onValueChange = onFullNameChange,
                label = "Họ và tên",
                placeholder = "Nhập họ tên",
                errorText = uiState.fullNameError,
                isError = !uiState.fullNameError.isNullOrBlank(),
                enabled = !uiState.isLoading,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Person, contentDescription = null, tint = Neutral400)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            AuthTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "example@email.com",
                errorText = uiState.emailError,
                isError = !uiState.emailError.isNullOrBlank(),
                enabled = !uiState.isLoading,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Email, contentDescription = null, tint = Neutral400)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mật khẩu
            PasswordField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Mật khẩu",
                placeholder = "Nhập mật khẩu",
                errorText = uiState.passwordError,
                isError = !uiState.passwordError.isNullOrBlank(),
                enabled = !uiState.isLoading,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Checklist yêu cầu mật khẩu
            PasswordCriteriaChecklist(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            // Xác nhận mật khẩu
            PasswordField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Xác nhận mật khẩu",
                placeholder = "Nhập lại mật khẩu",
                errorText = uiState.confirmPasswordError,
                isError = !uiState.confirmPasswordError.isNullOrBlank(),
                enabled = !uiState.isLoading,
                imeAction = ImeAction.Done,
                onImeAction = onSubmit
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nút "Đăng ký"
            GradientButton(
                text = "Đăng ký",
                onClick = onSubmit,
                loading = uiState.isLoading,
                enabled = uiState.isFormValid
            )
        }

        // Footer: Đã có tài khoản? Đăng nhập
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đã có tài khoản?",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = Neutral700)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Đăng nhập",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary700
                ),
                modifier = Modifier.clickable(onClick = onNavigateToLogin)
            )
        }
    }
}

/**
 * Checklist trực quan kiểm tra từng tiêu chí mật khẩu
 */
@Composable
private fun PasswordCriteriaChecklist(uiState: RegisterUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CriteriaItem(text = "Tối thiểu 8 ký tự", isMet = uiState.hasMinLength)
        CriteriaItem(text = "Gồm chữ hoa (A-Z) và chữ thường (a-z)", isMet = uiState.hasUppercase && uiState.hasLowercase)
        CriteriaItem(text = "Gồm ít nhất 1 chữ số (0-9)", isMet = uiState.hasDigit)
        CriteriaItem(text = "Gồm ít nhất 1 ký tự đặc biệt (@$!%*?&)", isMet = uiState.hasSpecialChar)
    }
}

@Composable
private fun CriteriaItem(text: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isMet) Success600 else Neutral400,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isMet) FontWeight.Medium else FontWeight.Normal,
                color = if (isMet) Success700 else Neutral500
            )
        )
    }
}

/**
 * Giao diện Bước 2: Xác nhận OTP
 */
@Composable
private fun RegisterOtpContent(
    uiState: RegisterUiState,
    shakeOffset: Float,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResendOtp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = shakeOffset.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Khiên Bảo Mật (Shield)
            SecurityShieldGraphic()

            Spacer(modifier = Modifier.height(16.dp))

            // Tiêu đề & Thông tin Email nhận mã
            Text(
                text = "Mã xác nhận bảo mật",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral950
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mã xác thực 6 số đã được gửi đến",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    color = Neutral500
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = uiState.email,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Neutral950
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6 ô nhập OTP
            OtpInput(
                value = uiState.otpCode,
                onValueChange = onOtpChange,
                isError = !uiState.otpError.isNullOrBlank(),
                enabled = !uiState.isLoading && !uiState.isOtpLocked,
                onOtpComplete = { onVerify() }
            )

            // Dòng thông báo lỗi OTP nếu có
            if (!uiState.otpError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = uiState.otpError,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Error600
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bộ đếm thời gian
            if (!uiState.isOtpLocked) {
                Text(
                    text = "Mã hết hạn sau ${uiState.countdownFormatted}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Warning600
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nút "Gửi lại mã"
            Text(
                text = "Gửi lại mã",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (uiState.isResendEnabled && !uiState.isLoading && !uiState.isOtpLocked) Primary700 else Neutral400
                ),
                modifier = Modifier.clickable(
                    enabled = uiState.isResendEnabled && !uiState.isLoading && !uiState.isOtpLocked,
                    onClick = onResendOtp
                )
            )
        }

        // Nút "Xác nhận" ở đáy màn hình
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            GradientButton(
                text = "Xác nhận",
                onClick = onVerify,
                loading = uiState.isLoading,
                enabled = uiState.otpCode.length == 6 && !uiState.isLoading && !uiState.isOtpLocked
            )
        }
    }
}

@Composable
private fun SecurityShieldGraphic(modifier: Modifier = Modifier) {
    val haloTransition = rememberInfiniteTransition(label = "shieldHaloTransition")
    val haloScale by haloTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldHaloScale"
    )
    val haloAlpha by haloTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldHaloAlpha"
    )

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerPoint = center
            val maxRadius = (size.width * 0.48f) * haloScale

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0x358B5CF6).copy(alpha = 0.30f * haloAlpha),
                        0.35f to Color(0x288B5CF6).copy(alpha = 0.22f * haloAlpha),
                        0.60f to Color(0x20EC4899).copy(alpha = 0.16f * haloAlpha),
                        0.82f to Color(0x153B82F6).copy(alpha = 0.10f * haloAlpha),
                        1.00f to Color.Transparent
                    ),
                    center = centerPoint,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = centerPoint
            )

            val innerRadius = maxRadius * 0.78f
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0x458B5CF6).copy(alpha = 0.38f * haloAlpha),
                        0.45f to Color(0x327C3AED).copy(alpha = 0.26f * haloAlpha),
                        0.75f to Color(0x1AEC4899).copy(alpha = 0.14f * haloAlpha),
                        1.00f to Color.Transparent
                    ),
                    center = centerPoint,
                    radius = innerRadius
                ),
                radius = innerRadius,
                center = centerPoint
            )
        }

        Canvas(
            modifier = Modifier.size(width = 165.dp, height = 158.dp)
        ) {
            val w = size.width
            val h = size.height

            val outerShield = Path().apply {
                moveTo(w * 0.5f, h * 0.05f)
                lineTo(w * 0.1f, h * 0.20f)
                lineTo(w * 0.1f, h * 0.47f)
                cubicTo(w * 0.1f, h * 0.72f, w * 0.28f, h * 0.89f, w * 0.5f, h * 0.96f)
                cubicTo(w * 0.72f, h * 0.89f, w * 0.9f, h * 0.72f, w * 0.9f, h * 0.47f)
                lineTo(w * 0.9f, h * 0.20f)
                close()
            }

            val shieldBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E3A8A),
                    Color(0xFF2563EB),
                    Color(0xFF6D28D9),
                    Color(0xFF8B5CF6)
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
            drawPath(path = outerShield, brush = shieldBrush)

            val borderBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF93C5FD),
                    Color(0xFFC4B5FD),
                    Color(0xFFDDD6FE)
                )
            )
            drawPath(path = outerShield, brush = borderBrush, style = Stroke(width = 5f))

            val innerShield = Path().apply {
                moveTo(w * 0.5f, h * 0.11f)
                lineTo(w * 0.16f, h * 0.24f)
                lineTo(w * 0.16f, h * 0.47f)
                cubicTo(w * 0.16f, h * 0.68f, w * 0.31f, h * 0.83f, w * 0.5f, h * 0.89f)
                cubicTo(w * 0.69f, h * 0.83f, w * 0.84f, h * 0.68f, w * 0.84f, h * 0.47f)
                lineTo(w * 0.84f, h * 0.24f)
                close()
            }
            drawPath(path = innerShield, color = Color.White.copy(alpha = 0.35f), style = Stroke(width = 3f))

            val lightSheen = Path().apply {
                moveTo(w * 0.5f, h * 0.09f)
                lineTo(w * 0.15f, h * 0.22f)
                lineTo(w * 0.15f, h * 0.47f)
                cubicTo(w * 0.15f, h * 0.68f, w * 0.30f, h * 0.83f, w * 0.5f, h * 0.90f)
                close()
            }
            drawPath(path = lightSheen, color = Color.White.copy(alpha = 0.14f))
        }
    }
}
