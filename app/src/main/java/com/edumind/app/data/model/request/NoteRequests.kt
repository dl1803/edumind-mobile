package com.edumind.app.data.model.request

import com.google.gson.annotations.SerializedName

data class CreateNoteRequest(
    @SerializedName("content")
    val content: String,

    @SerializedName("timestamp")
    val timestamp: Float
)

data class UpdateNoteRequest(
    @SerializedName("content")
    val content: String
)

