package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Home : Screen

    @Serializable
    data class Player(val songId: String, val songTitle: String) : Screen

    @Serializable
    object Search : Screen

    @kotlinx.serialization.Serializable
    data object Settings : Screen
}