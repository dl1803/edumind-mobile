package com.edumind.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral900
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.edumind.app.ui.auth.forgot.ForgotPasswordScreen
import com.edumind.app.ui.auth.login.LoginScreen
import com.edumind.app.ui.auth.register.RegisterScreen
import com.edumind.app.ui.course.CourseDetailScreen
import com.edumind.app.ui.explore.ExploreScreen
import com.edumind.app.ui.home.HomeScreen
import com.edumind.app.ui.search.SearchScreen
import com.edumind.app.ui.library.MyCoursesScreen
import com.edumind.app.ui.onboarding.OnboardingScreen
import com.edumind.app.ui.player.PlayerScreen
import com.edumind.app.ui.profile.ChangePasswordScreen
import com.edumind.app.ui.profile.EditProfileScreen
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

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize()
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
                        },
                        onNavigateToRegister = {
                            navController.navigate(Screen.Register.route)
                        },
                        onNavigateToForgotPassword = {
                            navController.navigate(Screen.ForgotPassword.route)
                        },
                        onNavigateToOtp = { _ ->
                            navController.navigate(Screen.Register.route)
                        }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(
                        onNavigateBackToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        onCourseClick = { courseId ->
                            navController.navigate(Screen.CourseDetail.createRoute(courseId))
                        },
                        onContinueClick = { _, lessonId ->
                            navController.navigate(Screen.VideoPlayer.createRoute(lessonId))
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        },
                        onAiChatClick = {
                            navController.navigate(Screen.Explore.route)
                        },
                        onNotificationClick = {
                            navController.navigate(Screen.Notification.route)
                        },
                        onAvatarClick = {
                            navController.navigate(Screen.Profile.route)
                        },
                        onSeeAllCoursesClick = {
                            navController.navigate(Screen.MyCourses.route)
                        }
                    )
                }

                composable(Screen.Explore.route) {
                    ExploreScreen(
                        onCourseClick = { courseId ->
                            navController.navigate(Screen.CourseDetail.createRoute(courseId))
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        }
                    )
                }

                composable(Screen.MyCourses.route) {
                    MyCoursesScreen(
                        onCourseClick = { courseId ->
                            navController.navigate(Screen.CourseDetail.createRoute(courseId))
                        },
                        onResumeLearningClick = { _, lessonId ->
                            navController.navigate(Screen.VideoPlayer.createRoute(lessonId))
                        },
                        onExploreClick = {
                            navController.navigate(Screen.Explore.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(Screen.Progress.route) {
                    ProgressScreen(
                        onExploreClick = {
                            navController.navigate(Screen.Explore.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onCourseClick = { courseId ->
                            navController.navigate(Screen.CourseProgressDetail.createRoute(courseId))
                        },
                        onPlayClick = { courseId ->
                            navController.navigate(Screen.CourseProgressDetail.createRoute(courseId))
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onEditProfileClick = {
                            navController.navigate(Screen.EditProfile.route)
                        },
                        onChangePasswordClick = {
                            navController.navigate(Screen.ChangePassword.route)
                        },
                        onNotificationsClick = {
                            navController.navigate(Screen.Notification.route)
                        },
                        onLogoutSuccess = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.EditProfile.route) {
                    EditProfileScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.ChangePassword.route) {
                    ChangePasswordScreen(
                        onBackClick = { navController.popBackStack() },
                        onForgotPasswordClick = {
                            navController.navigate(Screen.ForgotPassword.route)
                        }
                    )
                }

                composable(Screen.Notification.route) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .statusBarsPadding()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Thông báo",
                                fontFamily = EduMindFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Neutral900
                            )
                            Text(
                                text = "Trung tâm thông báo đang được phát triển",
                                fontFamily = EduMindFontFamily,
                                fontSize = 13.sp,
                                color = Neutral500,
                                textAlign = TextAlign.Center
                            )
                            androidx.compose.material3.OutlinedButton(
                                onClick = { navController.popBackStack() }
                            ) {
                                Text(
                                    text = "Quay lại",
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                composable(
                    route = Screen.CourseDetail.route,
                    arguments = listOf(
                        navArgument(Screen.ARG_COURSE_ID) { type = NavType.StringType }
                    )
                ) {
                    CourseDetailScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToPlayer = { lessonId ->
                            navController.navigate(Screen.VideoPlayer.createRoute(lessonId))
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToExplore = {
                            navController.navigate(Screen.Explore.route) {
                                popUpTo(Screen.Home.route)
                                launchSingleTop = true
                            }
                        },
                        onCourseClick = { courseId ->
                            navController.navigate(Screen.CourseDetail.createRoute(courseId))
                        }
                    )
                }

                composable(
                    route = Screen.VideoPlayer.route,
                    arguments = listOf(
                        navArgument(Screen.ARG_LESSON_ID) { type = NavType.StringType }
                    )
                ) {
                    PlayerScreen(
                        onBackClick = { navController.popBackStack() },
                        onNavigateToAiSummary = { },
                        onNavigateToAiChat = { }
                    )
                }

                composable(Screen.PlayerPoc.route) {
                    PlayerScreen(
                        onBackClick = { navController.popBackStack() },
                        onNavigateToAiSummary = { },
                        onNavigateToAiChat = { }
                    )
                }

                composable(
                    route = Screen.CourseProgressDetail.route,
                    arguments = listOf(
                        navArgument(Screen.ARG_COURSE_ID) { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val courseId = backStackEntry.arguments?.getString(Screen.ARG_COURSE_ID) ?: ""
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .statusBarsPadding()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Chi tiết tiến độ khóa học",
                                fontFamily = EduMindFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Neutral900
                            )
                            Text(
                                text = "Mã khóa học: $courseId\n(Giao diện khóa học đã mua, đang học dở)",
                                fontFamily = EduMindFontFamily,
                                fontSize = 13.sp,
                                color = Neutral500,
                                textAlign = TextAlign.Center
                            )
                            androidx.compose.material3.OutlinedButton(
                                onClick = { navController.popBackStack() }
                            ) {
                                Text(
                                    text = "Quay lại",
                                    fontFamily = EduMindFontFamily,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

        // Floating Bottom Navigation Dock nổi đè lên trên ở đáy màn hình
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
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
