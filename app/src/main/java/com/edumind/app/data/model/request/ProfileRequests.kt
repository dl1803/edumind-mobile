package com.edumind.app.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null
)

data class ChangePasswordRequest(
    @SerializedName("current_password")
    val currentPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)
