package com.edumind.app.ui.player

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsManifest
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.edumind.app.data.model.response.LessonDetail
import com.edumind.app.repository.LessonAccessDeniedException
import com.edumind.app.repository.LessonNotFoundException
import com.edumind.app.repository.LessonRepository
import com.edumind.app.repository.ProgressRepository
import com.edumind.app.repository.VideoProcessingException
import com.edumind.app.ui.navigation.Screen
import com.edumind.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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

sealed interface LessonLoadState {
    data object Loading : LessonLoadState
    data class Ready(val lesson: LessonDetail) : LessonLoadState
    data class Forbidden(val message: String) : LessonLoadState
    data class Processing(val message: String) : LessonLoadState
    data class Error(val message: String) : LessonLoadState
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(context))
        .build()

    private val _uiState = MutableStateFlow(PocPlayerState())
    val uiState: StateFlow<PocPlayerState> = _uiState.asStateFlow()

    private val _loadState = MutableStateFlow<LessonLoadState>(LessonLoadState.Loading)
    val loadState: StateFlow<LessonLoadState> = _loadState.asStateFlow()

    private val _availableQualities = MutableStateFlow<List<QualityOption>>(emptyList())
    val availableQualities: StateFlow<List<QualityOption>> = _availableQualities.asStateFlow()

    private val _selectedQuality = MutableStateFlow<QualityOption?>(null)
    val selectedQuality: StateFlow<QualityOption?> = _selectedQuality.asStateFlow()

    private val _resumePromptPositionMs = MutableStateFlow<Long?>(null)
    val resumePromptPositionMs: StateFlow<Long?> = _resumePromptPositionMs.asStateFlow()

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var tickerJob: Job? = null
    private var controlsTimeoutJob: Job? = null
    private var currentLessonId: String? = null
    private var currentStreamUrl: String? = null

    init {
        setupPlayerListener()
        setupNetworkListener(context)
        val navLessonId = savedStateHandle.get<String>(Screen.ARG_LESSON_ID)
        currentLessonId = navLessonId
        if (!navLessonId.isNullOrBlank()) {
            fetchLesson(navLessonId)
        } else {
            // Tuyến dự phòng debug / preview
            load(Constants.SAMPLE_HLS_URL)
        }
    }

    private fun setupNetworkListener(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                viewModelScope.launch(Dispatchers.Main) {
                    progressRepository.syncPending()
                    if (exoPlayer.playerError != null || exoPlayer.playbackState == Player.STATE_IDLE) {
                        delay(300)
                        Log.d(TAG, "Network restored, auto recovering ExoPlayer...")
                        retryPlayback(autoPlay = exoPlayer.playWhenReady)
                    }
                }
            }
        }
        try {
            networkCallback?.let { connectivityManager?.registerDefaultNetworkCallback(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot register network callback: ${e.message}")
        }
    }

    fun fetchLesson(lessonId: String) {
        viewModelScope.launch {
            _loadState.value = LessonLoadState.Loading
            lessonRepository.getLessonDetail(lessonId).fold(
                onSuccess = { lesson ->
                    _loadState.value = LessonLoadState.Ready(lesson)
                    _uiState.update { it.copy(title = lesson.title) }

                    val streamUrl = resolvePlaybackUrl(lesson)
                    load(streamUrl)

                    val localPosition = progressRepository.getPlaybackPosition(lesson.id)
                    val candidatePositionMs = if (localPosition != null && localPosition.positionMs > 0L) {
                        localPosition.positionMs
                    } else {
                        lesson.lastPositionMs
                    }

                    val totalDurationMs = lesson.durationMs
                    val isCompleted = localPosition?.completed == true
                    val isNearStart = candidatePositionMs <= 5_000L
                    val isNearEnd = totalDurationMs > 0L && (totalDurationMs - candidatePositionMs <= 10_000L)

                    if (!isCompleted && !isNearStart && !isNearEnd) {
                        exoPlayer.pause()
                        _resumePromptPositionMs.value = candidatePositionMs
                    } else {
                        _resumePromptPositionMs.value = null
                        if (candidatePositionMs > 0L && !isCompleted) {
                            seekTo(candidatePositionMs)
                        }
                        exoPlayer.play()
                    }
                },
                onFailure = { error ->
                    when (error) {
                        is LessonAccessDeniedException -> {
                            _loadState.value = LessonLoadState.Forbidden(error.message ?: "Chưa đăng ký khóa học")
                        }
                        is VideoProcessingException -> {
                            _loadState.value = LessonLoadState.Processing(error.message ?: "Video đang xử lý")
                        }
                        is LessonNotFoundException -> {
                            _loadState.value = LessonLoadState.Error(error.message ?: "Bài giảng không tồn tại")
                        }
                        else -> {
                            _loadState.value = LessonLoadState.Error(error.localizedMessage ?: "Lỗi tải bài giảng")
                        }
                    }
                }
            )
        }
    }

    fun confirmResume() {
        val targetMs = _resumePromptPositionMs.value ?: 0L
        seekTo(targetMs)
        exoPlayer.play()
        _resumePromptPositionMs.value = null
    }

    fun restartFromBeginning() {
        seekTo(0L)
        exoPlayer.play()
        _resumePromptPositionMs.value = null
    }

    fun setQuality(option: QualityOption?) {
        _selectedQuality.value = option
        val parametersBuilder = exoPlayer.trackSelectionParameters.buildUpon()
        if (option == null) {
            exoPlayer.trackSelectionParameters = parametersBuilder
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                .build()
        } else {
            val override = TrackSelectionOverride(
                option.trackGroup,
                listOf(option.trackIndex)
            )
            exoPlayer.trackSelectionParameters = parametersBuilder
                .setOverrideForType(override)
                .build()
        }
    }

    fun saveCurrentProgress(completed: Boolean = false) {
        val lessonId = currentLessonId ?: return
        val pos = exoPlayer.currentPosition
        val dur = exoPlayer.duration
        if (dur > 0L && pos >= 0L) {
            viewModelScope.launch {
                progressRepository.saveProgress(
                    lessonId = lessonId,
                    positionMs = pos,
                    totalDurationMs = dur,
                    completed = completed
                ).onSuccess { percent ->
                    Log.d(TAG, "Progress saved successfully: $percent% (pos: ${pos}ms / ${dur}ms)")
                }.onFailure { e ->
                    Log.e(TAG, "Failed to save progress: ${e.message}")
                }
            }
        }
    }

    private fun resolvePlaybackUrl(lesson: LessonDetail): String {
        return lesson.hlsStreamUrl.ifBlank { Constants.SAMPLE_HLS_URL }
    }

    fun retry() {
        currentLessonId?.let { fetchLesson(it) }
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                val options = mutableListOf<QualityOption>()
                for (group in tracks.groups) {
                    if (group.type == C.TRACK_TYPE_VIDEO) {
                        val mediaTrackGroup = group.mediaTrackGroup
                        for (i in 0 until mediaTrackGroup.length) {
                            if (group.isTrackSupported(i)) {
                                val format = mediaTrackGroup.getFormat(i)
                                if (format.height > 0) {
                                    options.add(
                                        QualityOption(
                                            height = format.height,
                                            bitrate = format.bitrate,
                                            trackGroup = mediaTrackGroup,
                                            trackIndex = i
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                val distinctOptions = options
                    .groupBy { it.height }
                    .map { it.value.maxByOrNull { opt -> opt.bitrate } ?: it.value.first() }
                    .sortedByDescending { it.height }

                _availableQualities.value = if (distinctOptions.size >= 2) distinctOptions else emptyList()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startPositionTicker()
                    scheduleControlsAutoHide()
                } else {
                    stopPositionTicker()
                    saveCurrentProgress()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isBuffering = playbackState == Player.STATE_BUFFERING
                val isHls = exoPlayer.currentManifest is HlsManifest

                if (playbackState == Player.STATE_READY) {
                    Log.d(TAG, "ExoPlayer READY - HlsManifest: $isHls, duration: ${exoPlayer.duration}ms")
                } else if (playbackState == Player.STATE_ENDED) {
                    saveCurrentProgress(completed = true)
                }

                _uiState.update {
                    it.copy(
                        playbackState = playbackState,
                        isBuffering = isBuffering,
                        durationMs = exoPlayer.duration.coerceAtLeast(0L),
                        isHlsManifest = isHls,
                        errorMessage = if (playbackState == Player.STATE_READY) null else it.errorMessage
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
        currentStreamUrl = url
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun retryPlayback(autoPlay: Boolean = true) {
        val targetPos = exoPlayer.currentPosition.coerceAtLeast(0L)
        val url = currentStreamUrl
        if (url != null) {
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem, targetPos)
            exoPlayer.prepare()
            if (autoPlay) {
                exoPlayer.play()
            }
        } else {
            exoPlayer.prepare()
            if (targetPos > 0L) {
                exoPlayer.seekTo(targetPos)
            }
            if (autoPlay) {
                exoPlayer.play()
            }
        }
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun togglePlayPause() {
        if (exoPlayer.playerError != null || exoPlayer.playbackState == Player.STATE_IDLE) {
            retryPlayback(autoPlay = true)
        } else if (exoPlayer.isPlaying) {
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
        saveCurrentProgress()
    }

    fun seekTo(positionMs: Long) {
        val duration = exoPlayer.duration.takeIf { it > 0L } ?: _uiState.value.durationMs
        val clamped = positionMs.coerceIn(0L, duration.coerceAtLeast(0L))
        if (exoPlayer.playerError != null || exoPlayer.playbackState == Player.STATE_IDLE) {
            val url = currentStreamUrl
            if (url != null) {
                val mediaItem = MediaItem.fromUri(url)
                exoPlayer.setMediaItem(mediaItem, clamped)
                exoPlayer.prepare()
            }
        } else {
            exoPlayer.seekTo(clamped)
        }
        _uiState.update { it.copy(positionMs = clamped, errorMessage = null) }
        scheduleControlsAutoHide()
        saveCurrentProgress()
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
            var tickCount = 0
            while (isActive) {
                _uiState.update {
                    it.copy(
                        positionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                    )
                }
                tickCount++
                if (tickCount % 30 == 0) { // Mỗi 15 giây lưu 1 lần (30 * 500ms = 15s)
                    saveCurrentProgress()
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
        saveCurrentProgress()
        stopPositionTicker()
        controlsTimeoutJob?.cancel()
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot unregister network callback: ${e.message}")
        }
        exoPlayer.release()
        Log.d(TAG, "ExoPlayer instance has been safely released.")
    }
}
