package com.edumind.app.data.model.request

import com.google.gson.annotations.SerializedName

data class SaveProgressRequest(
    @SerializedName("lesson_id")
    val lessonId: String,

    @SerializedName("current_position")
    val currentPosition: Float,

    @SerializedName("total_duration")
    val totalDuration: Float,

    @SerializedName("completed")
    val completed: Boolean = false
)

