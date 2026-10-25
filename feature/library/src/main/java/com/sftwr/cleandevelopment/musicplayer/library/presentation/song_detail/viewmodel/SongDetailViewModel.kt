package com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractExtraMetadata
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatSize
import com.sftwr.cleandevelopment.musicplayer.library.core.util.formatTime
import com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail.SongDetailUiModel
import com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail.SongDetailUiState
import com.sftwr.cleandevelopment.musicplayer.model.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel responsible for managing song detail states, extracting extended audio file metadata,
 * and parsing track attributes asynchronously on background thread pools.
 */
@RequiresApi(Build.VERSION_CODES.S)
@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Safely extract target song ID from type-safe navigation arguments or fallback keys
    private val songId: Long = runCatching {
        savedStateHandle.toRoute<Screen.SongDetail>().songId
    }.getOrNull()
        ?: savedStateHandle.get<Long>("id")
        ?: savedStateHandle.get<String>("id")?.toLongOrNull()
        ?: -1L

    private val _uiState = MutableStateFlow(SongDetailUiState())
    val uiState: StateFlow<SongDetailUiState> = _uiState.asStateFlow()

    init {
        loadSongDetail()
    }

    /**
     * Fetches device audio files, locates the matching song by ID, and extracts its extended metadata and album art.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun loadSongDetail() {
        if (songId == -1L) {
            _uiState.update { it.copy(isLoading = false, error = "شناسه آهنگ نامعتبر است") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                getDeviceSongsUseCase().collect { domainSongs ->
                    val domainSong = domainSongs.find { it.id == songId }

                    if (domainSong != null) {
                        Log.d(
                            "SongDetailViewModel",
                            "domainSong: ${domainSong.id}, songId: $songId, path: ${domainSong.path}"
                        )

                        // Offload heavy metadata and album art extraction tasks to IO dispatcher pool
                        val extraMetadata = withContext(Dispatchers.IO) {
                            domainSong.path.extractExtraMetadata()
                        }
                        val artworkBytes = withContext(Dispatchers.IO) {
                            domainSong.path.extractAlbumArt()
                        }

                        val artUriOrBytes: Any? = artworkBytes ?: if (domainSong.albumId != -1L) {
                            "content://media/external/audio/albumart/${domainSong.albumId}"
                        } else {
                            null
                        }

                        // Map domain entity and extracted attributes into UI model representation
                        val songDetailUiModel = SongDetailUiModel(
                            id = domainSong.id.toString(),
                            title = domainSong.title,
                            artist = domainSong.artist,
                            album = domainSong.album,
                            albumId = domainSong.albumId,
                            duration = domainSong.duration.formatTime(),
                            size = domainSong.size.formatSize(),
                            path = domainSong.path,
                            albumArtUri = artUriOrBytes,
                            genre = extraMetadata.genre ?: "نامشخص",
                            year = extraMetadata.year ?: "نامشخص",
                            bitrate = extraMetadata.bitrate ?: "",
                            sampleRate = extraMetadata.sampleRate ?: "",
                            mimeType = extraMetadata.mimeType ?: "نامشخص"
                        )

                        _uiState.update {
                            it.copy(
                                song = songDetailUiModel,
                                isLoading = false,
                                error = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                error = "آهنگ مورد نظر پیدا نشد",
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "خطای ناشناخته", isLoading = false) }
            }
        }
    }
}