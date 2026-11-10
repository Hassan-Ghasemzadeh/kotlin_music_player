package com.sftwr.cleandevelopment.musicplayer.library.presentation.genres

import com.sftwr.cleandevelopment.musicplayer.domain.model.GenreItem

data class GenresUiState(
    val genres: List<GenreItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)
