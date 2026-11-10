package com.sftwr.cleandevelopment.musicplayer.library.core.manager

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SongPreferencesRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.DeleteSongUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import com.sftwr.cleandevelopment.musicplayer.library.service.MusicPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.sftwr.cleandevelopment.musicplayer.library.core.equalizer.EqualizerAudioFxManager

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi

@Singleton
@OptIn(UnstableApi::class)
class PlayerManager @Inject constructor(
    val exoPlayer: ExoPlayer,
    private val deleteSongUseCase: DeleteSongUseCase,
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase,
    private val songPreferencesRepository: SongPreferencesRepository,
    private val playlistRepository: PlaylistRepository,
    val musicRepository: MusicRepository,
    val equalizerAudioFxManager: EqualizerAudioFxManager,
    @param:ApplicationContext private val context: Context,
) {

    private val mainHandler = Handler(Looper.getMainLooper())

    private inline fun runOnMainThread(crossinline action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post { action() }
        }
    }

    private val _currentSong = MutableStateFlow<SongUiModel?>(null)
    val currentSong: StateFlow<SongUiModel?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var activeQueue: List<SongUiModel> = emptyList()
    val hasNext: Boolean
        get() {
            if (Looper.myLooper() != Looper.getMainLooper()) return false
            if (exoPlayer.hasNextMediaItem()) return true
            val songs = if (activeQueue.isNotEmpty()) activeQueue else _allSongs.value
            val current = _currentSong.value ?: return false
            val index = songs.indexOfFirst { it.id == current.id }
            return index != -1 && index < songs.size - 1
        }
    val hasPrevious: Boolean
        get() {
            if (Looper.myLooper() != Looper.getMainLooper()) return false
            if (exoPlayer.hasPreviousMediaItem()) return true
            val songs = if (activeQueue.isNotEmpty()) activeQueue else _allSongs.value
            val current = _currentSong.value ?: return false
            val index = songs.indexOfFirst { it.id == current.id }
            return index > 0
        }

    private val editedSongsCache = mutableMapOf<String, SongUiModel>()
    private val artworkScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val playerScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var isRestoredOnStartup = false
    private var artworkJob: Job? = null
    private var queueJob: Job? = null
    private var isSettingQueue = false

    init {
        playerScope.launch(Dispatchers.IO) {
            runCatching {
                musicRepository.syncWithDevice()
            }
        }

        playerScope.launch {
            getDeviceSongsUseCase().collect { domainSongs ->
                val songUiModels = domainSongs.map { song ->
                    SongUiModel(
                        id = song.id.toString(),
                        title = song.title,
                        artist = song.artist,
                        duration = song.duration.formatTime(),
                        uri = song.path,
                        year = song.year,
                        album = song.album,
                        genre = song.genre,
                        albumId = song.albumId,
                        albumArtUri = song.albumArtUri,
                        isFavorite = song.isFavorite,
                    )
                }
                setSongs(songUiModels)
            }
        }

        playerScope.launch {
            _currentSong.collect { song ->
                if (song != null) {
                    songPreferencesRepository.saveLastPlayedSongId(song.id)
                } else {
                    songPreferencesRepository.clearLastPlayedSongId()
                }
            }
        }

        var lastRecordedId: String? = null

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                _isPlaying.value = isPlayingState
                if (isPlayingState) {
                    val sessionId = try { exoPlayer.audioSessionId } catch (_: Exception) { 0 }
                    if (sessionId > 0) {
                        equalizerAudioFxManager.attachAudioSession(sessionId)
                    }
                    _currentSong.value?.let { song ->
                        if (lastRecordedId != song.id) {
                            lastRecordedId = song.id
                            song.id.toLongOrNull()?.let { songId ->
                                playerScope.launch(Dispatchers.IO) {
                                    playlistRepository.recordSongPlayed(songId)
                                }
                            }
                        }
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (isSettingQueue) return
                val sessionId = try { exoPlayer.audioSessionId } catch (_: Exception) { 0 }
                if (sessionId > 0) {
                    equalizerAudioFxManager.attachAudioSession(sessionId)
                }
                val currentIndex = exoPlayer.currentMediaItemIndex
                if (currentIndex in activeQueue.indices) {
                    val song = activeQueue[currentIndex]
                    _currentSong.value = song
                    loadArtworkForIndex(currentIndex)
                    if (exoPlayer.isPlaying && lastRecordedId != song.id) {
                        lastRecordedId = song.id
                        song.id.toLongOrNull()?.let { songId ->
                            playerScope.launch(Dispatchers.IO) {
                                playlistRepository.recordSongPlayed(songId)
                            }
                        }
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    checkAndPlayNextOrStop()
                }
            }
        })
    }

    fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        playerScope.launch(Dispatchers.IO) {
            playlistRepository.toggleFavorite(songId, isFavorite)
        }
    }

    private val artworkCache = mutableMapOf<String, ByteArray>()

    private fun loadArtworkBytes(song: SongUiModel): ByteArray? {
        artworkCache[song.id]?.let { return it }

        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, song.uri.toUri())
                retriever.embeddedPicture
            } finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    retriever.close()
                } else {
                    retriever.release()
                }
            }
        }.getOrNull()?.also { bytes ->
            artworkCache[song.id] = bytes
        }
    }

    private fun loadArtworkForIndex(index: Int) {
        artworkJob?.cancel()
        val song = activeQueue.getOrNull(index) ?: return
        artworkJob = artworkScope.launch {
            val artBytes = loadArtworkBytes(song)
            if (artBytes != null) {
                val updatedMediaItem = song.toMediaItem(artBytes)
                withContext(Dispatchers.Main) {
                    if (activeQueue.getOrNull(index) == song && exoPlayer.currentMediaItemIndex == index) {
                        exoPlayer.replaceMediaItem(index, updatedMediaItem)
                    }
                }
            }
        }
    }

    private val _allSongs = MutableStateFlow<List<SongUiModel>>(emptyList())
    val allSongs: StateFlow<List<SongUiModel>> = _allSongs.asStateFlow()

    fun setSongs(songs: List<SongUiModel>) {
        val mergedSongs = songs.map { song -> editedSongsCache[song.id] ?: song }
        if (mergedSongs.isNotEmpty() || _allSongs.value.isEmpty()) {
            _allSongs.value = mergedSongs
        }
        _currentSong.value?.let { current ->
            val updatedCurrent = mergedSongs.find { it.id == current.id }
            if (updatedCurrent != null) {
                _currentSong.value = updatedCurrent
            }
        }
        restoreLastPlayedSongIfNeeded()
    }

    /**
     * Retrieves the persisted last played song ID from storage.
     */
    suspend fun getSavedLastPlayedSongId(): String? {
        return songPreferencesRepository.getLastPlayedSongId()
    }

    /**
     * Checks for a persisted last played song ID and restores the song into [_currentSong]
     * state without auto-playing or preparing ExoPlayer unless playback is explicitly triggered.
     */
    private fun restoreLastPlayedSongIfNeeded() {
        if (_currentSong.value != null || isRestoredOnStartup) return
        if (_allSongs.value.isEmpty()) return

        playerScope.launch {
            val lastSongId = withContext(Dispatchers.IO) {
                songPreferencesRepository.getLastPlayedSongId()
            }
            if (!lastSongId.isNullOrEmpty() && _currentSong.value == null) {
                val matchingSong = _allSongs.value.find { it.id == lastSongId }
                if (matchingSong != null && _currentSong.value == null) {
                    _currentSong.value = matchingSong
                    isRestoredOnStartup = true
                } else if (matchingSong == null) {
                    isRestoredOnStartup = true
                }
            } else {
                isRestoredOnStartup = true
            }
        }
    }

    private fun checkAndPlayNextOrStop() {
        runOnMainThread {
            if (exoPlayer.hasNextMediaItem()) {
                exoPlayer.seekToNextMediaItem()
                exoPlayer.play()
            } else {
                exoPlayer.seekTo(0)
                exoPlayer.pause()
            }
        }
    }

    suspend fun stopPlayback() {
        queueJob?.cancel()
        artworkJob?.cancel()
        withContext(Dispatchers.Main) {
            equalizerAudioFxManager.releaseEffects()
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            _currentSong.value = null
            _isPlaying.value = false
            activeQueue = emptyList()
        }
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
        runOnMainThread {
            if (exoPlayer.hasNextMediaItem()) {
                exoPlayer.seekToNextMediaItem()
                exoPlayer.play()
            } else {
                val songs = if (activeQueue.isNotEmpty()) activeQueue else _allSongs.value
                val current = _currentSong.value
                if (songs.isNotEmpty() && current != null) {
                    val currentIndex = songs.indexOfFirst { it.id == current.id }
                    if (currentIndex != -1 && currentIndex + 1 in songs.indices) {
                        val nextSong = songs[currentIndex + 1]
                        val nextIndex = currentIndex + 1
                        activeQueue = songs
                        val mediaItems = songs.map { artworkCache[it.id].let { art -> it.toMediaItem(art) } }
                        _currentSong.value = nextSong
                        exoPlayer.setMediaItems(mediaItems, nextIndex, 0L)
                        exoPlayer.prepare()
                        exoPlayer.play()
                        startPlaybackService()
                        loadArtworkForIndex(nextIndex)
                    }
                }
            }
        }
    }

    fun playPrevious() {
        runOnMainThread {
            if (exoPlayer.hasPreviousMediaItem()) {
                exoPlayer.seekToPreviousMediaItem()
                exoPlayer.play()
            } else {
                val songs = if (activeQueue.isNotEmpty()) activeQueue else _allSongs.value
                val current = _currentSong.value
                if (songs.isNotEmpty() && current != null) {
                    val currentIndex = songs.indexOfFirst { it.id == current.id }
                    if (currentIndex != -1 && currentIndex - 1 in songs.indices) {
                        val prevSong = songs[currentIndex - 1]
                        val prevIndex = currentIndex - 1
                        activeQueue = songs
                        val mediaItems = songs.map { artworkCache[it.id].let { art -> it.toMediaItem(art) } }
                        _currentSong.value = prevSong
                        exoPlayer.setMediaItems(mediaItems, prevIndex, 0L)
                        exoPlayer.prepare()
                        exoPlayer.play()
                        startPlaybackService()
                        loadArtworkForIndex(prevIndex)
                    }
                }
            }
        }
    }

    fun seekTo(position: Long) {
        runOnMainThread {
            exoPlayer.seekTo(position)
        }
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

    private fun SongUiModel.toMediaItem(artworkBytes: ByteArray? = null): MediaItem {
        val artUri = uri.toUri()

        return MediaItem.Builder()
            .setUri(uri.toUri())
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .apply {
                        if (artworkBytes != null) {
                            setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                        } else {
                            setArtworkUri(artUri)
                        }
                    }
                    .build()
            )
            .build()
    }

    suspend fun setQueue(songs: List<SongUiModel>, startIndex: Int = 0) {
        queueJob?.cancel()

        val (processedQueue, isSameQueue) = withContext(Dispatchers.Default) {
            val mapped = songs.map { song -> editedSongsCache[song.id] ?: song }
            val same = activeQueue.size == mapped.size &&
                    activeQueue.map { it.id } == mapped.map { it.id }
            Pair(mapped, same)
        }

        if (processedQueue.isEmpty() || startIndex !in processedQueue.indices) {
            return
        }

        startPlaybackService()
        activeQueue = processedQueue
        val clickedSong = processedQueue[startIndex]

        val mediaItems = withContext(Dispatchers.Default) {
            processedQueue.map { song ->
                val artBytes = artworkCache[song.id]
                song.toMediaItem(artBytes)
            }
        }

        withContext(Dispatchers.Main) {
            if (isSameQueue && exoPlayer.mediaItemCount > 0) {
                try {
                    isSettingQueue = true
                    _currentSong.value = clickedSong
                    exoPlayer.seekTo(startIndex, 0L)
                    exoPlayer.play()
                } finally {
                    isSettingQueue = false
                }
            } else {
                try {
                    isSettingQueue = true
                    exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
                    exoPlayer.prepare()
                    exoPlayer.play()
                    _currentSong.value = clickedSong
                } finally {
                    isSettingQueue = false
                }
            }
            loadArtworkForIndex(startIndex)
        }
    }

    suspend fun playSongById(songId: String, songsList: List<SongUiModel>? = null) {
        val targetSongs = songsList ?: _allSongs.value
        val index = targetSongs.indexOfFirst { it.id == songId }

        if (index != -1) {
            val currentPlaying = currentSong.value
            if (currentPlaying?.id == songId) {
                withContext(Dispatchers.Main) {
                    togglePlayPause()
                }
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
        runOnMainThread {
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
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

        runOnMainThread {
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
    }

    fun togglePlayPause() {
        runOnMainThread {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                if (exoPlayer.currentMediaItem == null && _currentSong.value != null) {
                    val song = _currentSong.value!!
                    val targetQueue = if (activeQueue.isNotEmpty()) activeQueue else if (_allSongs.value.isNotEmpty()) _allSongs.value else listOf(song)
                    val index = targetQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                    activeQueue = targetQueue
                    val mediaItems = targetQueue.map { artworkCache[it.id].let { art -> it.toMediaItem(art) } }
                    exoPlayer.setMediaItems(mediaItems, index, 0L)
                    exoPlayer.prepare()
                    exoPlayer.play()
                } else {
                    exoPlayer.play()
                }
                startPlaybackService()
            }
        }
    }
}