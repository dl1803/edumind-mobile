package com.edumind.app.data.remote.api

import com.edumind.app.data.model.request.SubmitExerciseRequest
import com.edumind.app.data.model.response.ExerciseDto
import com.edumind.app.data.model.response.ExerciseResultDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ExerciseApiService {

    @GET("exercises/{exercise_id}")
    suspend fun getExercise(
        @Path("exercise_id") exerciseId: String
    ): Response<ExerciseDto>

    @POST("exercises/{exercise_id}/submit")
    suspend fun submitExercise(
        @Path("exercise_id") exerciseId: String,
        @Body request: SubmitExerciseRequest
    ): Response<ExerciseResultDto>
}
