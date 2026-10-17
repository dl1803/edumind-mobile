package com.edumind.app.ui.explore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.R
import com.edumind.app.ui.explore.SortOption
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.NebulaBrush
import com.edumind.app.ui.theme.Neutral100
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral50
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Primary200
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary500
import com.edumind.app.ui.theme.Primary700

@Composable
fun ExploreFilterBar(
    totalCount: Int,
    activeFilterCount: Int,
    currentSort: SortOption,
    onOpenFilterSheet: () -> Unit,
    onToggleSort: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$totalCount khóa học",
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Neutral500
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isFilterActive = activeFilterCount > 0
            val filterBorderColor = if (isFilterActive) Primary500 else Neutral200
            val filterBgColor = if (isFilterActive) Primary50 else Neutral50
            val filterTextColor = if (isFilterActive) Primary700 else Neutral700

            Row(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(filterBgColor)
                    .border(1.dp, filterBorderColor, RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenFilterSheet)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = "Bộ lọc",
                    tint = filterTextColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Bộ lọc",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = if (isFilterActive) FontWeight.Bold else FontWeight.Medium,
                    color = filterTextColor
                )

                if (isFilterActive) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(NebulaBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeFilterCount.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Neutral50)
                    .border(1.dp, Neutral200, RoundedCornerShape(12.dp))
                    .clickable(onClick = onToggleSort)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrows_sort),
                    contentDescription = null,
                    tint = Neutral600,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (currentSort) {
                        SortOption.NEWEST -> "Mới nhất"
                        SortOption.POPULAR -> "Phổ biến"
                        SortOption.PRICE_ASC -> "Giá tăng"
                        SortOption.PRICE_DESC -> "Giá giảm"
                    },
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Neutral700
                )
            }
        }
    }
}
