package com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.PlaylistUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getDeviceSongsUseCase: GetDeviceSongsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadInitialData()
    }

    // Observe the list of songs reactively from the repository
    val songsState: StateFlow<List<Song>> = getDeviceSongsUseCase.invoke()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Automatically calculate and expose the total song count as a reactive StateFlow
    val trackCount: StateFlow<Int> = songsState
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                playlists = listOf(
                    PlaylistUiModel("Recently Added", "11 Tracks", Icons.Default.LibraryAdd),
                    PlaylistUiModel("Recently Played", "11 Tracks", Icons.Default.History),
                    PlaylistUiModel(
                        "Most Played", "11 Tracks",
                        Icons.AutoMirrored.Filled.TrendingUp
                    ),
                    PlaylistUiModel("Favorite Tracks", "18 Tracks", Icons.Default.Favorite)
                )
            )
        }
    }

    fun onPlaylistClicked(playlistTitle: String) {
        viewModelScope.launch {
            _uiEvent.send(HomeUiEvent.OpenPlaylist(playlistTitle))
        }
    }

    fun onSearchClicked() {
        viewModelScope.launch {
            _uiEvent.send(HomeUiEvent.NavigateToSearch)
        }
    }

    fun onSongPlayClicked(songId: String) {
        viewModelScope.launch {
            _uiEvent.send(HomeUiEvent.NavigateToPlayer(songId))
        }
    }

    fun onAddPlaylistClicked() {
        _uiState.value = _uiState.value.copy(isCreateDialogVisible = true)
    }

    fun onDismissCreateDialog() {
        _uiState.value = _uiState.value.copy(
            isCreateDialogVisible = false,
            newPlaylistName = ""
        )
    }

    fun onNewPlaylistNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(newPlaylistName = name)
    }

    fun confirmCreatePlaylist() {
        val currentName = _uiState.value.newPlaylistName
        if (currentName.isNotBlank()) {
            val newPlaylist = PlaylistUiModel(
                title = currentName,
                subtitle = "0 Tracks",
                icon = Icons.AutoMirrored.Filled.QueueMusic
            )

            val updatedPlaylists = _uiState.value.playlists + newPlaylist

            _uiState.value = _uiState.value.copy(
                playlists = updatedPlaylists,
                isCreateDialogVisible = false,
                newPlaylistName = ""
            )
        }
    }
}