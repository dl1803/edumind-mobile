package com.edumind.app.ui.player.pip

import kotlinx.coroutines.flow.StateFlow

class PipPlayerController(
    val isPlaying: () -> Boolean,
    val togglePlayPause: () -> Unit,
    val seekBackward: () -> Unit
)

interface PipHost {
    val isPipSupported: Boolean
    val isInPipMode: StateFlow<Boolean>
    fun bindPipPlayer(controller: PipPlayerController?)
    fun enterPip()
    fun updatePipActions(isPlaying: Boolean)
}
