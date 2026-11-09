package com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.domain.model.PlaylistType
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.CreateCustomPlaylistUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetPlaylistsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing playlist state, custom playlist creation flow,
 * track multi-selection, and real-time Room database updates.
 */
@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    getPlaylistsUseCase: GetPlaylistsUseCase,
    getDeviceSongsUseCase: GetDeviceSongsUseCase,
    private val createCustomPlaylistUseCase: CreateCustomPlaylistUseCase,
) : ViewModel() {

    private val _dialogState = MutableStateFlow(DialogState())

    private data class DialogState(
        val isNameDialogVisible: Boolean = false,
        val isTrackSelectionVisible: Boolean = false,
        val name: String = "",
        val error: String? = null,
        val selectedSongIds: Set<Long> = emptySet(),
        val searchQuery: String = ""
    )

    val uiState: StateFlow<PlaylistsUiState> = combine(
        getPlaylistsUseCase(),
        getDeviceSongsUseCase(),
        _dialogState
    ) { playlists, domainSongs, dialog ->
        val items = playlists.map { it.toUiItem() }
        val songModels = domainSongs.map { song ->
            SongUiModel(
                id = song.id.toString(),
                title = song.title,
                artist = song.artist,
                duration = song.duration.formatTime(),
                uri = song.path,
                year = song.year,
                album = song.album,
                genre = song.genre,
                albumId = song.albumId,
                albumArtUri = song.albumArtUri,
            )
        }
        val filteredSongs = if (dialog.searchQuery.isBlank()) {
            songModels
        } else {
            songModels.filter {
                it.title.contains(dialog.searchQuery, ignoreCase = true) ||
                        it.artist.contains(dialog.searchQuery, ignoreCase = true)
            }
        }

        PlaylistsUiState(
            playlists = items,
            totalPlaylistCount = items.size,
            isLoading = false,
            isNameDialogVisible = dialog.isNameDialogVisible,
            isTrackSelectionVisible = dialog.isTrackSelectionVisible,
            newPlaylistName = dialog.name,
            nameError = dialog.error,
            availableSongs = filteredSongs,
            selectedSongIds = dialog.selectedSongIds,
            trackSearchQuery = dialog.searchQuery
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaylistsUiState(isLoading = true)
    )

    fun onAddPlaylistClick() {
        _dialogState.update {
            it.copy(
                isNameDialogVisible = true,
                isTrackSelectionVisible = false,
                name = "",
                error = null,
                selectedSongIds = emptySet(),
                searchQuery = ""
            )
        }
    }

    fun onDismissDialog() {
        _dialogState.update {
            it.copy(
                isNameDialogVisible = false,
                isTrackSelectionVisible = false,
                name = "",
                error = null,
                selectedSongIds = emptySet(),
                searchQuery = ""
            )
        }
    }

    fun onPlaylistNameChanged(name: String) {
        val error = if (name.isNotBlank() && name.length > 50) {
            "Playlist name must be under 50 characters"
        } else {
            null
        }
        _dialogState.update { it.copy(name = name, error = error) }
    }

    fun onProceedToTrackSelection() {
        val name = _dialogState.value.name.trim()
        if (name.isBlank()) {
            _dialogState.update { it.copy(error = "Playlist name cannot be empty") }
            return
        }
        _dialogState.update {
            it.copy(
                isNameDialogVisible = false,
                isTrackSelectionVisible = true,
                error = null
            )
        }
    }

    fun onToggleSongSelection(songId: Long) {
        _dialogState.update { state ->
            val updatedSet = if (state.selectedSongIds.contains(songId)) {
                state.selectedSongIds - songId
            } else {
                state.selectedSongIds + songId
            }
            state.copy(selectedSongIds = updatedSet)
        }
    }

    fun onToggleSelectAll(allSongIds: List<Long>) {
        _dialogState.update { state ->
            val isAllSelected = state.selectedSongIds.containsAll(allSongIds)
            val updatedSet = if (isAllSelected) {
                emptySet()
            } else {
                allSongIds.toSet()
            }
            state.copy(selectedSongIds = updatedSet)
        }
    }

    fun onTrackSearchQueryChanged(query: String) {
        _dialogState.update { it.copy(searchQuery = query) }
    }

    fun confirmCreatePlaylist() {
        val name = _dialogState.value.name.trim()
        if (name.isBlank()) {
            _dialogState.update { it.copy(error = "Playlist name cannot be empty") }
            return
        }
        val selectedIds = _dialogState.value.selectedSongIds.toList()

        viewModelScope.launch {
            val result = createCustomPlaylistUseCase(name, selectedIds)
            if (result.isSuccess) {
                onDismissDialog()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to create playlist"
                _dialogState.update { it.copy(error = errorMsg) }
            }
        }
    }
}

private fun Playlist.toUiItem(): PlaylistUiItem {
    return when (this) {
        is Playlist.Default -> {
            val icon = when (type) {
                PlaylistType.RECENTLY_ADDED -> Icons.Default.LibraryAdd
                PlaylistType.RECENTLY_PLAYED -> Icons.Default.History
                PlaylistType.MOST_PLAYED -> Icons.AutoMirrored.Filled.TrendingUp
                PlaylistType.FAVORITES -> Icons.Default.Favorite
                PlaylistType.CUSTOM -> Icons.AutoMirrored.Filled.QueueMusic
            }
            PlaylistUiItem(
                id = id,
                title = name,
                subtitle = "$trackCount Tracks",
                icon = icon,
                isDefault = true,
                type = type
            )
        }
        is Playlist.Custom -> {
            PlaylistUiItem(
                id = id,
                title = name,
                subtitle = "$trackCount Tracks",
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                isDefault = false,
                type = PlaylistType.CUSTOM
            )
        }
    }
}
