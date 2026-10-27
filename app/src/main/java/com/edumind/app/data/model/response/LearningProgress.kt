package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

data class LearningProgressResponse(
    @SerializedName("total_enrolled_courses")
    val totalEnrolledCourses: Int = 0,

    @SerializedName("total_hours_watched")
    val totalHoursWatched: Float = 0f,

    @SerializedName("total_lessons_completed")
    val totalLessonsCompleted: Int = 0,

    @SerializedName("courses")
    val courses: List<CourseProgress> = emptyList(),

    val streakDays: Int = 0,
    val totalExercisesPassed: Int = 0,
    val totalExercises: Int = 0,
    val dailyGoalMinutes: Int = 45,
    val weeklyStudy: List<DailyStudyTime> = emptyList()
)

data class CourseProgress(
    @SerializedName("course_id")
    val courseId: String,

    @SerializedName("course_title")
    val courseTitle: String,

    @SerializedName("thumbnail_url")
    val thumbnailUrl: String,

    @SerializedName("progress_percent")
    val progressPercent: Float,

    @SerializedName("completed_lessons")
    val completedLessons: Int,

    @SerializedName("total_lessons")
    val totalLessons: Int
)

data class DailyStudyTime(
    val dayOfWeek: String,
    val minutes: Int,
    val isToday: Boolean = false
)
