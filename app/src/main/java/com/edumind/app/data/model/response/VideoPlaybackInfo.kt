package com.edumind.app.data.model.response

data class VideoPlaybackInfo(
    val lessonId: String,
    val hlsUrl: String,
    val expiresAt: Long
)

