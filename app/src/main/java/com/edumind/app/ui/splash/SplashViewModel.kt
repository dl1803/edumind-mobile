package com.edumind.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.local.datastore.AppPreferences
import com.edumind.app.data.local.prefs.TokenManager
import com.edumind.app.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashDestination {
    data object Home : SplashDestination
    data object Onboarding : SplashDestination
    data object Login : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val appPreferences: AppPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = MutableSharedFlow<SplashDestination>()
    val destination: SharedFlow<SplashDestination> = _destination.asSharedFlow()

    fun checkInitialRoute() {
        viewModelScope.launch {
            // Giữ màn hình tối thiểu 1500ms để hiệu ứng animation và thương hiệu được hiển thị trọn vẹn
            val minDisplayDelay = launch { delay(1500) }

            // Kiểm tra trạng thái đã đăng nhập
            val hasToken = tokenManager.isLoggedIn()

            // Đợi animation hoàn thành
            minDisplayDelay.join()

            if (hasToken) {
                // Có refresh token -> gọi refresh token
                val refreshResult = authRepository.refreshToken()
                if (refreshResult.isSuccess) {
                    _destination.emit(SplashDestination.Home)
                } else {
                    // Refresh thất bại -> xóa token, chuyển về Login
                    tokenManager.clearTokens()
                    _destination.emit(SplashDestination.Login)
                }
            } else {
                val hasSeenOnboarding = appPreferences.hasSeenOnboarding.first()
                if (hasSeenOnboarding) {
                    _destination.emit(SplashDestination.Login)
                } else {
                    _destination.emit(SplashDestination.Onboarding)
                }
            }
        }
    }
}
