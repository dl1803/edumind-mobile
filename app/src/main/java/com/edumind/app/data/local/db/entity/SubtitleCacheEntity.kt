package com.edumind.app.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subtitle_cache")
data class SubtitleCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "lesson_id")
    val lessonId: String,

    @ColumnInfo(name = "segments_json")
    val segmentsJson: String
)
