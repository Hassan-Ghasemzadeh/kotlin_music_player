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
    data object Settings : Screen
}