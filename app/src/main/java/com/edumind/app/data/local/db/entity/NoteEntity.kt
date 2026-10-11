package com.edumind.app.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bảng lưu trữ Ghi chú theo bài học và mốc thời gian video
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "lesson_id")
    val lessonId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "timestamp_seconds")
    val timestampSeconds: Int,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = true // false nếu tạo lúc offline và đang chờ đồng bộ lên BE
)
