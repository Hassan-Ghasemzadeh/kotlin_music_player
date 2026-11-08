package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Sealed class representing a playlist in the domain layer.
 */
sealed class Playlist {
    abstract val id: Long
    abstract val name: String
    abstract val trackCount: Int
    abstract val type: PlaylistType

    /**
     * Default (Smart) Playlist dynamically computed or queried from device audio metadata.
     */
    data class Default(
        override val id: Long,
        override val name: String,
        override val trackCount: Int,
        override val type: PlaylistType
    ) : Playlist()

    /**
     * User-created custom playlist stored permanently in local storage.
     */
    data class Custom(
        override val id: Long,
        override val name: String,
        override val trackCount: Int,
        val createdAt: Long = System.currentTimeMillis()
    ) : Playlist() {
        override val type: PlaylistType = PlaylistType.CUSTOM
    }
}
