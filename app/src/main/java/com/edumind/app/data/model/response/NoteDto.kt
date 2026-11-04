package com.edumind.app.data.model.response

import com.edumind.app.data.local.db.entity.NoteEntity
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class NoteDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("timestamp")
    val timestamp: Float,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class NoteListResponse(
    @SerializedName("data")
    val data: List<NoteDto> = emptyList()
)

data class Note(
    val id: String,
    val lessonId: String,
    val content: String,
    val timestampSeconds: Float,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = true
)

fun NoteDto.toDomain(lessonId: String): Note {
    return Note(
        id = this.id,
        lessonId = lessonId,
        content = this.content,
        timestampSeconds = this.timestamp,
        createdAt = parseIsoToMillis(this.createdAt),
        updatedAt = parseIsoToMillis(this.updatedAt ?: this.createdAt),
        isSynced = true
    )
}

fun Note.toEntity(userId: String = ""): NoteEntity {
    return NoteEntity(
        id = this.id,
        lessonId = this.lessonId,
        userId = userId,
        timestampSeconds = this.timestampSeconds.toInt(),
        content = this.content,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isSynced = this.isSynced
    )
}

fun NoteEntity.toDomain(): Note {
    return Note(
        id = this.id,
        lessonId = this.lessonId,
        content = this.content,
        timestampSeconds = this.timestampSeconds.toFloat(),
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isSynced = this.isSynced
    )
}

private fun parseIsoToMillis(isoString: String?): Long {
    if (isoString.isNullOrBlank()) return System.currentTimeMillis()
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        format.parse(isoString)?.time ?: System.currentTimeMillis()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

