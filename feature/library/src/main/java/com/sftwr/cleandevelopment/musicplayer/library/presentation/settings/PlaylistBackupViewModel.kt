package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.ExportCustomPlaylistsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetCustomPlaylistsForBackupUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.ImportCustomPlaylistsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistBackupViewModel @Inject constructor(
    private val getCustomPlaylistsForBackupUseCase: GetCustomPlaylistsForBackupUseCase,
    private val exportCustomPlaylistsUseCase: ExportCustomPlaylistsUseCase,
    private val importCustomPlaylistsUseCase: ImportCustomPlaylistsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistBackupUiState(isLoading = true))
    val uiState: StateFlow<PlaylistBackupUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    init {
        loadCustomPlaylists()
    }

    private fun loadCustomPlaylists() {
        viewModelScope.launch {
            getCustomPlaylistsForBackupUseCase().collect { list ->
                _uiState.update { state ->
                    val selectionItems = list.map { info ->
                        val existing = state.playlists.find { it.id == info.id }
                        PlaylistSelectionItem(
                            id = info.id,
                            name = info.name,
                            trackCount = info.trackCount,
                            isSelected = existing?.isSelected ?: true
                        )
                    }
                    val allSelected = selectionItems.isNotEmpty() && selectionItems.all { it.isSelected }
                    state.copy(
                        isLoading = false,
                        playlists = selectionItems,
                        isAllSelected = allSelected
                    )
                }
            }
        }
    }

    fun toggleSelection(playlistId: Long) {
        _uiState.update { state ->
            val updated = state.playlists.map {
                if (it.id == playlistId) it.copy(isSelected = !it.isSelected) else it
            }
            val allSelected = updated.isNotEmpty() && updated.all { it.isSelected }
            state.copy(playlists = updated, isAllSelected = allSelected)
        }
    }

    fun toggleSelectAll(selected: Boolean) {
        _uiState.update { state ->
            val updated = state.playlists.map { it.copy(isSelected = selected) }
            state.copy(playlists = updated, isAllSelected = selected)
        }
    }

    fun exportPlaylists(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val selectedIds = _uiState.value.playlists.filter { it.isSelected }.map { it.id }
                exportCustomPlaylistsUseCase(selectedIds, uri)
                _eventFlow.emit(UiEvent.ShowToast("Playlists backed up successfully!"))
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowToast("Failed to backup playlists: ${e.localizedMessage}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun importPlaylists(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                importCustomPlaylistsUseCase(uri)
                _eventFlow.emit(UiEvent.ShowToast("Playlists restored successfully!"))
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowToast("Failed to restore playlists: ${e.localizedMessage}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    sealed interface UiEvent {
        data class ShowToast(val message: String) : UiEvent
    }
}
