package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.PermissionItem
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PermissionRepository
import javax.inject.Inject

/**
 * Use case for evaluating and retrieving current runtime permission statuses across the app.
 */
class CheckPermissionsUseCase @Inject constructor(
    private val permissionRepository: PermissionRepository
) {
    /**
     * Executes permission inspection and returns list of permission items.
     */
    operator fun invoke(): List<PermissionItem> {
        return permissionRepository.getRequiredPermissions()
    }
}
