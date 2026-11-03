package com.sftwr.cleandevelopment.musicplayer.library.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MusicPlaybackServiceEntryPoint {
    fun exoPlayer(): ExoPlayer
}

@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "music_playback_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val player = EntryPointAccessors.fromApplication(
            applicationContext,
            MusicPlaybackServiceEntryPoint::class.java
        ).exoPlayer()

        val activityIntent = packageManager.getLaunchIntentForPackage(packageName)
        val sessionBuilder = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())

        if (activityIntent != null) {
            sessionBuilder.setSessionActivity(
                PendingIntent.getActivity(
                    this, 0, activityIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }

        val session = sessionBuilder.build()
        mediaSession = session
        addSession(session)

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setNotificationId(NOTIFICATION_ID)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setChannelName(com.sftwr.cleandevelopment.musicplayer.home.R.string.media_notification_channel_name)
                .build()
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(com.sftwr.cleandevelopment.musicplayer.home.R.string.media_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls and metadata display"
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updated = mediaItems.map { item ->
                item.buildUpon()
                    .setUri(item.requestMetadata.mediaUri ?: item.localConfiguration?.uri)
                    .build()
            }.toMutableList()
            return Futures.immediateFuture(updated)
        }
    }
}