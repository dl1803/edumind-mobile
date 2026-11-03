package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

data class SaveProgressResponse(
    @SerializedName("message")
    val message: String = "OK",

    @SerializedName("course_progress_percent")
    val courseProgressPercent: Float = 0f
)

data class ContinueLearningDto(
    @SerializedName("course_id")
    val courseId: String = "",
    @SerializedName("course_title")
    val courseTitle: String = "",
    @SerializedName("course_thumbnail_url")
    val courseThumbnailUrl: String? = null,
    @SerializedName("next_lesson_id")
    val nextLessonId: String = "",
    @SerializedName("next_lesson_title")
    val nextLessonTitle: String = "",
    @SerializedName("last_position")
    val lastPosition: Float = 0f,
    @SerializedName("progress_percent")
    val progressPercent: Float = 0f,
    @SerializedName("last_watched_at")
    val lastWatchedAt: String? = null
)

data class ContinueLearningListResponse(
    @SerializedName("data")
    val data: List<ContinueLearningDto> = emptyList()
)

fun ContinueLearningDto.toDomain(): ContinueLearningItem {
    return ContinueLearningItem(
        courseId = courseId,
        lessonId = nextLessonId,
        courseTitle = courseTitle,
        currentLessonTitle = nextLessonTitle,
        thumbnailUrl = courseThumbnailUrl ?: "",
        progressPercent = progressPercent.toInt().coerceIn(0, 100),
        remainingTimeText = ""
    )
}

