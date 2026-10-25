package com.edumind.app.ui.profile

import com.edumind.app.data.model.response.UserProfile
import com.edumind.app.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    private val profileRepository: ProfileRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: EditProfileViewModel

    private val initialUser = UserProfile(
        id = "user-123",
        fullName = "Nguyễn Văn A",
        email = "vana@edumind.edu.vn",
        avatarUrl = "https://example.com/avatar_original.jpg",
        role = "student"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadCurrentProfile_success_populatesUserData() = runTest(testDispatcher) {
        coEvery { profileRepository.getProfile() } returns Result.success(initialUser)

        viewModel = EditProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Nguyễn Văn A", state.fullName)
        assertEquals("vana@edumind.edu.vn", state.email)
        assertEquals("https://example.com/avatar_original.jpg", state.currentAvatarUrl)
        assertFalse(state.isInitialLoading)
        assertNull(state.errorMessage)
        assertTrue(state.isSaveEnabled)
    }

    @Test
    fun init_loadCurrentProfile_failure_setsErrorMessage() = runTest(testDispatcher) {
        val errorMessage = "Không thể tải thông tin hồ sơ."
        coEvery { profileRepository.getProfile() } returns Result.failure(Exception(errorMessage))

        viewModel = EditProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isInitialLoading)
        assertEquals(errorMessage, state.errorMessage)
    }

    @Test
    fun onFullNameChange_validName_updatesStateAndClearsError() = runTest(testDispatcher) {
        coEvery { profileRepository.getProfile() } returns Result.success(initialUser)
        viewModel = EditProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFullNameChange("Trần Thị B")

        val state = viewModel.uiState.value
        assertEquals("Trần Thị B", state.fullName)
        assertNull(state.fullNameError)
        assertTrue(state.isSaveEnabled)
    }

    @Test
    fun onFullNameChange_invalidName_setsFullNameErrorAndDisablesSave() = runTest(testDispatcher) {
        coEvery { profileRepository.getProfile() } returns Result.success(initialUser)
        viewModel = EditProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFullNameChange("")

        val state = viewModel.uiState.value
        assertEquals("", state.fullName)
        assertNotNull(state.fullNameError)
        assertFalse(state.isSaveEnabled)
    }

    @Test
    fun onAvatarSelected_updatesSelectedAvatarUri() = runTest(testDispatcher) {
        coEvery { profileRepository.getProfile() } returns Result.success(initialUser)
        viewModel = EditProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAvatarSelected("content://media/external/images/media/42")

        val state = viewModel.uiState.value
        assertEquals("content://media/external/images/media/42", state.selectedAvatarUri)
    }

    @Test
    fun saveProfile_whenFullNameInvalid_doesNotCallRepository() = runTest(testDispatcher) {
        coEvery { profileRepository.getProfile() } returns Result.success(initialUser)
        viewModel = EditProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFullNameChange("")
        viewModel.saveProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { profileRepository.updateProfile(any(), any()) }
        assertFalse(viewModel.uiState.value.isSaveSuccess)
    }

    @Test
    fun saveProfile_success_updatesUserAndSetsIsSaveSuccess() = runTest(testDispatcher) {
        val updatedUser = initialUser.copy(
            fullName = "Nguyễn Văn Đổi Mới",
            avatarUrl = "https://example.com/new_avatar.jpg"
        )
        coEvery { profileRepository.getProfile() } returns Result.success(initialUser)
        coEvery {
            profileRepository.updateProfile("Nguyễn Văn Đổi Mới", "https://example.com/new_avatar.jpg")
        } returns Result.success(updatedUser)

        viewModel = EditProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFullNameChange("Nguyễn Văn Đổi Mới")
        viewModel.onAvatarSelected("https://example.com/new_avatar.jpg")
        viewModel.saveProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertTrue(state.isSaveSuccess)
        assertEquals("Nguyễn Văn Đổi Mới", state.fullName)
        assertEquals("https://example.com/new_avatar.jpg", state.currentAvatarUrl)
        assertNull(state.selectedAvatarUri)
        assertNull(state.errorMessage)
    }

    @Test
    fun saveProfile_failure_setsErrorMessage() = runTest(testDispatcher) {
        val failureMessage = "Cập nhật hồ sơ thất bại."
        coEvery { profileRepository.getProfile() } returns Result.success(initialUser)
        coEvery {
            profileRepository.updateProfile(any(), any())
        } returns Result.failure(Exception(failureMessage))

        viewModel = EditProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFullNameChange("Nguyễn Văn Đổi Mới")
        viewModel.saveProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertFalse(state.isSaveSuccess)
        assertEquals(failureMessage, state.errorMessage)
    }

    @Test
    fun resetSaveSuccess_resetsFlagToFalse() = runTest(testDispatcher) {
        val updatedUser = initialUser.copy(fullName = "Trần Văn C")
        coEvery { profileRepository.getProfile() } returns Result.success(initialUser)
        coEvery { profileRepository.updateProfile(any(), any()) } returns Result.success(updatedUser)

        viewModel = EditProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFullNameChange("Trần Văn C")
        viewModel.saveProfile()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSaveSuccess)

        viewModel.resetSaveSuccess()
        assertFalse(viewModel.uiState.value.isSaveSuccess)
    }
}
