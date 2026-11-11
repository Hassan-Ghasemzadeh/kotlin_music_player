package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Data class representing a required system runtime permission along with its descriptive metadata and status.
 *
 * @property permission Manifest permission string key.
 * @property title Human-readable permission title.
 * @property description Detailed explanation of why the permission is required by the app.
 * @property status Current permission granted status.
 * @property isRequired Indicates whether this permission is mandatory for core app functionality.
 */
data class PermissionItem(
    val permission: String,
    val title: String,
    val description: String,
    val status: PermissionStatus,
    val isRequired: Boolean = true
)
