package com.edumind.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.local.datastore.AppPreferences
import com.edumind.app.data.local.prefs.TokenManager
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
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _destination = MutableSharedFlow<SplashDestination>()
    val destination: SharedFlow<SplashDestination> = _destination.asSharedFlow()

    fun checkInitialRoute() {
        viewModelScope.launch {
            // Giữ màn hình tối thiểu 1500ms để hiệu ứng animation và thương hiệu được hiển thị trọn vẹn
            val minDisplayDelay = launch { delay(1500) }

            // 1. Kiểm tra trạng thái đã đăng nhập
            val hasToken = tokenManager.isLoggedIn()

            // Đợi animation hoàn thành
            minDisplayDelay.join()

            if (hasToken) {
                // Đã có token -> Đi thẳng vào trang chủ
                _destination.emit(SplashDestination.Home)
            } else {
                // 2. Chưa có token -> Kiểm tra xem đã từng xem Onboarding chưa
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
