package com.edumind.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "edumind_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Khóa lưu trạng thái đã xem qua màn hình Onboarding hay chưa
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    /**
     * Flow quan sát cờ has_seen_onboarding. Mặc định là false (người dùng mới tải app).
     */
    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_SEEN_ONBOARDING] ?: false
    }

    /**
     * Đánh dấu người dùng đã hoàn thành hoặc bấm bỏ qua Onboarding.
     */
    suspend fun setSeenOnboarding(hasSeen: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING] = hasSeen
        }
    }
}
