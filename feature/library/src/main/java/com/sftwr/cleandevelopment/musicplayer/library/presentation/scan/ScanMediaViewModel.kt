package com.sftwr.cleandevelopment.musicplayer.library.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MediaScanRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetScanningProgressUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.ScanMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanMediaViewModel @Inject constructor(
    private val scanMediaUseCase: ScanMediaUseCase,
    private val getScanningProgressUseCase: GetScanningProgressUseCase,
    private val musicRepository: MusicRepository,
    private val mediaScanRepository: MediaScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanMediaUiState())
    val uiState: StateFlow<ScanMediaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getScanningProgressUseCase().collect { progress ->
                _uiState.update {
                    it.copy(
                        isScanning = progress.isScanning,
                        currentPathBeingScanned = progress.currentPath,
                        scannedCount = progress.scannedCount,
                        isCompleted = progress.isCompleted
                    )
                }
            }
        }
        startScan()
    }

    fun startScan() {
        viewModelScope.launch {
            scanMediaUseCase()
        }
    }

    fun onOkClick(onDismiss: () -> Unit) {
        onDismiss()
        viewModelScope.launch {
            musicRepository.syncWithDevice()
            mediaScanRepository.resetScanState()
        }
    }

    fun dismiss(onDismiss: () -> Unit) {
        onDismiss()
        viewModelScope.launch {
            mediaScanRepository.resetScanState()
        }
    }
}
