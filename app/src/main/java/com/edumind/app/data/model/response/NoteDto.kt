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
) {
    val timestampMs: Long
        get() = (timestampSeconds.coerceAtLeast(0f) * 1000f).toLong()

    val formattedTimestamp: String
        get() {
            val totalSeconds = timestampSeconds.toInt().coerceAtLeast(0)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

    val tagLabel: String?
        get() {
            val trimmed = content.trim()
            return when {
                trimmed.startsWith("[Cú pháp]") -> "Cú pháp"
                trimmed.startsWith("[Lỗi thường gặp]") -> "Lỗi thường gặp"
                trimmed.startsWith("[Lưu ý quan trọng]") || trimmed.startsWith("[Lưu ý]") -> "Lưu ý quan trọng"
                else -> null
            }
        }

    val cleanContent: String
        get() {
            val trimmed = content.trim()
            return when {
                trimmed.startsWith("[Cú pháp]") -> trimmed.removePrefix("[Cú pháp]").trim()
                trimmed.startsWith("[Lỗi thường gặp]") -> trimmed.removePrefix("[Lỗi thường gặp]").trim()
                trimmed.startsWith("[Lưu ý quan trọng]") -> trimmed.removePrefix("[Lưu ý quan trọng]").trim()
                trimmed.startsWith("[Lưu ý]") -> trimmed.removePrefix("[Lưu ý]").trim()
                else -> trimmed
            }
        }
}

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

fun NoteDto.toEntity(lessonId: String, localId: String = this.id, userId: String = ""): NoteEntity {
    return NoteEntity(
        id = localId,
        lessonId = lessonId,
        userId = userId,
        timestampSeconds = this.timestamp.toInt(),
        content = this.content,
        createdAt = parseIsoToMillis(this.createdAt),
        updatedAt = parseIsoToMillis(this.updatedAt ?: this.createdAt),
        isSynced = true,
        serverId = this.id,
        isDeleted = false
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

