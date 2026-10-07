package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

data class RefreshResponse (
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("expires_in")
    val expiresIn: Int
)