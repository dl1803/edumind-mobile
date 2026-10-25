package com.edumind.app.ui.profile

import app.cash.turbine.test
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val profileRepository: ProfileRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ProfileViewModel

    private val sampleUser = UserProfile(
        id = "user-123",
        fullName = "Nguyễn Văn A",
        email = "vana@edumind.edu.vn",
        avatarUrl = "https://example.com/avatar.jpg",
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
    fun init_loadProfile_success_emitsSuccessStateWithUserData() = runTest(testDispatcher) {
        coEvery { profileRepository.getProfile() } returns Result.success(sampleUser)

        viewModel = ProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        val successState = state as ProfileUiState.Success
        assertEquals(sampleUser, successState.user)
        assertFalse(successState.isLoggingOut)
        assertEquals(3, successState.unreadNotificationCount)
    }

    @Test
    fun init_loadProfile_failure_emitsErrorState() = runTest(testDispatcher) {
        val errorMessage = "Không thể tải thông tin hồ sơ."
        coEvery { profileRepository.getProfile() } returns Result.failure(Exception(errorMessage))

        viewModel = ProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Error)
        val errorState = state as ProfileUiState.Error
        assertEquals(errorMessage, errorState.message)
    }

    @Test
    fun retry_reloadsProfileSuccessfully() = runTest(testDispatcher) {
        coEvery { profileRepository.getProfile() } returns Result.failure(Exception("Lỗi mạng"))

        viewModel = ProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ProfileUiState.Error)

        coEvery { profileRepository.getProfile() } returns Result.success(sampleUser)
        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        assertEquals(sampleUser, (state as ProfileUiState.Success).user)
    }

    @Test
    fun loadProfile_withShowLoadingFalse_doesNotFlashLoadingState() = runTest(testDispatcher) {
        val updatedUser = sampleUser.copy(fullName = "Nguyễn Văn Đổi Mới")
        coEvery { profileRepository.getProfile() } returns Result.success(sampleUser)

        viewModel = ProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ProfileUiState.Success)

        coEvery { profileRepository.getProfile() } returns Result.success(updatedUser)
        viewModel.loadProfile(showLoading = false)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        assertEquals("Nguyễn Văn Đổi Mới", (state as ProfileUiState.Success).user.fullName)
    }

    @Test
    fun logout_success_emitsLogoutEvent() = runTest(testDispatcher) {
        coEvery { profileRepository.getProfile() } returns Result.success(sampleUser)
        coEvery { profileRepository.logout() } returns Result.success(Unit)

        viewModel = ProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.logoutEvent.test {
            viewModel.logout()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(Unit, awaitItem())
            coVerify(exactly = 1) { profileRepository.logout() }
        }
    }

    @Test
    fun logout_failure_revertsIsLoggingOutState() = runTest(testDispatcher) {
        coEvery { profileRepository.getProfile() } returns Result.success(sampleUser)
        coEvery { profileRepository.logout() } returns Result.failure(Exception("Lỗi kết nối khi đăng xuất"))

        viewModel = ProfileViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        assertFalse((state as ProfileUiState.Success).isLoggingOut)
    }
}
