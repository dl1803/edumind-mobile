package com.edumind.app.ui.player

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsManifest
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.edumind.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PlayerViewModel"

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(context))
        .build()

    private val _uiState = MutableStateFlow(PocPlayerState())
    val uiState: StateFlow<PocPlayerState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null
    private var controlsTimeoutJob: Job? = null

    init {
        setupPlayerListener()
        load(Constants.SAMPLE_HLS_URL)
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startPositionTicker()
                    scheduleControlsAutoHide()
                } else {
                    stopPositionTicker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                val isHls = exoPlayer.currentManifest is HlsManifest

                if (playbackState == Player.STATE_READY) {
                    Log.d(TAG, "ExoPlayer READY - HlsManifest: $isHls, duration: ${exoPlayer.duration}ms")
                }

                _uiState.update {
                    it.copy(
                        playbackState = playbackState,
                        isBuffering = isBuffering,
                        durationMs = exoPlayer.duration.coerceAtLeast(0L),
                        isHlsManifest = isHls
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "ExoPlayer Error: ${error.errorCodeName} - ${error.message}", error)
                _uiState.update {
                    it.copy(
                        errorMessage = "Lỗi phát video: ${error.localizedMessage ?: "Vui lòng thử lại"}",
                        isBuffering = false
                    )
                }
            }
        })
    }

    fun load(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        scheduleControlsAutoHide()
    }

    fun pause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
        exoPlayer.seekTo(clamped)
        _uiState.update { it.copy(positionMs = clamped) }
        scheduleControlsAutoHide()
    }

    fun seekRelative(offsetMs: Long) {
        val target = exoPlayer.currentPosition + offsetMs
        seekTo(target)
    }

    fun toggleControlsVisibility() {
        val nextVisible = !_uiState.value.isControlsVisible
        _uiState.update { it.copy(isControlsVisible = nextVisible) }
        if (nextVisible && _uiState.value.isPlaying) {
            scheduleControlsAutoHide()
        }
    }

    private fun scheduleControlsAutoHide() {
        controlsTimeoutJob?.cancel()
        controlsTimeoutJob = viewModelScope.launch {
            delay(4000)
            _uiState.update { it.copy(isControlsVisible = false) }
        }
    }

    private fun startPositionTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                _uiState.update {
                    it.copy(
                        positionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                    )
                }
                delay(500)
            }
        }
    }

    private fun stopPositionTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPositionTicker()
        controlsTimeoutJob?.cancel()
        exoPlayer.release()
        Log.d(TAG, "ExoPlayer instance has been safely released.")
    }
}
