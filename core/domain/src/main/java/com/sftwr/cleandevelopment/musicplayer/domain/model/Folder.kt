package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Domain model representing a music folder containing audio tracks.
 */
data class Folder(
    val path: String,
    val name: String,
    val trackCount: Int
)

typealias FolderItem = Folder
