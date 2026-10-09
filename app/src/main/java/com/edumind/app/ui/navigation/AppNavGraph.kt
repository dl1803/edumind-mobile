package com.edumind.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.edumind.app.ui.auth.LoginScreen
import com.edumind.app.ui.explore.ExploreScreen
import com.edumind.app.ui.home.HomeScreen
import com.edumind.app.ui.library.MyCoursesScreen
import com.edumind.app.ui.onboarding.OnboardingScreen
import com.edumind.app.ui.profile.ProfileScreen
import com.edumind.app.ui.progress.ProgressScreen
import com.edumind.app.ui.splash.SplashDestination
import com.edumind.app.ui.splash.SplashScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Chỉ hiển thị Bottom Navigation Bar khi route hiện tại nằm trong 5 tab chính
    val shouldShowBottomBar = bottomNavItems.any { it.route == currentRoute }

    // Xử lý phím Back: Nếu đang ở tab phụ (Explore, Profile,...) thì quay về Home
    if (shouldShowBottomBar && currentRoute != Screen.Home.route) {
        BackHandler {
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // Ghi chú: Nội dung màn hình trải toàn màn hình, Floating Dock nổi phía trên
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (shouldShowBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                // --- Auth Flow ---
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onNavigate = { destination ->
                            when (destination) {
                                SplashDestination.Home -> {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                                SplashDestination.Onboarding -> {
                                    navController.navigate(Screen.Onboarding.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                                SplashDestination.Login -> {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            }
                        }
                    )
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                // --- Main Flow (5 Tabs) ---
                composable(Screen.Home.route) {
                    HomeScreen()
                }

                composable(Screen.Explore.route) {
                    ExploreScreen()
                }

                composable(Screen.MyCourses.route) {
                    MyCoursesScreen()
                }

                composable(Screen.Progress.route) {
                    ProgressScreen()
                }

                composable(Screen.Profile.route) {
                    ProfileScreen()
                }
            }
        }
    }
}
