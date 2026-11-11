@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.sftwr.cleandevelopment.musicplayer.model

import kotlinx.serialization.Serializable

/**
 * Represents the type-safe navigation screens and destinations within the application,
 * leveraging "kotlinx.serialization" for Jetpack Compose Navigation arguments parsing.
 */
@Serializable
sealed interface Screen {

    @Serializable
    data object Home : Screen

    @Serializable
    data object Player : Screen

    @Serializable
    data object Search : Screen

    @Serializable
    data class SongDetail(val songId: Long = -1L) : Screen

    @Serializable
    data class EditTagScreen(val songId: Long = -1L) : Screen

    @Serializable
    data class PlaylistDetail(
        val playlistId: Long,
        val playlistName: String,
        val playlistTypeString: String
    ) : Screen

    @Serializable
    data object Albums : Screen

    @Serializable
    data object Artists : Screen

    @Serializable
    data object Favorites : Screen

    @Serializable
    data object Folders : Screen

    @Serializable
    data object Genres : Screen

    @Serializable
    data class AlbumDetail(
        val albumId: Long,
        val albumTitle: String
    ) : Screen

    @Serializable
    data class ArtistDetail(
        val artistName: String
    ) : Screen

    @Serializable
    data class FolderDetail(
        val folderPath: String,
        val folderName: String
    ) : Screen

    @Serializable
    data class GenreDetail(
        val genreName: String
    ) : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object Permissions : Screen
}