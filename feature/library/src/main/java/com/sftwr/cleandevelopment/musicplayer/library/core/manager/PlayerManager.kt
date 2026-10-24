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
import androidx.core.net.toUri
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.DeleteSongUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

@Singleton
class PlayerManager @Inject constructor(
    val exoPlayer: ExoPlayer,
    private val deleteSongUseCase: DeleteSongUseCase,
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase,
) {

    // Currently playing song state
    private val _currentSong = MutableStateFlow<SongUiModel?>(null)
    val currentSong: StateFlow<SongUiModel?> = _currentSong.asStateFlow()

    // Play/Pause state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Internal active queue list for tracking songs
    private var activeQueue: List<SongUiModel> = emptyList()
    val hasNext: Boolean
        get() = exoPlayer.hasNextMediaItem()
    val hasPrevious: Boolean
        get() = exoPlayer.hasPreviousMediaItem()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                _isPlaying.value = isPlayingState
            }

            // Triggered when media item transitions automatically or manually
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

    private val _allSongs = MutableStateFlow<List<SongUiModel>>(emptyList())
    val allSongs: StateFlow<List<SongUiModel>> = _allSongs.asStateFlow()

    // قرار دادن لیست اولیه آهنگ‌ها (مثلاً موقع بالا آمدن برنامه)
    fun setSongs(songs: List<SongUiModel>) {
        _allSongs.value = songs
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
     * Stop playback, clear media items, and reset current song state.
     */
    fun stopPlayback() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _currentSong.value = null
        _isPlaying.value = false
        activeQueue = emptyList()
    }

    suspend fun deleteSong(songId: Long): DeleteResult {
        val result = deleteSongUseCase(songId)

        if (result is DeleteResult.Success) {
            val currentPlayingId = currentSong.value?.id
            if (currentPlayingId == songId.toString()) {
                stopPlayback()
            }
        }

        return result
    }

    fun playNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
            exoPlayer.play()
        }
    }

    fun playPrevious() {
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
            exoPlayer.play()
        }
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    /**
     * Set the entire playback queue
     */
    fun setQueue(songs: List<SongUiModel>, startIndex: Int = 0) {
        activeQueue = songs

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setUri(song.uri.toUri())
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
        activeQueue = listOf(song)

        val audioUri = song.uri.toUri()
        val mediaItem = MediaItem.Builder()
            .setUri(audioUri)
            .setMediaId(song.id)
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }


    fun updateSongMetadata(
        songId: Long,
        newTitle: String,
        newArtist: String,
        newAlbum: String,
        newYear: String,
        newGenre: String
    ) {
        _allSongs.value = _allSongs.value.map { song ->
            if (song.id.toLongOrNull() == songId || song.id == songId.toString()) {
                song.copy(
                    title = newTitle,
                    artist = newArtist,
                    album = newAlbum,
                    year = newYear,
                    genre = newGenre
                )
            } else {
                song
            }
        }

        activeQueue = activeQueue.map { song ->
            if (song.id.toLongOrNull() == songId || song.id == songId.toString()) {
                song.copy(
                    title = newTitle,
                    artist = newArtist,
                    album = newAlbum,
                    year = newYear,
                    genre = newGenre
                )
            } else {
                song
            }
        }

        val current = _currentSong.value
        if (current?.id?.toLongOrNull() == songId || current?.id == songId.toString()) {
            _currentSong.value = current.copy(
                title = newTitle,
                artist = newArtist,
                album = newAlbum,
                year = newYear,
                genre = newGenre
            )
        }

        val currentIndex = exoPlayer.currentMediaItemIndex
        if (currentIndex in activeQueue.indices) {
            val activeSong = activeQueue[currentIndex]
            if (activeSong.id.toLongOrNull() == songId || activeSong.id == songId.toString()) {
                val isPlayingNow = exoPlayer.isPlaying
                val currentPosition = exoPlayer.currentPosition

                val newMediaItem = MediaItem.Builder()
                    .setUri(activeSong.uri.toUri())
                    .setMediaId(activeSong.id)
                    .build()

                exoPlayer.replaceMediaItem(currentIndex, newMediaItem)
                exoPlayer.seekTo(currentIndex, currentPosition)
                if (isPlayingNow) {
                    exoPlayer.play()
                }
            }
        }
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