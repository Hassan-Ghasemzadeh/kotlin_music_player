package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel

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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        set(value) {
            savedStateHandle["scroll_index"] = value
        }

    var scrollOffset: Int
        get() = savedStateHandle.get<Int>("scroll_offset") ?: 0
        set(value) {
            savedStateHandle["scroll_offset"] = value
        }

    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<TracksUiState> = playerManager.allSongs
        .map { songs ->
            TracksUiState(
                songs = songs,
                isLoading = _isLoading.value,
                isRefreshing = _isRefreshing.value
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, TracksUiState())

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
            _isLoading.value = true

            getDeviceSongsUseCase().collect { domainSongs ->
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

                // 👈 ارسال لیست به پلیرمنجر تا به عنوان منبع واحد ثبت شود
                playerManager.setSongs(songUiModels)
                _isLoading.value = false
            }
        }
    }

    /**
     * Handles track selection, sets the playback queue, and triggers navigation to the player.
     */
    fun onSongClicked(songId: String) {
        val songs = uiState.value.songs
        val index = songs.indexOfFirst { it.id == songId }

        if (index != -1) {
            playerManager.setQueue(songs, startIndex = index)
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
}

/**
 * UI events emitted from TracksViewModel for one-time actions like navigation.
 */
sealed interface TracksUiEvent {
    object NavigateToSearch : TracksUiEvent
}

sealed interface PlayerUiEvent {
    data class ShowDeletePermissionDialog(val intentSender: android.content.IntentSender) :
        PlayerUiEvent

    data class ShowToast(val message: String) : PlayerUiEvent
}