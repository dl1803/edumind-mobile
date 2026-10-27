package com.edumind.app.ui.profile

data class EditProfileUiState(
    val initialFullName: String = "",
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
    val hasChanges: Boolean
        get() = (fullName.trim() != initialFullName.trim()) || (selectedAvatarUri != null)

    val isSaveEnabled: Boolean
        get() = hasChanges &&
                fullName.trim().length >= 2 &&
                fullNameError == null &&
                !isSaving &&
                !isInitialLoading
}
