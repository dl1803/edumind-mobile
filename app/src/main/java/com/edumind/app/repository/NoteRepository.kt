package com.edumind.app.repository

import com.edumind.app.data.model.response.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(lessonId: String): Flow<List<Note>>

    suspend fun fetchNotesFromRemote(lessonId: String): Result<List<Note>>

    suspend fun createNote(
        lessonId: String,
        content: String,
        timestampSeconds: Float
    ): Result<Note>

    suspend fun updateNote(
        noteId: String,
        content: String
    ): Result<Note>

    suspend fun deleteNote(noteId: String): Result<Unit>

    suspend fun getUnsyncedNotes(): List<Note>
}

