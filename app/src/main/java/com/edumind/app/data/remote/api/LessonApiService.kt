package com.edumind.app.data.remote.api

import com.edumind.app.data.model.response.LessonDetailResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LessonApiService {

    @GET("lessons/{lesson_id}")
    suspend fun getLessonDetail(
        @Path("lesson_id") lessonId: String
    ): Response<LessonDetailResponseDto>
}

