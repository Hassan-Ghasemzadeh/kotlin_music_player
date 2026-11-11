package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.PermissionItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.PermissionStatus
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PermissionRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CheckPermissionsUseCaseTest {

    private lateinit var fakeRepository: FakePermissionRepository
    private lateinit var checkPermissionsUseCase: CheckPermissionsUseCase

    @Before
    fun setUp() {
        fakeRepository = FakePermissionRepository()
        checkPermissionsUseCase = CheckPermissionsUseCase(fakeRepository)
    }

    @Test
    fun invoke_returnsListOfRequiredPermissionsFromRepository() {
        val permissions = checkPermissionsUseCase()
        assertEquals(2, permissions.size)
        assertEquals("Audio & Music Library Access", permissions[0].title)
        assertEquals(PermissionStatus.GRANTED, permissions[0].status)
        assertEquals(PermissionStatus.DENIED, permissions[1].status)
    }
}

private class FakePermissionRepository : PermissionRepository {
    override fun getRequiredPermissions(): List<PermissionItem> {
        return listOf(
            PermissionItem(
                permission = "android.permission.READ_MEDIA_AUDIO",
                title = "Audio & Music Library Access",
                description = "Required to play audio",
                status = PermissionStatus.GRANTED
            ),
            PermissionItem(
                permission = "android.permission.POST_NOTIFICATIONS",
                title = "Notifications",
                description = "Required for media controls",
                status = PermissionStatus.DENIED
            )
        )
    }

    override fun checkPermissionStatus(permission: String): PermissionStatus {
        return if (permission == "android.permission.READ_MEDIA_AUDIO") PermissionStatus.GRANTED else PermissionStatus.DENIED
    }
}
