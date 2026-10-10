package com.edumind.app.ui.auth

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.R
import com.edumind.app.ui.auth.components.AuthTextField
import com.edumind.app.ui.auth.components.GradientButton
import com.edumind.app.ui.auth.components.PasswordField
import com.edumind.app.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToOtp: (email: String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Shake animation offset
    val shakeOffsetX = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LoginUiEvent.TriggerShake -> {
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
                is LoginUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is LoginUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.showUnverifiedSnackbar) {
        if (uiState.showUnverifiedSnackbar) {
            val result = snackbarHostState.showSnackbar(
                message = "Tài khoản chưa được kích hoạt.",
                actionLabel = "Gửi lại mã",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                onNavigateToOtp(uiState.email)
            }
            viewModel.dismissUnverifiedSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .then(
                        if (uiState.isLocked || uiState.showAccountLockedDialog) {
                            Modifier.alpha(0.25f)
                        } else {
                            Modifier
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Khối trên cùng: Header & Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = shakeOffsetX.value.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Logo EduMind (w-32, mb-4 theo prototype)
                    Image(
                        painter = painterResource(id = R.drawable.logo_edumind),
                        contentDescription = "EduMind Logo",
                        modifier = Modifier
                            .width(128.dp)
                            .wrapContentHeight()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tiêu đề Đăng nhập (22px font-semibold text-neutral-950, không có subtitle)
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Neutral950
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Trạng thái lỗi từ Server (401 / sai thông tin đăng nhập khi validation client đã hợp lệ)
                    val isServerError = uiState.generalErrorMessage != null && uiState.emailError.isNullOrBlank() && uiState.passwordError.isNullOrBlank()

                    // Input Email:
                    // - Client-side error: Báo lỗi inline ngay dưới ô email (uiState.emailError)
                    // - Server-side error: Giữ viền đỏ cảnh báo khi đăng nhập thất bại, không để bất kỳ dòng chữ báo lỗi nào dưới ô này
                    AuthTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = "Email",
                        placeholder = "Nhập email",
                        errorText = uiState.emailError,
                        isError = !uiState.emailError.isNullOrBlank() || isServerError,
                        enabled = !uiState.isLocked && !uiState.isLoading,
                        onFocus = { viewModel.clearErrors() },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Password:
                    // - Client-side error: Báo lỗi inline ngay dưới ô password (uiState.passwordError)
                    // - Server-side error: Giữ viền đỏ cảnh báo và thêm đúng 1 dòng text đỏ duy nhất: ⚠ Tài khoản hoặc mật khẩu không chính xác.
                    val passwordErrorText = when {
                        !uiState.passwordError.isNullOrBlank() -> uiState.passwordError
                        isServerError -> "Tài khoản hoặc mật khẩu không chính xác."
                        else -> null
                    }

                    PasswordField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        errorText = passwordErrorText,
                        isError = !uiState.passwordError.isNullOrBlank() || isServerError,
                        placeholder = "Nhập mật khẩu",
                        enabled = !uiState.isLocked && !uiState.isLoading,
                        imeAction = ImeAction.Done,
                        onFocus = { viewModel.clearErrors() },
                        onImeAction = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.login()
                        }
                    )

                    // Link Quên mật khẩu? (Căn phải ngay dưới ô mật khẩu theo prototype)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Quên mật khẩu?",
                            color = Primary700,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.clickable { onNavigateToForgotPassword() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nút Đăng nhập (Gradient Nebula Dream 3 màu, bo góc 8dp, shadow tím)
                    GradientButton(
                        text = "Đăng nhập",
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.login()
                        },
                        enabled = !uiState.isLoading && !uiState.isLocked,
                        loading = uiState.isLoading
                    )
                }

                // Chân trang: Link Đăng ký ngay (đã xóa thanh Home Indicator hardcode)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chưa có tài khoản?",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = Neutral600
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Đăng ký ngay",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = Primary700,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable { onNavigateToRegister() }
                        )
                    }
                }
            }

            // Modal Locked Dialog chuẩn prototype M-03 (Tài khoản bị tạm khóa)
            if (uiState.isLocked || uiState.showAccountLockedDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x990A0A0A))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Error100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = Error600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tài khoản bị tạm khóa",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Neutral950
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (!uiState.generalErrorMessage.isNullOrBlank()) {
                                    uiState.generalErrorMessage!!
                                } else {
                                    "Tài khoản của bạn đã bị tạm khóa do nhập sai mật khẩu quá 5 lần. Vui lòng thử lại sau 15 phút hoặc liên hệ quản trị viên."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Neutral600,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .background(NebulaBrush)
                                    .clickable {
                                        viewModel.dismissAccountLockedDialog()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Đã hiểu",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
