package com.edumind.app.ui.navigation

/**
 * Quản lý tập trung toàn bộ Route trong hệ thống Mobile App EduMind.
 * Mỗi Object đại diện cho một màn hình hoặc điểm đến trong NavGraph.
 */
sealed class Screen(val route: String) {
    // --- Auth Flow (Không hiện Bottom Nav Bar) ---
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")

    // --- Main Flow (5 Tab chính có Floating Glass Dock) ---
    data object Home : Screen("home")
    data object Explore : Screen("explore")
    data object MyCourses : Screen("my_courses")
    data object Progress : Screen("progress")
    data object Profile : Screen("profile")

    // --- Chi tiết & Tính năng phụ (Ẩn Bottom Nav Bar) ---
    data object CourseDetail : Screen("course_detail/{$ARG_COURSE_ID}") {
        fun createRoute(courseId: String): String = "course_detail/$courseId"
    }
    data object Search : Screen("search")
    data object VideoPlayer : Screen("video_player/{$ARG_LESSON_ID}") {
        fun createRoute(lessonId: String): String = "video_player/$lessonId"
    }
    data object Notification : Screen("notification")

    companion object {
        const val ARG_COURSE_ID = "courseId"
        const val ARG_LESSON_ID = "lessonId"
    }
}
