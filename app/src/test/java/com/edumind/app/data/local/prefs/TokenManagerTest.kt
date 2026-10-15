package com.edumind.app.data.local.prefs

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TokenManagerTest {

    private class FakeTokenStorage : TokenStorage {
        var access: String? = null
        var refresh: String? = null

        override fun saveTokens(accessToken: String, refreshToken: String) {
            access = accessToken
            refresh = refreshToken
        }

        override fun getAccessToken(): String? = access

        override fun getRefreshToken(): String? = refresh

        override fun clearTokens() {
            access = null
            refresh = null
        }
    }

    private lateinit var fakeStorage: FakeTokenStorage
    private lateinit var tokenManager: TokenManager

    @Before
    fun setup() {
        fakeStorage = FakeTokenStorage()
        tokenManager = TokenManager(fakeStorage)
    }

    @Test
    fun saveTokens_storesBothTokensCorrectly() {
        tokenManager.saveTokens("mock_access", "mock_refresh")
        assertEquals("mock_access", tokenManager.getAccessToken())
        assertEquals("mock_refresh", tokenManager.getRefreshToken())
    }

    @Test
    fun isLoggedIn_returnsTrueWhenAccessTokenExists() {
        tokenManager.saveTokens("token_xyz", "refresh_xyz")
        assertTrue(tokenManager.isLoggedIn())
    }

    @Test
    fun isLoggedIn_returnsFalseWhenAccessTokenIsNull() {
        assertFalse(tokenManager.isLoggedIn())
    }

    @Test
    fun clearTokens_removesBothKeys() {
        tokenManager.saveTokens("mock_access", "mock_refresh")
        tokenManager.clearTokens()
        assertNull(tokenManager.getAccessToken())
        assertNull(tokenManager.getRefreshToken())
        assertFalse(tokenManager.isLoggedIn())
    }
}
