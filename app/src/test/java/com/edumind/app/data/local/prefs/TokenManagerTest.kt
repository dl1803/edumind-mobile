package com.edumind.app.data.local.prefs

import android.content.SharedPreferences
import com.edumind.app.util.Constants
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TokenManagerTest {

    private val prefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private lateinit var tokenManager: TokenManager

    @Before
    fun setup() {
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        tokenManager = TokenManager(prefs)
    }

    @Test
    fun saveTokens_storesBothTokensCorrectly() {
        tokenManager.saveTokens("mock_access", "mock_refresh")

        verify { editor.putString(Constants.ACCESS_TOKEN_KEY, "mock_access") }
        verify { editor.putString(Constants.REFRESH_TOKEN_KEY, "mock_refresh") }
        verify { editor.apply() }
    }

    @Test
    fun isLoggedIn_returnsTrueWhenAccessTokenExists() {
        every { prefs.getString(Constants.ACCESS_TOKEN_KEY, null) } returns "token_xyz"
        assertTrue(tokenManager.isLoggedIn())
    }

    @Test
    fun isLoggedIn_returnsFalseWhenAccessTokenIsNull() {
        every { prefs.getString(Constants.ACCESS_TOKEN_KEY, null) } returns null
        assertFalse(tokenManager.isLoggedIn())
    }

    @Test
    fun clearTokens_removesBothKeys() {
        tokenManager.clearTokens()

        verify { editor.remove(Constants.ACCESS_TOKEN_KEY) }
        verify { editor.remove(Constants.REFRESH_TOKEN_KEY) }
        verify { editor.apply() }
    }
}
