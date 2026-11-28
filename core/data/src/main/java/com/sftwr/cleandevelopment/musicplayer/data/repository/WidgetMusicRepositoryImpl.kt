package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.sftwr.cleandevelopment.musicplayer.data.util.LrcParser
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.model.WidgetPlaybackState
import com.sftwr.cleandevelopment.musicplayer.domain.player.PlayerControlBridge
import com.sftwr.cleandevelopment.musicplayer.domain.repository.LyricRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.WidgetMusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.widget.WidgetStateNotifier
import com.sftwr.cleandevelopment.musicplayer.model.Lyric
import com.sftwr.cleandevelopment.musicplayer.model.LyricLine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "music_player_widget_prefs")

@Singleton
class WidgetMusicRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val lyricRepository: LyricRepository,
    private val playerControlBridgeProvider: Provider<PlayerControlBridge>,
    private val widgetStateNotifierProvider: Provider<WidgetStateNotifier>
) : WidgetMusicRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var tickerJob: Job? = null
    private var lyricsObserverJob: Job? = null
    private var currentObservedSongId: Long? = null
    private val lyricsCache = ConcurrentHashMap<Long, Lyric>()

    @Volatile
    private var basePositionMs: Long = 0L
    @Volatile
    private var lastPositionSystemTime: Long = System.currentTimeMillis()

    private val samplePlaylist = listOf(
        Song(
            id = 101L,
            title = "TRIALS",
            artist = "Starset",
            album = "DIVISIONS",
            albumId = 1L,
            contentUri = Uri.parse("content://media/external/audio/media/101"),
            duration = 233000L,
            isFavorite = true
        ),
        Song(
            id = 102L,
            title = "Echoes",
            artist = "Starset",
            album = "DIVISIONS",
            albumId = 1L,
            contentUri = Uri.parse("content://media/external/audio/media/102"),
            duration = 215000L,
            isFavorite = false
        ),
        Song(
            id = 103L,
            title = "MANIFEST",
            artist = "Starset",
            album = "DIVISIONS",
            albumId = 1L,
            contentUri = Uri.parse("content://media/external/audio/media/103"),
            duration = 267000L,
            isFavorite = true
        )
    )

    private var currentPlaylistIndex = 0

    private val _stateFlow = MutableStateFlow(
        WidgetPlaybackState(
            currentSong = samplePlaylist[0],
            isPlaying = true,
            currentPositionMs = 0L,
            durationMs = 233000L,
            isShuffleEnabled = false,
            isFavorite = true,
            hasNext = true,
            hasPrevious = false,
            lyrics = null
        )
    )

    init {
        scope.launch {
            runCatching {
                val prefs = context.widgetDataStore.data.first()
                val songId = prefs[WidgetPreferencesKeys.KEY_SONG_ID]
                val title = prefs[WidgetPreferencesKeys.KEY_TRACK_TITLE]
                if (songId != null && title != null) {
                    val artist = prefs[WidgetPreferencesKeys.KEY_ARTIST_NAME] ?: "Unknown Artist"
                    val album = prefs[WidgetPreferencesKeys.KEY_ALBUM_NAME] ?: "Unknown Album"
                    val artUri = prefs[WidgetPreferencesKeys.KEY_ALBUM_ART_URI]
                    val isPlaying = prefs[WidgetPreferencesKeys.KEY_IS_PLAYING] ?: false
                    val position = prefs[WidgetPreferencesKeys.KEY_CURRENT_POSITION] ?: 0L
                    val duration = prefs[WidgetPreferencesKeys.KEY_DURATION] ?: 233000L
                    val isShuffle = prefs[WidgetPreferencesKeys.KEY_IS_SHUFFLE] ?: false
                    val isFavorite = prefs[WidgetPreferencesKeys.KEY_IS_FAVORITE] ?: false
                    val hasNext = prefs[WidgetPreferencesKeys.KEY_HAS_NEXT] ?: true
                    val hasPrevious = prefs[WidgetPreferencesKeys.KEY_HAS_PREVIOUS] ?: false
                    val revision = prefs[WidgetPreferencesKeys.KEY_REVISION] ?: System.currentTimeMillis()

                    val song = Song(
                        id = songId,
                        title = title,
                        artist = artist,
                        album = album,
                        albumId = -1L,
                        contentUri = Uri.EMPTY,
                        albumArtUri = if (artUri.isNullOrEmpty()) null else artUri,
                        duration = duration,
                        isFavorite = isFavorite
                    )

                    val restoredState = WidgetPlaybackState(
                        currentSong = song,
                        isPlaying = isPlaying,
                        currentPositionMs = position,
                        durationMs = duration,
                        isShuffleEnabled = isShuffle,
                        isFavorite = isFavorite,
                        hasNext = hasNext,
                        hasPrevious = hasPrevious,
                        lastUpdatedTimestamp = revision
                    )
                    _stateFlow.value = restoredState
                    updateBasePosition(position)
                    observeLyricsForCurrentSong(songId)
                    startPositionTickerIfNeeded(isPlaying)
                    return@launch
                }
            }
            val initialSongId = _stateFlow.value.currentSong?.id ?: 101L
            updateBasePosition(0L)
            observeLyricsForCurrentSong(initialSongId)
            persistStateToDataStore(_stateFlow.value)
            startPositionTickerIfNeeded(_stateFlow.value.isPlaying)
        }
    }

    private fun updateBasePosition(positionMs: Long) {
        basePositionMs = positionMs.coerceAtLeast(0L)
        lastPositionSystemTime = System.currentTimeMillis()
    }

    private fun calculateCurrentPositionMs(state: WidgetPlaybackState): Long {
        if (!state.isPlaying) return state.currentPositionMs
        val elapsed = System.currentTimeMillis() - lastPositionSystemTime
        return (basePositionMs + elapsed).coerceIn(0L, state.durationMs.coerceAtLeast(0L))
    }

    private fun calculateLyricIndex(lyrics: Lyric?, positionMs: Long): Int {
        val lines = lyrics?.lines ?: return -1
        if (lines.isEmpty()) return -1
        val index = lines.indexOfLast { it.timeMs <= positionMs }
        return if (index >= 0) index else 0
    }

    override fun getWidgetPlaybackState(): Flow<WidgetPlaybackState> = _stateFlow.asStateFlow()

    override suspend fun getWidgetPlaybackStateDirect(): WidgetPlaybackState = _stateFlow.value

    override suspend fun togglePlayPause() {
        val currentState = _stateFlow.value
        val curPos = calculateCurrentPositionMs(currentState)
        val newIsPlaying = !currentState.isPlaying
        val newState = currentState.copy(
            currentPositionMs = curPos,
            isPlaying = newIsPlaying,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        _stateFlow.value = newState
        updateBasePosition(curPos)
        persistStateToDataStore(newState)
        startPositionTickerIfNeeded(newIsPlaying)

        runCatching {
            playerControlBridgeProvider.get()?.togglePlayPause()
        }
    }

    private val currentHasNext: Boolean
        get() {
            val state = _stateFlow.value
            if (state.isShuffleEnabled && samplePlaylist.size > 1) return true
            return currentPlaylistIndex < samplePlaylist.size - 1
        }

    private val currentHasPrevious: Boolean
        get() {
            val state = _stateFlow.value
            if (state.isShuffleEnabled && samplePlaylist.size > 1) return true
            return currentPlaylistIndex > 0
        }

    override suspend fun skipToNext() {
        val currentState = _stateFlow.value
        if (!currentState.hasNext) return

        if (currentPlaylistIndex < samplePlaylist.size - 1) {
            currentPlaylistIndex++
        } else if (currentState.isShuffleEnabled && samplePlaylist.size > 1) {
            currentPlaylistIndex = (currentPlaylistIndex + 1) % samplePlaylist.size
        }
        val nextSong = samplePlaylist[currentPlaylistIndex]
        val currentPlaying = currentState.isPlaying
        val nextHasNext = currentHasNext
        val nextHasPrev = currentHasPrevious

        val newState = currentState.copy(
            currentSong = nextSong,
            currentPositionMs = 0L,
            durationMs = nextSong.duration.coerceAtLeast(1L),
            isPlaying = currentPlaying,
            isFavorite = nextSong.isFavorite,
            hasNext = nextHasNext,
            hasPrevious = nextHasPrev,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        _stateFlow.value = newState
        updateBasePosition(0L)
        observeLyricsForCurrentSong(nextSong.id)
        persistStateToDataStore(_stateFlow.value)
        startPositionTickerIfNeeded(currentPlaying)

        runCatching {
            playerControlBridgeProvider.get()?.playNext()
        }
    }

    override suspend fun skipToPrevious() {
        val currentState = _stateFlow.value
        if (!currentState.hasPrevious) return

        if (currentPlaylistIndex > 0) {
            currentPlaylistIndex--
        } else if (currentState.isShuffleEnabled && samplePlaylist.size > 1) {
            currentPlaylistIndex = if (currentPlaylistIndex - 1 < 0) samplePlaylist.size - 1 else currentPlaylistIndex - 1
        }
        val prevSong = samplePlaylist[currentPlaylistIndex]
        val currentPlaying = currentState.isPlaying
        val prevHasNext = currentHasNext
        val prevHasPrev = currentHasPrevious

        val newState = currentState.copy(
            currentSong = prevSong,
            currentPositionMs = 0L,
            durationMs = prevSong.duration.coerceAtLeast(1L),
            isPlaying = currentPlaying,
            isFavorite = prevSong.isFavorite,
            hasNext = prevHasNext,
            hasPrevious = prevHasPrev,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        _stateFlow.value = newState
        updateBasePosition(0L)
        observeLyricsForCurrentSong(prevSong.id)
        persistStateToDataStore(_stateFlow.value)
        startPositionTickerIfNeeded(currentPlaying)

        runCatching {
            playerControlBridgeProvider.get()?.playPrevious()
        }
    }

    override suspend fun toggleShuffle() {
        val currentState = _stateFlow.value
        val curPos = calculateCurrentPositionMs(currentState)
        val newShuffle = !currentState.isShuffleEnabled
        val newHasNext = if (newShuffle && samplePlaylist.size > 1) true else currentPlaylistIndex < samplePlaylist.size - 1
        val newHasPrevious = if (newShuffle && samplePlaylist.size > 1) true else currentPlaylistIndex > 0
        val newState = currentState.copy(
            currentPositionMs = curPos,
            isShuffleEnabled = newShuffle,
            hasNext = newHasNext,
            hasPrevious = newHasPrevious,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        _stateFlow.value = newState
        updateBasePosition(curPos)
        persistStateToDataStore(newState)

        runCatching {
            playerControlBridgeProvider.get()?.toggleShuffle()
        }
    }

    override suspend fun toggleLike() {
        val currentState = _stateFlow.value
        val curPos = calculateCurrentPositionMs(currentState)
        val newFav = !currentState.isFavorite
        val newState = currentState.copy(
            currentPositionMs = curPos,
            isFavorite = newFav,
            currentSong = currentState.currentSong?.copy(isFavorite = newFav),
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        _stateFlow.value = newState
        updateBasePosition(curPos)
        persistStateToDataStore(newState)

        runCatching {
            currentState.currentSong?.id?.let { songId ->
                playerControlBridgeProvider.get()?.toggleFavorite(songId, newFav)
            }
        }
    }

    override suspend fun seekTo(positionMs: Long) {
        val currentState = _stateFlow.value
        val newPos = positionMs.coerceIn(0L, currentState.durationMs)
        val newState = currentState.copy(
            currentPositionMs = newPos,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        _stateFlow.value = newState
        updateBasePosition(newPos)
        persistStateToDataStore(newState)
        startPositionTickerIfNeeded(currentState.isPlaying)

        runCatching {
            playerControlBridgeProvider.get()?.seekTo(newPos)
        }
    }

    override suspend fun updatePlaybackPosition(positionMs: Long) {
        seekTo(positionMs)
    }

    override suspend fun updateWidgetState(state: WidgetPlaybackState) {
        _stateFlow.value = state
        updateBasePosition(state.currentPositionMs)
        state.currentSong?.id?.let { observeLyricsForCurrentSong(it) }
        persistStateToDataStore(state)
        startPositionTickerIfNeeded(state.isPlaying)
    }

    override suspend fun syncPlaybackState(
        songId: Long?,
        title: String?,
        artist: String?,
        album: String?,
        albumArtUri: String?,
        isPlaying: Boolean,
        positionMs: Long,
        durationMs: Long,
        isShuffle: Boolean,
        isFavorite: Boolean,
        hasNext: Boolean,
        hasPrevious: Boolean
    ) {
        val current = _stateFlow.value
        val curSong = current.currentSong
        val resolvedDuration = when {
            durationMs > 0L -> durationMs
            curSong != null && curSong.duration > 0L -> curSong.duration
            else -> 1L
        }

        val activeSong = if (songId != null && title != null) {
            Song(
                id = songId,
                title = title,
                artist = artist ?: "Unknown Artist",
                album = album ?: "Unknown Album",
                albumId = -1L,
                contentUri = Uri.EMPTY,
                albumArtUri = albumArtUri,
                duration = resolvedDuration,
                isFavorite = isFavorite
            )
        } else {
            current.currentSong
        }

        val newState = current.copy(
            currentSong = activeSong,
            isPlaying = isPlaying,
            currentPositionMs = positionMs.coerceAtLeast(0L),
            durationMs = resolvedDuration,
            isShuffleEnabled = isShuffle,
            isFavorite = isFavorite,
            hasNext = hasNext,
            hasPrevious = hasPrevious,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )

        _stateFlow.value = newState
        updateBasePosition(positionMs.coerceAtLeast(0L))
        if (songId != null) {
            observeLyricsForCurrentSong(songId)
        }
        persistStateToDataStore(_stateFlow.value)
        startPositionTickerIfNeeded(isPlaying)
    }

    override suspend fun refreshLyricsForSong(songId: Long) {
        lyricsCache.remove(songId)
        val currentSongId = _stateFlow.value.currentSong?.id
        if (currentSongId == songId) {
            currentObservedSongId = null
            observeLyricsForCurrentSong(songId)
        }
    }

    private fun observeLyricsForCurrentSong(songId: Long) {
        if (currentObservedSongId == songId && lyricsObserverJob?.isActive == true) {
            return
        }
        currentObservedSongId = songId
        lyricsObserverJob?.cancel()
        lyricsObserverJob = scope.launch(Dispatchers.IO) {
            lyricRepository.getLyricForSong(songId).collect { fetchedLyric ->
                val mappedLyric = if (fetchedLyric != null && fetchedLyric.content.isNotBlank()) {
                    val linesToUse = if (fetchedLyric.lines.isNotEmpty()) {
                        fetchedLyric.lines
                    } else {
                        val parsed = LrcParser.parse(fetchedLyric.content)
                        if (parsed.lines.isNotEmpty()) {
                            parsed.lines
                        } else {
                            fetchedLyric.content
                                .lines()
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .mapIndexed { index, lineText ->
                                    LyricLine(timeMs = index * 4000L, text = lineText)
                                }
                        }
                    }
                    val sortedLines = linesToUse.sortedBy { it.timeMs }
                    fetchedLyric.copy(lines = sortedLines, isSynced = sortedLines.any { it.timeMs > 0L })
                } else null

                if (mappedLyric != null) {
                    lyricsCache[songId] = mappedLyric
                } else {
                    lyricsCache.remove(songId)
                }

                val timestamp = System.currentTimeMillis()
                val curPos = calculateCurrentPositionMs(_stateFlow.value)
                val updatedState = _stateFlow.value.copy(
                    currentPositionMs = curPos,
                    lyrics = mappedLyric,
                    lastUpdatedTimestamp = timestamp
                )
                _stateFlow.value = updatedState
                persistStateToDataStore(updatedState)
            }
        }
    }

    private fun startPositionTickerIfNeeded(isPlaying: Boolean) {
        tickerJob?.cancel()
        if (!isPlaying) return

        tickerJob = scope.launch(Dispatchers.Default) {
            var lastPublishedLyricIndex: Int? = null

            while (isActive) {
                val current = _stateFlow.value
                if (!current.isPlaying) break

                val currentPos = calculateCurrentPositionMs(current)
                val newLyricIndex = calculateLyricIndex(current.lyrics, currentPos)

                val hasEnded = currentPos >= current.durationMs && current.durationMs > 0L
                val lineChanged = lastPublishedLyricIndex == null || lastPublishedLyricIndex != newLyricIndex

                if (hasEnded) {
                    val endedState = current.copy(
                        currentPositionMs = current.durationMs,
                        isPlaying = false,
                        lastUpdatedTimestamp = System.currentTimeMillis()
                    )
                    updateBasePosition(current.durationMs)
                    _stateFlow.value = endedState
                    persistStateToDataStore(endedState)
                    break
                } else if (lineChanged) {
                    lastPublishedLyricIndex = newLyricIndex

                    val updatedState = current.copy(
                        currentPositionMs = currentPos,
                        lastUpdatedTimestamp = System.currentTimeMillis()
                    )
                    _stateFlow.value = updatedState
                    persistStateToDataStore(updatedState)
                } else {
                    _stateFlow.value = current.copy(currentPositionMs = currentPos)
                }

                delay(200L)
            }
        }
    }

    private suspend fun persistStateToDataStore(state: WidgetPlaybackState) {
        runCatching {
            context.widgetDataStore.edit { prefs ->
                state.currentSong?.let { song ->
                    prefs[WidgetPreferencesKeys.KEY_SONG_ID] = song.id
                    prefs[WidgetPreferencesKeys.KEY_TRACK_TITLE] = song.title
                    prefs[WidgetPreferencesKeys.KEY_ARTIST_NAME] = song.artist
                    prefs[WidgetPreferencesKeys.KEY_ALBUM_NAME] = song.album
                    prefs[WidgetPreferencesKeys.KEY_ALBUM_ART_URI] = song.albumArtUri ?: ""
                }
                prefs[WidgetPreferencesKeys.KEY_IS_PLAYING] = state.isPlaying
                prefs[WidgetPreferencesKeys.KEY_CURRENT_POSITION] = state.currentPositionMs
                prefs[WidgetPreferencesKeys.KEY_DURATION] = state.durationMs
                prefs[WidgetPreferencesKeys.KEY_IS_SHUFFLE] = state.isShuffleEnabled
                prefs[WidgetPreferencesKeys.KEY_IS_FAVORITE] = state.isFavorite
                prefs[WidgetPreferencesKeys.KEY_HAS_NEXT] = state.hasNext
                prefs[WidgetPreferencesKeys.KEY_HAS_PREVIOUS] = state.hasPrevious
                prefs[WidgetPreferencesKeys.KEY_REVISION] = state.lastUpdatedTimestamp
                prefs[WidgetPreferencesKeys.KEY_ACTIVE_LYRIC_INDEX] = state.currentLyricIndex
            }

            widgetStateNotifierProvider.get()?.updateAllWidgets(state)
        }
    }
}
