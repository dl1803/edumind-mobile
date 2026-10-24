package com.edumind.app.ui.profile

data class EditProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val currentAvatarUrl: String? = null,
    val selectedAvatarUri: String? = null,
    val fullNameError: String? = null,
    val isInitialLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val isSaveEnabled: Boolean
        get() = fullName.trim().length >= 2 &&
                fullNameError == null &&
                !isSaving &&
                !isInitialLoading
}
