package com.edumind.app.data.local.db.dao

import androidx.room.*
import com.edumind.app.data.local.db.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Upsert
    suspend fun upsertNotes(notes: List<NoteEntity>)

    @Query("SELECT * FROM notes WHERE lesson_id = :lessonId AND is_deleted = 0 ORDER BY timestamp_seconds ASC")
    fun getNotesByLesson(lessonId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_synced = 0 ORDER BY updated_at ASC")
    suspend fun getUnsyncedNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE server_id = :serverId LIMIT 1")
    suspend fun getNoteByServerId(serverId: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE lesson_id = :lessonId AND is_synced = 1 AND is_deleted = 0 AND server_id IS NOT NULL")
    suspend fun getSyncedNotesByLesson(lessonId: String): List<NoteEntity>

    @Query("UPDATE notes SET server_id = :serverId, is_synced = CASE WHEN updated_at = :pushedUpdatedAt THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun applyPushResult(id: String, serverId: String, pushedUpdatedAt: Long)

    @Query("UPDATE notes SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteNote(id: String, updatedAt: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("DELETE FROM notes")
    suspend fun clearAll()
}
