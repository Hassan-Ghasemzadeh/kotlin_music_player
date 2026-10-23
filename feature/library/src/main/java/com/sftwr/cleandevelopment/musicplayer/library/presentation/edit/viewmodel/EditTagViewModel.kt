package com.sftwr.cleandevelopment.musicplayer.library.presentation.edit.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.UpdateSongTagsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractExtraMetadata
import com.sftwr.cleandevelopment.musicplayer.model.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface EditTagUiEvent {
    data class ShowToast(val message: String) : EditTagUiEvent
    data class ShowPermissionDialog(val intentSender: android.content.IntentSender) : EditTagUiEvent
    object NavigateBack : EditTagUiEvent
}

data class EditTagState(
    val songId: Long = 0L,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val year: String = "",
    val genre: String = "",
    val songUri: String = "",
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
)

@RequiresApi(Build.VERSION_CODES.S)
@HiltViewModel
class EditTagViewModel @Inject constructor(
    private val updateSongTagsUseCase: UpdateSongTagsUseCase,
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTagState())
    val uiState: StateFlow<EditTagState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<EditTagUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        val songId = runCatching {
            savedStateHandle.toRoute<Screen.EditTagScreen>().songId
        }.getOrElse {
            savedStateHandle.get<Long>("songId") ?: -1L
        }

        if (songId != -1L) {
            loadSongDetails(songId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun loadSongDetailsExplicitly(songId: Long) {
        if (uiState.value.songId == songId && !uiState.value.isLoading) return
        loadSongDetails(songId)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun loadSongDetails(songId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, songId = songId) }

            val songs = getDeviceSongsUseCase().firstOrNull()
            val song = songs?.find { it.id == songId }
            if (song != null) {
                val extraMetadata = withContext(Dispatchers.IO) {
                    song.path.extractExtraMetadata()
                }
                _uiState.update {
                    it.copy(
                        title = song.title,
                        artist = song.artist,
                        album = song.album,
                        songUri = song.path,
                        genre = extraMetadata.genre?.ifEmpty { "نامشخص" } ?: "نامشخص",
                        year = extraMetadata.year?.ifEmpty { "نامشخص" } ?: "نامشخص",
                        isLoading = false)
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onArtistChanged(value: String) {
        _uiState.update { it.copy(artist = value) }
    }

    fun onAlbumChanged(value: String) {
        _uiState.update { it.copy(album = value) }
    }

    fun onYearChanged(value: String) {
        _uiState.update { it.copy(year = value) }
    }

    fun onGenreChanged(value: String) {
        _uiState.update { it.copy(genre = value) }
    }

    fun saveTags() {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val currentState = _uiState.value
            val result = updateSongTagsUseCase(
                songId = currentState.songId,
                title = currentState.title,
                artist = currentState.artist,
                album = currentState.album,
                year = currentState.year,
                genre = currentState.genre
            )

            _uiState.update { it.copy(isSaving = false) }

            when (result) {
                is UpdateTagResult.Success -> {
                    _uiEvent.send(EditTagUiEvent.ShowToast("تگ‌ها با موفقیت ویرایش شدند"))
                    _uiEvent.send(EditTagUiEvent.NavigateBack)
                }

                is UpdateTagResult.RequiresPermission -> {
                    _uiEvent.send(EditTagUiEvent.ShowPermissionDialog(result.intentSender))
                }

                is UpdateTagResult.Error -> {
                    _uiEvent.send(EditTagUiEvent.ShowToast("خطا: ${result.message}"))                }
            }
        }
    }
}