package com.sftwr.cleandevelopment.musicplayer.library.presentation.permissions

import androidx.lifecycle.ViewModel
import com.sftwr.cleandevelopment.musicplayer.domain.model.PermissionStatus
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.CheckPermissionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val checkPermissionsUseCase: CheckPermissionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        val permissions = checkPermissionsUseCase()
        val allGranted = permissions.isNotEmpty() && permissions.all { it.status == PermissionStatus.GRANTED }
        _uiState.update {
            it.copy(
                permissions = permissions,
                allGranted = allGranted
            )
        }
    }
}
