package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

data class SaveProgressResponse(
    @SerializedName("message")
    val message: String = "OK",

    @SerializedName("course_progress_percent")
    val courseProgressPercent: Float = 0f
)

