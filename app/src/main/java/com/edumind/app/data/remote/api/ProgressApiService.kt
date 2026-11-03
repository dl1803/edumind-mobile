package com.edumind.app.data.remote.api

import com.edumind.app.data.model.request.SaveProgressRequest
import com.edumind.app.data.model.response.ContinueLearningListResponse
import com.edumind.app.data.model.response.LearningProgressResponse
import com.edumind.app.data.model.response.SaveProgressResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProgressApiService {

    @POST("progress")
    suspend fun saveProgress(
        @Body request: SaveProgressRequest
    ): Response<SaveProgressResponse>

    @GET("progress/overview")
    suspend fun getProgressOverview(): Response<LearningProgressResponse>

    @GET("progress/continue")
    suspend fun getContinueLearning(): Response<ContinueLearningListResponse>
}

