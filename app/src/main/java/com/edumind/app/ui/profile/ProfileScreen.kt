package com.edumind.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.edumind.app.data.model.response.UserProfile
import com.edumind.app.ui.common.ErrorView
import com.edumind.app.ui.common.SkeletonBox
import com.edumind.app.ui.theme.*

@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onPaymentHistoryClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            onLogoutSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvasMobile)
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                ProfileLoadingSkeleton()
            }

            is ProfileUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is ProfileUiState.Success -> {
                ProfileContent(
                    user = state.user,
                    unreadCount = state.unreadNotificationCount,
                    onEditProfileClick = onEditProfileClick,
                    onChangePasswordClick = onChangePasswordClick,
                    onPaymentHistoryClick = onPaymentHistoryClick,
                    onNotificationsClick = onNotificationsClick,
                    onSettingsClick = onSettingsClick,
                    onLogoutClick = { showLogoutDialog = true }
                )
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = {
                    Text(
                        text = "Xác nhận đăng xuất",
                        fontFamily = EduMindFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Neutral900
                    )
                },
                text = {
                    Text(
                        text = "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này trên thiết bị?",
                        fontFamily = EduMindFontFamily,
                        fontSize = 13.sp,
                        color = Neutral600
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                        }
                    ) {
                        Text(
                            text = "Đăng xuất",
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Error600
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false }
                    ) {
                        Text(
                            text = "Hủy bỏ",
                            fontFamily = EduMindFontFamily,
                            color = Neutral600
                        )
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun ProfileContent(
    user: UserProfile,
    unreadCount: Int,
    onEditProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onPaymentHistoryClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Lớp nền tím chủ đạo loang mờ ảo lan từ đỉnh xuống gần giữa bảng menu chức năng (~480dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFFEDE9FE),       // Tím pastel thương hiệu (#EDE9FE) ở đỉnh
                            0.28f to Color(0xFFF3E8FF),      // Lan tỏa quanh avatar
                            0.60f to Color(0xFFFAF5FF),      // Loang mờ xuống nửa trên bảng menu chức năng
                            1.0f to Color.White              // Hòa tan hoàn toàn ở gần giữa bảng menu
                        )
                    )
                )
        )

        // Lớp nội dung xếp chồng lên trên
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Top Action Bar: Đã bỏ text "Hồ sơ cá nhân", chuông thông báo nằm góc phải
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chuông thông báo đồng bộ
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onNotificationsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Thông báo",
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer(alpha = 0.99f)
                                .drawWithCache {
                                    onDrawWithContent {
                                        drawContent()
                                        drawRect(NebulaBrush, blendMode = BlendMode.SrcIn)
                                    }
                                }
                        )
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 3.dp, y = (-2).dp)
                                    .size(16.dp)
                                    .background(Error600, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    style = TextStyle(
                                        fontFamily = EduMindFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 10.sp,
                                        platformStyle = @Suppress("DEPRECATION") PlatformTextStyle(
                                            includeFontPadding = false
                                        ),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Avatar & Thông tin cá nhân
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                // Khung Avatar với icon camera không bị che cắt
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Avatar 76dp với viền Nebula tròn
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(NebulaBrush, shape = CircleShape)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = user.fullName.take(1).uppercase(),
                                fontFamily = EduMindFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                color = Primary700
                            )
                        }
                    }

                    // Nút camera chỉnh sửa độc lập, không bị clip bởi viền tròn avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Primary700)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { onEditProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = "Đổi ảnh đại diện",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Tên người dùng (Đã bỏ icon tích tím theo yêu cầu)
                Text(
                    text = user.fullName,
                    fontFamily = EduMindFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Neutral900
                )

                // Email
                Text(
                    text = user.email,
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    color = Neutral500
                )

                // Role badge & Join date
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEDE9FE), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFDDD6FE), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Học viên",
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Primary700
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Tham gia: 08/2026",
                            fontFamily = EduMindFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Neutral600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menu Card (nổi bật trên nền loang mờ, bo góc 20dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column {
                    ProfileMenuItem(
                        title = "Chỉnh sửa hồ sơ",
                        subtitle = "Họ và tên, ảnh đại diện",
                        icon = Icons.Outlined.Edit,
                        iconBg = Color(0xFFFAF5FF),
                        iconTint = Primary700,
                        onClick = onEditProfileClick
                    )

                    HorizontalDivider(color = Color(0xFFF5F5F5))

                    ProfileMenuItem(
                        title = "Đổi mật khẩu",
                        subtitle = "Cập nhật mật khẩu bảo vệ tài khoản",
                        icon = Icons.Outlined.Lock,
                        iconBg = Color(0xFFEFF6FF),
                        iconTint = Color(0xFF2563EB),
                        onClick = onChangePasswordClick
                    )

                    HorizontalDivider(color = Color(0xFFF5F5F5))

                    ProfileMenuItem(
                        title = "Lịch sử thanh toán",
                        subtitle = "Hóa đơn VietQR & khóa học đã mua",
                        icon = Icons.Outlined.Receipt,
                        iconBg = Color(0xFFF0FDF4),
                        iconTint = Color(0xFF15803D),
                        onClick = onPaymentHistoryClick
                    )

                    HorizontalDivider(color = Color(0xFFF5F5F5))

                    ProfileMenuItem(
                        title = "Thông báo",
                        subtitle = "Khóa học mới, bài tập, nhắc nhở",
                        icon = Icons.Outlined.Notifications,
                        iconBg = Color(0xFFFFFBEB),
                        iconTint = Color(0xFFB45309),
                        badgeText = "3 mới",
                        onClick = onNotificationsClick
                    )

                    HorizontalDivider(color = Color(0xFFF5F5F5))

                    ProfileMenuItem(
                        title = "Cài đặt",
                        subtitle = "Chế độ tối, chất lượng video, lưu trữ",
                        icon = Icons.Outlined.Settings,
                        iconBg = Color(0xFFF5F5F5),
                        iconTint = Neutral700,
                        onClick = onSettingsClick
                    )

                    HorizontalDivider(color = Color(0xFFF5F5F5))

                    ProfileMenuItem(
                        title = "Đăng xuất",
                        subtitle = "Đăng xuất khỏi thiết bị này",
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        iconBg = Color(0xFFFEF2F2),
                        iconTint = Error600,
                        titleColor = Error600,
                        onClick = onLogoutClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Version
            Text(
                text = "EduMind v1.0.0",
                fontFamily = EduMindFontFamily,
                fontSize = 11.sp,
                color = Neutral400,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    titleColor: Color = Neutral900,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    fontFamily = EduMindFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    fontFamily = EduMindFontFamily,
                    fontSize = 11.sp,
                    color = Neutral400
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEE2E2), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontFamily = EduMindFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Error600
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Neutral400,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun ProfileLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            SkeletonBox(modifier = Modifier.size(36.dp), shape = CircleShape)
        }

        Spacer(modifier = Modifier.height(8.dp))
        SkeletonBox(modifier = Modifier.size(76.dp), shape = CircleShape)
        SkeletonBox(modifier = Modifier.width(140.dp).height(18.dp), shape = RoundedCornerShape(4.dp))
        SkeletonBox(modifier = Modifier.width(180.dp).height(14.dp), shape = RoundedCornerShape(4.dp))

        Spacer(modifier = Modifier.height(16.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(260.dp), shape = RoundedCornerShape(16.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileContentPreview() {
    ProfileContent(
        user = UserProfile(
            id = "1",
            fullName = "Nguyễn Văn Minh",
            email = "minh.nguyen@gmail.com",
            avatarUrl = null,
            role = "student",
            createdAt = "2026-08-15T08:00:00Z"
        ),
        unreadCount = 3,
        onEditProfileClick = {},
        onChangePasswordClick = {},
        onPaymentHistoryClick = {},
        onNotificationsClick = {},
        onSettingsClick = {},
        onLogoutClick = {}
    )
}

