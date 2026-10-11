package com.edumind.app.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bảng lưu cache danh sách và chi tiết khóa học cho chế độ Offline
 */
@Entity(tableName = "courses_cache")
data class CourseCache(
    @PrimaryKey
    @ColumnInfo(name = "course_id")
    val courseId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "instructor_name")
    val instructorName: String,

    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String?,

    @ColumnInfo(name = "price_vnd")
    val priceVnd: Long,

    @ColumnInfo(name = "raw_json")
    val rawJson: String, // Lưu toàn bộ payload JSON để deserialize nhanh khi offline

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
