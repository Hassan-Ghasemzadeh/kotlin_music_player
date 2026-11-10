package com.sftwr.cleandevelopment.musicplayer.library.presentation.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen
import kotlinx.serialization.InternalSerializationApi

/**
 * Extension function to register the home screen and its navigation graph
 * without exposing internal ViewModels or UI components to the :app module.
 */
@OptIn(InternalSerializationApi::class)
fun NavGraphBuilder.homeGraph(
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onOpenPlaylist: (String) -> Unit,
    onNavigateToTracks: () -> Unit,
    onNavigateToAlbums: () -> Unit = {},
    onNavigateToArtists: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToFolders: () -> Unit = {},
    onNavigateToGenres: () -> Unit = {},
    onOpenPlaylistDetail: (id: Long, title: String, typeName: String) -> Unit = { _, _, _ -> },
    onOpenDrawer: () -> Unit = {},
) {
    composable<Screen.Home> {

        HomeRoute(
            onNavigateToPlayer = onNavigateToPlayer,
            onNavigateToSearch = onNavigateToSearch,
            onOpenPlaylist = onOpenPlaylist,
            onNavigateToTracks = onNavigateToTracks,
            onNavigateToAlbums = onNavigateToAlbums,
            onNavigateToArtists = onNavigateToArtists,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToFolders = onNavigateToFolders,
            onNavigateToGenres = onNavigateToGenres,
            onOpenPlaylistDetail = onOpenPlaylistDetail,
            onOpenDrawer = onOpenDrawer
        )
    }
}
