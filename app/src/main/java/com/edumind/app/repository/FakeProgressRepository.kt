package com.edumind.app.repository

import com.edumind.app.data.model.response.CourseProgress
import com.edumind.app.data.model.response.DailyStudyTime
import com.edumind.app.data.model.response.LearningProgressResponse
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeProgressRepository @Inject constructor() : ProgressRepository {

    var shouldReturnEmpty: Boolean = false
    var shouldReturnError: Boolean = false
    private var currentGoalMinutes: Int = 45

    override suspend fun getProgressOverview(): Result<LearningProgressResponse> {
        delay(300)

        if (shouldReturnError) {
            return Result.failure(Exception("Không thể tải dữ liệu tiến độ học tập. Vui lòng thử lại."))
        }

        if (shouldReturnEmpty) {
            return Result.success(
                LearningProgressResponse(
                    totalEnrolledCourses = 0,
                    totalHoursWatched = 0f,
                    totalLessonsCompleted = 0,
                    courses = emptyList(),
                    streakDays = 0,
                    totalExercisesPassed = 0,
                    totalExercises = 0,
                    dailyGoalMinutes = currentGoalMinutes,
                    weeklyStudy = emptyList()
                )
            )
        }

        return Result.success(
            LearningProgressResponse(
                totalEnrolledCourses = 2,
                totalHoursWatched = 18.5f,
                totalLessonsCompleted = 2,
                courses = listOf(
                    CourseProgress(
                        courseId = "c_python_01",
                        courseTitle = "Nhập môn Lập trình Python",
                        thumbnailUrl = "file:///android_asset/courses/course_python.jpg",
                        progressPercent = 65f,
                        completedLessons = 13,
                        totalLessons = 20
                    ),
                    CourseProgress(
                        courseId = "c_web_03",
                        courseTitle = "Lập trình Web Frontend Hiện đại",
                        thumbnailUrl = "file:///android_asset/courses/course_web.jpg",
                        progressPercent = 30f,
                        completedLessons = 6,
                        totalLessons = 20
                    )
                ),
                streakDays = 7,
                totalExercisesPassed = 24,
                totalExercises = 28,
                dailyGoalMinutes = currentGoalMinutes,
                weeklyStudy = listOf(
                    DailyStudyTime(dayOfWeek = "T2", minutes = 50),
                    DailyStudyTime(dayOfWeek = "T3", minutes = 45),
                    DailyStudyTime(dayOfWeek = "T4", minutes = 70),
                    DailyStudyTime(dayOfWeek = "T5", minutes = 30),
                    DailyStudyTime(dayOfWeek = "T6", minutes = 60),
                    DailyStudyTime(dayOfWeek = "T7", minutes = 90, isToday = true),
                    DailyStudyTime(dayOfWeek = "CN", minutes = 0)
                )
            )
        )
    }

    override suspend fun updateDailyGoal(minutes: Int): Result<Unit> {
        delay(200)
        currentGoalMinutes = minutes
        return Result.success(Unit)
    }
}
