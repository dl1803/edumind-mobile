package com.edumind.app.repository

import com.edumind.app.data.local.db.dao.NoteDao
import com.edumind.app.data.model.request.CreateNoteRequest
import com.edumind.app.data.model.request.UpdateNoteRequest
import com.edumind.app.data.model.response.Note
import com.edumind.app.data.model.response.toDomain
import com.edumind.app.data.model.response.toEntity
import com.edumind.app.data.remote.api.NoteApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl constructor(
    private val noteApiService: NoteApiService,
    private val noteDao: NoteDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : NoteRepository {

    @Inject
    constructor(
        noteApiService: NoteApiService,
        noteDao: NoteDao
    ) : this(noteApiService, noteDao, Dispatchers.IO)

    override fun getNotes(lessonId: String): Flow<List<Note>> {
        return noteDao.getNotesByLesson(lessonId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun fetchNotesFromRemote(lessonId: String): Result<List<Note>> =
        withContext(ioDispatcher) {
            try {
                val response = noteApiService.getLessonNotes(lessonId)
                if (response.isSuccessful) {
                    val dtoList = response.body()?.data ?: emptyList()
                    val notes = dtoList.map { it.toDomain(lessonId) }
                    val entities = notes.map { it.toEntity() }
                    noteDao.upsertNotes(entities)
                    Result.success(notes)
                } else {
                    Result.failure(mapHttpError(response.code(), "Không thể tải ghi chú bài giảng."))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Lỗi kết nối mạng khi tải danh sách ghi chú.", e))
            } catch (e: Exception) {
                Result.failure(e)
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
        try {
            val request = CreateNoteRequest(content = content.trim(), timestamp = timestampSeconds)
            val response = noteApiService.createNote(lessonId, request)
            if (response.isSuccessful && response.body() != null) {
                val note = response.body()!!.toDomain(lessonId)
                noteDao.upsertNote(note.toEntity())
                Result.success(note)
            } else if (response.code() == 404 || response.code() == 503) {
                // Fallback offline / local-first khi backend chưa hỗ trợ endpoint hoặc server lỗi
                val localNote = Note(
                    id = java.util.UUID.randomUUID().toString(),
                    lessonId = lessonId,
                    content = content.trim(),
                    timestampSeconds = timestampSeconds,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )
                noteDao.upsertNote(localNote.toEntity())
                Result.success(localNote)
            } else {
                Result.failure(mapHttpError(response.code(), "Không thể tạo ghi chú."))
            }
        } catch (e: IOException) {
            // Mất mạng -> Lưu offline vào Room DB
            val localNote = Note(
                id = java.util.UUID.randomUUID().toString(),
                lessonId = lessonId,
                content = content.trim(),
                timestampSeconds = timestampSeconds,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
            noteDao.upsertNote(localNote.toEntity())
            Result.success(localNote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(
        noteId: String,
        content: String,
        timestampSeconds: Float?
    ): Result<Note> = withContext(ioDispatcher) {
        if (content.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Nội dung ghi chú không được để trống."))
        }
        try {
            val request = UpdateNoteRequest(content = content.trim())
            val response = noteApiService.updateNote(noteId, request)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val existingEntity = noteDao.getNoteById(noteId)
                val restoredLessonId = existingEntity?.lessonId ?: ""
                val restoredUserId = existingEntity?.userId ?: ""
                val effectiveTimestamp = timestampSeconds?.toInt() ?: existingEntity?.timestampSeconds ?: dto.timestamp.toInt()
                val note = dto.toDomain(lessonId = restoredLessonId).copy(
                    timestampSeconds = effectiveTimestamp.toFloat()
                )
                noteDao.upsertNote(note.toEntity(userId = restoredUserId).copy(timestampSeconds = effectiveTimestamp))
                Result.success(note)
            } else if (response.code() == 404 || response.code() == 503) {
                val existingEntity = noteDao.getNoteById(noteId)
                if (existingEntity != null) {
                    val updatedEntity = existingEntity.copy(
                        content = content.trim(),
                        timestampSeconds = timestampSeconds?.toInt() ?: existingEntity.timestampSeconds,
                        updatedAt = System.currentTimeMillis(),
                        isSynced = false
                    )
                    noteDao.upsertNote(updatedEntity)
                    Result.success(updatedEntity.toDomain())
                } else {
                    Result.failure(mapHttpError(response.code(), "Không thể cập nhật ghi chú."))
                }
            } else {
                Result.failure(mapHttpError(response.code(), "Không thể cập nhật ghi chú."))
            }
        } catch (e: IOException) {
            val existingEntity = noteDao.getNoteById(noteId)
            if (existingEntity != null) {
                val updatedEntity = existingEntity.copy(
                    content = content.trim(),
                    timestampSeconds = timestampSeconds?.toInt() ?: existingEntity.timestampSeconds,
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )
                noteDao.upsertNote(updatedEntity)
                Result.success(updatedEntity.toDomain())
            } else {
                Result.failure(Exception("Lỗi kết nối mạng khi cập nhật ghi chú.", e))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val response = noteApiService.deleteNote(noteId)
                if (response.isSuccessful || response.code() == 404) {
                    noteDao.deleteNoteById(noteId)
                    Result.success(Unit)
                } else {
                    Result.failure(mapHttpError(response.code(), "Không thể xóa ghi chú."))
                }
            } catch (e: IOException) {
                noteDao.deleteNoteById(noteId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getUnsyncedNotes(): List<Note> = withContext(ioDispatcher) {
        noteDao.getUnsyncedNotes().map { it.toDomain() }
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

