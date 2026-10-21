package com.sftwr.cleandevelopment.musicplayer.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Home : Screen

    @Serializable
    data object Player : Screen

    @Serializable
    data object Search : Screen

    @OptIn(kotlinx.serialization.InternalSerializationApi::class)
    @Serializable
    data class SongDetail(val songId: Long = -1L) : Screen

    @Serializable
    data object Settings : Screen
}