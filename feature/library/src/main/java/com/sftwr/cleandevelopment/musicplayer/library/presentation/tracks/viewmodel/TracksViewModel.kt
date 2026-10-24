package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractExtraMetadata
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.S)
@HiltViewModel
class TracksViewModel @Inject constructor(
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase,
    private val playerManager: PlayerManager,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var scrollIndex: Int
        get() = savedStateHandle.get<Int>("scroll_index") ?: 0
        set(value) { savedStateHandle["scroll_index"] = value }

    var scrollOffset: Int
        get() = savedStateHandle.get<Int>("scroll_offset") ?: 0
        set(value) { savedStateHandle["scroll_offset"] = value }

    private val _uiState = MutableStateFlow(TracksUiState())
    val uiState: StateFlow<TracksUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<TracksUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    private val _deleteEvent = Channel<PlayerUiEvent>()
    val deleteEvent = _deleteEvent.receiveAsFlow()

    val currentPlayingSong = playerManager.currentSong
    val isPlayerPlaying = playerManager.isPlaying

    init {
        loadTracks()
    }

    private var pendingDeleteSongId: Long? = null


    fun deleteCurrentOrSelectedSong(songId: Long) {
        pendingDeleteSongId = songId
        viewModelScope.launch {
            val result = playerManager.deleteSong(songId)
            handleDeleteResult(result)
        }
    }

    fun retryDeleteAfterPermission() {
        pendingDeleteSongId?.let { id ->
            deleteCurrentOrSelectedSong(id)
            pendingDeleteSongId = null
        }
    }

    /**
     * Fetches actual device songs from the UseCase and maps them to SongUiModel safely.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun loadTracks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getDeviceSongsUseCase().collect { domainSongs ->
                // 👈 استفاده از toList() برای جلوگیری از ConcurrentModificationException
                val songUiModels = domainSongs.toList().map { song ->
                    val extraMetadata = withContext(Dispatchers.IO) {
                        song.path.extractExtraMetadata()
                    }
                    SongUiModel(
                        id = song.id.toString(),
                        title = song.title,
                        artist = song.artist,
                        duration = song.duration.formatTime(),
                        uri = song.path,
                        year = extraMetadata.year ?: "",
                        album = song.path,
                        genre = extraMetadata.genre ?: "",
                    )
                }

                _uiState.value = _uiState.value.copy(
                    songs = songUiModels,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Handles track selection, sets the playback queue, and triggers navigation to the player.
     */
    fun onSongClicked(songId: String) {
        val songs = _uiState.value.songs
        val index = songs.indexOfFirst { it.id == songId }

        if (index != -1) {
            playerManager.setQueue(songs, startIndex = index)

            viewModelScope.launch {
                _uiEvent.send(TracksUiEvent.NavigateToPlayer(songId))
            }
        }
    }

    fun onSearchClicked() {
        viewModelScope.launch {
            _uiEvent.send(TracksUiEvent.NavigateToSearch)
        }
    }

    private fun handleDeleteResult(result: DeleteResult) {
        viewModelScope.launch {
            when (result) {
                is DeleteResult.Success -> {
                    _deleteEvent.send(PlayerUiEvent.ShowToast("آهنگ با موفقیت حذف شد"))
                }

                is DeleteResult.RequiresPermission -> {
                    _deleteEvent.send(PlayerUiEvent.ShowDeletePermissionDialog(result.intentSender))
                }

                is DeleteResult.Error -> {
                    _deleteEvent.send(PlayerUiEvent.ShowToast("خطا در حذف آهنگ"))
                }
            }
        }
    }

    /**
     * Refreshes the track list from device storage safely.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun refreshTracks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            getDeviceSongsUseCase().collect { domainSongs ->

                // 👈 استفاده از toList() برای امنیت تردینگ
                val songUiModels = domainSongs.toList().map { song ->
                    val extraMetadata = withContext(Dispatchers.IO) {
                        song.path.extractExtraMetadata()
                    }
                    SongUiModel(
                        id = song.id.toString(),
                        title = song.title,
                        artist = song.artist,
                        duration = song.duration.formatTime(),
                        uri = song.path,
                        year = extraMetadata.year ?: "",
                        album = song.path,
                        genre = extraMetadata.genre ?: "",
                    )
                }

                _uiState.value = _uiState.value.copy(
                    songs = songUiModels,
                    isRefreshing = false
                )
            }
        }
    }
}

/**
 * UI events emitted from TracksViewModel for one-time actions like navigation.
 */
sealed interface TracksUiEvent {
    data class NavigateToPlayer(val songId: String) : TracksUiEvent
    object NavigateToSearch : TracksUiEvent
}

sealed interface PlayerUiEvent {
    data class ShowDeletePermissionDialog(val intentSender: android.content.IntentSender) :
        PlayerUiEvent

    data class ShowToast(val message: String) : PlayerUiEvent
}