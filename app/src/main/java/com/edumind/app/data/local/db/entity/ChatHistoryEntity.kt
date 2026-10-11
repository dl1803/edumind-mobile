package com.edumind.app.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bảng lưu trữ lịch sử tin nhắn hỏi đáp với AI theo từng bài học
 */
@Entity(tableName = "chat_history")
data class ChatHistoryEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "lesson_id")
    val lessonId: String,

    @ColumnInfo(name = "role")
    val role: String, // "user" hoặc "assistant"

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "citations_json")
    val citationsJson: String? = null, // JSON chuỗi các mốc trích dẫn timestamp

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
