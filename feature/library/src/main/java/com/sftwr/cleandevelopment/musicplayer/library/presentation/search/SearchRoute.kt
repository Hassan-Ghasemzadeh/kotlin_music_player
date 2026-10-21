package com.sftwr.cleandevelopment.musicplayer.library.presentation.search


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.viewModel.SearchPlayerUiEvent
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.viewModel.SearchViewModel

@Composable
fun SearchRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onInfoClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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

// گوش دادن به رویدادها
    LaunchedEffect(key1 = true) {
        viewModel.deleteEvent.collect { event ->
            when (event) {
                is SearchPlayerUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is SearchPlayerUiEvent.ShowDeletePermissionDialog -> {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(event.intentSender).build()
                    deletePermissionLauncher.launch(intentSenderRequest)
                }
            }
        }
    }
    val actions = remember(viewModel, onNavigateBack, onNavigateToPlayer) {
        SearchActions(
            onQueryChange = { viewModel.onQueryChanged(it) },
            onClearQueryClick = { viewModel.onClearQuery() },
            onCancelClick = onNavigateBack,
            onSongClick = { songId ->
                viewModel.onSongClicked(songId)
            },
            onDeleteClick = { id ->
                viewModel.deleteCurrentOrSelectedSong(songId = id)
            },
            onInfoClick = onInfoClick
        )
    }

    SearchScreen(
        uiState = uiState,
        actions = actions
    )
}