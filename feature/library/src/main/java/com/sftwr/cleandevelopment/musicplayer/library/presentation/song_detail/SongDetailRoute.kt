package com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail.viewmodel.SongDetailViewModel

@Composable
fun SongDetailRoute(
    onBackClick: () -> Unit,
    viewModel: SongDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SongDetailScreen(
        onBackClick = onBackClick,
        songDetail = uiState.song,
        error = uiState.error
    )
}