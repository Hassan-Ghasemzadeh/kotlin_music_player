package com.sftwr.cleandevelopment.musicplayer.library.presentation.permissions

import com.sftwr.cleandevelopment.musicplayer.domain.model.PermissionItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.PermissionStatus
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PermissionRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.CheckPermissionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakePermissionRepositoryForViewModel
    private lateinit var checkPermissionsUseCase: CheckPermissionsUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakePermissionRepositoryForViewModel()
        checkPermissionsUseCase = CheckPermissionsUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initialStateReflectsPermissionStatuses() = runTest {
        val viewModel = PermissionsViewModel(checkPermissionsUseCase)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.permissions.size)
        assertFalse(viewModel.uiState.value.allGranted)
    }

    @Test
    fun refreshPermissions_whenAllGranted_updatesAllGrantedToTrue() = runTest {
        val viewModel = PermissionsViewModel(checkPermissionsUseCase)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        fakeRepository.grantAll()
        viewModel.refreshPermissions()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.allGranted)
    }
}

class FakePermissionRepositoryForViewModel : PermissionRepository {
    private var allGrantedState = false

    fun grantAll() {
        allGrantedState = true
    }

    override fun getRequiredPermissions(): List<PermissionItem> {
        val status = if (allGrantedState) PermissionStatus.GRANTED else PermissionStatus.DENIED
        return listOf(
            PermissionItem(
                permission = "android.permission.READ_MEDIA_AUDIO",
                title = "Audio Access",
                description = "Scan audio files",
                status = PermissionStatus.GRANTED
            ),
            PermissionItem(
                permission = "android.permission.POST_NOTIFICATIONS",
                title = "Notifications",
                description = "Playback controls",
                status = status
            )
        )
    }

    override fun checkPermissionStatus(permission: String): PermissionStatus {
        return if (allGrantedState || permission == "android.permission.READ_MEDIA_AUDIO") {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.DENIED
        }
    }
}
