package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel.PlayerUiEvent
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel.TracksUiEvent
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel.TracksViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Stateful route wrapper for the Tracks screen, responsible for collecting ViewModel states,
 * handling one-time UI side-effects, and managing scoped storage permission intent launchers.
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun TracksRoute(
    viewModel: TracksViewModel = hiltViewModel(),
    onNavigateToSearch: () -> Unit,
    onBackClick: () -> Unit,
    onInfoClick: (Long) -> Unit,
    onTagEditClick: (Long) -> Unit,
) {
    val context = LocalContext.current

    // Collect UI state safely with lifecycle awareness to prevent unnecessary resource consumption
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Register activity result launcher for handling media deletion permission requests (Scoped Storage)
    val deletePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.retryDeleteAfterPermission()
        } else {
            Toast.makeText(context, "Delete permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle one-time navigation UI events as side-effects
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is TracksUiEvent.NavigateToSearch -> onNavigateToSearch()
            }
        }
    }

    // Handle playback and file deletion UI events (Toasts and Permission Dialogs)
    LaunchedEffect(key1 = true) {
        viewModel.deleteEvent.collect { event ->
            when (event) {
                is PlayerUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is PlayerUiEvent.ShowDeletePermissionDialog -> {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(event.intentSender).build()
                    deletePermissionLauncher.launch(intentSenderRequest)
                }
            }
        }
    }

    // Pass immutable state and action callbacks down to the stateless UI screen
    TracksScreen(
        songs = uiState.songs,
        isLoading = uiState.isLoading,
        onSongClick = { songId -> viewModel.onSongClicked(songId) },
        onBackClick = onBackClick,
        onSearchClick = onNavigateToSearch,
        onInfoClick = onInfoClick,
        onTagEditClick = onTagEditClick,
    )
}