package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

enum class LessonType {
    VIDEO,
    QUIZ
}

data class CourseLesson(
    val id: String,
    val title: String,
    val durationText: String,
    val type: LessonType = LessonType.VIDEO,
    val isFreePreview: Boolean = false,
    val isCompleted: Boolean = false,
    val isCurrent: Boolean = false
)

data class CourseSection(
    val id: String,
    val title: String,
    val durationSummary: String,
    val lessons: List<CourseLesson>
)

data class InstructorSummary(
    val id: String,
    val name: String,
    val title: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val rating: Float = 4.9f,
    val courseCount: Int = 12,
    val studentCount: Int = 1520
)

data class CourseDetail(
    val id: String,
    val title: String,
    val categoryName: String,
    val thumbnailUrl: String,
    val priceVnd: Long,
    val originalPriceVnd: Long? = null,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val studentCount: Int = 0,
    val totalHours: Int = 0,
    val description: String,
    val keyFeatures: List<String> = emptyList(),
    val learningObjectives: List<String> = emptyList(),
    val requirements: List<String> = emptyList(),
    val instructor: InstructorSummary,
    val sections: List<CourseSection> = emptyList(),
    val isEnrolled: Boolean = false,
    val progressPercent: Int = 0,
    val lastLessonId: String? = null,
    val lastLessonTitle: String? = null
)

data class EnrollmentResponseDto(
    @SerializedName("enrollment_id")
    val enrollmentId: String = "",
    @SerializedName("course_id")
    val courseId: String = "",
    @SerializedName("first_lesson_id")
    val firstLessonId: String? = null,
    @SerializedName("enrolled_at")
    val enrolledAt: String? = null
)

typealias EnrollResponseDto = EnrollmentResponseDto

data class MyCoursesResponseDto(
    @SerializedName("data")
    val data: List<EnrolledCourseDto> = emptyList()
)

data class EnrolledCourseDto(
    @SerializedName("course_id")
    val courseId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerializedName("instructor_name")
    val instructorName: String? = null,
    @SerializedName("progress_percent")
    val progressPercent: Float = 0f,
    @SerializedName("next_lesson_id")
    val nextLessonId: String? = null,
    @SerializedName("next_lesson_title")
    val nextLessonTitle: String? = null,
    @SerializedName("last_position")
    val lastPosition: Float? = null,
    @SerializedName("category_name")
    val categoryName: String? = null,
    @SerializedName("enrolled_at")
    val enrolledAt: String? = null,
    @SerializedName("completed_at")
    val completedAt: String? = null,
    @SerializedName("total_lessons")
    val totalLessons: Int? = null,
    @SerializedName("completed_lessons")
    val completedLessons: Int? = null
)

typealias MyCourseItemDto = EnrolledCourseDto

fun EnrolledCourseDto.toDomain(): MyCourse {
    val progress = progressPercent.toInt().coerceIn(0, 100)
    return MyCourse(
        id = courseId,
        title = title,
        thumbnailUrl = thumbnailUrl ?: "",
        instructorName = instructorName ?: "EduMind Giảng viên",
        progressPercent = progress,
        totalLessons = totalLessons ?: 20,
        completedLessons = completedLessons ?: if (progress == 100) 20 else (progress * 20 / 100),
        nextLessonTitle = nextLessonTitle ?: "",
        nextLessonId = nextLessonId ?: "",
        categoryName = categoryName ?: "Công nghệ thông tin",
        rating = 4.8f,
        reviewCount = 120
    )
}

