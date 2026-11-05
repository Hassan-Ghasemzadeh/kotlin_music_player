package com.sftwr.cleandevelopment.musicplayer.library.core.manager

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
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
                    val song = activeQueue[currentIndex]
                    _currentSong.value = song
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

    suspend fun stopPlayback() {
        withContext(Dispatchers.Main) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
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

    private fun SongUiModel.toMediaItem(artwork: ByteArray? = null): MediaItem {
        val artUri = albumArtUri?.takeIf { it.isNotBlank() }?.toUri()
        return MediaItem.Builder()
            .setUri(uri.toUri())
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .apply {
                        if (artwork != null) setArtworkData(
                            artwork,
                            MediaMetadata.PICTURE_TYPE_FRONT_COVER
                        )
                        else if (artUri != null) setArtworkUri(artUri)
                    }
                    .build()
            )
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadArtworkBytes(song: SongUiModel): ByteArray? {
        runCatching {
            MediaMetadataRetriever().use { r ->
                r.setDataSource(context, song.uri.toUri())
                r.embeddedPicture?.let { return it }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val bmp = context.contentResolver.loadThumbnail(
                    song.uri.toUri(), Size(512, 512), null
                )
                val out = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                return out.toByteArray()
            }
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun setQueue(songs: List<SongUiModel>, startIndex: Int = 0) {
        val processedQueue = withContext(Dispatchers.Default) {
            songs.map { song -> editedSongsCache[song.id] ?: song }
        }

        if (processedQueue.isEmpty() || startIndex !in processedQueue.indices) {
            return
        }

        val mediaItems = withContext(Dispatchers.IO) {
            processedQueue.map { it.toMediaItem(loadArtworkBytes(it)) }
        }

        startPlaybackService()
        activeQueue = processedQueue

        withContext(Dispatchers.Main) {
            exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
            exoPlayer.prepare()
            exoPlayer.play()

            if (startIndex in activeQueue.indices) {
                _currentSong.value = activeQueue[startIndex]
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun playSongById(songId: String, songsList: List<SongUiModel>? = null) {
        val targetSongs = songsList ?: _allSongs.value
        val index = targetSongs.indexOfFirst { it.id == songId }

        if (index != -1) {
            val currentPlaying = currentSong.value
            if (currentPlaying?.id == songId) {
                togglePlayPause()
                return
            }
            setQueue(targetSongs, startIndex = index)
        }
    }

    fun playSong(song: SongUiModel) {
        startPlaybackService()
        val finalSong = editedSongsCache[song.id] ?: song
        _currentSong.value = finalSong
        activeQueue = listOf(finalSong)

        val mediaItem = finalSong.toMediaItem()
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
        val targetIdStr = songId.toString()
        val updatedSong =
            (_allSongs.value.find { it.id == targetIdStr } ?: currentSong.value)?.copy(
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

                val updatedMediaItem = activeSong.toMediaItem()
                exoPlayer.replaceMediaItem(currentIndex, updatedMediaItem)
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
                val mediaItem = song.toMediaItem()
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            } else {
                exoPlayer.play()
            }
            startPlaybackService()
        }
    }
}