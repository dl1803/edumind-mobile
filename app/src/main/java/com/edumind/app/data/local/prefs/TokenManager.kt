package com.edumind.app.data.local.prefs

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val storage: TokenStorage
) {
    constructor(prefs: SharedPreferences) : this(EncryptedTokenStorage(prefs))

    fun saveTokens(accessToken: String, refreshToken: String) {
        storage.saveTokens(accessToken, refreshToken)
    }

    fun getAccessToken(): String? {
        return storage.getAccessToken()
    }

    fun getRefreshToken(): String? {
        return storage.getRefreshToken()
    }

    fun clearTokens() {
        storage.clearTokens()
    }

    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }
}