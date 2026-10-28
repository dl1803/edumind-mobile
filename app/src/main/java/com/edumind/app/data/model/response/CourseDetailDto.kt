package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

data class CourseDetailResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerializedName("price")
    val price: Long = 0L,
    @SerializedName("original_price")
    val originalPrice: Long? = null,
    @SerializedName("rating_avg")
    val ratingAvg: Float? = null,
    @SerializedName("rating_count")
    val ratingCount: Int? = null,
    @SerializedName("enrolled_count")
    val enrolledCount: Int = 0,
    @SerializedName("category")
    val category: CategoryDto? = null,
    @SerializedName("instructor")
    val instructor: InstructorProfileDto? = null,
    @SerializedName("is_enrolled")
    val isEnrolled: Boolean = false,
    @SerializedName("progress_percent")
    val progressPercent: Float? = null,
    @SerializedName("chapters")
    val chapters: List<ChapterDto> = emptyList(),
    @SerializedName("total_lessons")
    val totalLessons: Int = 0,
    @SerializedName("total_duration")
    val totalDuration: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class CategoryDto(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = ""
)

data class InstructorProfileDto(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null
)

data class ChapterDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("order")
    val order: Int = 0,
    @SerializedName("lessons")
    val lessons: List<LessonSummaryDto> = emptyList()
)

data class LessonSummaryDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("order")
    val order: Int = 0,
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("is_locked")
    val isLocked: Boolean = false,
    @SerializedName("is_completed")
    val isCompleted: Boolean? = null,
    @SerializedName("has_quiz")
    val hasQuiz: Boolean = false,
    @SerializedName("has_exercise")
    val hasExercise: Boolean = false
)

fun CourseDetailResponse.toDomain(): CourseDetail {
    val durationHours = totalDuration / 3600
    return CourseDetail(
        id = id,
        title = title,
        categoryName = category?.name ?: "Tổng hợp",
        thumbnailUrl = thumbnailUrl ?: "",
        priceVnd = price,
        originalPriceVnd = originalPrice,
        rating = ratingAvg ?: 0f,
        reviewCount = ratingCount ?: 0,
        studentCount = enrolledCount,
        totalHours = durationHours.coerceAtLeast(1),
        description = description ?: "",
        keyFeatures = listOf(
            "Thời lượng học thực chiến $totalLessons bài giảng",
            "Trợ giảng AI hỗ trợ giải đáp 24/7",
            "Cấp chứng chỉ hoàn thành khóa học"
        ),
        learningObjectives = listOf(
            "Nắm vững tư duy và kiến thức nền tảng vững chắc",
            "Tự xây dựng các dự án thực tế phục vụ công việc",
            "Tối ưu mã nguồn chuẩn Clean Architecture"
        ),
        requirements = listOf("Máy tính có kết nối Internet", "Tư duy logic cơ bản"),
        instructor = InstructorSummary(
            id = instructor?.id ?: "",
            name = instructor?.name ?: "Giảng viên EduMind",
            title = instructor?.title ?: "Giảng viên chuyên môn",
            avatarUrl = instructor?.avatarUrl,
            bio = instructor?.bio,
            rating = ratingAvg ?: 4.9f
        ),
        sections = chapters.map { ch ->
            CourseSection(
                id = ch.id,
                title = ch.title,
                durationSummary = "${ch.lessons.size} bài học",
                lessons = ch.lessons.map { l ->
                    val mins = l.duration / 60
                    val secs = l.duration % 60
                    CourseLesson(
                        id = l.id,
                        title = l.title,
                        durationText = String.format("%02d:%02d", mins, secs),
                        type = if (l.hasQuiz) LessonType.QUIZ else LessonType.VIDEO,
                        isFreePreview = !l.isLocked,
                        isCompleted = l.isCompleted ?: false,
                        isCurrent = false
                    )
                }
            )
        },
        isEnrolled = isEnrolled,
        progressPercent = progressPercent?.toInt() ?: 0
    )
}
