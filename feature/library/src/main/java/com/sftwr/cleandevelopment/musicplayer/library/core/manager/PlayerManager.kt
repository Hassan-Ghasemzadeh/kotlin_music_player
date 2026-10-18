package com.sftwr.cleandevelopment.musicplayer.library.core.manager

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    val exoPlayer: ExoPlayer,
) {

    // Currently playing song state
    private val _currentSong = MutableStateFlow<SongUiModel?>(null)
    val currentSong: StateFlow<SongUiModel?> = _currentSong.asStateFlow()

    // Play/Pause state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // لیست صف پخش داخلی برای ردیابی آهنگ‌ها
    private var activeQueue: List<SongUiModel> = emptyList()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                _isPlaying.value = isPlayingState
            }

            // وقتی آهنگ به صورت خودکار یا دستی عوض می‌شود، این متد صدا زده می‌شود
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val currentIndex = exoPlayer.currentMediaItemIndex
                if (currentIndex in activeQueue.indices) {
                    _currentSong.value = activeQueue[currentIndex]
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    checkAndPlayNextOrStop()
                }
            }
        })
    }

    private fun checkAndPlayNextOrStop() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
            exoPlayer.play()
        } else {
            exoPlayer.seekTo(0)
            exoPlayer.pause()
        }
    }

    /**
     * تنظیم کل صف پخشی (به جای setPlaylist از این نام واضح‌تر استفاده شد)
     */
    fun setQueue(songs: List<SongUiModel>, startIndex: Int = 0) {
        activeQueue = songs

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setUri(Uri.parse(song.uri))
                .setMediaId(song.id)
                .build()
        }

        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        exoPlayer.seekTo(startIndex, 0L)
        exoPlayer.play()

        if (startIndex in songs.indices) {
            _currentSong.value = songs[startIndex]
        }
    }

    /**
     * Play a selected song individually if queue is not set.
     */
    fun playSong(song: SongUiModel) {
        _currentSong.value = song
        activeQueue = listOf(song) // اگر تک آهنگ بود، صف را روی همین یکی تنظیم می‌کنیم

        val audioUri = Uri.parse(song.uri)
        val mediaItem = MediaItem.fromUri(audioUri)

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    /**
     * Toggle play/pause state.
     */
    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }
}