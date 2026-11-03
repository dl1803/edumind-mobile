package com.edumind.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_positions")
data class PlaybackPositionEntity(
    @PrimaryKey
    val lessonId: String,
    val positionMs: Long,
    val durationMs: Long,
    val completed: Boolean,
    val updatedAt: Long,
    val synced: Boolean
)

