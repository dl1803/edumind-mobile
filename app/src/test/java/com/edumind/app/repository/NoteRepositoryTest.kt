package com.edumind.app.repository

import com.edumind.app.data.local.db.dao.NoteDao
import com.edumind.app.data.local.db.entity.NoteEntity
import com.edumind.app.data.model.request.CreateNoteRequest
import com.edumind.app.data.model.request.UpdateNoteRequest
import com.edumind.app.data.model.response.MessageResponse
import com.edumind.app.data.model.response.NoteDto
import com.edumind.app.data.model.response.NoteListResponse
import com.edumind.app.data.remote.api.NoteApiService
import com.edumind.app.util.NetworkUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NoteRepositoryTest {

    private val noteApiService: NoteApiService = mockk(relaxed = true)
    private val noteDao: NoteDao = mockk(relaxed = true)
    private val networkUtils: NetworkUtils = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: NoteRepositoryImpl

    private val lessonId = "lesson_01"
    private val localId = "local_100"
    private val serverId = "server_100"

    private val sampleDto = NoteDto(
        id = serverId,
        content = "Ghi nhớ khái niệm biến",
        timestamp = 45.5f,
        createdAt = "2026-11-01T10:00:00Z",
        updatedAt = "2026-11-01T10:00:00Z"
    )

    private fun entity(
        id: String = localId,
        serverId: String? = this.serverId,
        content: String = "Nội dung cũ",
        isSynced: Boolean = true,
        isDeleted: Boolean = false,
        updatedAt: Long = 1_000L
    ) = NoteEntity(
        id = id,
        lessonId = lessonId,
        userId = "",
        timestampSeconds = 45,
        content = content,
        createdAt = 1_000L,
        updatedAt = updatedAt,
        isSynced = isSynced,
        serverId = serverId,
        isDeleted = isDeleted
    )

    @Before
    fun setUp() {
        every { networkUtils.isConnectedNow() } returns true
        repository = NoteRepositoryImpl(
            noteApiService = noteApiService,
            noteDao = noteDao,
            networkUtils = networkUtils,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun fetchNotesFromRemote_mergesWithoutOverwritingPendingLocalChanges() = runTest(testDispatcher) {
        val dtoNew = sampleDto.copy(id = "s_new")
        val dtoPending = sampleDto.copy(id = "s_pending")
        val dtoSynced = sampleDto.copy(id = "s_synced")
        coEvery { noteApiService.getLessonNotes(lessonId) } returns Response.success(
            NoteListResponse(data = listOf(dtoNew, dtoPending, dtoSynced))
        )
        coEvery { noteDao.getNoteByServerId("s_new") } returns null
        coEvery { noteDao.getNoteByServerId("s_pending") } returns
            entity(id = "l_pending", serverId = "s_pending", isSynced = false)
        coEvery { noteDao.getNoteByServerId("s_synced") } returns
            entity(id = "l_synced", serverId = "s_synced")
        coEvery { noteDao.getSyncedNotesByLesson(lessonId) } returns emptyList()
        val saved = slot<List<NoteEntity>>()
        coEvery { noteDao.upsertNotes(capture(saved)) } returns Unit

        val result = repository.fetchNotesFromRemote(lessonId)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(listOf("s_new", "l_synced"), saved.captured.map { it.id })
        assertEquals(listOf("s_new", "s_synced"), saved.captured.map { it.serverId })
        assertTrue(saved.captured.all { it.isSynced && !it.isDeleted })
    }

    @Test
    fun fetchNotesFromRemote_deletesLocalSyncedNotesDeletedOnServer() = runTest(testDispatcher) {
        val dtoKeep = sampleDto.copy(id = "s_keep")
        coEvery { noteApiService.getLessonNotes(lessonId) } returns Response.success(
            NoteListResponse(data = listOf(dtoKeep))
        )
        coEvery { noteDao.getNoteByServerId("s_keep") } returns entity(id = "l_keep", serverId = "s_keep")
        coEvery { noteDao.upsertNotes(any()) } returns Unit

        val ghostEntity = entity(id = "l_ghost", serverId = "s_ghost")
        val keepEntity = entity(id = "l_keep", serverId = "s_keep")
        coEvery { noteDao.getSyncedNotesByLesson(lessonId) } returns listOf(keepEntity, ghostEntity)
        coEvery { noteDao.deleteNoteById("l_ghost") } returns Unit

        val result = repository.fetchNotesFromRemote(lessonId)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { noteDao.deleteNoteById("l_ghost") }
        coVerify(exactly = 0) { noteDao.deleteNoteById("l_keep") }
    }

    @Test
    fun fetchNotesFromRemote_http404_returnsFailureWithoutWritingRoom() = runTest(testDispatcher) {
        coEvery { noteApiService.getLessonNotes(lessonId) } returns Response.error(
            404,
            "Not Found".toResponseBody(null)
        )

        val result = repository.fetchNotesFromRemote(lessonId)
        advanceUntilIdle()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("không tồn tại") == true)
        coVerify(exactly = 0) { noteDao.upsertNotes(any()) }
    }

    @Test
    fun createNote_offline_savesPendingWithoutApiCall() = runTest(testDispatcher) {
        every { networkUtils.isConnectedNow() } returns false
        val saved = slot<NoteEntity>()
        coEvery { noteDao.upsertNote(capture(saved)) } returns Unit

        val result = repository.createNote(lessonId, "  Ghi chú offline  ", 60f)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertFalse(saved.captured.isSynced)
        assertNull(saved.captured.serverId)
        assertEquals("Ghi chú offline", saved.captured.content)
        assertEquals(60, saved.captured.timestampSeconds)
        coVerify(exactly = 0) { noteApiService.createNote(any(), any()) }
    }

    @Test
    fun createNote_online_pushesAndRecordsServerId() = runTest(testDispatcher) {
        val saved = slot<NoteEntity>()
        coEvery { noteDao.upsertNote(capture(saved)) } returns Unit
        coEvery { noteDao.getNoteById(any()) } answers { saved.captured }
        coEvery {
            noteApiService.createNote(lessonId, CreateNoteRequest(content = "Nội dung mới", timestamp = 60f))
        } returns Response.success(sampleDto)

        val result = repository.createNote(lessonId, "Nội dung mới", 60f)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            noteDao.applyPushResult(saved.captured.id, serverId, saved.captured.updatedAt)
        }
    }

    @Test
    fun createNote_onIoException_keepsPendingAndStillSucceeds() = runTest(testDispatcher) {
        val saved = slot<NoteEntity>()
        coEvery { noteDao.upsertNote(capture(saved)) } returns Unit
        coEvery { noteDao.getNoteById(any()) } answers { saved.captured }
        coEvery { noteApiService.createNote(any(), any()) } throws IOException("mất mạng")

        val result = repository.createNote(lessonId, "Nội dung mới", 60f)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { noteDao.applyPushResult(any(), any(), any()) }
    }

    @Test
    fun createNote_blankContent_returnsFailureWithoutApiCall() = runTest(testDispatcher) {
        val result = repository.createNote(lessonId, "   ", 60f)
        advanceUntilIdle()

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { noteApiService.createNote(any(), any()) }
        coVerify(exactly = 0) { noteDao.upsertNote(any()) }
    }

    @Test
    fun updateNote_offline_marksPendingAndKeepsServerId() = runTest(testDispatcher) {
        every { networkUtils.isConnectedNow() } returns false
        coEvery { noteDao.getNoteById(localId) } returns entity()
        val saved = slot<NoteEntity>()
        coEvery { noteDao.upsertNote(capture(saved)) } returns Unit

        val result = repository.updateNote(localId, "Nội dung đã sửa")
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals("Nội dung đã sửa", saved.captured.content)
        assertFalse(saved.captured.isSynced)
        assertEquals(serverId, saved.captured.serverId)
        coVerify(exactly = 0) { noteApiService.updateNote(any(), any()) }
    }

    @Test
    fun updateNote_softDeleted_returnsFailure() = runTest(testDispatcher) {
        coEvery { noteDao.getNoteById(localId) } returns entity(isDeleted = true)

        val result = repository.updateNote(localId, "Nội dung đã sửa")
        advanceUntilIdle()

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { noteDao.upsertNote(any()) }
        coVerify(exactly = 0) { noteApiService.updateNote(any(), any()) }
    }

    @Test
    fun updateNote_online_putsContentWithServerId() = runTest(testDispatcher) {
        var current = entity()
        coEvery { noteDao.getNoteById(localId) } answers { current }
        coEvery { noteDao.upsertNote(any()) } answers { current = firstArg<NoteEntity>() }
        coEvery {
            noteApiService.updateNote(serverId, UpdateNoteRequest(content = "Nội dung đã sửa"))
        } returns Response.success(sampleDto.copy(content = "Nội dung đã sửa"))

        val result = repository.updateNote(localId, "Nội dung đã sửa")
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { noteDao.applyPushResult(localId, serverId, current.updatedAt) }
    }

    @Test
    fun deleteNote_online_withServerId_callsApiThenRemovesRow() = runTest(testDispatcher) {
        coEvery { noteDao.getNoteById(localId) } returns entity(isSynced = false, isDeleted = true)
        coEvery { noteApiService.deleteNote(serverId) } returns Response.success(
            MessageResponse(message = "Đã xóa ghi chú.")
        )

        val result = repository.deleteNote(localId)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { noteDao.softDeleteNote(localId, any()) }
        coVerify(exactly = 1) { noteApiService.deleteNote(serverId) }
        coVerify(exactly = 1) { noteDao.deleteNoteById(localId) }
    }

    @Test
    fun deleteNote_withoutServerId_removesRowWithoutApiCall() = runTest(testDispatcher) {
        coEvery { noteDao.getNoteById(localId) } returns
            entity(serverId = null, isSynced = false, isDeleted = true)

        val result = repository.deleteNote(localId)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { noteApiService.deleteNote(any()) }
        coVerify(exactly = 1) { noteDao.deleteNoteById(localId) }
    }

    @Test
    fun deleteNote_offline_onlySoftDeletes() = runTest(testDispatcher) {
        every { networkUtils.isConnectedNow() } returns false

        val result = repository.deleteNote(localId)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { noteDao.softDeleteNote(localId, any()) }
        coVerify(exactly = 0) { noteApiService.deleteNote(any()) }
        coVerify(exactly = 0) { noteDao.deleteNoteById(any()) }
    }

    @Test
    fun syncPendingNotes_failureOfOneNoteDoesNotStopTheBatch() = runTest(testDispatcher) {
        val creating = entity(id = "local_a", serverId = null, isSynced = false)
        val updating = entity(id = "local_b", serverId = "server_b", isSynced = false, updatedAt = 2_000L)
        coEvery { noteDao.getUnsyncedNotes() } returns listOf(creating, updating)
        coEvery { noteDao.getNoteById("local_a") } returns creating
        coEvery { noteDao.getNoteById("local_b") } returns updating
        coEvery { noteApiService.createNote(any(), any()) } returns Response.error(
            500,
            "Server Error".toResponseBody(null)
        )
        coEvery { noteApiService.updateNote("server_b", any()) } returns Response.success(sampleDto)

        repository.syncPendingNotes()
        advanceUntilIdle()

        coVerify(exactly = 0) { noteDao.applyPushResult("local_a", any(), any()) }
        coVerify(exactly = 1) { noteDao.applyPushResult("local_b", "server_b", 2_000L) }
    }

    @Test
    fun syncPendingNotes_update404_removesRowInsteadOfRetryingForever() = runTest(testDispatcher) {
        val pending = entity(isSynced = false)
        coEvery { noteDao.getUnsyncedNotes() } returns listOf(pending)
        coEvery { noteDao.getNoteById(localId) } returns pending
        coEvery { noteApiService.updateNote(serverId, any()) } returns Response.error(
            404,
            "Not Found".toResponseBody(null)
        )

        repository.syncPendingNotes()
        advanceUntilIdle()

        coVerify(exactly = 1) { noteDao.deleteNoteById(localId) }
    }

    @Test
    fun getNotes_readsFromRoomFlow() = runTest(testDispatcher) {
        coEvery { noteDao.getNotesByLesson(lessonId) } returns flowOf(
            listOf(entity(id = "note_1", content = "Ghi chú local"))
        )

        val notes = repository.getNotes(lessonId).first()

        assertEquals(1, notes.size)
        assertEquals("note_1", notes[0].id)
        assertEquals("Ghi chú local", notes[0].content)
        assertEquals(45f, notes[0].timestampSeconds)
    }
}
