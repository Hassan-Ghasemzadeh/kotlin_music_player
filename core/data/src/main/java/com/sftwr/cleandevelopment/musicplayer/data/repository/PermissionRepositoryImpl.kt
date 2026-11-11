package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.sftwr.cleandevelopment.musicplayer.domain.model.PermissionItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.PermissionStatus
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PermissionRepository {

    override fun getRequiredPermissions(): List<PermissionItem> {
        val list = mutableListOf<PermissionItem>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(
                PermissionItem(
                    permission = Manifest.permission.READ_MEDIA_AUDIO,
                    title = "Audio & Music Library Access",
                    description = "Required to scan, index, and play audio files stored on your device storage.",
                    status = checkPermissionStatus(Manifest.permission.READ_MEDIA_AUDIO)
                )
            )
            list.add(
                PermissionItem(
                    permission = Manifest.permission.POST_NOTIFICATIONS,
                    title = "Notifications & Media Controls",
                    description = "Required to display persistent music playback controls, mini-player, and timer updates in the notification shade.",
                    status = checkPermissionStatus(Manifest.permission.POST_NOTIFICATIONS)
                )
            )
        } else {
            list.add(
                PermissionItem(
                    permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                    title = "Storage Access",
                    description = "Required to scan, read, and play music tracks from device storage.",
                    status = checkPermissionStatus(Manifest.permission.READ_EXTERNAL_STORAGE)
                )
            )
        }

        return list
    }

    override fun checkPermissionStatus(permission: String): PermissionStatus {
        val result = ContextCompat.checkSelfPermission(context, permission)
        return if (result == PackageManager.PERMISSION_GRANTED) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.DENIED
        }
    }
}
