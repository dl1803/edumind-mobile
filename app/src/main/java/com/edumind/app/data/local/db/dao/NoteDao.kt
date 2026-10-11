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

    @Query("SELECT * FROM notes WHERE lesson_id = :lessonId ORDER BY timestamp_seconds ASC")
    fun getNotesByLesson(lessonId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_synced = 0")
    suspend fun getUnsyncedNotes(): List<NoteEntity>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("DELETE FROM notes")
    suspend fun clearAll()
}
