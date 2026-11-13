package com.edumind.app.ui.player

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.local.datastore.SubtitleFontSize
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.NebulaBlue
import com.edumind.app.ui.theme.NebulaGradient
import com.edumind.app.ui.theme.NebulaPink
import com.edumind.app.ui.theme.NebulaPurple
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral600
import com.edumind.app.ui.theme.Neutral700
import com.edumind.app.ui.theme.Neutral900
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsSheet(
    subtitleEnabled: Boolean,
    subtitleFontSize: SubtitleFontSize,
    onSubtitleEnabledChange: (Boolean) -> Unit,
    onSubtitleFontSizeChange: (SubtitleFontSize) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Neutral900,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Neutral600)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NebulaGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Cài đặt phụ đề",
                            fontFamily = EduMindFontFamily,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tùy chỉnh cỡ chữ và hiển thị lời thoại",
                            fontFamily = EduMindFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Neutral400
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Neutral400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SubtitleToggleCard(
                        subtitleEnabled = subtitleEnabled,
                        onSubtitleEnabledChange = onSubtitleEnabledChange,
                        isLandscape = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(134.dp)
                    )

                    SubtitleFontSizeSliderCard(
                        subtitleFontSize = subtitleFontSize,
                        onSubtitleFontSizeChange = onSubtitleFontSizeChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(134.dp)
                    )
                }
            } else {
                SubtitleToggleCard(
                    subtitleEnabled = subtitleEnabled,
                    onSubtitleEnabledChange = onSubtitleEnabledChange,
                    isLandscape = false,
                    modifier = Modifier.fillMaxWidth()
                )

                SubtitleFontSizeSliderCard(
                    subtitleFontSize = subtitleFontSize,
                    onSubtitleFontSizeChange = onSubtitleFontSizeChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SubtitlePreviewCard(
                subtitleEnabled = subtitleEnabled,
                subtitleFontSize = subtitleFontSize,
                isLandscape = isLandscape,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SubtitleToggleCard(
    subtitleEnabled: Boolean,
    onSubtitleEnabledChange: (Boolean) -> Unit,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLandscape) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Neutral700, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Hiển thị phụ đề",
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tự động hiển thị lời thoại theo tiến độ",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Neutral400
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Switch(
                    checked = subtitleEnabled,
                    onCheckedChange = onSubtitleEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NebulaPurple,
                        uncheckedThumbColor = Neutral400,
                        uncheckedTrackColor = Neutral700,
                        uncheckedBorderColor = Neutral600
                    )
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Neutral700, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hiển thị phụ đề",
                    fontFamily = EduMindFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "Tự động hiển thị lời thoại theo tiến độ",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Neutral400
                )
            }
            Switch(
                checked = subtitleEnabled,
                onCheckedChange = onSubtitleEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NebulaPurple,
                    uncheckedThumbColor = Neutral400,
                    uncheckedTrackColor = Neutral700,
                    uncheckedBorderColor = Neutral600
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubtitleFontSizeSliderCard(
    subtitleFontSize: SubtitleFontSize,
    onSubtitleFontSizeChange: (SubtitleFontSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Neutral700, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Cỡ chữ phụ đề",
            fontFamily = EduMindFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        val currentStep = when (subtitleFontSize) {
            SubtitleFontSize.SMALL -> 0f
            SubtitleFontSize.MEDIUM -> 1f
            SubtitleFontSize.LARGE -> 2f
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "A",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (subtitleFontSize == SubtitleFontSize.SMALL) NebulaBlue else Neutral400
            )

            Slider(
                value = currentStep,
                onValueChange = { floatValue ->
                    val step = floatValue.roundToInt().coerceIn(0, 2)
                    val newSize = when (step) {
                        0 -> SubtitleFontSize.SMALL
                        1 -> SubtitleFontSize.MEDIUM
                        else -> SubtitleFontSize.LARGE
                    }
                    if (newSize != subtitleFontSize) {
                        onSubtitleFontSizeChange(newSize)
                    }
                },
                valueRange = 0f..2f,
                steps = 1,
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, NebulaPurple, CircleShape)
                    )
                },
                track = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Neutral700)
                    ) {
                        val progressFraction = (currentStep / 2f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(NebulaGradient)
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "A",
                fontFamily = EduMindFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (subtitleFontSize == SubtitleFontSize.LARGE) NebulaPink else Neutral400
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Nhỏ",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = if (subtitleFontSize == SubtitleFontSize.SMALL) FontWeight.Bold else FontWeight.Normal,
                color = if (subtitleFontSize == SubtitleFontSize.SMALL) NebulaBlue else Neutral400,
                modifier = Modifier.clickable { onSubtitleFontSizeChange(SubtitleFontSize.SMALL) }
            )
            Text(
                text = "Vừa",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = if (subtitleFontSize == SubtitleFontSize.MEDIUM) FontWeight.Bold else FontWeight.Normal,
                color = if (subtitleFontSize == SubtitleFontSize.MEDIUM) NebulaPurple else Neutral400,
                modifier = Modifier.clickable { onSubtitleFontSizeChange(SubtitleFontSize.MEDIUM) }
            )
            Text(
                text = "Lớn",
                fontFamily = EduMindFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = if (subtitleFontSize == SubtitleFontSize.LARGE) FontWeight.Bold else FontWeight.Normal,
                color = if (subtitleFontSize == SubtitleFontSize.LARGE) NebulaPink else Neutral400,
                modifier = Modifier.clickable { onSubtitleFontSizeChange(SubtitleFontSize.LARGE) }
            )
        }
    }
}

@Composable
private fun SubtitlePreviewCard(
    subtitleEnabled: Boolean,
    subtitleFontSize: SubtitleFontSize,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Neutral700, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "XEM TRƯỚC PHỤ ĐỀ TRÊN VIDEO",
            fontFamily = EduMindFontFamily,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Neutral400,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (isLandscape) 38.dp else 44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF020617))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (subtitleEnabled) {
                val previewSize = if (isLandscape) (subtitleFontSize.textSp + 2).sp else subtitleFontSize.textSp.sp
                val previewLineHeight = if (isLandscape) (subtitleFontSize.lineHeightSp + 4).sp else subtitleFontSize.lineHeightSp.sp
                Text(
                    text = "Chào mừng các bạn đến với bài học EduMind",
                    fontFamily = EduMindFontFamily,
                    fontSize = previewSize,
                    lineHeight = previewLineHeight,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            } else {
                Text(
                    text = "Phụ đề đang tắt",
                    fontFamily = EduMindFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Neutral400,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
