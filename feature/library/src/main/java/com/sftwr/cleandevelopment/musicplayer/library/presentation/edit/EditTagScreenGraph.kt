package com.sftwr.cleandevelopment.musicplayer.library.presentation.edit

import android.app.Activity
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sftwr.cleandevelopment.musicplayer.library.presentation.edit.viewmodel.EditTagUiEvent
import com.sftwr.cleandevelopment.musicplayer.library.presentation.edit.viewmodel.EditTagViewModel
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Extension function to register EditTagScreen in the NavGraphBuilder.
 */
@RequiresApi(Build.VERSION_CODES.S)
fun NavGraphBuilder.editTagScreenGraph(
    onBackClick: () -> Unit,
) {
    composable<Screen.EditTagScreen> { backStackEntry ->
        val context = LocalContext.current

        val screenArgs = backStackEntry.toRoute<Screen.EditTagScreen>()
        val songId = screenArgs.songId

        val viewModel: EditTagViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsState()

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.saveTags()
            } else {
                Toast.makeText(context, "دسترسی برای ویرایش فایل داده نشد", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        LaunchedEffect(key1 = songId) {
            viewModel.loadSongDetailsExplicitly(songId)
        }

        LaunchedEffect(key1 = true) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is EditTagUiEvent.ShowToast -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }

                    is EditTagUiEvent.NavigateBack -> onBackClick()
                    is EditTagUiEvent.ShowPermissionDialog -> {
                        val intentSenderRequest =
                            IntentSenderRequest.Builder(event.intentSender).build()
                        permissionLauncher.launch(intentSenderRequest)
                    }
                }
            }
        }

        // Render the EditTagScreen with ViewModel states and callbacks
        EditTagScreen(
            state = state,
            onTitleChange = viewModel::onTitleChanged,
            onArtistChange = viewModel::onArtistChanged,
            onAlbumChange = viewModel::onAlbumChanged,
            onYearChange = viewModel::onYearChanged,
            onGenreChange = viewModel::onGenreChanged,
            onSaveClick = { viewModel.saveTags() },
            onBackClick = onBackClick
        )
    }
}