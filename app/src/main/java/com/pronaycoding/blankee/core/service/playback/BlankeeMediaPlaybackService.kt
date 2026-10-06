package com.pronaycoding.blankee.core.service.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.pronaycoding.blankee.MainActivity
import com.pronaycoding.blankee.R
import org.koin.android.ext.android.inject

class BlankeeMediaPlaybackService : Service() {
    private val globalPlaybackState: GlobalPlaybackState by inject()

    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()
        createChannel()
        mediaSession =
            MediaSessionCompat(this, "BlankeePlayback").apply {
                setCallback(
                    object : MediaSessionCompat.Callback() {
                        override fun onPlay() {
                            globalPlaybackState.setCanPlay(true)
                            refreshFromGlobals()
                        }

                        override fun onPause() {
                            globalPlaybackState.setCanPlay(false)
                            refreshFromGlobals()
                        }
                    },
                )
                setMediaButtonReceiver(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@BlankeeMediaPlaybackService,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE,
                    ),
                )
                isActive = true
            }
    }

    private fun refreshFromGlobals() {
        if (!globalPlaybackState.lastHasAudibleMix) return
        startForeground(
            NOTIFICATION_ID,
            buildNotification(globalPlaybackState.canPlay.value, true),
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            Intent.ACTION_MEDIA_BUTTON -> {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(globalPlaybackState.canPlay.value, globalPlaybackState.lastHasAudibleMix),
                )
                MediaButtonReceiver.handleIntent(mediaSession, intent)
                if (!globalPlaybackState.lastHasAudibleMix) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf(startId)
                    return START_NOT_STICKY
                }
            }
            ACTION_UPDATE -> {
                val canPlay = intent.getBooleanExtra(EXTRA_CAN_PLAY, true)
                val hasMix = intent.getBooleanExtra(EXTRA_HAS_MIX, false)
                if (!hasMix) {
                    // Defensive fallback: if this instance was started via startForegroundService,
                    // Android expects startForeground() before we stop.
                    startForeground(NOTIFICATION_ID, buildNotification(canPlay = false, hasMix = false))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }
                    stopSelf(startId)
                    return START_NOT_STICKY
                }
                startForeground(NOTIFICATION_ID, buildNotification(canPlay, hasMix))
            }
        }
        return START_STICKY
    }

    private fun buildNotification(
        canPlay: Boolean,
        hasMix: Boolean,
    ): Notification {
        val openApp =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                pendingIntentFlags(),
            )

        val toggle =
            PendingIntent.getBroadcast(
                this,
                1,
                Intent(this, MediaPlaybackActionReceiver::class.java).apply {
                    action = MediaPlaybackActionReceiver.ACTION_TOGGLE_PLAYBACK
                },
                pendingIntentFlags(),
            )

        val playPauseAction =
            if (canPlay) {
                NotificationCompat.Action
                    .Builder(
                        android.R.drawable.ic_media_pause,
                        getString(R.string.notification_action_pause),
                        toggle,
                    ).build()
            } else {
                NotificationCompat.Action
                    .Builder(
                        android.R.drawable.ic_media_play,
                        getString(R.string.notification_action_play),
                        toggle,
                    ).build()
            }

        val state =
            if (canPlay) {
                PlaybackStateCompat.STATE_PLAYING
            } else {
                PlaybackStateCompat.STATE_PAUSED
            }
        mediaSession.setPlaybackState(
            PlaybackStateCompat
                .Builder()
                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE,
                ).build(),
        )

        val text =
            if (canPlay) {
                getString(R.string.notification_playing)
            } else {
                getString(R.string.notification_paused)
            }

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_blankee)
            .setContentIntent(openApp)
            .setOngoing(hasMix && canPlay)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(playPauseAction)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0),
            ).build()
    }

    private fun pendingIntentFlags(): Int {
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        }
        return flags
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = getString(R.string.notification_channel_desc)
                    setShowBadge(false)
                }
            mgr?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        if (::mediaSession.isInitialized) {
            mediaSession.isActive = false
            mediaSession.release()
        }
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "blankee_playback"
        const val NOTIFICATION_ID = 7101
        private const val ACTION_UPDATE = "com.pronaycoding.blankee.action.UPDATE_PLAYBACK_NOTIFICATION"
        const val EXTRA_CAN_PLAY = "extra_can_play"
        const val EXTRA_HAS_MIX = "extra_has_mix"

        fun intentUpdate(
            context: Context,
            canPlay: Boolean,
            hasAudibleMix: Boolean,
        ): Intent =
            Intent(context, BlankeeMediaPlaybackService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_CAN_PLAY, canPlay)
                putExtra(EXTRA_HAS_MIX, hasAudibleMix)
            }
    }
}
