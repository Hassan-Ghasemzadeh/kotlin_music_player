package com.sftwr.cleandevelopment.musicplayer.library.core.manager

import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import java.util.concurrent.CopyOnWriteArraySet

/**
 * A [ForwardingPlayer] wrapper that supports dynamically switching its underlying
 * delegate [ExoPlayer] instance (e.g. during dual-player crossfade transitions).
 *
 * All listeners registered on this wrapper are automatically transferred and notified
 * when the delegate switches to a new [ExoPlayer] instance.
 */
@OptIn(UnstableApi::class)
class CrossfadeForwardingPlayer(
    initialPlayer: ExoPlayer
) : ForwardingPlayer(initialPlayer) {

    @Volatile
    private var activePlayer: ExoPlayer = initialPlayer
    private val listeners = CopyOnWriteArraySet<Player.Listener>()

    fun setDelegate(newPlayer: ExoPlayer) {
        if (activePlayer == newPlayer) return
        val oldPlayer = activePlayer
        activePlayer = newPlayer

        listeners.forEach { listener ->
            runCatching { oldPlayer.removeListener(listener) }
            runCatching { newPlayer.addListener(listener) }
        }

        val currentItem = newPlayer.currentMediaItem
        val isPlayingState = newPlayer.isPlaying
        listeners.forEach { listener ->
            runCatching { listener.onMediaItemTransition(currentItem, MEDIA_ITEM_TRANSITION_REASON_AUTO) }
            runCatching { listener.onIsPlayingChanged(isPlayingState) }
        }
    }

    val currentActivePlayer: ExoPlayer
        get() = activePlayer

    val audioSessionId: Int
        get() = try { activePlayer.audioSessionId } catch (_: Exception) { 0 }

    var skipSilenceEnabled: Boolean
        get() = try { activePlayer.skipSilenceEnabled } catch (_: Exception) { false }
        set(value) {
            try { activePlayer.skipSilenceEnabled = value } catch (_: Exception) {}
        }

    override fun addListener(listener: Player.Listener) {
        listeners.add(listener)
        activePlayer.addListener(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
        activePlayer.removeListener(listener)
    }

    override fun getWrappedPlayer(): Player = activePlayer

    override fun getApplicationLooper(): Looper = activePlayer.applicationLooper
    override fun play() = activePlayer.play()
    override fun pause() = activePlayer.pause()
    override fun prepare() = activePlayer.prepare()
    override fun stop() = activePlayer.stop()
    override fun release() = activePlayer.release()

    override fun isPlaying(): Boolean = activePlayer.isPlaying
    override fun getPlayWhenReady(): Boolean = activePlayer.playWhenReady
    override fun setPlayWhenReady(playWhenReady: Boolean) { activePlayer.playWhenReady = playWhenReady }
    override fun getPlaybackState(): Int = activePlayer.playbackState
    override fun getPlaybackSuppressionReason(): Int = activePlayer.playbackSuppressionReason
    override fun getDuration(): Long = activePlayer.duration
    override fun getCurrentPosition(): Long = activePlayer.currentPosition
    override fun getBufferedPosition(): Long = activePlayer.bufferedPosition
    override fun getBufferedPercentage(): Int = activePlayer.bufferedPercentage
    override fun getTotalBufferedDuration(): Long = activePlayer.totalBufferedDuration

    override fun getCurrentMediaItemIndex(): Int = activePlayer.currentMediaItemIndex
    override fun getCurrentMediaItem(): MediaItem? = activePlayer.currentMediaItem
    override fun getMediaItemCount(): Int = activePlayer.mediaItemCount
    override fun getMediaItemAt(index: Int): MediaItem = activePlayer.getMediaItemAt(index)

    override fun seekTo(mediaItemIndex: Int, positionMs: Long) = activePlayer.seekTo(mediaItemIndex, positionMs)
    override fun seekTo(positionMs: Long) = activePlayer.seekTo(positionMs)

    override fun hasNextMediaItem(): Boolean = activePlayer.hasNextMediaItem()
    override fun hasPreviousMediaItem(): Boolean = activePlayer.hasPreviousMediaItem()
    override fun seekToNextMediaItem() = activePlayer.seekToNextMediaItem()
    override fun seekToPreviousMediaItem() = activePlayer.seekToPreviousMediaItem()

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) { activePlayer.shuffleModeEnabled = shuffleModeEnabled }
    override fun getShuffleModeEnabled(): Boolean = activePlayer.shuffleModeEnabled
    override fun setRepeatMode(repeatMode: Int) { activePlayer.repeatMode = repeatMode }
    override fun getRepeatMode(): Int = activePlayer.repeatMode

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) { activePlayer.playbackParameters = playbackParameters }
    override fun getPlaybackParameters(): PlaybackParameters = activePlayer.playbackParameters

    override fun setMediaItems(mediaItems: MutableList<MediaItem>) = activePlayer.setMediaItems(mediaItems)
    override fun setMediaItems(mediaItems: MutableList<MediaItem>, resetPosition: Boolean) = activePlayer.setMediaItems(mediaItems, resetPosition)
    override fun setMediaItems(mediaItems: MutableList<MediaItem>, startIndex: Int, startPositionMs: Long) = activePlayer.setMediaItems(mediaItems, startIndex, startPositionMs)
    override fun setMediaItem(mediaItem: MediaItem) = activePlayer.setMediaItem(mediaItem)
    override fun replaceMediaItem(index: Int, mediaItem: MediaItem) = activePlayer.replaceMediaItem(index, mediaItem)
    override fun clearMediaItems() = activePlayer.clearMediaItems()
}
