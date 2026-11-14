package com.sftwr.cleandevelopment.musicplayer.library.presentation.player.viewmodel

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.AddSongToPlaylistUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetPlaylistsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.model.VisualizerStyle
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.ShareUtils
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * PlayerViewModel manages the audio playback states, transport controls (play, pause, skip, seek),
 * repeat/shuffle configurations, favorites, sharing, custom playlist adding, and periodic progress tracking.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase,
    private val playlistRepository: PlaylistRepository,
    getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    getSettingsUseCase: GetSettingsUseCase,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    // Expose underlying ExoPlayer instance for audio session management
    val exoPlayer = playerManager.exoPlayer

    val swipeArtworkToSkip: StateFlow<Boolean> = getSettingsUseCase()
        .map { it.swipeArtworkToSkipEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val shakeToSkip: StateFlow<Boolean> = getSettingsUseCase()
        .map { it.shakeToSkipEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private class ShakeDetector(
        context: Context,
        private val onShake: () -> Unit
    ) : android.hardware.SensorEventListener {
        private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? android.hardware.SensorManager
        private val accelerometer = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        private var lastShakeTime = 0L

        fun start() {
            accelerometer?.let {
                sensorManager?.registerListener(this, it, android.hardware.SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

        fun stop() {
            sensorManager?.unregisterListener(this)
        }

        override fun onSensorChanged(event: android.hardware.SensorEvent?) {
            if (event?.sensor?.type == android.hardware.Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val gX = x / android.hardware.SensorManager.GRAVITY_EARTH
                val gY = y / android.hardware.SensorManager.GRAVITY_EARTH
                val gZ = z / android.hardware.SensorManager.GRAVITY_EARTH

                val gForce = kotlin.math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

                if (gForce > 2.7f) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastShakeTime > 1500) {
                        lastShakeTime = currentTime
                        onShake()
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
    }

    private val shakeDetector = ShakeDetector(context) {
        if (shakeToSkip.value && isPlaying.value) {
            skipNext()
        }
    }

    // Safely retrieve the active audio session ID for visualizers
    val audioSessionId: Int
        @OptIn(UnstableApi::class)
        get() = try {
            exoPlayer.audioSessionId
        } catch (_: Exception) {
            0
        }

    // States are directly driven by PlayerManager or connected StateFlows
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentSong: StateFlow<SongUiModel?> = playerManager.currentSong

    val visualizerStyle: StateFlow<VisualizerStyle> = getSettingsUseCase()
        .map { settings ->
            VisualizerStyle.fromId(settings.selectedVisualizer.ifEmpty { settings.circularVisualizerType })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VisualizerStyle.DEFAULT
        )

    // Only custom playlists are shown in the Add to Playlist dialog
    val playlists: StateFlow<List<Playlist>> = getPlaylistsUseCase()
        .map { list -> list.filter { it is Playlist.Custom } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isAddToPlaylistDialogVisible = MutableStateFlow(false)
    val isAddToPlaylistDialogVisible: StateFlow<Boolean> = _isAddToPlaylistDialogVisible.asStateFlow()

    private val _toastChannel = Channel<String>()
    val toastEvent = _toastChannel.receiveAsFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    // List of device songs loaded from storage repository
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    // Shuffle and repeat state flow streams
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    init {
        loadDeviceSongs()
        startPositionUpdater()
        observeCurrentSong()
        observeShakeToSkip()
    }

    private fun observeShakeToSkip() {
        viewModelScope.launch {
            shakeToSkip.collect { enabled ->
                if (enabled) {
                    shakeDetector.start()
                } else {
                    shakeDetector.stop()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        shakeDetector.stop()
    }

    private fun observeCurrentSong() {
        viewModelScope.launch {
            currentSong.collect { song ->
                if (song != null) {
                    _currentPosition.value = runCatching { exoPlayer.currentPosition }.getOrDefault(0L).coerceAtLeast(0L)
                } else {
                    _currentPosition.value = 0L
                }
            }
        }
    }

    /**
     * Periodically updates the playback position of the currently playing track.
     */
    private fun startPositionUpdater() {
        viewModelScope.launch {
            while (true) {
                if (isPlaying.value) {
                    _currentPosition.value = runCatching { exoPlayer.currentPosition }.getOrDefault(0L).coerceAtLeast(0L)
                }
                delay(200L.milliseconds)
            }
        }
    }

    /**
     * Loads local audio tracks from device storage using the use case.
     */
    private fun loadDeviceSongs() {
        viewModelScope.launch {
            getDeviceSongsUseCase().collect { deviceSongs ->
                _songs.value = deviceSongs
            }
        }
    }

    fun toggleFavorite() {
        currentSong.value?.let { song ->
            song.id.toLongOrNull()?.let { songId ->
                viewModelScope.launch {
                    val newFavoriteState = !song.isFavorite
                    playlistRepository.toggleFavorite(songId, newFavoriteState)
                    val msg = if (newFavoriteState) "به لیست دلخواه اضافه شد" else "از لیست دلخواه حذف شد"
                    _toastChannel.send(msg)
                }
            }
        }
    }

    fun openAddToPlaylistDialog() {
        _isAddToPlaylistDialogVisible.value = true
    }

    fun closeAddToPlaylistDialog() {
        _isAddToPlaylistDialogVisible.value = false
    }

    fun addSongToPlaylist(playlist: Playlist) {
        currentSong.value?.let { song ->
            song.id.toLongOrNull()?.let { songId ->
                viewModelScope.launch {
                    val result = addSongToPlaylistUseCase(playlist.id, songId)
                    if (result.isSuccess) {
                        _toastChannel.send("به لیست ${playlist.name} اضافه شد")
                    } else {
                        _toastChannel.send("خطا در افزودن به لیست پخش")
                    }
                    closeAddToPlaylistDialog()
                }
            }
        }
    }

    fun shareSong(context: Context) {
        currentSong.value?.let { song ->
            ShareUtils.shareSong(context, song)
        }
    }

    /**
     * Toggles playback between play and pause states via player manager.
     */
    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    /**
     * Skips playback to the next track in the active queue.
     */
    fun skipNext() {
        playerManager.playNext()
    }

    /**
     * Skips playback to the previous track in the active queue.
     */
    fun skipPrevious() {
        playerManager.playPrevious()
    }

    /**
     * Toggles shuffle mode state across player manager and ExoPlayer instance.
     */
    fun toggleShuffle() {
        val newShuffleState = !_isShuffleEnabled.value
        _isShuffleEnabled.value = newShuffleState
        runOnMainThread {
            exoPlayer.shuffleModeEnabled = newShuffleState
        }
    }

    /**
     * Cycles through available playback repeat modes (OFF, ALL, ONE).
     */
    fun toggleRepeat() {
        val nextMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = nextMode
        runOnMainThread {
            exoPlayer.repeatMode = when (nextMode) {
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            }
        }
    }

    private inline fun runOnMainThread(crossinline action: () -> Unit) {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            action()
        } else {
            android.os.Handler(android.os.Looper.getMainLooper()).post { action() }
        }
    }

    /**
     * Seeks playback position to the specified target time in milliseconds.
     */
    fun seekTo(position: Long) {
        playerManager.seekTo(position)
        _currentPosition.value = position
    }
}

/**
 * Playback repeat modes for the media player.
 */
enum class RepeatMode { OFF, ALL, ONE }
