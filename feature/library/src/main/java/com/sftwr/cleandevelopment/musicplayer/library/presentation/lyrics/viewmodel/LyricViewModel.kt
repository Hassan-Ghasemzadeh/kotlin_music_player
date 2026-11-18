package com.sftwr.cleandevelopment.musicplayer.library.presentation.lyrics.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.DeleteLyricUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetAllLyricsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetLyricForSongUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.RenameLyricUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SaveLyricUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SearchLyricOnlineUseCase
import com.sftwr.cleandevelopment.musicplayer.model.Lyric
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface OnlineSearchState {
    data object Idle : OnlineSearchState
    data object Loading : OnlineSearchState
    data class Success(val lyric: Lyric) : OnlineSearchState
    data class Error(val message: String) : OnlineSearchState
}

@HiltViewModel
class LyricViewModel @Inject constructor(
    private val getLyricForSongUseCase: GetLyricForSongUseCase,
    private val saveLyricUseCase: SaveLyricUseCase,
    private val deleteLyricUseCase: DeleteLyricUseCase,
    private val getAllLyricsUseCase: GetAllLyricsUseCase,
    private val renameLyricUseCase: RenameLyricUseCase,
    private val searchLyricOnlineUseCase: SearchLyricOnlineUseCase
) : ViewModel() {

    private val _onlineSearchState = MutableStateFlow<OnlineSearchState>(OnlineSearchState.Idle)
    val onlineSearchState: StateFlow<OnlineSearchState> = _onlineSearchState.asStateFlow()

    private val _toastEvent = Channel<String>()
    val toastEvent = _toastEvent.receiveAsFlow()

    val allSavedLyrics: StateFlow<List<Lyric>> = getAllLyricsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getLyricForSong(songId: Long): StateFlow<Lyric?> {
        return getLyricForSongUseCase(songId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun searchOnline(title: String, artist: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            _onlineSearchState.value = OnlineSearchState.Loading
            val result = searchLyricOnlineUseCase(title, artist)
            result.fold(
                onSuccess = { lyric ->
                    _onlineSearchState.value = OnlineSearchState.Success(lyric)
                },
                onFailure = { error ->
                    _onlineSearchState.value = OnlineSearchState.Error(error.localizedMessage ?: "Lyric search failed")
                }
            )
        }
    }

    fun resetOnlineSearchState() {
        _onlineSearchState.value = OnlineSearchState.Idle
    }

    fun saveLyric(songId: Long, title: String, artist: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val lyric = Lyric(
                songId = songId,
                title = title,
                artist = artist,
                content = content
            )
            saveLyricUseCase(lyric)
            _toastEvent.send("Lyric saved successfully")
        }
    }

    fun loadAndSaveLrcFromUri(context: Context, songId: Long, title: String, artist: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                }
                if (!fileContent.isNullOrBlank()) {
                    val lyric = Lyric(
                        songId = songId,
                        title = title,
                        artist = artist,
                        content = fileContent
                    )
                    saveLyricUseCase(lyric)
                    _toastEvent.send("Lyric imported successfully")
                } else {
                    _toastEvent.send("Failed to read file content")
                }
            } catch (e: Exception) {
                _toastEvent.send("Error importing file: ${e.message}")
            }
        }
    }

    fun deleteLyric(songId: Long) {
        viewModelScope.launch {
            deleteLyricUseCase(songId)
            _toastEvent.send("Lyric deleted")
        }
    }

    fun renameLyric(songId: Long, newTitle: String, newArtist: String) {
        viewModelScope.launch {
            renameLyricUseCase(songId, newTitle, newArtist)
            _toastEvent.send("Lyric renamed")
        }
    }

}
