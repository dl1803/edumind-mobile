package com.edumind.app.data.remote

import com.edumind.app.data.model.response.VideoPlaybackInfo
import com.edumind.app.data.remote.api.LessonApiService
import com.edumind.app.repository.LessonAccessDeniedException
import com.edumind.app.repository.LessonNotFoundException
import com.edumind.app.repository.VideoProcessingException
import com.edumind.app.util.ApiErrorParser
import com.edumind.app.util.ClockProvider
import com.edumind.app.util.Constants
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoUrlManager @Inject constructor(
    private val lessonApiService: LessonApiService,
    private val clock: ClockProvider
) {
    private val cache = ConcurrentHashMap<String, VideoPlaybackInfo>()
    private val mutex = Mutex()

    suspend fun getPlaybackUrl(lessonId: String): Result<VideoPlaybackInfo> = mutex.withLock {
        val cached = cache[lessonId]
        if (cached != null && cached.expiresAt - clock.currentTimeMillis() > REFRESH_MARGIN_MS) {
            Result.success(cached)
        } else {
            fetch(lessonId)
        }
    }

    suspend fun renew(lessonId: String): Result<VideoPlaybackInfo> {
        val staleEntry = cache[lessonId]
        return mutex.withLock {
            val current = cache[lessonId]
            if (current != null && current !== staleEntry) {
                Result.success(current)
            } else {
                fetch(lessonId)
            }
        }
    }

    private suspend fun fetch(lessonId: String): Result<VideoPlaybackInfo> {
        return try {
            val response = lessonApiService.getLessonDetail(lessonId)
            val streamUrl = response.body()?.hlsStreamUrl
            when {
                response.isSuccessful && !streamUrl.isNullOrBlank() -> {
                    val info = VideoPlaybackInfo(
                        lessonId = lessonId,
                        hlsUrl = streamUrl,
                        expiresAt = clock.currentTimeMillis() + Constants.VIDEO_URL_TTL_MS
                    )
                    cache[lessonId] = info
                    Result.success(info)
                }
                response.isSuccessful -> Result.failure(VideoProcessingException())
                response.code() == 403 -> Result.failure(LessonAccessDeniedException())
                response.code() == 404 -> Result.failure(LessonNotFoundException())
                response.code() == 503 -> Result.failure(VideoProcessingException())
                else -> {
                    val parsed = ApiErrorParser.parseError(response.code(), response.errorBody())
                    val message = parsed.message.ifBlank { "Lỗi tải đường dẫn phát video (${response.code()})" }
                    Result.failure(Exception(message))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(Exception("Lỗi kết nối mạng khi tải đường dẫn phát video. Vui lòng kiểm tra lại.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val REFRESH_MARGIN_MS = 60_000L
    }
}

