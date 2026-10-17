package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel

import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val playerManager: PlayerManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TracksUiState())
    val uiState: StateFlow<TracksUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<TracksUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadTracks()
    }

    /**
     * Fetch songs from the repository/usecase and update UI state.
     */
    private fun loadTracks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // TODO: Replace with actual data from use case
            val fetchedSongs = listOf(
                SongUiModel("1", "Nightfall Melody", "Aria & The Echoes", "3:45"),
                SongUiModel("2", "Midnight Drive", "Synthetics", "4:12")
            )

            _uiState.value = _uiState.value.copy(
                songs = fetchedSongs,
                isLoading = false
            )
        }
    }

    /**
     * Called when a song item is clicked in the list.
     */
    fun onSongClicked(songId: String) {
        viewModelScope.launch {
            // Find the song by id from current list
            val selectedSong = _uiState.value.songs.find { it.id == songId }

            selectedSong?.let { song ->
                // 1. Update global player manager state
                playerManager.playSong(song)

                // 2. (Optional) If you also want to navigate to full Player screen, trigger event:
                _uiEvent.send(TracksUiEvent.NavigateToPlayer(songId))
            }
        }
    }

    fun onSearchClicked() {
        viewModelScope.launch {
            _uiEvent.send(TracksUiEvent.NavigateToSearch)
        }
    }
}

sealed interface TracksUiEvent {
    data class NavigateToPlayer(val songId: String) : TracksUiEvent
    object NavigateToSearch : TracksUiEvent
}