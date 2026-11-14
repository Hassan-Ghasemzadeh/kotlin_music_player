package com.sftwr.cleandevelopment.musicplayer.library.core.manager

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.sftwr.cleandevelopment.musicplayer.domain.model.ReplayGainMode
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SongPreferencesRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.DeleteSongUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.equalizer.EqualizerAudioFxManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import com.sftwr.cleandevelopment.musicplayer.library.service.MusicPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
@OptIn(UnstableApi::class)
class PlayerManager @Inject constructor(
    @param:Named("playerA") private val playerA: ExoPlayer,
    @param:Named("playerB") private val playerB: ExoPlayer,
    private val deleteSongUseCase: DeleteSongUseCase,
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase,
    private val songPreferencesRepository: SongPreferencesRepository,
    private val playlistRepository: PlaylistRepository,
    val musicRepository: MusicRepository,
    val equalizerAudioFxManager: EqualizerAudioFxManager,
    private val getSettingsUseCase: GetSettingsUseCase,
    @param:ApplicationContext private val context: Context,
) : PlayerController {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var isAlbumArtworkEnabled = true
    @Volatile
    private var crossfadeDurationSeconds = 0

    private var currentPlayer: ExoPlayer = playerA
    private var nextPlayer: ExoPlayer = playerB

    val exoPlayer = CrossfadeForwardingPlayer(playerA)

    @Volatile
    private var isCrossfading = false
    private var crossfadeJob: Job? = null
    private var crossfadeRampJob: Job? = null

    private var currentReplayGainMode = ReplayGainMode.OFF

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private var userMasterVolume = 1.0f

    fun setVolume(vol: Float) {
        runOnMainThread {
            val clamped = vol.coerceIn(0f, 1f)
            userMasterVolume = clamped
            _volume.value = clamped
            setPlayerVolume(currentPlayer, 1.0f)
            if (isCrossfading) {
                setPlayerVolume(nextPlayer, 1.0f)
            }
        }
    }

    private val replayGainMultiplier: Float
        get() = when (currentReplayGainMode) {
            ReplayGainMode.OFF -> 1.0f
            ReplayGainMode.TRACK -> 0.90f
            ReplayGainMode.ALBUM -> 0.85f
        }

    private fun setPlayerVolume(player: ExoPlayer, volume: Float) {
        player.volume = (volume * userMasterVolume * replayGainMultiplier).coerceIn(0f, 1f)
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        runOnMainThread {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (isPlaying.value) {
                        currentPlayer.pause()
                        if (isCrossfading) nextPlayer.pause()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    setPlayerVolume(currentPlayer, 0.2f)
                    if (isCrossfading) setPlayerVolume(nextPlayer, 0.2f)
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (!isCrossfading) {
                        setPlayerVolume(currentPlayer, 1.0f)
                    }
                }
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (audioManager == null) return true
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setOnAudioFocusChangeListener(audioFocusChangeListener, mainHandler)
                .build()
            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun updateIsPlayingState() {
        val playing = currentPlayer.isPlaying || (isCrossfading && nextPlayer.isPlaying)
        _isPlaying.value = playing
    }

    init {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            getSettingsUseCase().collect { settings ->
                val prev = isAlbumArtworkEnabled
                isAlbumArtworkEnabled = settings.isAlbumArtworkEnabled
                if (prev != isAlbumArtworkEnabled) {
                    runOnMainThread {
                        val count = exoPlayer.mediaItemCount
                        if (count > 0) {
                            val currentItems = (0 until count).map { exoPlayer.getMediaItemAt(it) }
                            val newItems = currentItems.map { item ->
                                val id = item.mediaId
                                val song = activeQueue.find { it.id == id } ?: _allSongs.value.find { it.id == id }
                                song?.toMediaItem(artworkCache[song.id]) ?: item
                            }
                            val idx = exoPlayer.currentMediaItemIndex
                            val pos = exoPlayer.currentPosition
                            exoPlayer.setMediaItems(newItems.toMutableList(), idx, pos)
                            exoPlayer.prepare()
                        }
                    }
                }
            }
        }
    }

    private inline fun runOnMainThread(crossinline action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post { action() }
        }
    }

    private val _currentSong = MutableStateFlow<SongUiModel?>(null)
    override val currentSong: StateFlow<SongUiModel?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _queue = MutableStateFlow<List<SongUiModel>>(emptyList())
    val queue: StateFlow<List<SongUiModel>> = _queue.asStateFlow()

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

        playerScope.launch {
            getSettingsUseCase().collect { settings ->
                runOnMainThread {
                    val params = PlaybackParameters(settings.playbackSpeed, settings.playbackPitch)
                    playerA.playbackParameters = params
                    playerB.playbackParameters = params
                    playerA.skipSilenceEnabled = settings.skipSilenceEnabled
                    playerB.skipSilenceEnabled = settings.skipSilenceEnabled
                    currentReplayGainMode = settings.replayGainMode
                    setPlayerVolume(currentPlayer, if (isPlaying.value) 1.0f else currentPlayer.volume)
                    if (isCrossfading) {
                        setPlayerVolume(nextPlayer, 1.0f)
                    }

                    val changed = crossfadeDurationSeconds != settings.crossfadeDurationSeconds
                    crossfadeDurationSeconds = settings.crossfadeDurationSeconds
                    if (changed) {
                        if (isPlaying.value && crossfadeDurationSeconds > 0) {
                            startCrossfadeVolumeMonitor()
                        } else if (crossfadeDurationSeconds == 0) {
                            cancelCrossfade()
                        }
                    }
                }
            }
        }

        playerScope.launch {
            isPlaying.collect { playing ->
                if (playing && crossfadeDurationSeconds > 0) {
                    startCrossfadeVolumeMonitor()
                } else if (!playing) {
                    crossfadeJob?.cancel()
                    if (!isCrossfading) {
                        runOnMainThread {
                            setPlayerVolume(currentPlayer, 1.0f)
                            setPlayerVolume(nextPlayer, 1.0f)
                        }
                    }
                }
            }
        }

        val sleepTimerFilter = android.content.IntentFilter(com.sftwr.cleandevelopment.musicplayer.data.worker.SleepTimerWorker.ACTION_STOP_SLEEP_TIMER_PLAYBACK)
        val sleepTimerReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == com.sftwr.cleandevelopment.musicplayer.data.worker.SleepTimerWorker.ACTION_STOP_SLEEP_TIMER_PLAYBACK) {
                    runOnMainThread {
                        if (exoPlayer.isPlaying) {
                            currentPlayer.pause()
                            if (isCrossfading) nextPlayer.pause()
                        }
                    }
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            sleepTimerReceiver,
            sleepTimerFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        var lastRecordedId: String? = null

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                updateIsPlayingState()
                if (isPlayingState) {
                    val sessionId = exoPlayer.audioSessionId
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
                if (isSettingQueue || isCrossfading) return
                val sessionId = exoPlayer.audioSessionId
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
                if (playbackState == Player.STATE_ENDED && !isCrossfading) {
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

    private fun cancelCrossfade() {
        crossfadeRampJob?.cancel()
        crossfadeRampJob = null
        if (isCrossfading) {
            isCrossfading = false
            runOnMainThread {
                nextPlayer.pause()
                nextPlayer.clearMediaItems()
                setPlayerVolume(nextPlayer, 1.0f)
                setPlayerVolume(currentPlayer, 1.0f)
                updateIsPlayingState()
            }
        } else {
            runOnMainThread {
                setPlayerVolume(currentPlayer, 1.0f)
                setPlayerVolume(nextPlayer, 1.0f)
                updateIsPlayingState()
            }
        }
    }

    private fun getNextTrackIndex(): Int {
        if (activeQueue.isEmpty()) return -1
        if (currentPlayer.repeatMode == Player.REPEAT_MODE_ONE) {
            return currentPlayer.currentMediaItemIndex
        }
        val nextIdx = currentPlayer.nextMediaItemIndex
        return if (nextIdx != C.INDEX_UNSET && nextIdx in activeQueue.indices) {
            nextIdx
        } else {
            -1
        }
    }

    private fun startCrossfadeVolumeMonitor() {
        crossfadeJob?.cancel()
        if (crossfadeDurationSeconds <= 0) {
            cancelCrossfade()
            return
        }
        crossfadeJob = playerScope.launch {
            while (isPlaying.value && crossfadeDurationSeconds > 0) {
                runOnMainThread {
                    if (currentPlayer.isPlaying && !isCrossfading) {
                        val durationMs = currentPlayer.duration
                        val positionMs = currentPlayer.currentPosition
                        val crossfadeDurationMs = crossfadeDurationSeconds * 1000L

                        if (durationMs > 0L && crossfadeDurationMs > 0L) {
                            val remainingMs = durationMs - positionMs
                            val effectiveCrossfadeMs = minOf(crossfadeDurationMs, durationMs / 2)

                            if (remainingMs in 1..effectiveCrossfadeMs) {
                                val nextIndex = getNextTrackIndex()
                                if (nextIndex != -1 && nextIndex in activeQueue.indices) {
                                    triggerCrossfade(nextIndex, effectiveCrossfadeMs)
                                }
                            }
                        }
                    }
                }
                delay(100.milliseconds)
            }
        }
    }

    private fun triggerCrossfade(nextIndex: Int, fadeDurationMs: Long) {
        if (isCrossfading) return
        val nextSong = activeQueue.getOrNull(nextIndex) ?: return
        isCrossfading = true

        val mediaItems = activeQueue.map { song ->
            val artBytes = artworkCache[song.id]
            song.toMediaItem(artBytes)
        }

        runOnMainThread {
            requestAudioFocus()
            setPlayerVolume(nextPlayer, 0.0f)
            nextPlayer.setMediaItems(mediaItems, nextIndex, 0L)
            nextPlayer.prepare()
            nextPlayer.play()
            startPlaybackService()

            val fadingOutPlayer = currentPlayer
            currentPlayer = nextPlayer
            nextPlayer = fadingOutPlayer

            exoPlayer.setDelegate(currentPlayer)

            _currentSong.value = nextSong
            updateIsPlayingState()
            loadArtworkForIndex(nextIndex)

            val sessionId = try { currentPlayer.audioSessionId } catch (_: Exception) { 0 }
            if (sessionId > 0) {
                equalizerAudioFxManager.attachAudioSession(sessionId)
            }

            crossfadeRampJob?.cancel()
            crossfadeRampJob = playerScope.launch {
                val startTime = System.currentTimeMillis()
                while (isCrossfading) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = (elapsed.toFloat() / fadeDurationMs.toFloat()).coerceIn(0f, 1f)

                    runOnMainThread {
                        setPlayerVolume(fadingOutPlayer, 1.0f - progress)
                        setPlayerVolume(currentPlayer, progress)
                        updateIsPlayingState()
                    }

                    if (progress >= 1.0f) break
                    delay(20.milliseconds)
                }

                runOnMainThread {
                    if (isCrossfading) {
                        completeCrossfadeTransition(fadingOutPlayer)
                    }
                }
            }
        }
    }

    private fun completeCrossfadeTransition(fadingOutPlayer: ExoPlayer) {
        setPlayerVolume(fadingOutPlayer, 0.0f)
        fadingOutPlayer.pause()
        fadingOutPlayer.clearMediaItems()
        setPlayerVolume(fadingOutPlayer, 1.0f)

        setPlayerVolume(currentPlayer, 1.0f)
        isCrossfading = false

        updateIsPlayingState()

        val sessionId = try { currentPlayer.audioSessionId } catch (_: Exception) { 0 }
        if (sessionId > 0) {
            equalizerAudioFxManager.attachAudioSession(sessionId)
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
        if (activeQueue.isEmpty()) {
            _queue.value = mergedSongs
        }
        _currentSong.value?.let { current ->
            val updatedCurrent = mergedSongs.find { it.id == current.id }
            if (updatedCurrent != null) {
                _currentSong.value = updatedCurrent
            }
        }
        restoreLastPlayedSongIfNeeded()
    }

    suspend fun getSavedLastPlayedSongId(): String? {
        return songPreferencesRepository.getLastPlayedSongId()
    }

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
        cancelCrossfade()
        crossfadeJob?.cancel()
        withContext(Dispatchers.Main) {
            equalizerAudioFxManager.releaseEffects()
            playerA.stop()
            playerA.clearMediaItems()
            setPlayerVolume(playerA, 1.0f)
            playerB.stop()
            playerB.clearMediaItems()
            setPlayerVolume(playerB, 1.0f)
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

    override fun playNext() {
        runOnMainThread {
            cancelCrossfade()
            if (exoPlayer.hasNextMediaItem()) {
                exoPlayer.seekToNextMediaItem()
                requestAudioFocus()
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
                        _queue.value = songs
                        val mediaItems = songs.map { artworkCache[it.id].let { art -> it.toMediaItem(art) } }
                        _currentSong.value = nextSong
                        currentPlayer.setMediaItems(mediaItems, nextIndex, 0L)
                        currentPlayer.prepare()
                        requestAudioFocus()
                        currentPlayer.play()
                        startPlaybackService()
                        loadArtworkForIndex(nextIndex)
                    }
                }
            }
        }
    }

    override fun playPrevious() {
        runOnMainThread {
            cancelCrossfade()
            if (exoPlayer.hasPreviousMediaItem()) {
                exoPlayer.seekToPreviousMediaItem()
                requestAudioFocus()
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
                        _queue.value = songs
                        val mediaItems = songs.map { artworkCache[it.id].let { art -> it.toMediaItem(art) } }
                        _currentSong.value = prevSong
                        currentPlayer.setMediaItems(mediaItems, prevIndex, 0L)
                        currentPlayer.prepare()
                        requestAudioFocus()
                        currentPlayer.play()
                        startPlaybackService()
                        loadArtworkForIndex(prevIndex)
                    }
                }
            }
        }
    }

    fun seekTo(position: Long) {
        runOnMainThread {
            cancelCrossfade()
            currentPlayer.seekTo(position)
        }
    }

    fun setPlaybackParameters(speed: Float, pitch: Float) {
        runOnMainThread {
            exoPlayer.playbackParameters = PlaybackParameters(speed, pitch)
            if (isCrossfading) {
                nextPlayer.playbackParameters = PlaybackParameters(speed, pitch)
            }
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
        val artUri = if (isAlbumArtworkEnabled) uri.toUri() else null
        val effectiveArtworkBytes = if (isAlbumArtworkEnabled) artworkBytes else null

        return MediaItem.Builder()
            .setUri(uri.toUri())
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .apply {
                        if (effectiveArtworkBytes != null) {
                            setArtworkData(effectiveArtworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                        } else if (artUri != null) {
                            setArtworkUri(artUri)
                        }
                    }
                    .build()
            )
            .build()
    }

    suspend fun setQueue(songs: List<SongUiModel>, startIndex: Int = 0) {
        queueJob?.cancel()
        cancelCrossfade()

        val (processedQueue, isSameQueue) = withContext(Dispatchers.Default) {
            val mapped = songs.map { song -> editedSongsCache[song.id] ?: song }
            val same = activeQueue.size == mapped.size &&
                    activeQueue.map { it.id } == mapped.map { it.id }
            Pair(mapped, same)
        }

        if (processedQueue.isEmpty() || startIndex !in processedQueue.indices) {
            return
        }

        activeQueue = processedQueue
        _queue.value = processedQueue
        val clickedSong = processedQueue[startIndex]

        val mediaItems = withContext(Dispatchers.Default) {
            processedQueue.map { song ->
                val artBytes = artworkCache[song.id]
                song.toMediaItem(artBytes)
            }
        }

        withContext(Dispatchers.Main) {
            requestAudioFocus()
            if (isSameQueue && currentPlayer.mediaItemCount > 0) {
                try {
                    isSettingQueue = true
                    _currentSong.value = clickedSong
                    currentPlayer.seekTo(startIndex, 0L)
                    currentPlayer.play()
                } finally {
                    isSettingQueue = false
                }
            } else {
                try {
                    isSettingQueue = true
                    currentPlayer.setMediaItems(mediaItems, startIndex, 0L)
                    currentPlayer.prepare()
                    currentPlayer.play()
                    _currentSong.value = clickedSong
                } finally {
                    isSettingQueue = false
                }
            }
            startPlaybackService()
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
        cancelCrossfade()
        startPlaybackService()
        val finalSong = editedSongsCache[song.id] ?: song
        _currentSong.value = finalSong
        activeQueue = listOf(finalSong)
        _queue.value = listOf(finalSong)

        val mediaItem = finalSong.toMediaItem()
        runOnMainThread {
            requestAudioFocus()
            currentPlayer.setMediaItem(mediaItem)
            currentPlayer.prepare()
            currentPlayer.play()
        }
    }

    fun playQueueItem(index: Int) {
        runOnMainThread {
            val songs = if (activeQueue.isNotEmpty()) activeQueue else _allSongs.value
            if (index in songs.indices) {
                playerScope.launch {
                    setQueue(songs, index)
                }
            }
        }
    }

    fun removeFromQueue(index: Int) {
        runOnMainThread {
            val currentQueue = if (activeQueue.isNotEmpty()) activeQueue.toMutableList() else _allSongs.value.toMutableList()
            if (index in currentQueue.indices) {
                currentQueue.removeAt(index)
                activeQueue = currentQueue
                _queue.value = currentQueue
                try {
                    exoPlayer.removeMediaItem(index)
                } catch (_: Exception) {}
            }
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
            val currentIndex = currentPlayer.currentMediaItemIndex
            if (currentIndex in activeQueue.indices) {
                val activeSong = activeQueue[currentIndex]
                if (activeSong.id.toLongOrNull() == songId || activeSong.id == targetIdStr) {
                    val isPlayingNow = currentPlayer.isPlaying
                    val currentPosition = currentPlayer.currentPosition

                    val updatedMediaItem = activeSong.toMediaItem()
                    currentPlayer.replaceMediaItem(currentIndex, updatedMediaItem)
                    currentPlayer.seekTo(currentIndex, currentPosition)
                    if (isPlayingNow) {
                        currentPlayer.play()
                    }
                }
            }
        }
    }

    override fun togglePlayPause() {
        runOnMainThread {
            if (exoPlayer.isPlaying) {
                currentPlayer.pause()
                if (isCrossfading) {
                    nextPlayer.pause()
                }
            } else {
                requestAudioFocus()
                if (currentPlayer.currentMediaItem == null && _currentSong.value != null) {
                    val song = _currentSong.value!!
                    val targetQueue = if (activeQueue.isNotEmpty()) activeQueue else if (_allSongs.value.isNotEmpty()) _allSongs.value else listOf(song)
                    val index = targetQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                    activeQueue = targetQueue
                    val mediaItems = targetQueue.map { artworkCache[it.id].let { art -> it.toMediaItem(art) } }
                    currentPlayer.setMediaItems(mediaItems, index, 0L)
                    currentPlayer.prepare()
                    currentPlayer.play()
                } else {
                    currentPlayer.play()
                    if (isCrossfading) {
                        nextPlayer.play()
                    }
                }
                startPlaybackService()
            }
        }
    }
}
