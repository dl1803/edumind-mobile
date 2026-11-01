package com.edumind.app.repository

import com.edumind.app.data.model.response.LessonDetail
import com.edumind.app.util.Constants
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeLessonRepository @Inject constructor() : LessonRepository {

    override suspend fun getLessonDetail(lessonId: String): Result<LessonDetail> {
        delay(300)
        return Result.success(
            LessonDetail(
                id = lessonId,
                title = "Bài 03: Biến & Kiểu dữ liệu cơ bản",
                description = "Tìm hiểu cách khai báo biến, số nguyên, số thực và chuỗi ký tự trong Python.",
                courseId = "c_python_01",
                chapterId = "ch_01",
                hlsStreamUrl = Constants.SAMPLE_HLS_URL,
                durationSec = 765,
                lastPositionSec = 0f,
                transcript = emptyList(),
                chapters = emptyList(),
                quizMarkers = emptyList(),
                nextLessonId = null,
                prevLessonId = null
            )
        )
    }
}

