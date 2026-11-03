package com.sftwr.cleandevelopment.musicplayer.library.core.manager

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.DeleteSongUseCase
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import com.sftwr.cleandevelopment.musicplayer.library.service.MusicPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    val exoPlayer: ExoPlayer,
    private val deleteSongUseCase: DeleteSongUseCase,
    @param:ApplicationContext private val context: Context,
) {

    private val _currentSong = MutableStateFlow<SongUiModel?>(null)
    val currentSong: StateFlow<SongUiModel?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var activeQueue: List<SongUiModel> = emptyList()
    val hasNext: Boolean get() = exoPlayer.hasNextMediaItem()
    val hasPrevious: Boolean get() = exoPlayer.hasPreviousMediaItem()

    private val editedSongsCache = mutableMapOf<String, SongUiModel>()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                _isPlaying.value = isPlayingState
            }

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

    fun setSongs(songs: List<SongUiModel>) {
        val mergedSongs = songs.map { song -> editedSongsCache[song.id] ?: song }
        if (mergedSongs.isNotEmpty() || _allSongs.value.isEmpty()) {
            _allSongs.value = mergedSongs
        }
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

    private fun startPlaybackService() {
        try {
            val intent = Intent(context, MusicPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun SongUiModel.toMediaItem(): MediaItem {
        val artUri = albumArtUri?.toUri() ?: if (albumId != -1L) {
            "content://media/external/audio/albumart/$albumId".toUri()
        } else {
            null
        }

        return MediaItem.Builder()
            .setUri(uri.toUri())
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(artUri)
                    .build()
            )
            .build()
    }

    fun setQueue(songs: List<SongUiModel>, startIndex: Int = 0) {
        startPlaybackService() // استارت امن سرویس برای اجرای پس‌زمینه
        activeQueue = songs.map { song -> editedSongsCache[song.id] ?: song }
        val mediaItems = activeQueue.map { song -> song.toMediaItem() }

        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        exoPlayer.seekTo(startIndex, 0L)
        exoPlayer.play()

        if (startIndex in activeQueue.indices) {
            _currentSong.value = activeQueue[startIndex]
        }
    }

    fun playSong(song: SongUiModel) {
        startPlaybackService() // استارت امن سرویس برای اجرای پس‌زمینه
        val finalSong = editedSongsCache[song.id] ?: song
        _currentSong.value = finalSong
        activeQueue = listOf(finalSong)

        exoPlayer.setMediaItem(finalSong.toMediaItem())
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
        val targetIdStr = songId.toString()
        val updatedSong = (_allSongs.value.find { it.id == targetIdStr } ?: currentSong.value)?.copy(
            title = newTitle,
            artist = newArtist,
            album = newAlbum,
            year = newYear,
            genre = newGenre
        ) ?: SongUiModel(
            id = targetIdStr,
            title = newTitle,
            artist = newArtist,
            album = newAlbum,
            duration = "",
            uri = "",
            year = newYear,
            genre = newGenre
        )

        editedSongsCache[targetIdStr] = updatedSong

        var found = false
        _allSongs.value = _allSongs.value.map { song ->
            if (song.id.toLongOrNull() == songId || song.id == targetIdStr) {
                found = true
                updatedSong
            } else {
                song
            }
        }

        if (!found) {
            _allSongs.value += updatedSong
        }

        activeQueue = activeQueue.map { song ->
            if (song.id.toLongOrNull() == songId || song.id == targetIdStr) updatedSong else song
        }

        val current = _currentSong.value
        if (current?.id?.toLongOrNull() == songId || current?.id == targetIdStr) {
            _currentSong.value = updatedSong
        }

        val currentIndex = exoPlayer.currentMediaItemIndex
        if (currentIndex in activeQueue.indices) {
            val activeSong = activeQueue[currentIndex]
            if (activeSong.id.toLongOrNull() == songId || activeSong.id == targetIdStr) {
                val isPlayingNow = exoPlayer.isPlaying
                val currentPosition = exoPlayer.currentPosition

                exoPlayer.replaceMediaItem(currentIndex, activeSong.toMediaItem())
                exoPlayer.seekTo(currentIndex, currentPosition)
                if (isPlayingNow) {
                    exoPlayer.play()
                }
            }
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.currentMediaItem == null && _currentSong.value != null) {
                val song = _currentSong.value!!
                activeQueue = listOf(song)
                exoPlayer.setMediaItem(song.toMediaItem())
                exoPlayer.prepare()
            }
            startPlaybackService()
            exoPlayer.play()
        }
    }
}