package com.edumind.app.ui.auth.forgot

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.edumind.app.util.Validators

@Composable
fun ForgotPasswordScreen(
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        viewModel.handleBackNavigation(onExitToLogin = onNavigateBackToLogin)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            ForgotPasswordTopBar(
                title = when (uiState.currentStep) {
                    ForgotPasswordStep.EMAIL -> "Quên mật khẩu"
                    ForgotPasswordStep.OTP -> "Xác nhận email"
                    ForgotPasswordStep.NEW_PASSWORD -> "Đặt lại mật khẩu"
                },
                onBackClick = {
                    viewModel.handleBackNavigation(onExitToLogin = onNavigateBackToLogin)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            StepIndicator(currentStep = uiState.currentStep)

            uiState.generalError?.let { errorMsg ->
                Surface(
                    color = Error100,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = errorMsg,
                        color = Error600,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            AnimatedContent(
                targetState = uiState.currentStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                transitionSpec = {
                    if (targetState.stepIndex > initialState.stepIndex) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "ForgotPasswordStepsTransition"
            ) { step ->
                when (step) {
                    ForgotPasswordStep.EMAIL -> {
                        StepEmailContent(
                            email = uiState.email,
                            emailError = uiState.emailError,
                            isLoading = uiState.isLoading,
                            onEmailChange = viewModel::onEmailChanged,
                            onSubmit = viewModel::submitEmail,
                            onNavigateToLogin = onNavigateBackToLogin
                        )
                    }
                    ForgotPasswordStep.OTP -> {
                        StepOtpContent(
                            email = uiState.email,
                            otp = uiState.otp,
                            otpError = uiState.otpError,
                            countdownSeconds = uiState.countdownSeconds,
                            resendCooldownSeconds = uiState.resendCooldownSeconds,
                            isResendEnabled = uiState.isResendEnabled,
                            isLoading = uiState.isLoading,
                            shakeTrigger = uiState.shakeTrigger,
                            onOtpChange = viewModel::onOtpChanged,
                            onResendClick = viewModel::resendOtp,
                            onVerifyClick = { viewModel.verifyOtp() }
                        )
                    }
                    ForgotPasswordStep.NEW_PASSWORD -> {
                        StepNewPasswordContent(
                            password = uiState.newPassword,
                            passwordError = uiState.newPasswordError,
                            confirmPassword = uiState.confirmPassword,
                            confirmPasswordError = uiState.confirmPasswordError,
                            isLoading = uiState.isLoading,
                            onPasswordChange = viewModel::onNewPasswordChanged,
                            onConfirmPasswordChange = viewModel::onConfirmPasswordChanged,
                            onSubmit = viewModel::submitNewPassword
                        )
                    }
                }
            }
        }
    }

    if (uiState.isCancelDialogOpen) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCancelDialog,
            title = { Text("Hủy xác thực?", fontWeight = FontWeight.Bold) },
            text = { Text("Quay lại sẽ hủy phiên xác thực mã OTP hiện tại. Bạn có chắc chắn muốn quay lại bước nhập Email?") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmCancelOtp) {
                    Text("Đồng ý", color = Error600, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissCancelDialog) {
                    Text("Tiếp tục xác thực")
                }
            }
        )
    }

    if (uiState.isSuccessDialogOpen) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Success100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Success600,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Đặt lại mật khẩu thành công!",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Mật khẩu của bạn đã được cập nhật mới. Bạn có thể sử dụng mật khẩu này để đăng nhập ngay bây giờ.",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                GradientButton(
                    text = "Đăng nhập ngay",
                    onClick = onNavigateBackToLogin,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Neutral950
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Neutral900
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
private fun StepIndicator(currentStep: ForgotPasswordStep) {
    val steps = listOf(
        Pair(1, "Email"),
        Pair(2, "OTP"),
        Pair(3, "Mật khẩu")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .align(Alignment.TopCenter)
                .padding(top = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(
                        if (currentStep.stepIndex >= 2) NebulaBrush135
                        else Brush.linearGradient(listOf(Neutral200, Neutral200))
                    )
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(
                        if (currentStep.stepIndex >= 3) NebulaBrush135
                        else Brush.linearGradient(listOf(Neutral200, Neutral200))
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            steps.forEach { (index, label) ->
                val isCompleted = index < currentStep.stepIndex
                val isCurrent = index == currentStep.stepIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(64.dp)
                ) {
                    val circleModifier = when {
                        isCurrent -> Modifier
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor = NebulaShadow,
                                spotColor = NebulaShadow
                            )
                            .background(NebulaBrush135, CircleShape)
                        isCompleted -> Modifier
                            .shadow(
                                elevation = 2.dp,
                                shape = CircleShape,
                                ambientColor = NebulaShadow,
                                spotColor = NebulaShadow
                            )
                            .background(NebulaBrush135, CircleShape)
                        else -> Modifier
                            .background(Neutral200, CircleShape)
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .then(circleModifier),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "$index",
                                color = if (isCurrent) Color.White else Neutral500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                        color = when {
                            isCurrent -> Primary700
                            isCompleted -> Primary600
                            else -> Neutral400
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun StepEmailContent(
    email: String,
    emailError: String?,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val minHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Primary50)
                        .border(1.dp, Primary100, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Primary700,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nhập email đã đăng ký để nhận mã xác nhận đặt lại mật khẩu.",
                    fontSize = 14.sp,
                    color = Neutral600,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    placeholder = "Nhập email đã đăng ký",
                    errorText = emailError,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null
                        )
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GradientButton(
                    text = "Tiếp tục",
                    onClick = onSubmit,
                    loading = isLoading,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Đã nhớ mật khẩu?",
                        fontSize = 14.sp,
                        color = Neutral700
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Đăng nhập",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary700,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepOtpContent(
    email: String,
    otp: String,
    otpError: String?,
    countdownSeconds: Int,
    resendCooldownSeconds: Int,
    isResendEnabled: Boolean,
    isLoading: Boolean,
    shakeTrigger: Long,
    onOtpChange: (String) -> Unit,
    onResendClick: () -> Unit,
    onVerifyClick: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0L) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    (-20f) at 50
                    20f at 100
                    (-15f) at 150
                    15f at 200
                    (-10f) at 250
                    10f at 300
                    0f at 400
                }
            )
        }
    }

    val minutes = countdownSeconds / 60
    val seconds = countdownSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val minHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Mã xác thực 6 số đã được gửi đến",
                    fontSize = 14.sp,
                    color = Neutral600,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Neutral950,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Box(modifier = Modifier.offset(x = offsetX.value.dp)) {
                    OtpInput(
                        value = otp,
                        onValueChange = onOtpChange,
                        isError = !otpError.isNullOrBlank(),
                        enabled = !isLoading,
                        onOtpComplete = { onVerifyClick() }
                    )
                }

                if (!otpError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = otpError,
                        color = Error600,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Mã hết hạn sau ",
                        fontSize = 14.sp,
                        color = Neutral600
                    )
                    Text(
                        text = timeFormatted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = Warning600
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Chưa nhận được mã? ",
                        fontSize = 14.sp,
                        color = Neutral600
                    )
                    val resendText = if (resendCooldownSeconds > 0) {
                        "Gửi lại mã (${resendCooldownSeconds}s)"
                    } else {
                        "Gửi lại mã"
                    }
                    Text(
                        text = resendText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isResendEnabled) Primary700 else Neutral400,
                        modifier = if (isResendEnabled) Modifier.clickable { onResendClick() } else Modifier
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GradientButton(
                    text = "Xác nhận",
                    onClick = onVerifyClick,
                    loading = isLoading,
                    enabled = !isLoading && otp.length == 6
                )
            }
        }
    }
}

@Composable
private fun StepNewPasswordContent(
    password: String,
    passwordError: String?,
    confirmPassword: String,
    confirmPasswordError: String?,
    isLoading: Boolean,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val isPasswordMatch = password.isNotEmpty() && password == confirmPassword
    val hasMinLength = Validators.hasMinLength(password)
    val hasUpperAndLower = Validators.hasUppercase(password) && Validators.hasLowercase(password)
    val hasDigit = Validators.hasDigit(password)
    val hasSpecialChar = Validators.hasSpecialChar(password)
    val isAllCriteriaMet = hasMinLength && hasUpperAndLower && hasDigit && hasSpecialChar

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val minHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECFDF5))
                        .border(1.dp, Color(0xFFD1FAE5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GppGood,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Tạo mật khẩu mới cho tài khoản của bạn.",
                    fontSize = 14.sp,
                    color = Neutral600,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = "Mật khẩu mới",
                    placeholder = "Nhập mật khẩu mới",
                    errorText = passwordError,
                    enabled = !isLoading,
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(8.dp))

                PasswordCriteriaChecklist(
                    hasMinLength = hasMinLength,
                    hasUpperAndLower = hasUpperAndLower,
                    hasDigit = hasDigit,
                    hasSpecialChar = hasSpecialChar
                )

                Spacer(modifier = Modifier.height(12.dp))

                PasswordField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = "Xác nhận mật khẩu mới",
                    placeholder = "Nhập lại mật khẩu mới",
                    errorText = confirmPasswordError,
                    enabled = !isLoading,
                    imeAction = ImeAction.Done,
                    onImeAction = onSubmit
                )

                if (isPasswordMatch && confirmPasswordError == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Success600,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mật khẩu khớp",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Success600
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GradientButton(
                    text = "Đặt lại mật khẩu",
                    onClick = onSubmit,
                    loading = isLoading,
                    enabled = !isLoading && isAllCriteriaMet && confirmPassword.isNotEmpty()
                )
            }
        }
    }
}

@Composable
private fun PasswordCriteriaChecklist(
    hasMinLength: Boolean,
    hasUpperAndLower: Boolean,
    hasDigit: Boolean,
    hasSpecialChar: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CriteriaItem(text = "Tối thiểu 8 ký tự", isMet = hasMinLength)
        CriteriaItem(text = "Gồm chữ hoa (A-Z) và chữ thường (a-z)", isMet = hasUpperAndLower)
        CriteriaItem(text = "Gồm ít nhất 1 chữ số (0-9)", isMet = hasDigit)
        CriteriaItem(text = "Gồm ít nhất 1 ký tự đặc biệt (@$!%*?&)", isMet = hasSpecialChar)
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
                fontSize = 12.sp,
                fontWeight = if (isMet) FontWeight.Medium else FontWeight.Normal,
                color = if (isMet) Success700 else Neutral500
            )
        )
    }
}
