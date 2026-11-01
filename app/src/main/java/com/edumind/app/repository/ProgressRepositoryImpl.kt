package com.edumind.app.repository

import com.edumind.app.data.model.request.SaveProgressRequest
import com.edumind.app.data.model.response.LearningProgressResponse
import com.edumind.app.data.remote.api.ProgressApiService
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val progressApiService: ProgressApiService
) : ProgressRepository {

    override suspend fun getProgressOverview(): Result<LearningProgressResponse> {
        return try {
            val response = progressApiService.getProgressOverview()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Lỗi tải tổng quan tiến độ (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Lỗi kết nối mạng khi tải tiến độ. Vui lòng kiểm tra lại.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDailyGoal(minutes: Int): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun saveProgress(
        lessonId: String,
        positionMs: Long,
        totalDurationMs: Long,
        completed: Boolean
    ): Result<Float> {
        return try {
            val positionSec = (positionMs / 1000f).coerceAtLeast(0f)
            val durationSec = (totalDurationMs / 1000f).coerceAtLeast(0f)
            val isCompleted = completed || (durationSec > 0f && positionSec >= durationSec * 0.9f)

            val request = SaveProgressRequest(
                lessonId = lessonId,
                currentPosition = positionSec,
                totalDuration = durationSec,
                completed = isCompleted
            )

            val response = progressApiService.saveProgress(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.courseProgressPercent)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Lỗi lưu tiến độ (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Lỗi mạng khi lưu tiến độ.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

