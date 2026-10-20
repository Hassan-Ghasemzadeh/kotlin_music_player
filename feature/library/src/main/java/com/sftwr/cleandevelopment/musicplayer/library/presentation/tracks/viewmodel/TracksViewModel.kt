package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase,
    private val playerManager: PlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TracksUiState())
    val uiState: StateFlow<TracksUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<TracksUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadTracks()
    }

    /**
     * Fetches actual device songs from the UseCase and maps them to SongUiModel.
     */
    private fun loadTracks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getDeviceSongsUseCase().collect { domainSongs ->
                val songUiModels = domainSongs.map { song ->
                    SongUiModel(
                        id = song.id.toString(),
                        title = song.title,
                        artist = song.artist,
                        duration = song.duration.formatTime(),
                        uri = song.path
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
            // Send the entire playlist along with the selected song index to the player manager
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

    fun deleteCurrentOrSelectedSong(songId: Long) {
        viewModelScope.launch {
            val success = playerManager.deleteSong(songId)
            if (success) {
                _uiEvent.send(TracksUiEvent.DeleteSong("آهنگ با موفقیت حذف شد"))
            } else {
                _uiEvent.send(TracksUiEvent.DeleteSong("خطا در حذف آهنگ"))
            }
        }
    }

    /**
     * Refreshes the track list from device storage.
     */
    fun refreshTracks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            getDeviceSongsUseCase().collect { domainSongs ->
                val songUiModels = domainSongs.map { song ->
                    SongUiModel(
                        id = song.id.toString(),
                        title = song.title,
                        artist = song.artist,
                        duration = song.duration.formatTime(),
                        uri = song.path
                    )
                }

                _uiState.value = _uiState.value.copy(
                    songs = songUiModels,
                    isRefreshing = false // Disable refreshing state after receiving data
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
    data class DeleteSong(val message: String) : TracksUiEvent
}