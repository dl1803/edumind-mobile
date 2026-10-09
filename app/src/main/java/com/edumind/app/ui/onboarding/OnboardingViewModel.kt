package com.edumind.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edumind.app.data.local.datastore.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _navigateToLogin = MutableSharedFlow<Unit>()
    val navigateToLogin: SharedFlow<Unit> = _navigateToLogin.asSharedFlow()

    /**
     * Được gọi khi người dùng bấm "Bỏ qua" hoặc bấm "Bắt đầu" ở slide cuối.
     * Lưu trạng thái has_seen_onboarding = true vào DataStore và phát sự kiện chuyển hướng.
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            appPreferences.setSeenOnboarding(true)
            _navigateToLogin.emit(Unit)
        }
    }

    fun complete() = completeOnboarding()
}
