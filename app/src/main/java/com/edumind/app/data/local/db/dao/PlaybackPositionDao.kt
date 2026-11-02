package com.edumind.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.edumind.app.data.local.db.entity.PlaybackPositionEntity

@Dao
interface PlaybackPositionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlaybackPositionEntity)

    @Query("SELECT * FROM playback_positions WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getByLessonId(lessonId: String): PlaybackPositionEntity?

    @Query("SELECT * FROM playback_positions WHERE synced = 0")
    suspend fun getUnsynced(): List<PlaybackPositionEntity>

    @Query("UPDATE playback_positions SET synced = 1 WHERE lessonId = :lessonId")
    suspend fun markSynced(lessonId: String)
}

