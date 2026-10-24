package com.edumind.app.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.ui.common.EduMindButton
import com.edumind.app.ui.common.EduMindTextField
import com.edumind.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            delay(800)
            viewModel.resetSuccess()
            onBackClick()
        }
    }

    ChangePasswordContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onForgotPasswordClick = onForgotPasswordClick,
        onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onToggleCurrentPasswordVisibility = viewModel::toggleCurrentPasswordVisibility,
        onToggleNewPasswordVisibility = viewModel::toggleNewPasswordVisibility,
        onToggleConfirmPasswordVisibility = viewModel::toggleConfirmPasswordVisibility,
        onSubmitClick = viewModel::changePassword
    )
}

@Composable
fun ChangePasswordContent(
    uiState: ChangePasswordUiState,
    onBackClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onCurrentPasswordChange: (String) -> Unit = {},
    onNewPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onToggleCurrentPasswordVisibility: () -> Unit = {},
    onToggleNewPasswordVisibility: () -> Unit = {},
    onToggleConfirmPasswordVisibility: () -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Neutral700
                    )
                }

                Text(
                    text = "Đổi mật khẩu",
                    fontFamily = EduMindFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Neutral900
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    EduMindButton(
                        text = "Lưu mật khẩu mới",
                        enabled = uiState.isSaveEnabled,
                        loading = uiState.isLoading,
                        onClick = onSubmitClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Field 1: Mật khẩu hiện tại
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Mật khẩu hiện tại",
                        fontFamily = EduMindFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Neutral700
                    )

                    EduMindTextField(
                        value = uiState.currentPassword,
                        onValueChange = onCurrentPasswordChange,
                        placeholder = "Nhập mật khẩu hiện tại",
                        errorText = uiState.currentPasswordError,
                        visualTransformation = if (uiState.isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = Neutral400)
                        },
                        trailingIcon = {
                            IconButton(onClick = onToggleCurrentPasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.isCurrentPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = "Ẩn/Hiện mật khẩu",
                                    tint = Neutral400
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Text "Quên mật khẩu?" nằm ở dưới góc phải
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Quên mật khẩu?",
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Primary700,
                            modifier = Modifier
                                .clickable { onForgotPasswordClick() }
                                .padding(top = 2.dp)
                        )
                    }
                }

                // Field 2: Mật khẩu mới
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Mật khẩu mới",
                        fontFamily = EduMindFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Neutral700
                    )

                    EduMindTextField(
                        value = uiState.newPassword,
                        onValueChange = onNewPasswordChange,
                        placeholder = "Nhập mật khẩu mới",
                        visualTransformation = if (uiState.isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = Neutral400)
                        },
                        trailingIcon = {
                            IconButton(onClick = onToggleNewPasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.isNewPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = "Ẩn/Hiện mật khẩu",
                                    tint = Neutral400
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Checklist tiêu chí độ mạnh
                    Column(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ChecklistCriteriaItem(
                            text = "Tối thiểu 8 ký tự",
                            isSatisfied = uiState.hasMinLength
                        )
                        ChecklistCriteriaItem(
                            text = "Bao gồm cả chữ và số",
                            isSatisfied = uiState.hasLetterAndDigit
                        )
                    }
                }

                // Field 3: Xác nhận mật khẩu mới
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Xác nhận mật khẩu mới",
                        fontFamily = EduMindFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Neutral700
                    )

                    EduMindTextField(
                        value = uiState.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        placeholder = "Nhập lại mật khẩu mới",
                        visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = Neutral400)
                        },
                        trailingIcon = {
                            IconButton(onClick = onToggleConfirmPasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.isConfirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = "Ẩn/Hiện mật khẩu",
                                    tint = Neutral400
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.isConfirmMatching) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Mật khẩu xác nhận hoàn toàn trùng khớp",
                                fontFamily = EduMindFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = Color(0xFF15803D)
                            )
                        }
                    } else if (uiState.confirmPassword.isNotEmpty()) {
                        Text(
                            text = "Mật khẩu xác nhận không trùng khớp",
                            fontFamily = EduMindFontFamily,
                            fontSize = 11.sp,
                            color = Error600,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Error message từ ViewModel nếu có
                uiState.errorMessage?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        fontFamily = EduMindFontFamily,
                        fontSize = 12.sp,
                        color = Error600,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Toast thông báo đã đổi mật khẩu thành công
            AnimatedVisibility(
                visible = uiState.isSuccess,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { 50 }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xE6171717), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Đã cập nhật mật khẩu mới",
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistCriteriaItem(
    text: String,
    isSatisfied: Boolean
) {
    val tintColor = if (isSatisfied) Color(0xFF15803D) else Neutral400
    val iconBg = if (isSatisfied) Color(0xFFDCFCE7) else Color(0xFFF5F5F5)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(iconBg, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(10.dp)
            )
        }

        Text(
            text = text,
            fontFamily = EduMindFontFamily,
            fontWeight = if (isSatisfied) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.sp,
            color = tintColor
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ChangePasswordContentPreview() {
    ChangePasswordContent(
        uiState = ChangePasswordUiState(
            currentPassword = "oldPassword123",
            newPassword = "NewPassword123",
            confirmPassword = "NewPassword123"
        )
    )
}
