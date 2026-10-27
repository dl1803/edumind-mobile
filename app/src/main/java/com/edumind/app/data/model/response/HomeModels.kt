package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Model khóa học nổi bật trên Banner
 */
data class FeaturedCourse(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("subtitle")
    val subtitle: String = "",
    @SerializedName("banner_url")
    val bannerUrl: String,
    @SerializedName("price")
    val priceVnd: Long = 0L,
    @SerializedName("original_price")
    val originalPriceVnd: Long? = null,
    @SerializedName("rating_avg")
    val rating: Float = 0f,
    @SerializedName("rating_count")
    val reviewCount: Int = 0,
    val badgeText: String = "Nổi bật nhất tuần",
    val hasAiAssistant: Boolean = true
)

/**
 * Model mục học đang tiến hành
 */
data class ContinueLearningItem(
    @SerializedName("course_id")
    val courseId: String,
    @SerializedName("next_lesson_id")
    val lessonId: String,
    @SerializedName("course_title")
    val courseTitle: String,
    @SerializedName("next_lesson_title")
    val currentLessonTitle: String,
    @SerializedName("course_thumbnail_url")
    val thumbnailUrl: String,
    @SerializedName("progress_percent")
    val progressPercent: Int = 0,
    val remainingTimeText: String = ""
)

/**
 * Model tóm tắt khóa học cho lưới hiển thị
 */
data class CourseSummary(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String,
    @SerializedName("instructor_name")
    val instructorName: String,
    @SerializedName("instructor_avatar_url")
    val instructorAvatarUrl: String? = null,
    @SerializedName("rating_avg")
    val rating: Float = 0f,
    @SerializedName("rating_count")
    val reviewCount: Int = 0,
    @SerializedName("price")
    val priceVnd: Long = 0L
)
