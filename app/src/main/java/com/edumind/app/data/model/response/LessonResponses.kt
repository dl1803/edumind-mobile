package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

data class TranscriptSegmentDto(
    @SerializedName("start") val start: Float = 0f,
    @SerializedName("end") val end: Float = 0f,
    @SerializedName("text") val text: String = ""
)

data class VideoChapterDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("start_time") val startTime: Float = 0f
)

data class QuizMarkerDto(
    @SerializedName("quiz_id") val quizId: String = "",
    @SerializedName("timestamp") val timestamp: Float = 0f,
    @SerializedName("is_answered") val isAnswered: Boolean = false
)

data class LessonDetailResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("course_id") val courseId: String,
    @SerializedName("chapter_id") val chapterId: String? = null,
    @SerializedName("hls_stream_url") val hlsStreamUrl: String? = null,
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("last_position") val lastPosition: Float = 0f,
    @SerializedName("transcript") val transcript: List<TranscriptSegmentDto> = emptyList(),
    @SerializedName("chapters") val chapters: List<VideoChapterDto> = emptyList(),
    @SerializedName("quiz_markers") val quizMarkers: List<QuizMarkerDto> = emptyList(),
    @SerializedName("next_lesson_id") val nextLessonId: String? = null,
    @SerializedName("prev_lesson_id") val prevLessonId: String? = null
)

// Domain Models
data class TranscriptSegment(
    val startSec: Float,
    val endSec: Float,
    val text: String
)

data class VideoChapter(
    val title: String,
    val startTimeSec: Float
)

data class QuizMarker(
    val quizId: String,
    val timestampSec: Float,
    val isAnswered: Boolean
)

data class LessonDetail(
    val id: String,
    val title: String,
    val description: String?,
    val courseId: String,
    val chapterId: String?,
    val hlsStreamUrl: String,
    val durationSec: Int,
    val lastPositionSec: Float,
    val transcript: List<TranscriptSegment>,
    val chapters: List<VideoChapter>,
    val quizMarkers: List<QuizMarker>,
    val nextLessonId: String?,
    val prevLessonId: String?
) {
    val durationMs: Long get() = durationSec * 1000L
    val lastPositionMs: Long get() = (lastPositionSec * 1000L).toLong()
}

fun LessonDetailResponseDto.toDomain(): LessonDetail {
    return LessonDetail(
        id = id,
        title = title,
        description = description,
        courseId = courseId,
        chapterId = chapterId,
        hlsStreamUrl = hlsStreamUrl ?: "",
        durationSec = duration,
        lastPositionSec = lastPosition,
        transcript = transcript.map { TranscriptSegment(it.start, it.end, it.text) },
        chapters = chapters.map { VideoChapter(it.title, it.startTime) },
        quizMarkers = quizMarkers.map { QuizMarker(it.quizId, it.timestamp, it.isAnswered) },
        nextLessonId = nextLessonId,
        prevLessonId = prevLessonId
    )
}

