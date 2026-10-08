package com.edumind.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 1. PRIMARY BRAND COLORS (Royal Purple Scale)
val Primary950 = Color(0xFF2E1065)
val Primary900 = Color(0xFF581C87)
val Primary800 = Color(0xFF6B21A8)
val Primary700 = Color(0xFF7C3AED) // Màu nhận diện cốt lõi (Core Primary)
val Primary600 = Color(0xFF8B5CF6) // Hover, border focus, icon active
val Primary500 = Color(0xFFA855F7)
val Primary400 = Color(0xFFC084FC)
val Primary300 = Color(0xFFD8B4FE)
val Primary200 = Color(0xFFE9D5FF)
val Primary100 = Color(0xFFF3E8FF) // Badge background, bubble chat học viên
val Primary50  = Color(0xFFFAF5FF) // Nền active menu, hover row

// 2. BRAND GRADIENT (Nebula Transition)
val NebulaBlue   = Color(0xFF3B82F6)
val NebulaPurple = Color(0xFF8B5CF6)
val NebulaPink   = Color(0xFFEC4899)

// Dải Gradient Nebula dùng cho nút CTA chính và thanh tiến độ
val NebulaGradient = Brush.linearGradient(
    colors = listOf(NebulaBlue, NebulaPurple, NebulaPink)
)

val NebulaBrush = Brush.horizontalGradient(
    colors = listOf(NebulaBlue, NebulaPurple, NebulaPink)
)

// Dải Gradient khi hover/pressed
val NebulaGradientHover = Brush.linearGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED), Color(0xFFDB2777))
)

// 3. SEMANTIC COLORS (Trạng thái hệ thống)
val Success700 = Color(0xFF15803D)
val Success600 = Color(0xFF16A34A)
val Success100 = Color(0xFFDCFCE7)

val Warning700 = Color(0xFFA16207)
val Warning600 = Color(0xFFCA8A04)
val Warning100 = Color(0xFFFEF9C3)

val Error700   = Color(0xFFB91C1C)
val Error600   = Color(0xFFDC2626)
val Error100   = Color(0xFFFEE2E2)

val Info600    = Color(0xFF2563EB)
val Info100    = Color(0xFFDBEAFE)

// 4. NEUTRAL / GRAY SCALE
val Neutral950 = Color(0xFF0A0A0A) // Body text chính
val Neutral900 = Color(0xFF171717)
val Neutral800 = Color(0xFF262626)
val Neutral700 = Color(0xFF404040) // Subtitle, text phụ
val Neutral600 = Color(0xFF525252)
val Neutral500 = Color(0xFF737373) // Caption, time text
val Neutral400 = Color(0xFFA3A3A3) // Placeholder
val Neutral300 = Color(0xFFD4D4D4) // Border default
val Neutral200 = Color(0xFFE5E5E5) // Border card, divider
val Neutral100 = Color(0xFFF5F5F5) // Background input
val Neutral50  = Color(0xFFFAFAFA)
val White      = Color(0xFFFFFFFF)

// 5. CANVAS & SPECIAL BACKGROUNDS
val BgCanvasMobile = Color(0xFFFAFAFA)
val BgChatAi       = Color(0xFFF5F3FF)
val BgOverlay      = Color(0x80000000) // 50% opacity black
