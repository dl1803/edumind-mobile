package com.edumind.app.data.local.prefs

import android.content.SharedPreferences
import com.edumind.app.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val prefs: SharedPreferences
) {
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(Constants.ACCESS_TOKEN_KEY, accessToken)
            .putString(Constants.REFRESH_TOKEN_KEY, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(Constants.ACCESS_TOKEN_KEY, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(Constants.REFRESH_TOKEN_KEY, null)
    }

    fun clearTokens() {
        prefs.edit()
            .remove(Constants.ACCESS_TOKEN_KEY)
            .remove(Constants.REFRESH_TOKEN_KEY)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }
}