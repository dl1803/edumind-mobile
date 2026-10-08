package com.edumind.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        title = "Trang chủ",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = Screen.Explore.route,
        title = "Khám phá",
        selectedIcon = Icons.Outlined.Explore,
        unselectedIcon = Icons.Outlined.Explore
    ),
    BottomNavItem(
        route = Screen.MyCourses.route,
        title = "Khóa học",
        selectedIcon = Icons.Outlined.AutoStories,
        unselectedIcon = Icons.Outlined.AutoStories
    ),
    BottomNavItem(
        route = Screen.Progress.route,
        title = "Tiến độ",
        selectedIcon = Icons.Outlined.TrendingUp,
        unselectedIcon = Icons.Outlined.TrendingUp
    ),
    BottomNavItem(
        route = Screen.Profile.route,
        title = "Hồ sơ",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)
