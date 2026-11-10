package com.sftwr.cleandevelopment.musicplayer.library.presentation.folders

import com.sftwr.cleandevelopment.musicplayer.domain.model.FolderItem

data class FoldersUiState(
    val folders: List<FolderItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)
