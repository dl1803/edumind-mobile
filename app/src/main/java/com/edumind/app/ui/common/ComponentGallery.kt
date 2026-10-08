package com.edumind.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.edumind.app.ui.theme.EduMindTheme

@Preview(showBackground = true, heightDp = 1600)
@Composable
fun ComponentGalleryPreview() {
    EduMindTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("1. Buttons", style = MaterialTheme.typography.titleMedium)
            EduMindButton("Primary Nebula", onClick = {}, style = EduMindButtonStyle.Primary)
            EduMindButton("Outlined", onClick = {}, style = EduMindButtonStyle.Outlined)
            EduMindButton("Destructive", onClick = {}, style = EduMindButtonStyle.Destructive)
            EduMindButton("Loading...", onClick = {}, loading = true)
            EduMindButton("Disabled", onClick = {}, enabled = false)

            Text("2. TextFields", style = MaterialTheme.typography.titleMedium)
            EduMindTextField(value = "Nguyễn Văn Ánh", onValueChange = {}, label = "Họ và tên")
            EduMindTextField(value = "", onValueChange = {}, placeholder = "Nhập email của bạn...")
            EduMindTextField(value = "abc", onValueChange = {}, errorText = "Email không hợp lệ")

            Text("3. Offline Banner", style = MaterialTheme.typography.titleMedium)
            OfflineBanner(isOffline = true)

            Text("4. Skeleton Shimmer", style = MaterialTheme.typography.titleMedium)
            SkeletonBox(modifier = Modifier.size(120.dp, 60.dp))
            SkeletonLines(lines = 3)

            Text("5. Error View", style = MaterialTheme.typography.titleMedium)
            ErrorView(message = "Không thể kết nối đến máy chủ", onRetry = {})

            Text("6. Empty State", style = MaterialTheme.typography.titleMedium)
            EmptyStateView(title = "Chưa có dữ liệu", message = "Dữ liệu sẽ hiển thị tại đây khi có cập nhật mới.")
        }
    }
}
