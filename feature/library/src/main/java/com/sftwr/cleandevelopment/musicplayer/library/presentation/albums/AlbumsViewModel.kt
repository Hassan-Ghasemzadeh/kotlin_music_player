package com.sftwr.cleandevelopment.musicplayer.library.presentation.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetAlbumsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel responsible for managing albums state, loading music albums, and filtering by search query.
 */
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    getAlbumsUseCase: GetAlbumsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<AlbumsUiState> = combine(
        getAlbumsUseCase(),
        _searchQuery,
        _isLoading
    ) { albums, query, isLoading ->
        val filteredAlbums = if (query.isBlank()) {
            albums
        } else {
            albums.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true)
            }
        }
        AlbumsUiState(
            albums = filteredAlbums,
            searchQuery = query,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlbumsUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
