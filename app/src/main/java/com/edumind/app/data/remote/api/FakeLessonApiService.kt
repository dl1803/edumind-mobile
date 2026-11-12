package com.edumind.app.data.remote.api

import com.edumind.app.data.model.response.LessonDetailResponseDto
import com.edumind.app.util.Constants
import retrofit2.Response

class FakeLessonApiService : LessonApiService {

    override suspend fun getLessonDetail(lessonId: String): Response<LessonDetailResponseDto> {
        return Response.success(
            LessonDetailResponseDto(
                id = lessonId,
                title = "Bài giảng mẫu",
                courseId = "fake_course",
                hlsStreamUrl = Constants.SAMPLE_HLS_URL,
                duration = 765
            )
        )
    }
}

