package com.sftwr.cleandevelopment.musicplayer.library.presentation.permissions

import com.sftwr.cleandevelopment.musicplayer.domain.model.PermissionItem

/**
 * UI State representing the status of runtime permissions.
 *
 * @property permissions List of required app permissions with their status.
 * @property allGranted True if all required permissions are currently granted.
 */
data class PermissionsUiState(
    val permissions: List<PermissionItem> = emptyList(),
    val allGranted: Boolean = false
)
