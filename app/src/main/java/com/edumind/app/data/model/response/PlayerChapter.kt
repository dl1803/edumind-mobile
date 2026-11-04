package com.edumind.app.data.model.response

import java.util.Locale

/**
 * Mô hình dữ liệu biểu diễn phân đoạn mốc thời gian trong video hoặc một bài giảng trong chương.
 */
data class PlayerChapter(
    val id: String,
    val title: String,
    val startMs: Long?,
    val durationText: String,
    val lessonId: String,
    val isCurrent: Boolean = false,
    val isCompleted: Boolean = false,
    val isLocked: Boolean = false,
    val groupTitle: String? = null
) {
    val isTimestampMarker: Boolean get() = startMs != null

    companion object {
        fun formatMs(ms: Long): String {
            val totalSec = (ms.coerceAtLeast(0L) / 1000).toInt()
            val minutes = totalSec / 60
            val seconds = totalSec % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}

/**
 * Chuyển đổi danh sách mốc phân đoạn trong video (VideoChapter) sang danh sách PlayerChapter.
 */
fun List<VideoChapter>.toPlayerChapters(
    currentLessonId: String,
    groupTitle: String? = "Phân đoạn bài hiện tại"
): List<PlayerChapter> {
    return mapIndexed { index, chapter ->
        val startMs = (chapter.startTimeSec * 1000L).toLong()
        PlayerChapter(
            id = "${currentLessonId}_seg_$index",
            title = chapter.title,
            startMs = startMs,
            durationText = PlayerChapter.formatMs(startMs),
            lessonId = currentLessonId,
            groupTitle = groupTitle
        )
    }
}

/**
 * Chuyển đổi danh sách chương mục khóa học (CourseSection) sang danh sách PlayerChapter phân nhóm.
 */
fun List<CourseSection>.toPlayerChapters(
    currentLessonId: String
): List<PlayerChapter> {
    return flatMap { section ->
        section.lessons.map { lesson ->
            PlayerChapter(
                id = lesson.id,
                title = lesson.title,
                startMs = null,
                durationText = lesson.durationText,
                lessonId = lesson.id,
                isCurrent = lesson.id == currentLessonId,
                isCompleted = lesson.isCompleted,
                isLocked = lesson.isLocked && !lesson.isCompleted,
                groupTitle = section.title
            )
        }
    }
}

