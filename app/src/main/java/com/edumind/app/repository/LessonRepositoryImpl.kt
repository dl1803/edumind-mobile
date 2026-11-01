package com.edumind.app.repository

import com.edumind.app.data.model.response.LessonDetail
import com.edumind.app.data.model.response.toDomain
import com.edumind.app.data.remote.api.LessonApiService
import com.edumind.app.util.ApiErrorParser
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val lessonApiService: LessonApiService
) : LessonRepository {

    override suspend fun getLessonDetail(lessonId: String): Result<LessonDetail> {
        return try {
            val response = lessonApiService.getLessonDetail(lessonId)
            when (response.code()) {
                200 -> {
                    val body = response.body()
                    if (body != null) {
                        val domain = body.toDomain()
                        if (domain.hlsStreamUrl.isBlank()) {
                            Result.failure(VideoProcessingException())
                        } else {
                            Result.success(domain)
                        }
                    } else {
                        Result.failure(Exception("Dữ liệu bài giảng rỗng từ máy chủ."))
                    }
                }
                403 -> Result.failure(LessonAccessDeniedException())
                404 -> Result.failure(LessonNotFoundException())
                503 -> Result.failure(VideoProcessingException())
                else -> {
                    val parsed = ApiErrorParser.parseError(response.code(), response.errorBody())
                    val msg = parsed.message.ifBlank { "Lỗi tải bài giảng (${response.code()})" }
                    Result.failure(Exception(msg))
                }
            }
        } catch (e: IOException) {
            Result.failure(Exception("Lỗi kết nối mạng khi tải bài giảng. Vui lòng kiểm tra lại.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

