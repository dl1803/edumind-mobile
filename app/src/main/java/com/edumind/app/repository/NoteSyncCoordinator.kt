package com.edumind.app.repository

import com.edumind.app.data.local.prefs.TokenManager
import com.edumind.app.util.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteSyncCoordinator @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val noteRepository: NoteRepository,
    private val tokenManager: TokenManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            networkUtils.isOnline
                .filter { it }
                .collect {
                    if (!tokenManager.getAccessToken().isNullOrBlank()) {
                        scope.launch { noteRepository.syncPendingNotes() }
                    }
                }
        }
    }
}

