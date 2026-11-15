package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

data class PlaylistBackupUiState(
    val isLoading: Boolean = false,
    val playlists: List<PlaylistSelectionItem> = emptyList(),
    val isAllSelected: Boolean = false,
    val userMessage: String? = null
)

data class PlaylistSelectionItem(
    val id: Long,
    val name: String,
    val trackCount: Int,
    val isSelected: Boolean
)
