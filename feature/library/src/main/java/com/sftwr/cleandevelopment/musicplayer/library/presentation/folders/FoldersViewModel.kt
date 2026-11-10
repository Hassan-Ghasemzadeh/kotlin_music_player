package com.sftwr.cleandevelopment.musicplayer.library.presentation.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetFoldersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel managing music folders state and search query filtering.
 */
@HiltViewModel
class FoldersViewModel @Inject constructor(
    getFoldersUseCase: GetFoldersUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<FoldersUiState> = combine(
        getFoldersUseCase(),
        _searchQuery,
        _isLoading
    ) { folders, query, isLoading ->
        val filtered = if (query.isBlank()) {
            folders
        } else {
            folders.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.path.contains(query, ignoreCase = true)
            }
        }
        FoldersUiState(
            folders = filtered,
            searchQuery = query,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FoldersUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
