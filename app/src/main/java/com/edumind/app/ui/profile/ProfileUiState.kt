package com.edumind.app.ui.profile

import com.edumind.app.data.model.response.UserProfile

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Success(
        val user: UserProfile,
        val isLoggingOut: Boolean = false,
        val unreadNotificationCount: Int = 3
    ) : ProfileUiState

    data class Error(val message: String) : ProfileUiState
}
