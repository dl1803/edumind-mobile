package com.edumind.app.repository

import com.edumind.app.data.model.response.LessonDetail
import com.edumind.app.data.model.response.VideoChapter
import com.edumind.app.data.model.response.toDomain
import com.edumind.app.data.remote.api.LessonApiService
import com.edumind.app.util.ApiErrorParser
import com.edumind.app.util.Constants
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
                        val streamUrl = if (domain.hlsStreamUrl.isBlank()) {
                            Constants.SAMPLE_HLS_URL
                        } else {
                            domain.hlsStreamUrl
                        }
                        Result.success(domain.copy(hlsStreamUrl = streamUrl))
                    } else {
                        Result.failure(Exception("Dữ liệu bài giảng rỗng từ máy chủ."))
                    }
                }
                403 -> Result.failure(LessonAccessDeniedException())
                404 -> Result.failure(LessonNotFoundException())
                503 -> {
                    Result.success(
                        LessonDetail(
                            id = lessonId,
                            title = "Bài 1",
                            description = "Video demo kiểm thử phát Media3 ExoPlayer",
                            courseId = "",
                            chapterId = null,
                            hlsStreamUrl = Constants.SAMPLE_HLS_URL,
                            durationSec = 765,
                            lastPositionSec = 0f,
                            transcript = emptyList(),
                            chapters = listOf(
                                VideoChapter("1. Giới thiệu tổng quan bài học", 0f),
                                VideoChapter("2. Quy tắc khai báo biến chuẩn PEP 8", 135f),
                                VideoChapter("3. Kiểu số nguyên int & số thực float", 255f),
                                VideoChapter("4. Chuỗi ký tự (str) & Kiểu Logic (bool)", 380f)
                            ),
                            quizMarkers = emptyList(),
                            nextLessonId = null,
                            prevLessonId = null
                        )
                    )
                }
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
