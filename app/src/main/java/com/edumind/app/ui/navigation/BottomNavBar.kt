package com.edumind.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.Neutral400
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Primary600
import com.edumind.app.ui.theme.Primary700

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Floating Dock bar
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color(0x337C3AED),
                    ambientColor = Color(0x1F000000)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White.copy(alpha = 0.92f))
                .border(
                    width = 1.dp,
                    color = Color(0xFF8B5CF6).copy(alpha = 0.20f),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) Primary700 else Neutral400,
                    animationSpec = tween(durationMillis = 200),
                    label = "iconColor"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Primary700 else Neutral500,
                    animationSpec = tween(durationMillis = 200),
                    label = "textColor"
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                onNavigateToRoute(item.route)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Hộp chứa Icon với gradient highlight khi active
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color(0xFF3B82F6).copy(alpha = 0.12f),
                                                    Color(0xFF8B5CF6).copy(alpha = 0.16f),
                                                    Color(0xFFEC4899).copy(alpha = 0.12f)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Primary600.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 3.dp)
                                } else {
                                    Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = item.title,
                        color = textColor,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
