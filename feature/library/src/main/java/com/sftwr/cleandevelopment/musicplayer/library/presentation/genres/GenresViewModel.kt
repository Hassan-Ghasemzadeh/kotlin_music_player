package com.sftwr.cleandevelopment.musicplayer.library.presentation.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetGenresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel managing music genres state and search query filtering.
 */
@HiltViewModel
class GenresViewModel @Inject constructor(
    getGenresUseCase: GetGenresUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<GenresUiState> = combine(
        getGenresUseCase(),
        _searchQuery,
        _isLoading
    ) { genres, query, isLoading ->
        val filtered = if (query.isBlank()) {
            genres
        } else {
            genres.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        GenresUiState(
            genres = filtered,
            searchQuery = query,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GenresUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
