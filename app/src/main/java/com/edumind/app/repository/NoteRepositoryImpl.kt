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
class NoteRepositoryImpl @Inject constructor(
    private val noteApiService: NoteApiService,
    private val noteDao: NoteDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : NoteRepository {

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
            } else {
                Result.failure(mapHttpError(response.code(), "Không thể tạo ghi chú."))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Lỗi kết nối mạng khi tạo ghi chú.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(noteId: String, content: String): Result<Note> =
        withContext(ioDispatcher) {
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
                    val note = dto.toDomain(lessonId = restoredLessonId)
                    noteDao.upsertNote(note.toEntity(userId = restoredUserId))
                    Result.success(note)
                } else {
                    Result.failure(mapHttpError(response.code(), "Không thể cập nhật ghi chú."))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Lỗi kết nối mạng khi cập nhật ghi chú.", e))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteNote(noteId: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val response = noteApiService.deleteNote(noteId)
                if (response.isSuccessful) {
                    noteDao.deleteNoteById(noteId)
                    Result.success(Unit)
                } else {
                    Result.failure(mapHttpError(response.code(), "Không thể xóa ghi chú."))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Lỗi kết nối mạng khi xóa ghi chú.", e))
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

