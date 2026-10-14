package com.sftwr.cleandevelopment.musicplayer.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    // Inject your use cases here from :core:domain (e.g., GetPlaylistsUseCase)
) : ViewModel() {

    // Backing field for UI state
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Channel for one-time side effects (navigation, alerts, etc.)
    private val _uiEvent = Channel<HomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadInitialData()
    }

    // Load playlists and initial data from domain layer
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // TODO: Fetch data from UseCase
            // and update _uiState with actual playlists
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    // Handle user actions and trigger navigation events if needed
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

    fun createNewPlaylist(playlistName: String) {
        viewModelScope.launch {
            // Logic to add playlist via domain layer
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
            viewModelScope.launch {
                onDismissCreateDialog()
            }
        }
    }
}