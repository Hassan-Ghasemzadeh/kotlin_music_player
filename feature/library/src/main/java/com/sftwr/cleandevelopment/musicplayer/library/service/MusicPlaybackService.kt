package com.sftwr.cleandevelopment.musicplayer.library.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.annotation.OptIn
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        val defaultBitmapLoader = DataSourceBitmapLoader(applicationContext)
        val customBitmapLoader = object : BitmapLoader {
            override fun supportsMimeType(mimeType: String): Boolean {
                return defaultBitmapLoader.supportsMimeType(mimeType)
            }

            override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
                val settable = SettableFuture.create<Bitmap>()
                try {
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    if (bitmap != null) {
                        settable.set(bitmap)
                    } else {
                        val future = defaultBitmapLoader.decodeBitmap(data)
                        Futures.addCallback(future, object : FutureCallback<Bitmap> {
                            override fun onSuccess(result: Bitmap?) { settable.set(result ?: loadDefaultBitmap()) }
                            override fun onFailure(t: Throwable) { settable.set(loadDefaultBitmap()) }
                        }, MoreExecutors.directExecutor())
                    }
                } catch (_: Exception) {
                    settable.set(loadDefaultBitmap())
                }
                return settable
            }

            override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
                val settableFuture = SettableFuture.create<Bitmap>()
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = uri.extractAlbumArt(applicationContext)
                        ?: runCatching {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                applicationContext.contentResolver.loadThumbnail(uri, Size(512, 512), null)
                            } else null
                        }.getOrNull()

                    if (bitmap != null) {
                        settableFuture.set(bitmap)
                    } else {
                        val future = defaultBitmapLoader.loadBitmap(uri)
                        Futures.addCallback(
                            future,
                            object : FutureCallback<Bitmap> {
                                override fun onSuccess(result: Bitmap?) {
                                    settableFuture.set(result ?: loadDefaultBitmap())
                                }

                                override fun onFailure(t: Throwable) {
                                    settableFuture.set(loadDefaultBitmap())
                                }
                            },
                            MoreExecutors.directExecutor()
                        )
                    }
                }
                return settableFuture
            }

            override fun loadBitmapFromMetadata(metadata: MediaMetadata): ListenableFuture<Bitmap>? {
                val artworkData = metadata.artworkData
                if (artworkData != null) {
                    val settable = SettableFuture.create<Bitmap>()
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = BitmapFactory.decodeByteArray(artworkData, 0, artworkData.size)
                        settable.set(bitmap ?: loadDefaultBitmap())
                    }
                    return settable
                }

                val artworkUri = metadata.artworkUri
                if (artworkUri != null) {
                    return loadBitmap(artworkUri)
                }

                val future = defaultBitmapLoader.loadBitmapFromMetadata(metadata)
                    ?: return Futures.immediateFuture(loadDefaultBitmap())

                val settableFuture = SettableFuture.create<Bitmap>()
                Futures.addCallback(
                    future,
                    object : FutureCallback<Bitmap> {
                        override fun onSuccess(result: Bitmap?) {
                            settableFuture.set(result ?: loadDefaultBitmap())
                        }

                        override fun onFailure(t: Throwable) {
                            settableFuture.set(loadDefaultBitmap())
                        }
                    },
                    MoreExecutors.directExecutor()
                )

                return settableFuture
            }

            private fun loadDefaultBitmap(): Bitmap {
                return BitmapFactory.decodeResource(
                    resources,
                    com.sftwr.cleandevelopment.musicplayer.home.R.drawable.ic_default_album_art
                )
            }
        }

        val activityIntent = packageManager.getLaunchIntentForPackage(packageName)
        val sessionBuilder = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .setBitmapLoader(customBitmapLoader)

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