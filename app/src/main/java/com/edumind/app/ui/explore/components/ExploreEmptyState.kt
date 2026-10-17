package com.edumind.app.ui.explore.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.common.CircularIllustration
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral900
import com.edumind.app.ui.theme.Primary400
import com.edumind.app.ui.theme.Primary700

@Composable
fun ExploreEmptyState(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularIllustration(
            icon = Icons.Default.Search,
            iconTint = Primary400
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Không tìm thấy khóa học nào",
            fontFamily = EduMindFontFamily,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Neutral900,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Không có khóa học phù hợp với bộ lọc hiện tại. Thử thay đổi bộ lọc hoặc danh mục khác.",
            fontFamily = EduMindFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Neutral500,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClearFilters,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, Primary700),
            modifier = Modifier.height(40.dp)
        ) {
            Text(
                text = "Xóa bộ lọc",
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Primary700
            )
        }
    }
}
