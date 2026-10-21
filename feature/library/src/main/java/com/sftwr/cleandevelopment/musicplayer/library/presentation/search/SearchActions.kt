package com.sftwr.cleandevelopment.musicplayer.library.presentation.search

// Encapsulates all user interactions for SearchScreen
data class SearchActions(
    val onQueryChange: (String) -> Unit = {},
    val onClearQueryClick: () -> Unit = {},
    val onCancelClick: () -> Unit = {},
    val onDeleteClick: (id: Long) -> Unit,
    val onSongClick: (String) -> Unit = {},
    val onInfoClick: (Long) -> Unit,
)