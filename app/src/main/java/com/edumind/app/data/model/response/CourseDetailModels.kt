package com.edumind.app.data.model.response

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
