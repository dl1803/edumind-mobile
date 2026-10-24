package com.edumind.app.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.edumind.app.ui.common.EduMindButton
import com.edumind.app.ui.common.EduMindTextField
import com.edumind.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.onAvatarSelected(uri?.toString())
    }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            delay(800)
            viewModel.resetSaveSuccess()
            onBackClick()
        }
    }

    EditProfileContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onPickPhotoClick = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onFullNameChange = viewModel::onFullNameChange,
        onSaveClick = viewModel::saveProfile
    )
}

@Composable
fun EditProfileContent(
    uiState: EditProfileUiState,
    onBackClick: () -> Unit = {},
    onPickPhotoClick: () -> Unit = {},
    onFullNameChange: (String) -> Unit = {},
    onSaveClick: () -> Unit = {}
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
                    text = "Chỉnh sửa hồ sơ",
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
                        text = "Lưu thay đổi",
                        enabled = uiState.isSaveEnabled,
                        loading = uiState.isSaving,
                        onClick = onSaveClick,
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
                verticalArrangement = Arrangement.spacedBy(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Uploader Area
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.size(104.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Avatar 96dp viền Nebula
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(NebulaBrush, shape = CircleShape)
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarModel = uiState.selectedAvatarUri ?: uiState.currentAvatarUrl
                            if (!avatarModel.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarModel,
                                    contentDescription = "Ảnh đại diện",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = uiState.fullName.take(1).ifEmpty { "N" }.uppercase(),
                                    fontFamily = EduMindFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    color = Primary700
                                )
                            }
                        }

                        // Floating Camera Button nổi hoàn toàn lên trên, không bị cắt xén
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Primary700)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { onPickPhotoClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Chọn ảnh",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "Thay đổi ảnh đại diện",
                        fontFamily = EduMindFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Primary700,
                        modifier = Modifier.clickable { onPickPhotoClick() }
                    )
                }

                // Field 1: Họ và tên
                Column(modifier = Modifier.fillMaxWidth()) {
                    EduMindTextField(
                        value = uiState.fullName,
                        onValueChange = onFullNameChange,
                        label = "Họ và tên",
                        placeholder = "Nhập họ và tên",
                        errorText = uiState.fullNameError,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Field 2: Địa chỉ Email (Read-only + Đã xác minh)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Địa chỉ Email",
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Neutral700
                        )

                        Text(
                            text = "Đã xác minh",
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF047857)
                        )
                    }

                    EduMindTextField(
                        value = uiState.email,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Error message nếu có
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

            // Toast thông báo đã lưu thành công
            AnimatedVisibility(
                visible = uiState.isSaveSuccess,
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
                            text = "Đã cập nhật hồ sơ",
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditProfileContentPreview() {
    EditProfileContent(
        uiState = EditProfileUiState(
            fullName = "Nguyễn Văn Minh",
            email = "minh.nguyen@gmail.com",
            isInitialLoading = false
        )
    )
}
