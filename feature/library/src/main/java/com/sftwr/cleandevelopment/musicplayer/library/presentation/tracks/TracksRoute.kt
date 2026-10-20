package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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

@Composable
fun TracksRoute(
    viewModel: TracksViewModel = hiltViewModel(),
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    // Collect UI state safely with lifecycle awareness
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val deletePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.retryDeleteAfterPermission()
        } else {
            Toast.makeText(context, "اجازه حذف داده نشد", Toast.LENGTH_SHORT).show()
        }
    }
    // Handle one-time UI events (Navigation side-effects)
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is TracksUiEvent.NavigateToPlayer -> onNavigateToPlayer
                is TracksUiEvent.NavigateToSearch -> onNavigateToSearch()
            }
        }
    }
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
    // Pass state and action callbacks down to the stateless UI screen
    TracksScreen(
        songs = uiState.songs,
        isLoading = uiState.isLoading, // اضافه شده
        onSongClick = { songId -> viewModel.onSongClicked(songId) },
        onBackClick = onBackClick,
        onSearchClick = onNavigateToSearch
    )
}