package com.edumind.app.data.remote.api

import com.edumind.app.data.model.request.CreateNoteRequest
import com.edumind.app.data.model.request.UpdateNoteRequest
import com.edumind.app.data.model.response.MessageResponse
import com.edumind.app.data.model.response.NoteDto
import com.edumind.app.data.model.response.NoteListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NoteApiService {

    @GET("lessons/{lesson_id}/notes")
    suspend fun getLessonNotes(
        @Path("lesson_id") lessonId: String
    ): Response<NoteListResponse>

    @POST("lessons/{lesson_id}/notes")
    suspend fun createNote(
        @Path("lesson_id") lessonId: String,
        @Body request: CreateNoteRequest
    ): Response<NoteDto>

    @PUT("notes/{note_id}")
    suspend fun updateNote(
        @Path("note_id") noteId: String,
        @Body request: UpdateNoteRequest
    ): Response<NoteDto>

    @DELETE("notes/{note_id}")
    suspend fun deleteNote(
        @Path("note_id") noteId: String
    ): Response<MessageResponse>
}

