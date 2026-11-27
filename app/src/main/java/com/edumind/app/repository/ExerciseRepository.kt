package com.edumind.app.repository

import com.edumind.app.data.model.request.SubmitExerciseRequest
import com.edumind.app.data.model.response.ExerciseDto
import com.edumind.app.data.model.response.ExerciseEntry
import com.edumind.app.data.model.response.ExerciseResultDto

interface ExerciseRepository {
    suspend fun getLessonExercise(lessonId: String): Result<ExerciseEntry?>
    suspend fun getExercise(exerciseId: String): Result<ExerciseDto>
    suspend fun submitExercise(
        exerciseId: String,
        request: SubmitExerciseRequest
    ): Result<ExerciseResultDto>
}
