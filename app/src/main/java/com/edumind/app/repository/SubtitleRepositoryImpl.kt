package com.edumind.app.repository

import com.edumind.app.data.local.db.dao.SubtitleCacheDao
import com.edumind.app.data.local.db.entity.SubtitleCacheEntity
import com.edumind.app.data.model.response.SubtitleSegment
import com.edumind.app.data.model.response.toSubtitleSegment
import com.edumind.app.data.remote.api.LessonApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubtitleRepositoryImpl @Inject constructor(
    private val lessonApiService: LessonApiService,
    private val subtitleCacheDao: SubtitleCacheDao
) : SubtitleRepository {

    private val gson = Gson()

    override suspend fun getSegments(lessonId: String): List<SubtitleSegment> {
        return try {
            val response = lessonApiService.getLessonDetail(lessonId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val (segments, json) = withContext(Dispatchers.Default) {
                    val mapped = body.transcript
                        .map { it.toSubtitleSegment() }
                        .filter { it.text.isNotEmpty() }
                    mapped to gson.toJson(mapped)
                }
                subtitleCacheDao.upsert(SubtitleCacheEntity(lessonId, json))
                segments
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            readCache(lessonId)
        }
    }

    private suspend fun readCache(lessonId: String): List<SubtitleSegment> {
        val cached = subtitleCacheDao.getByLessonId(lessonId) ?: return emptyList()
        return withContext(Dispatchers.Default) {
            try {
                gson.fromJson(cached.segmentsJson, Array<SubtitleSegment>::class.java)?.toList() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
