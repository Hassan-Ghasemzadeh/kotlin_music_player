package com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel

import androidx.compose.ui.graphics.vector.ImageVector
import com.sftwr.cleandevelopment.musicplayer.domain.model.PlaylistType

/**
 * UI representation of a playlist item for display in the Home screen list.
 */
data class PlaylistUiItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isDefault: Boolean = false,
    val type: PlaylistType = PlaylistType.CUSTOM
)

/**
 * Reactive UI state representing the playlist section and playlist creation dialog.
 */
data class PlaylistsUiState(
    val playlists: List<PlaylistUiItem> = emptyList(),
    val totalPlaylistCount: Int = 0,
    val isLoading: Boolean = false,
    val isCreateDialogVisible: Boolean = false,
    val newPlaylistName: String = "",
    val nameError: String? = null,
    val error: String? = null
)
