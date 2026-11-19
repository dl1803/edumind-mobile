package com.edumind.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.edumind.app.data.local.db.entity.SubtitleCacheEntity

@Dao
interface SubtitleCacheDao {
    @Upsert
    suspend fun upsert(entity: SubtitleCacheEntity)

    @Query("SELECT * FROM subtitle_cache WHERE lesson_id = :lessonId LIMIT 1")
    suspend fun getByLessonId(lessonId: String): SubtitleCacheEntity?

    @Query("DELETE FROM subtitle_cache WHERE lesson_id = :lessonId")
    suspend fun deleteByLessonId(lessonId: String)

    @Query("DELETE FROM subtitle_cache")
    suspend fun clearAll()
}
