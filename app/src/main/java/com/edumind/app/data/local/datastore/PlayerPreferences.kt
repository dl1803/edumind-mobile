package com.edumind.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.playerDataStore: DataStore<Preferences> by preferencesDataStore(name = "player_preferences")

enum class SubtitleFontSize(val textSp: Int, val lineHeightSp: Int) {
    SMALL(14, 20),
    MEDIUM(16, 22),
    LARGE(18, 24)
}

@Singleton
class PlayerPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences: Flow<Preferences> = context.playerDataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val subtitleEnabled: Flow<Boolean> = preferences.map { it[SUBTITLE_ENABLED] ?: true }

    val subtitleFontSize: Flow<SubtitleFontSize> = preferences.map { prefs ->
        SubtitleFontSize.entries.firstOrNull { it.name == prefs[SUBTITLE_FONT_SIZE] }
            ?: SubtitleFontSize.MEDIUM
    }

    suspend fun setSubtitleEnabled(enabled: Boolean) {
        context.playerDataStore.edit { it[SUBTITLE_ENABLED] = enabled }
    }

    suspend fun setSubtitleFontSize(fontSize: SubtitleFontSize) {
        context.playerDataStore.edit { it[SUBTITLE_FONT_SIZE] = fontSize.name }
    }

    private companion object {
        val SUBTITLE_ENABLED = booleanPreferencesKey("subtitle_enabled")
        val SUBTITLE_FONT_SIZE = stringPreferencesKey("subtitle_font_size")
    }
}

