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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
            viewModel.resetSuccess()
            onBackClick()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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
                        .padding(horizontal = 20.dp, vertical = 16.dp)
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

                    // Checklist tiêu chí độ mạnh (đồng bộ chuẩn 4 tiêu chí như RegisterScreen)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CriteriaItem(
                            text = "Tối thiểu 8 ký tự",
                            isMet = uiState.hasMinLength
                        )
                        CriteriaItem(
                            text = "Gồm chữ hoa (A-Z) và chữ thường (a-z)",
                            isMet = uiState.hasUpperAndLower
                        )
                        CriteriaItem(
                            text = "Gồm ít nhất 1 chữ số (0-9)",
                            isMet = uiState.hasDigit
                        )
                        CriteriaItem(
                            text = "Gồm ít nhất 1 ký tự đặc biệt (@$!%*?&)",
                            isMet = uiState.hasSpecialChar
                        )
                    }

                    if (uiState.isSameAsOldPassword) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = Error600,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Mật khẩu mới không được trùng với mật khẩu hiện tại",
                                fontFamily = EduMindFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Error600
                            )
                        }
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = Error600,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Mật khẩu xác nhận không trùng khớp",
                                fontFamily = EduMindFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Error600
                            )
                        }
                    }
                }

                // Error message từ ViewModel nếu có
                uiState.errorMessage?.let { errorMsg ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = Error600,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = errorMsg,
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Error600
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CriteriaItem(
    text: String,
    isMet: Boolean
) {
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
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            fontWeight = if (isMet) FontWeight.Medium else FontWeight.Normal,
            color = if (isMet) Success700 else Neutral500
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
