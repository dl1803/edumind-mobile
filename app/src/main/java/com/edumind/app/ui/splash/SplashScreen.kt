package com.edumind.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edumind.app.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashScreen(
    onNavigate: (SplashDestination) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    // Animation trạng thái xuất hiện khi mở ứng dụng
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        viewModel.checkInitialRoute()
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    // Lắng nghe sự kiện điều hướng từ ViewModel
    LaunchedEffect(viewModel.destination) {
        viewModel.destination.collectLatest { destination ->
            onNavigate(destination)
        }
    }

    SplashScreenContent(
        alpha = alphaAnim.value,
        scale = scaleAnim.value
    )
}

/**
 * Giao diện Splash Screen với hiệu ứng vầng sáng logo và Shimmer dải màu thương hiệu.
 */
@Composable
fun SplashScreenContent(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    scale: Float = 1f
) {
    // Animation nhịp thở cho vầng sáng logo
    val haloTransition = rememberInfiniteTransition(label = "haloTransition")
    val haloScale by haloTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloScale"
    )
    val haloAlpha by haloTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    // Animation quét Shimmer theo chiều ngang
    val shimmerTransition = rememberInfiniteTransition(label = "shimmerTransition")
    val shimmerProgress by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    // Tính toán độ lệch vị trí quét Shimmer
    val cycleWidthPx = with(LocalDensity.current) { 520.dp.toPx() }
    val shimmerOffset = shimmerProgress * cycleWidthPx

    val appNameBrush = Brush.linearGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFF2563EB),
            0.18f to Color(0xFF7C3AED),
            0.36f to Color(0xFFDB2777),
            0.46f to Color(0xFFF472B6),
            0.50f to Color(0xFFFFFFFF),
            0.54f to Color(0xFFF472B6),
            0.64f to Color(0xFFEC4899),
            0.82f to Color(0xFF7C3AED),
            1.00f to Color(0xFF2563EB)
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + cycleWidthPx, 0f),
        tileMode = TileMode.Repeated
    )

    val badgeIconBrush = Brush.linearGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFF2563EB),
            0.25f to Color(0xFF7C3AED),
            0.47f to Color(0xFFF472B6),
            0.50f to Color(0xFFFFFFFF),
            0.53f to Color(0xFFF472B6),
            0.75f to Color(0xFFEC4899),
            1.00f to Color(0xFF2563EB)
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + cycleWidthPx, 0f),
        tileMode = TileMode.Repeated
    )

    val badgeTextBrush = Brush.linearGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFF6B21A8),
            0.22f to Color(0xFF7C3AED),
            0.42f to Color(0xFFEC4899),
            0.48f to Color(0xFFF9A8D4),
            0.50f to Color(0xFFFFFFFF),
            0.52f to Color(0xFFF9A8D4),
            0.62f to Color(0xFFEC4899),
            0.80f to Color(0xFF7C3AED),
            1.00f to Color(0xFF6B21A8)
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + cycleWidthPx, 0f),
        tileMode = TileMode.Repeated
    )

    val badgeBorderBrush = Brush.linearGradient(
        colorStops = arrayOf(
            0.00f to Color(0x60C084FC),
            0.25f to Color(0x70EC4899),
            0.50f to Color(0xB0FFFFFF),
            0.75f to Color(0x603B82F6),
            1.00f to Color(0x60C084FC)
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + cycleWidthPx, 0f),
        tileMode = TileMode.Repeated
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .alpha(alpha)
            .drawBehind {
                // Ánh sáng loang nền tổng thể
                val centerOffset = Offset(size.width / 2f, size.height * 0.44f)
                val glowRadius = size.width * 0.85f
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0x0E8B5CF6),
                            0.38f to Color(0x06EC4899),
                            0.65f to Color(0x023B82F6),
                            1.00f to Color.Transparent
                        ),
                        center = centerOffset,
                        radius = glowRadius
                    ),
                    center = centerOffset,
                    radius = glowRadius
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .scale(scale)
        ) {
            // Vùng Logo và vầng hào quang
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(210.dp)
                    .drawBehind {
                        val centerPoint = center
                        val maxRadius = 175.dp.toPx() * haloScale

                        // Tầng khuếch tán ngoài
                        drawCircle(
                            brush = Brush.radialGradient(
                                colorStops = arrayOf(
                                    0.00f to Color(0x088B5CF6).copy(alpha = 0.03f * haloAlpha),
                                    0.35f to Color(0x0F8B5CF6).copy(alpha = 0.06f * haloAlpha),
                                    0.58f to Color(0x188B5CF6).copy(alpha = 0.09f * haloAlpha),
                                    0.78f to Color(0x10EC4899).copy(alpha = 0.06f * haloAlpha),
                                    0.90f to Color(0x063B82F6).copy(alpha = 0.02f * haloAlpha),
                                    1.00f to Color.Transparent
                                ),
                                center = centerPoint,
                                radius = maxRadius
                            ),
                            radius = maxRadius,
                            center = centerPoint
                        )

                        // Tầng vầng sáng viền logo
                        val innerRadius = maxRadius * 0.82f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colorStops = arrayOf(
                                    0.00f to Color.Transparent,
                                    0.28f to Color(0x088B5CF6).copy(alpha = 0.03f * haloAlpha),
                                    0.54f to Color(0x268B5CF6).copy(alpha = 0.15f * haloAlpha),
                                    0.74f to Color(0x1AEC4899).copy(alpha = 0.10f * haloAlpha),
                                    0.88f to Color(0x0C3B82F6).copy(alpha = 0.05f * haloAlpha),
                                    1.00f to Color.Transparent
                                ),
                                center = centerPoint,
                                radius = innerRadius
                            ),
                            radius = innerRadius,
                            center = centerPoint
                        )
                    }
            ) {
                // Logo ứng dụng
                Image(
                    painter = painterResource(id = R.drawable.logo_edumind),
                    contentDescription = "EduMind Logo",
                    modifier = Modifier.size(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tên ứng dụng EduMind
            Text(
                text = "EduMind",
                style = TextStyle(
                    brush = appNameBrush,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.2).sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Badge AI-Powered Learning
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 1.dp,
                        brush = badgeBorderBrush,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "✦",
                    style = TextStyle(
                        brush = badgeIconBrush,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "AI-Powered Learning",
                    style = TextStyle(
                        brush = badgeTextBrush,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun SplashScreenPreview() {
    SplashScreenContent()
}
