package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.presentation.albums.albumDetailGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.albums.albumsGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.artists.artistDetailGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.artists.artistsGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.edit.editTagScreenGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.favorites.favoritesGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.folders.folderDetailGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.folders.foldersGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.genres.genreDetailGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.genres.genresGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.home.homeGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.player.PlayerScreen
import com.sftwr.cleandevelopment.musicplayer.library.presentation.playlist_detail.playlistDetailGraph
import com.sftwr.cleandevelopment.musicplayer.library.presentation.search.SearchRoute
import com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail.SongDetailRoute
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.TracksRouteKey
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.tracksGraph
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Main application navigation graph hosting all top-level destinations, feature graphs,
 * and type-safe screen routes using Jetpack Compose Navigation.
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    playerManager: PlayerManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        modifier = modifier
    ) {
        // Home feature navigation graph
        homeGraph(
            onNavigateToPlayer = { songId ->
                navController.navigate(Screen.Player)
            },
            onNavigateToSearch = {
                navController.navigate(Screen.Search)
            },
            onOpenPlaylist = {
                // Legacy callback
            },
            onNavigateToTracks = {
                // Navigate to tracks library screen when card is selected
                navController.navigate(TracksRouteKey)
            },
            onNavigateToAlbums = {
                navController.navigate(Screen.Albums)
            },
            onNavigateToArtists = {
                navController.navigate(Screen.Artists)
            },
            onNavigateToFavorites = {
                navController.navigate(Screen.Favorites)
            },
            onNavigateToFolders = {
                navController.navigate(Screen.Folders)
            },
            onNavigateToGenres = {
                navController.navigate(Screen.Genres)
            },
            onOpenPlaylistDetail = { id, title, typeName ->
                navController.navigate(Screen.PlaylistDetail(id, title, typeName))
            }
        )

        // Favorites feature navigation graph
        favoritesGraph(
            playerManager = playerManager,
            onBackClick = {
                navController.popBackStack()
            },
            onSearchClick = {
                navController.navigate(Screen.Search)
            },
            onAlbumClick = { albumId, albumTitle ->
                navController.navigate(Screen.AlbumDetail(albumId, albumTitle))
            },
            onArtistClick = { artistName ->
                navController.navigate(Screen.ArtistDetail(artistName))
            },
            onGenreClick = { genreName ->
                navController.navigate(Screen.GenreDetail(genreName))
            },
            onInfoClick = { songId ->
                navController.navigate(Screen.SongDetail(songId))
            },
            onTagEditClick = { songId ->
                navController.navigate(Screen.EditTagScreen(songId))
            }
        )

        // Albums feature navigation graph
        albumsGraph(
            onAlbumClick = { albumId, albumTitle ->
                navController.navigate(Screen.AlbumDetail(albumId, albumTitle))
            },
            onBackClick = {
                navController.popBackStack()
            },
            onSearchClick = {
                navController.navigate(Screen.Search)
            }
        )

        // Artists feature navigation graph
        artistsGraph(
            onArtistClick = { artistName ->
                navController.navigate(Screen.ArtistDetail(artistName))
            },
            onBackClick = {
                navController.popBackStack()
            },
            onSearchClick = {
                navController.navigate(Screen.Search)
            }
        )

        // Folders feature navigation graph
        foldersGraph(
            onFolderClick = { folderPath, folderName ->
                navController.navigate(Screen.FolderDetail(folderPath, folderName))
            },
            onBackClick = {
                navController.popBackStack()
            },
            onSearchClick = {
                navController.navigate(Screen.Search)
            }
        )

        // Genres feature navigation graph
        genresGraph(
            onGenreClick = { genreName ->
                navController.navigate(Screen.GenreDetail(genreName))
            },
            onBackClick = {
                navController.popBackStack()
            },
            onSearchClick = {
                navController.navigate(Screen.Search)
            }
        )

        // Folder Detail feature navigation graph
        folderDetailGraph(
            playerManager = playerManager,
            onBackClick = {
                navController.popBackStack()
            },
            onSearchClick = {
                navController.navigate(Screen.Search)
            },
            onInfoClick = { songId ->
                navController.navigate(Screen.SongDetail(songId))
            },
            onTagEditClick = { songId ->
                navController.navigate(Screen.EditTagScreen(songId))
            }
        )

        // Genre Detail feature navigation graph
        genreDetailGraph(
            playerManager = playerManager,
            onBackClick = {
                navController.popBackStack()
            },
            onSearchClick = {
                navController.navigate(Screen.Search)
            },
            onInfoClick = { songId ->
                navController.navigate(Screen.SongDetail(songId))
            },
            onTagEditClick = { songId ->
                navController.navigate(Screen.EditTagScreen(songId))
            }
        )

        // Album Detail feature navigation graph
        albumDetailGraph(
            playerManager = playerManager,
            onBackClick = {
                navController.popBackStack()
            },
            onSearchClick = {
                navController.navigate(Screen.Search)
            },
            onInfoClick = { songId ->
                navController.navigate(Screen.SongDetail(songId))
            },
            onTagEditClick = { songId ->
                navController.navigate(Screen.EditTagScreen(songId))
            }
        )

        // Artist Detail feature navigation graph
        artistDetailGraph(
            playerManager = playerManager,
            onBackClick = {
                navController.popBackStack()
            },
            onSearchClick = {
                navController.navigate(Screen.Search)
            },
            onAlbumClick = { albumId, albumTitle ->
                navController.navigate(Screen.AlbumDetail(albumId, albumTitle))
            },
            onInfoClick = { songId ->
                navController.navigate(Screen.SongDetail(songId))
            },
            onTagEditClick = { songId ->
                navController.navigate(Screen.EditTagScreen(songId))
            }
        )

        // Playlist Detail feature navigation graph
        playlistDetailGraph(
            playerManager = playerManager,
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToSearch = {
                navController.navigate(Screen.Search)
            },
            onInfoClick = { songId ->
                navController.navigate(Screen.SongDetail(songId))
            },
            onTagEditClick = { songId ->
                navController.navigate(Screen.EditTagScreen(songId))
            }
        )

        // Tracks library feature navigation graph
        tracksGraph(
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToSearch = {
                navController.navigate(Screen.Search)
            },
            onInfoClick = { songId ->
                navController.navigate(Screen.SongDetail(songId))
            },
            onTagEditClick = { songId ->
                navController.navigate(Screen.EditTagScreen(songId))
            }
        )

        // Search destination screen route
        composable<Screen.Search> {
            SearchRoute(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player)
                },
                onInfoClick = { songId ->
                    navController.navigate(Screen.SongDetail(songId))
                },
                onTagEditClick = { songId ->
                    navController.navigate(Screen.EditTagScreen(songId))
                }
            )
        }

        // Full-screen audio player destination route
        composable<Screen.Player> {
            PlayerScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        // Detailed song metadata view destination route
        composable<Screen.SongDetail> {
            SongDetailRoute(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        // Edit tag feature navigation graph
        editTagScreenGraph(
            onBackClick = {
                navController.popBackStack()
            },
        )
    }
}
