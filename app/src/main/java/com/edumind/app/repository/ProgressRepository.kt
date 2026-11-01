package com.edumind.app.repository

import com.edumind.app.data.model.response.LearningProgressResponse

interface ProgressRepository {
    suspend fun getProgressOverview(): Result<LearningProgressResponse>
    suspend fun updateDailyGoal(minutes: Int): Result<Unit>
    suspend fun saveProgress(
        lessonId: String,
        positionMs: Long,
        totalDurationMs: Long,
        completed: Boolean = false
    ): Result<Float>
}
