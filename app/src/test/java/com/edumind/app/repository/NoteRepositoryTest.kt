package com.edumind.app.repository

import com.edumind.app.data.local.db.dao.NoteDao
import com.edumind.app.data.local.db.entity.NoteEntity
import com.edumind.app.data.model.request.CreateNoteRequest
import com.edumind.app.data.model.request.UpdateNoteRequest
import com.edumind.app.data.model.response.MessageResponse
import com.edumind.app.data.model.response.NoteDto
import com.edumind.app.data.model.response.NoteListResponse
import com.edumind.app.data.remote.api.NoteApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class NoteRepositoryTest {

    private val noteApiService: NoteApiService = mockk(relaxed = true)
    private val noteDao: NoteDao = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: NoteRepositoryImpl

    private val sampleLessonId = "lesson_01"
    private val sampleNoteId = "note_100"

    private val sampleNoteDto = NoteDto(
        id = sampleNoteId,
        content = "Ghi nhớ khái niệm biến",
        timestamp = 45.5f,
        createdAt = "2026-11-01T10:00:00Z",
        updatedAt = "2026-11-01T10:00:00Z"
    )

    @Before
    fun setUp() {
        repository = NoteRepositoryImpl(
            noteApiService = noteApiService,
            noteDao = noteDao,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun fetchNotesFromRemote_success_savesToRoomAndReturnsList() = runTest(testDispatcher) {
        val responseDto = NoteListResponse(data = listOf(sampleNoteDto))
        coEvery { noteApiService.getLessonNotes(sampleLessonId) } returns Response.success(responseDto)

        val result = repository.fetchNotesFromRemote(sampleLessonId)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        val list = result.getOrThrow()
        assertEquals(1, list.size)
        assertEquals(sampleNoteId, list[0].id)
        assertEquals(45.5f, list[0].timestampSeconds)

        coVerify(exactly = 1) { noteDao.upsertNotes(match { it.size == 1 && it[0].isSynced }) }
    }

    @Test
    fun fetchNotesFromRemote_http404_returnsFailure() = runTest(testDispatcher) {
        coEvery { noteApiService.getLessonNotes(sampleLessonId) } returns Response.error(
            404,
            "Not Found".toResponseBody(null)
        )

        val result = repository.fetchNotesFromRemote(sampleLessonId)
        advanceUntilIdle()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("không tồn tại") == true)
        coVerify(exactly = 0) { noteDao.upsertNotes(any()) }
    }

    @Test
    fun createNote_success_savesToRoom() = runTest(testDispatcher) {
        val request = CreateNoteRequest(content = "Nội dung ghi chú mới", timestamp = 60f)
        coEvery { noteApiService.createNote(sampleLessonId, request) } returns Response.success(
            sampleNoteDto.copy(content = "Nội dung ghi chú mới", timestamp = 60f)
        )

        val result = repository.createNote(sampleLessonId, "Nội dung ghi chú mới", 60f)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals("Nội dung ghi chú mới", result.getOrThrow().content)
        coVerify(exactly = 1) { noteDao.upsertNote(match { it.content == "Nội dung ghi chú mới" && it.isSynced }) }
    }

    @Test
    fun createNote_blankContent_returnsFailureWithoutApiCall() = runTest(testDispatcher) {
        val result = repository.createNote(sampleLessonId, "   ", 60f)
        advanceUntilIdle()

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { noteApiService.createNote(any(), any()) }
    }

    @Test
    fun updateNote_success_updatesRoom() = runTest(testDispatcher) {
        val request = UpdateNoteRequest(content = "Nội dung đã sửa")
        coEvery { noteApiService.updateNote(sampleNoteId, request) } returns Response.success(
            sampleNoteDto.copy(content = "Nội dung đã sửa")
        )

        val result = repository.updateNote(sampleNoteId, "Nội dung đã sửa")
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals("Nội dung đã sửa", result.getOrThrow().content)
        coVerify(exactly = 1) { noteDao.upsertNote(match { it.content == "Nội dung đã sửa" }) }
    }

    @Test
    fun deleteNote_success_deletesFromRoom() = runTest(testDispatcher) {
        coEvery { noteApiService.deleteNote(sampleNoteId) } returns Response.success(
            MessageResponse(message = "Đã xóa ghi chú.")
        )

        val result = repository.deleteNote(sampleNoteId)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { noteDao.deleteNoteById(sampleNoteId) }
    }

    @Test
    fun getNotes_readsFromRoomFlow() = runTest(testDispatcher) {
        val entity = NoteEntity(
            id = "note_1",
            lessonId = sampleLessonId,
            userId = "user_1",
            timestampSeconds = 30,
            content = "Ghi chú local",
            isSynced = true
        )
        coEvery { noteDao.getNotesByLesson(sampleLessonId) } returns flowOf(listOf(entity))

        val notes = repository.getNotes(sampleLessonId).first()

        assertEquals(1, notes.size)
        assertEquals("Ghi chú local", notes[0].content)
        assertEquals(30f, notes[0].timestampSeconds)
    }
}

