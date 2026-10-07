package com.edumind.app.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class SessionEvent {
    object Expired : SessionEvent()
}

@Singleton
class SessionEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity =  1)
    val  events = _events.asSharedFlow()

    fun emitSessionExpired() {
        _events.tryEmit(SessionEvent.Expired)
    }
}