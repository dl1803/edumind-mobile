package com.edumind.app.ui.exercise

import com.edumind.app.data.model.response.ExerciseDto
import com.edumind.app.data.model.response.ExerciseResultDto
import javax.inject.Inject
import javax.inject.Singleton

data class ExerciseSubmission(
    val exercise: ExerciseDto,
    val answers: Map<String, ExerciseAnswer>,
    val timeSpentSeconds: Int,
    val result: ExerciseResultDto
)

@Singleton
class ExerciseResultStore @Inject constructor() {

    private val submissions = mutableMapOf<String, ExerciseSubmission>()

    @Synchronized
    fun put(exerciseId: String, submission: ExerciseSubmission) {
        submissions[exerciseId] = submission
    }

    @Synchronized
    fun get(exerciseId: String): ExerciseSubmission? = submissions[exerciseId]
}
