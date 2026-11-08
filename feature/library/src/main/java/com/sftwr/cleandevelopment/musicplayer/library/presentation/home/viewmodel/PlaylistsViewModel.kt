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
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetPlaylistsUseCase
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
 * ViewModel managing playlist state, user custom playlist creation,
 * input validation, and real-time Room database updates.
 */
@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    getPlaylistsUseCase: GetPlaylistsUseCase,
    private val createCustomPlaylistUseCase: CreateCustomPlaylistUseCase,
) : ViewModel() {

    private val _dialogState = MutableStateFlow(DialogState())

    private data class DialogState(
        val isVisible: Boolean = false,
        val name: String = "",
        val error: String? = null
    )

    val uiState: StateFlow<PlaylistsUiState> = combine(
        getPlaylistsUseCase(),
        _dialogState
    ) { playlists, dialog ->
        val items = playlists.map { it.toUiItem() }
        PlaylistsUiState(
            playlists = items,
            totalPlaylistCount = items.size,
            isLoading = false,
            isCreateDialogVisible = dialog.isVisible,
            newPlaylistName = dialog.name,
            nameError = dialog.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaylistsUiState(isLoading = true)
    )

    fun onAddPlaylistClick() {
        _dialogState.update { it.copy(isVisible = true, name = "", error = null) }
    }

    fun onDismissDialog() {
        _dialogState.update { it.copy(isVisible = false, name = "", error = null) }
    }

    fun onPlaylistNameChanged(name: String) {
        val error = if (name.isNotBlank() && name.length > 50) {
            "Playlist name must be under 50 characters"
        } else {
            null
        }
        _dialogState.update { it.copy(name = name, error = error) }
    }

    fun confirmCreatePlaylist() {
        val name = _dialogState.value.name.trim()
        if (name.isBlank()) {
            _dialogState.update { it.copy(error = "Playlist name cannot be empty") }
            return
        }

        viewModelScope.launch {
            val result = createCustomPlaylistUseCase(name)
            if (result.isSuccess) {
                _dialogState.update { it.copy(isVisible = false, name = "", error = null) }
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
