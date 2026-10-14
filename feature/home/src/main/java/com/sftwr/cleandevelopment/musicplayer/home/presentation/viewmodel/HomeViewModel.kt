package com.sftwr.cleandevelopment.musicplayer.home.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.TrendingUp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.home.presentation.PlaylistUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // Inject your use cases here from :core:domain
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadInitialData()
    }

    // ۱. مقداردهی اولیه پلی‌لیست‌ها هنگام شروع
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                playlists = listOf(
                    PlaylistUiModel("Recently Added", "11 Tracks", Icons.Default.LibraryAdd),
                    PlaylistUiModel("Recently Played", "11 Tracks", Icons.Default.History),
                    PlaylistUiModel("Most Played", "11 Tracks", Icons.Default.TrendingUp),
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
                icon = Icons.Default.QueueMusic
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