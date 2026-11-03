package com.edumind.app.repository

import com.edumind.app.data.local.db.dao.PlaybackPositionDao
import com.edumind.app.data.local.db.entity.PlaybackPositionEntity
import com.edumind.app.data.model.request.SaveProgressRequest
import com.edumind.app.data.model.response.LearningProgressResponse
import com.edumind.app.data.remote.api.ProgressApiService
import com.edumind.app.util.ApiErrorParser
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val progressApiService: ProgressApiService,
    private val playbackPositionDao: PlaybackPositionDao
) : ProgressRepository {

    override suspend fun getProgressOverview(): Result<LearningProgressResponse> {
        return try {
            val response = progressApiService.getProgressOverview()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Dữ liệu tiến độ học tập hiện chưa khả dụng trên máy chủ hoặc máy chủ đang cập nhật."
                    401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
                    else -> {
                        val parsed = ApiErrorParser.parseError(response.code(), response.errorBody())
                        parsed.message.ifBlank { "Không thể tải tiến độ học tập lúc này (${response.code()})." }
                    }
                }
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

    override suspend fun getPlaybackPosition(lessonId: String): PlaybackPositionEntity? {
        return playbackPositionDao.getByLessonId(lessonId)
    }

    override suspend fun saveProgress(
        lessonId: String,
        positionMs: Long,
        totalDurationMs: Long,
        completed: Boolean
    ): Result<Float> {
        val positionSec = (positionMs / 1000f).coerceAtLeast(0f)
        val durationSec = (totalDurationMs / 1000f).coerceAtLeast(0f)
        val isCompleted = completed || (durationSec > 0f && positionSec >= durationSec * 0.9f)
        val now = System.currentTimeMillis()

        // 1. Luôn lưu vào cơ sở dữ liệu cục bộ trước (synced = false)
        val localEntity = PlaybackPositionEntity(
            lessonId = lessonId,
            positionMs = positionMs,
            durationMs = totalDurationMs,
            completed = isCompleted,
            updatedAt = now,
            synced = false
        )
        playbackPositionDao.upsert(localEntity)

        // 2. Thử gửi lên máy chủ qua mạng
        return try {
            val request = SaveProgressRequest(
                lessonId = lessonId,
                currentPosition = positionSec,
                totalDuration = durationSec,
                completed = isCompleted
            )
            val response = progressApiService.saveProgress(request)
            if (response.isSuccessful && response.body() != null) {
                playbackPositionDao.markSynced(lessonId)
                Result.success(response.body()!!.courseProgressPercent)
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Không tìm thấy bài giảng để lưu tiến độ."
                    401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
                    else -> {
                        val parsed = ApiErrorParser.parseError(response.code(), response.errorBody())
                        parsed.message.ifBlank { "Lỗi lưu tiến độ (${response.code()})." }
                    }
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            // Lỗi mạng: Dữ liệu đã an toàn ở cục bộ, trả về thành công để không gián đoạn giao diện
            val estimatedPercent = if (durationSec > 0f) (positionSec / durationSec * 100f).coerceIn(0f, 100f) else 0f
            Result.success(estimatedPercent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPending(): Result<Unit> {
        return try {
            val pendingList = playbackPositionDao.getUnsynced()
            for (item in pendingList) {
                try {
                    val positionSec = (item.positionMs / 1000f).coerceAtLeast(0f)
                    val durationSec = (item.durationMs / 1000f).coerceAtLeast(0f)
                    val request = SaveProgressRequest(
                        lessonId = item.lessonId,
                        currentPosition = positionSec,
                        totalDuration = durationSec,
                        completed = item.completed
                    )
                    val response = progressApiService.saveProgress(request)
                    if (response.isSuccessful) {
                        playbackPositionDao.markSynced(item.lessonId)
                    }
                } catch (e: Exception) {
                    // Tiếp tục đồng bộ các bản ghi tiếp theo nếu một bản ghi gặp lỗi mạng tạm thời
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
