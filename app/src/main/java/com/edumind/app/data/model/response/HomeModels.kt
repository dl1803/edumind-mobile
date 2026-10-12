package com.edumind.app.data.model.response

/**
 * Model khóa học nổi bật trên Banner
 */
data class FeaturedCourse(
    val id: String,
    val title: String,
    val subtitle: String,
    val bannerUrl: String,
    val priceVnd: Long,
    val originalPriceVnd: Long? = null,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val badgeText: String = "Nổi bật nhất tuần",
    val hasAiAssistant: Boolean = true
)

/**
 * Model mục học đang tiến hành
 */
data class ContinueLearningItem(
    val courseId: String,
    val lessonId: String,
    val courseTitle: String,
    val currentLessonTitle: String,
    val thumbnailUrl: String,
    val progressPercent: Int,
    val remainingTimeText: String
)

/**
 * Model tóm tắt khóa học cho lưới hiển thị
 */
data class CourseSummary(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val instructorName: String,
    val instructorAvatarUrl: String? = null,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val priceVnd: Long = 0L // 0L tương đương "Miễn phí"
)
