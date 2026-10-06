package com.edumind.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Sử dụng FontFamily mặc định (hoặc Inter nếu bundle tệp font trong res/font)
val EduMindFontFamily = FontFamily.Default

val Typography = Typography(
    // mobile/h1: 28sp, Bold (700)
    headlineLarge = TextStyle(
        fontFamily = EduMindFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.02).sp
    ),
    // mobile/h2: 22sp, SemiBold (600)
    headlineMedium = TextStyle(
        fontFamily = EduMindFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).sp
    ),
    // mobile/h3: 18sp, SemiBold (600)
    titleLarge = TextStyle(
        fontFamily = EduMindFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    // mobile/h4: 16sp, SemiBold (600)
    titleMedium = TextStyle(
        fontFamily = EduMindFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    // mobile/body-medium: 14sp, Medium (500)
    bodyLarge = TextStyle(
        fontFamily = EduMindFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.01.sp
    ),
    // mobile/body: 14sp, Regular (400)
    bodyMedium = TextStyle(
        fontFamily = EduMindFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.01.sp
    ),
    // mobile/caption: 12sp, Regular (400)
    bodySmall = TextStyle(
        fontFamily = EduMindFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.02.sp
    ),
    // mobile/label: 12sp, Medium (500)
    labelMedium = TextStyle(
        fontFamily = EduMindFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.04.sp
    ),
    // mobile/overline: 10sp, Medium (500)
    labelSmall = TextStyle(
        fontFamily = EduMindFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.08.sp
    )
)
