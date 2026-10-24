package com.edumind.app.repository

import com.edumind.app.data.model.response.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateProfile(fullName: String, avatarUrl: String? = null): Result<UserProfile>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}
