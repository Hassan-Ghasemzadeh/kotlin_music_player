package com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getDeviceSongsUseCase: GetDeviceSongsUseCase,
    private val musicRepository: MusicRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadInitialData()
        syncDeviceSongs()
    }

    fun syncDeviceSongs() {
        viewModelScope.launch {
            try {
                musicRepository.syncWithDevice()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    // Automatically calculate and expose the total album count as a reactive StateFlow
    val albumCount: StateFlow<Int> = songsState
        .map { songs -> songs.map { it.albumId }.distinct().size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Automatically calculate and expose the total artist count as a reactive StateFlow
    val artistCount: StateFlow<Int> = songsState
        .map { songs -> songs.map { it.artist.ifBlank { "Unknown Artist" } }.distinct().size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Automatically calculate and expose the total favorite song count as a reactive StateFlow
    val favoriteCount: StateFlow<Int> = songsState
        .map { songs -> songs.count { it.isFavorite } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Automatically calculate and expose the total folder count as a reactive StateFlow
    val folderCount: StateFlow<Int> = songsState
        .map { songs ->
            songs.map { song ->
                if (song.path.isNotBlank()) java.io.File(song.path).parent ?: "" else ""
            }.filter { it.isNotEmpty() }.distinct().size
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Automatically calculate and expose the total genre count as a reactive StateFlow
    val genreCount: StateFlow<Int> = songsState
        .map { songs -> songs.map { it.genre.ifBlank { "Unknown Genre" } }.distinct().size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = false
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
}