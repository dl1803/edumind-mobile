package com.edumind.app

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.edumind.app.ui.navigation.AppNavGraph
import com.edumind.app.ui.player.pip.PipHost
import com.edumind.app.ui.player.pip.PipPlayerController
import com.edumind.app.ui.theme.EduMindTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val ACTION_PIP_TOGGLE_PLAY = "com.edumind.app.action.PIP_TOGGLE_PLAY"
private const val ACTION_PIP_REWIND = "com.edumind.app.action.PIP_REWIND"
private const val REQUEST_PIP_TOGGLE_PLAY = 1
private const val REQUEST_PIP_REWIND = 2

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PipHost {

    private val _isInPipMode = MutableStateFlow(false)
    override val isInPipMode: StateFlow<Boolean> = _isInPipMode.asStateFlow()

    override val isPipSupported: Boolean
        get() = packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    private var pipPlayer: PipPlayerController? = null
    private var isPipReceiverRegistered = false

    private val pipActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val player = pipPlayer ?: return
            when (intent?.action) {
                ACTION_PIP_TOGGLE_PLAY -> player.togglePlayPause()
                ACTION_PIP_REWIND -> player.seekBackward()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduMindTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }

    override fun bindPipPlayer(controller: PipPlayerController?) {
        pipPlayer = controller
    }

    override fun enterPip() {
        val player = pipPlayer ?: return
        if (!isPipSupported || _isInPipMode.value) return
        _isInPipMode.value = true
        val entered = runCatching {
            enterPictureInPictureMode(buildPipParams(player.isPlaying()))
        }.getOrDefault(false)
        if (!entered) _isInPipMode.value = false
    }

    override fun updatePipActions(isPlaying: Boolean) {
        if (!_isInPipMode.value) return
        setPictureInPictureParams(buildPipParams(isPlaying))
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (pipPlayer?.isPlaying?.invoke() == true) enterPip()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        _isInPipMode.value = isInPictureInPictureMode
        if (isInPictureInPictureMode) registerPipReceiver() else unregisterPipReceiver()
    }

    override fun onStop() {
        super.onStop()
        if (_isInPipMode.value) {
            _isInPipMode.value = false
            unregisterPipReceiver()
        }
    }

    override fun onDestroy() {
        unregisterPipReceiver()
        pipPlayer = null
        super.onDestroy()
    }

    private fun buildPipParams(isPlaying: Boolean): PictureInPictureParams {
        val toggleTitle = if (isPlaying) "Tạm dừng" else "Phát"
        val toggleIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val toggleAction = RemoteAction(
            Icon.createWithResource(this, toggleIcon),
            toggleTitle,
            toggleTitle,
            pipPendingIntent(ACTION_PIP_TOGGLE_PLAY, REQUEST_PIP_TOGGLE_PLAY)
        )
        val rewindAction = RemoteAction(
            Icon.createWithResource(this, android.R.drawable.ic_media_rew),
            "Lùi 10 giây",
            "Lùi 10 giây",
            pipPendingIntent(ACTION_PIP_REWIND, REQUEST_PIP_REWIND)
        )
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setActions(listOf(toggleAction, rewindAction))
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(isPlaying)
        }
        return builder.build()
    }

    private fun pipPendingIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            this,
            requestCode,
            Intent(action).setPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun registerPipReceiver() {
        if (isPipReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(ACTION_PIP_TOGGLE_PLAY)
            addAction(ACTION_PIP_REWIND)
        }
        ContextCompat.registerReceiver(
            this,
            pipActionReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        isPipReceiverRegistered = true
    }

    private fun unregisterPipReceiver() {
        if (!isPipReceiverRegistered) return
        unregisterReceiver(pipActionReceiver)
        isPipReceiverRegistered = false
    }
}
