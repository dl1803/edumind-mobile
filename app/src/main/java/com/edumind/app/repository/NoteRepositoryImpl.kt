package com.edumind.app.repository

import com.edumind.app.data.local.db.dao.NoteDao
import com.edumind.app.data.local.db.entity.NoteEntity
import com.edumind.app.data.model.request.CreateNoteRequest
import com.edumind.app.data.model.request.UpdateNoteRequest
import com.edumind.app.data.model.response.Note
import com.edumind.app.data.model.response.toDomain
import com.edumind.app.data.model.response.toEntity
import com.edumind.app.data.remote.api.NoteApiService
import com.edumind.app.util.NetworkUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl constructor(
    private val noteApiService: NoteApiService,
    private val noteDao: NoteDao,
    private val networkUtils: NetworkUtils,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : NoteRepository {

    @Inject
    constructor(
        noteApiService: NoteApiService,
        noteDao: NoteDao,
        networkUtils: NetworkUtils
    ) : this(noteApiService, noteDao, networkUtils, Dispatchers.IO)

    private val syncMutex = Mutex()

    override fun getNotes(lessonId: String): Flow<List<Note>> {
        return noteDao.getNotesByLesson(lessonId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun fetchNotesFromRemote(lessonId: String): Result<Unit> =
        withContext(ioDispatcher) {
            syncMutex.withLock {
                try {
                    val response = noteApiService.getLessonNotes(lessonId)
                    if (response.isSuccessful) {
                        val remoteNotes = response.body()?.data ?: emptyList()
                        val remoteServerIds = remoteNotes.map { it.id }.toSet()
                        val mergedEntities = remoteNotes.mapNotNull { dto ->
                            val local = noteDao.getNoteByServerId(dto.id)
                            if (local != null && (!local.isSynced || local.isDeleted)) {
                                null
                            } else {
                                dto.toEntity(
                                    lessonId = lessonId,
                                    localId = local?.id ?: dto.id,
                                    userId = local?.userId.orEmpty()
                                )
                            }
                        }
                        noteDao.upsertNotes(mergedEntities)

                        val localSynced = noteDao.getSyncedNotesByLesson(lessonId)
                        localSynced.filter { it.serverId !in remoteServerIds }.forEach { ghost ->
                            noteDao.deleteNoteById(ghost.id)
                        }

                        Result.success(Unit)
                    } else {
                        Result.failure(mapHttpError(response.code(), "Không thể tải ghi chú bài giảng."))
                    }
                } catch (e: IOException) {
                    Result.failure(Exception("Lỗi kết nối mạng khi tải danh sách ghi chú.", e))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }

    override suspend fun createNote(
        lessonId: String,
        content: String,
        timestampSeconds: Float
    ): Result<Note> = withContext(ioDispatcher) {
        if (content.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Nội dung ghi chú không được để trống."))
        }
        val now = System.currentTimeMillis()
        val entity = NoteEntity(
            id = UUID.randomUUID().toString(),
            lessonId = lessonId,
            userId = "",
            timestampSeconds = timestampSeconds.toInt(),
            content = content.trim(),
            createdAt = now,
            updatedAt = now,
            isSynced = false
        )
        noteDao.upsertNote(entity)
        pushIfOnline(entity.id)
        Result.success(entity.toDomain())
    }

    override suspend fun updateNote(
        noteId: String,
        content: String,
        timestampSeconds: Float?
    ): Result<Note> = withContext(ioDispatcher) {
        if (content.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Nội dung ghi chú không được để trống."))
        }
        val existing = noteDao.getNoteById(noteId)
            ?: return@withContext Result.failure(mapHttpError(404, "Không thể cập nhật ghi chú."))
        if (existing.isDeleted) {
            return@withContext Result.failure(mapHttpError(404, "Ghi chú không tồn tại."))
        }
        val updated = existing.copy(
            content = content.trim(),
            timestampSeconds = timestampSeconds?.toInt() ?: existing.timestampSeconds,
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
        noteDao.upsertNote(updated)
        pushIfOnline(noteId)
        Result.success(updated.toDomain())
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> = withContext(ioDispatcher) {
        noteDao.softDeleteNote(noteId, System.currentTimeMillis())
        pushIfOnline(noteId)
        Result.success(Unit)
    }

    override suspend fun syncPendingNotes() {
        withContext(ioDispatcher) {
            syncMutex.withLock {
                noteDao.getUnsyncedNotes().forEach { pushNote(it.id) }
            }
        }
    }

    private suspend fun pushIfOnline(noteId: String) {
        if (networkUtils.isConnectedNow()) {
            syncMutex.withLock { pushNote(noteId) }
        }
    }

    private suspend fun pushNote(noteId: String) {
        val note = noteDao.getNoteById(noteId) ?: return
        if (note.isSynced && !note.isDeleted) return
        try {
            val serverId = note.serverId
            when {
                note.isDeleted -> pushDelete(note, serverId)
                serverId == null -> pushCreate(note)
                else -> pushUpdate(note, serverId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    private suspend fun pushCreate(note: NoteEntity) {
        val response = noteApiService.createNote(
            note.lessonId,
            CreateNoteRequest(content = note.content, timestamp = note.timestampSeconds.toFloat())
        )
        val body = response.body()
        if (response.isSuccessful && body != null) {
            noteDao.applyPushResult(note.id, body.id, note.updatedAt)
        }
    }

    private suspend fun pushUpdate(note: NoteEntity, serverId: String) {
        val response = noteApiService.updateNote(serverId, UpdateNoteRequest(content = note.content))
        if (response.isSuccessful) {
            noteDao.applyPushResult(note.id, serverId, note.updatedAt)
        } else if (response.code() == 404) {
            noteDao.deleteNoteById(note.id)
        }
    }

    private suspend fun pushDelete(note: NoteEntity, serverId: String?) {
        if (serverId == null) {
            noteDao.deleteNoteById(note.id)
            return
        }
        val response = noteApiService.deleteNote(serverId)
        if (response.isSuccessful || response.code() == 404) {
            noteDao.deleteNoteById(note.id)
        }
    }

    private fun mapHttpError(code: Int, defaultMsg: String): Exception {
        return when (code) {
            401 -> Exception("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.")
            403 -> Exception("Bạn cần đăng ký hoặc mua khóa học để sử dụng tính năng ghi chú.")
            404 -> Exception("Ghi chú hoặc bài giảng không tồn tại.")
            else -> Exception("$defaultMsg (Mã lỗi: $code)")
        }
    }
}
