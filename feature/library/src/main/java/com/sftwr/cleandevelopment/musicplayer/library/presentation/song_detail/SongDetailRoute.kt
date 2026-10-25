package com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail.viewmodel.SongDetailViewModel

/**
 * Stateful route wrapper composable for the Song Detail feature, responsible for collecting
 * UI states reactively from [SongDetailViewModel] and delegating rendering to [SongDetailScreen].
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SongDetailRoute(
    onBackClick: () -> Unit,
    viewModel: SongDetailViewModel = hiltViewModel()
) {
    // Collect screen state safely with lifecycle awareness
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Pass immutable state properties and navigation actions down to the stateless screen
    SongDetailScreen(
        onBackClick = onBackClick,
        songDetail = uiState.song,
        error = uiState.error
    )
}