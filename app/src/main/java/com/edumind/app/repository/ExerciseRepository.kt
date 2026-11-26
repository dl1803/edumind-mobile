package com.edumind.app.repository

import com.edumind.app.data.model.response.ExerciseDto
import com.edumind.app.data.model.response.ExerciseEntry

interface ExerciseRepository {
    suspend fun getLessonExercise(lessonId: String): Result<ExerciseEntry?>
    suspend fun getExercise(exerciseId: String): Result<ExerciseDto>
}
