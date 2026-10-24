package com.edumind.app.repository

import com.edumind.app.data.model.response.UserProfile
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeProfileRepository @Inject constructor() : ProfileRepository {

    private var currentProfile = UserProfile(
        id = "usr-001",
        fullName = "Nguyễn Văn Minh",
        email = "minh.nguyen@gmail.com",
        avatarUrl = null,
        role = "student",
        createdAt = "2026-08-15T08:00:00Z"
    )

    var shouldReturnError: Boolean = false

    override suspend fun getProfile(): Result<UserProfile> {
        delay(300)
        if (shouldReturnError) {
            return Result.failure(Exception("Không thể tải thông tin hồ sơ. Vui lòng thử lại."))
        }
        return Result.success(currentProfile)
    }

    override suspend fun updateProfile(fullName: String, avatarUrl: String?): Result<UserProfile> {
        delay(300)
        if (shouldReturnError) {
            return Result.failure(Exception("Không thể cập nhật hồ sơ. Vui lòng thử lại."))
        }
        currentProfile = currentProfile.copy(
            fullName = fullName.trim(),
            avatarUrl = avatarUrl ?: currentProfile.avatarUrl
        )
        return Result.success(currentProfile)
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        delay(300)
        if (shouldReturnError) {
            return Result.failure(Exception("Không thể đổi mật khẩu. Vui lòng thử lại."))
        }
        if (currentPassword == "wrong_password" || currentPassword.contains("wrong", ignoreCase = true)) {
            return Result.failure(Exception("Mật khẩu hiện tại không chính xác. Vui lòng thử lại."))
        }
        if (currentPassword == newPassword) {
            return Result.failure(Exception("Mật khẩu mới không được trùng với mật khẩu cũ."))
        }
        return Result.success(Unit)
    }

    override suspend fun logout(): Result<Unit> {
        delay(200)
        return Result.success(Unit)
    }
}
