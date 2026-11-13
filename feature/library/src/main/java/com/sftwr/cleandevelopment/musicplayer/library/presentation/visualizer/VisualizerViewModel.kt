package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.UpdateAudioSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.model.VisualizerStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Visualizer Selection Screen managing available visualizer options,
 * current DataStore preference state, and updating user visualizer selections.
 */
@HiltViewModel
class VisualizerViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateAudioSettingsUseCase: UpdateAudioSettingsUseCase
) : ViewModel() {

    val availableStyles: List<VisualizerStyle> = VisualizerStyle.entries

    val savedVisualizer: StateFlow<VisualizerStyle> = getSettingsUseCase()
        .map { settings ->
            VisualizerStyle.fromId(
                settings.selectedVisualizer.ifEmpty { settings.circularVisualizerType }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VisualizerStyle.DEFAULT
        )

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex: StateFlow<Int> = _selectedIndex.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = getSettingsUseCase().first()
            val currentStyle = VisualizerStyle.fromId(
                settings.selectedVisualizer.ifEmpty { settings.circularVisualizerType }
            )
            val index = availableStyles.indexOf(currentStyle).coerceAtLeast(0)
            _selectedIndex.value = index
        }
    }

    fun onPageChanged(index: Int) {
        _selectedIndex.value = index
    }

    fun applySelection(style: VisualizerStyle, onApplied: () -> Unit) {
        viewModelScope.launch {
            updateAudioSettingsUseCase.setCircularVisualizerType(style.id)
            _userMessage.value = "Visualizer applied"
            onApplied()
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
