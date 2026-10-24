package com.edumind.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.repository.ProfileRepository
import com.edumind.app.util.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentProfile()
    }

    fun loadCurrentProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitialLoading = true, errorMessage = null) }
            profileRepository.getProfile().fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            fullName = user.fullName,
                            email = user.email,
                            currentAvatarUrl = user.avatarUrl,
                            isInitialLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            errorMessage = error.localizedMessage ?: "Không thể tải thông tin hồ sơ."
                        )
                    }
                }
            )
        }
    }

    fun onFullNameChange(name: String) {
        val validation = Validators.validateFullName(name)
        _uiState.update {
            it.copy(
                fullName = name,
                fullNameError = if (validation.isValid) null else validation.errorMessage,
                errorMessage = null
            )
        }
    }

    fun onAvatarSelected(uri: String?) {
        _uiState.update {
            it.copy(
                selectedAvatarUri = uri,
                errorMessage = null
            )
        }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val validation = Validators.validateFullName(currentState.fullName)
        if (!validation.isValid) {
            _uiState.update { it.copy(fullNameError = validation.errorMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            profileRepository.updateProfile(
                fullName = currentState.fullName,
                avatarUrl = currentState.selectedAvatarUri ?: currentState.currentAvatarUrl
            ).fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            fullName = updatedUser.fullName,
                            currentAvatarUrl = updatedUser.avatarUrl,
                            selectedAvatarUri = null,
                            isSaving = false,
                            isSaveSuccess = true
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.localizedMessage ?: "Cập nhật hồ sơ thất bại."
                        )
                    }
                }
            )
        }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(isSaveSuccess = false) }
    }
}
