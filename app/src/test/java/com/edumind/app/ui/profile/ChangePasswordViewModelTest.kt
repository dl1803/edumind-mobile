package com.edumind.app.ui.profile

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {

    private val profileRepository: ProfileRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ChangePasswordViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ChangePasswordViewModel(profileRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fieldChanges_updateUiStateCorrectly() {
        viewModel.onCurrentPasswordChange("OldPass@123")
        viewModel.onNewPasswordChange("NewPass@456")
        viewModel.onConfirmPasswordChange("NewPass@456")

        val state = viewModel.uiState.value
        assertEquals("OldPass@123", state.currentPassword)
        assertEquals("NewPass@456", state.newPassword)
        assertEquals("NewPass@456", state.confirmPassword)
    }

    @Test
    fun visibilityToggles_flipBooleanStates() {
        assertFalse(viewModel.uiState.value.isCurrentPasswordVisible)
        assertFalse(viewModel.uiState.value.isNewPasswordVisible)
        assertFalse(viewModel.uiState.value.isConfirmPasswordVisible)

        viewModel.toggleCurrentPasswordVisibility()
        viewModel.toggleNewPasswordVisibility()
        viewModel.toggleConfirmPasswordVisibility()

        val stateAfter = viewModel.uiState.value
        assertTrue(stateAfter.isCurrentPasswordVisible)
        assertTrue(stateAfter.isNewPasswordVisible)
        assertTrue(stateAfter.isConfirmPasswordVisible)

        viewModel.toggleCurrentPasswordVisibility()
        assertFalse(viewModel.uiState.value.isCurrentPasswordVisible)
    }

    @Test
    fun validation_newPasswordCriteria() {
        viewModel.onNewPasswordChange("short1")
        assertFalse(viewModel.uiState.value.hasMinLength)
        assertTrue(viewModel.uiState.value.hasLetterAndDigit)
        assertFalse(viewModel.uiState.value.isNewPasswordValid)

        viewModel.onNewPasswordChange("passwordwithnonumbers")
        assertTrue(viewModel.uiState.value.hasMinLength)
        assertFalse(viewModel.uiState.value.hasLetterAndDigit)
        assertFalse(viewModel.uiState.value.isNewPasswordValid)

        viewModel.onNewPasswordChange("ValidPass123")
        assertTrue(viewModel.uiState.value.hasMinLength)
        assertTrue(viewModel.uiState.value.hasLetterAndDigit)
        assertTrue(viewModel.uiState.value.isNewPasswordValid)
    }

    @Test
    fun validation_confirmPasswordMatching() {
        viewModel.onNewPasswordChange("ValidPass123")
        viewModel.onConfirmPasswordChange("DiffPass123")
        assertFalse(viewModel.uiState.value.isConfirmMatching)

        viewModel.onConfirmPasswordChange("ValidPass123")
        assertTrue(viewModel.uiState.value.isConfirmMatching)
    }

    @Test
    fun validation_isSaveEnabledLogic() {
        assertFalse(viewModel.uiState.value.isSaveEnabled)

        // Same current and new password
        viewModel.onCurrentPasswordChange("ValidPass123")
        viewModel.onNewPasswordChange("ValidPass123")
        viewModel.onConfirmPasswordChange("ValidPass123")
        assertFalse(viewModel.uiState.value.isSaveEnabled)

        // Valid different new password
        viewModel.onNewPasswordChange("NewPass123")
        viewModel.onConfirmPasswordChange("NewPass123")
        assertTrue(viewModel.uiState.value.isSaveEnabled)
    }

    @Test
    fun changePassword_whenSaveDisabled_doesNotCallRepository() = runTest(testDispatcher) {
        viewModel.onCurrentPasswordChange("Pass")
        viewModel.changePassword()

        coVerify(exactly = 0) { profileRepository.changePassword(any(), any()) }
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun changePassword_success_clearsFieldsAndSetsSuccess() = runTest(testDispatcher) {
        coEvery { profileRepository.changePassword("OldPass123", "NewPass123") } returns Result.success(Unit)

        viewModel.onCurrentPasswordChange("OldPass123")
        viewModel.onNewPasswordChange("NewPass123")
        viewModel.onConfirmPasswordChange("NewPass123")
        viewModel.changePassword()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertEquals("", state.currentPassword)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
        assertNull(state.currentPasswordError)
        assertNull(state.errorMessage)
    }

    @Test
    fun changePassword_failure_currentPasswordWrong_setsCurrentPasswordError() = runTest(testDispatcher) {
        val errorMsg = "Mật khẩu hiện tại không chính xác."
        coEvery { profileRepository.changePassword(any(), any()) } returns Result.failure(Exception(errorMsg))

        viewModel.onCurrentPasswordChange("WrongPass123")
        viewModel.onNewPasswordChange("NewPass123")
        viewModel.onConfirmPasswordChange("NewPass123")
        viewModel.changePassword()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals(errorMsg, state.currentPasswordError)
        assertNull(state.errorMessage)
    }

    @Test
    fun changePassword_failure_generalError_setsErrorMessage() = runTest(testDispatcher) {
        val errorMsg = "Lỗi kết nối máy chủ."
        coEvery { profileRepository.changePassword(any(), any()) } returns Result.failure(Exception(errorMsg))

        viewModel.onCurrentPasswordChange("OldPass123")
        viewModel.onNewPasswordChange("NewPass123")
        viewModel.onConfirmPasswordChange("NewPass123")
        viewModel.changePassword()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals(errorMsg, state.errorMessage)
        assertNull(state.currentPasswordError)
    }

    @Test
    fun resetSuccess_resetsFlagToFalse() = runTest(testDispatcher) {
        coEvery { profileRepository.changePassword(any(), any()) } returns Result.success(Unit)

        viewModel.onCurrentPasswordChange("OldPass123")
        viewModel.onNewPasswordChange("NewPass123")
        viewModel.onConfirmPasswordChange("NewPass123")
        viewModel.changePassword()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSuccess)

        viewModel.resetSuccess()
        assertFalse(viewModel.uiState.value.isSuccess)
    }
}
