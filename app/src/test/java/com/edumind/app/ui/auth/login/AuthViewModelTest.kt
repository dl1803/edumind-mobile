package com.edumind.app.ui.auth.login

import app.cash.turbine.test
import com.edumind.app.data.model.response.LoginResponse
import com.edumind.app.data.model.response.UserProfile
import com.edumind.app.repository.AuthRepository
import com.edumind.app.util.AppException
import com.edumind.app.util.ClockProvider
import com.edumind.app.util.NetworkException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val authRepository: AuthRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private var simulatedTime: Long = 1000000L

    private lateinit var clockProvider: ClockProvider
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        clockProvider = ClockProvider { simulatedTime }
        viewModel = AuthViewModel(
            authRepository = authRepository,
            clockProvider = clockProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_success_updatesUiStateToSuccessAndResetsFailedAttempts() = runTest {
        val mockResponse = LoginResponse(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresIn = 900,
            tokenType = "Bearer",
            user = UserProfile(id = "u1", fullName = "Lợi", email = "test@edumind.edu.vn", avatarUrl = null, role = "student")
        )
        coEvery { authRepository.login("test@edumind.edu.vn", "ValidPass@123") } returns Result.success(mockResponse)

        viewModel.onEmailChanged("test@edumind.edu.vn")
        viewModel.onPasswordChanged("ValidPass@123")

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isLoginSuccess)
        assertFalse(state.isLocked)
        assertNull(state.generalErrorMessage)
    }

    @Test
    fun login_validationFailed_emitsTriggerShakeAndSetsFieldErrors() = runTest {
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("short")

        viewModel.eventFlow.test {
            viewModel.login()
            val event = awaitItem()
            assertTrue(event is LoginUiEvent.TriggerShake)
        }

        val state = viewModel.uiState.value
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        assertFalse(state.isLoading)
    }

    @Test
    fun login_failed5Times_locksAccountFor15Minutes() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(
            AppException(httpCode = 401, errorCode = "INVALID_CREDENTIALS", message = "Sai mật khẩu")
        )

        viewModel.onEmailChanged("test@edumind.edu.vn")
        viewModel.onPasswordChanged("WrongPass@123")

        for (i in 1..4) {
            viewModel.login()
            testDispatcher.scheduler.advanceUntilIdle()
            assertFalse("Lần $i chưa được khóa", viewModel.uiState.value.isLocked)
        }

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        val lockedState = viewModel.uiState.value
        assertTrue("Lần thứ 5 phải bị khóa", lockedState.isLocked)
        assertEquals(900L, lockedState.lockRemainingSeconds)
        assertEquals("Tài khoản bị khóa tạm thời 15 phút do nhập sai 5 lần.", lockedState.generalErrorMessage)
    }

    @Test
    fun login_after15MinutesPassed_autoUnlocksAccount() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(
            AppException(httpCode = 401, errorCode = "INVALID_CREDENTIALS", message = "Sai mật khẩu")
        )

        viewModel.onEmailChanged("test@edumind.edu.vn")
        viewModel.onPasswordChanged("WrongPass@123")

        for (i in 1..5) {
            viewModel.login()
            testDispatcher.scheduler.advanceUntilIdle()
        }
        assertTrue(viewModel.uiState.value.isLocked)

        // Giả lập tua thời gian qua 16 phút
        simulatedTime += 16 * 60 * 1000L

        val mockSuccess = LoginResponse(
            accessToken = "token",
            refreshToken = "refresh",
            expiresIn = 900,
            tokenType = "Bearer",
            user = UserProfile(id = "u1", fullName = "Lợi", email = "test@edumind.edu.vn", avatarUrl = null, role = "student")
        )
        coEvery { authRepository.login(any(), any()) } returns Result.success(mockSuccess)

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        val unlockedState = viewModel.uiState.value
        assertFalse(unlockedState.isLocked)
        assertTrue(unlockedState.isLoginSuccess)
    }

    @Test
    fun login_whenLockedAndNotExpired_blocksLoginAndShakes() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(
            AppException(httpCode = 401, errorCode = "INVALID_CREDENTIALS", message = "Sai mật khẩu")
        )

        viewModel.onEmailChanged("test@edumind.edu.vn")
        viewModel.onPasswordChanged("WrongPass@123")

        for (i in 1..5) {
            viewModel.login()
            testDispatcher.scheduler.advanceUntilIdle()
        }
        assertTrue(viewModel.uiState.value.isLocked)

        // Thời gian mới trôi qua 5 phút (chưa đủ 15 phút)
        simulatedTime += 5 * 60 * 1000L

        viewModel.eventFlow.test {
            viewModel.login()
            val event = awaitItem()
            assertTrue(event is LoginUiEvent.TriggerShake)
        }

        assertTrue(viewModel.uiState.value.isLocked)
        assertEquals(600L, viewModel.uiState.value.lockRemainingSeconds)
    }

    @Test
    fun login_serverReturns429_locksAccountAndShowsDialog() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(
            AppException(httpCode = 429, errorCode = "OTP_ATTEMPTS_EXCEEDED", message = "Quá nhiều yêu cầu")
        )

        viewModel.onEmailChanged("test@edumind.edu.vn")
        viewModel.onPasswordChanged("ValidPass@123")

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLocked)
        assertTrue(state.showAccountLockedDialog)
        assertEquals(900L, state.lockRemainingSeconds)
    }

    @Test
    fun login_serverReturns403EmailNotVerified_showsUnverifiedSnackbar() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(
            AppException(httpCode = 403, errorCode = "EMAIL_NOT_VERIFIED", message = "Chưa kích hoạt")
        )

        viewModel.onEmailChanged("unverified@edumind.edu.vn")
        viewModel.onPasswordChanged("ValidPass@123")

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showUnverifiedSnackbar)
    }

    @Test
    fun login_serverReturns403AccountLocked_showsAccountLockedDialog() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(
            AppException(httpCode = 403, errorCode = "ACCOUNT_LOCKED", message = "Tài khoản bị khóa")
        )

        viewModel.onEmailChanged("locked@edumind.edu.vn")
        viewModel.onPasswordChanged("ValidPass@123")

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAccountLockedDialog)
    }

    @Test
    fun login_networkException_emitsToastAndSetsErrorMessage() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(
            NetworkException("Không có kết nối mạng")
        )

        viewModel.onEmailChanged("test@edumind.edu.vn")
        viewModel.onPasswordChanged("ValidPass@123")

        viewModel.eventFlow.test {
            viewModel.login()
            val event = awaitItem()
            assertTrue(event is LoginUiEvent.ShowToast)
            assertEquals("Không có kết nối mạng", (event as LoginUiEvent.ShowToast).message)
        }

        assertEquals("Không có kết nối mạng", viewModel.uiState.value.generalErrorMessage)
    }
}
