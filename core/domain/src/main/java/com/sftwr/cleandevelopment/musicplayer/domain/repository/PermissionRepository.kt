package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.PermissionItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.PermissionStatus

/**
 * Repository interface for checking and evaluating system runtime permission states.
 */
interface PermissionRepository {

    /**
     * Inspects and returns the list of required app permissions and their current status.
     */
    fun getRequiredPermissions(): List<PermissionItem>

    /**
     * Checks the granted status of a specific manifest permission string.
     */
    fun checkPermissionStatus(permission: String): PermissionStatus
}
