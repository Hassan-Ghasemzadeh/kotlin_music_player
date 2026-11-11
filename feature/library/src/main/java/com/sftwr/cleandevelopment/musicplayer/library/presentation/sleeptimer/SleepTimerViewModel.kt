package com.sftwr.cleandevelopment.musicplayer.library.presentation.sleeptimer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.CancelSleepTimerUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSleepTimerRemainingTimeUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SetSleepTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SleepTimerViewModel @Inject constructor(
    private val getSleepTimerRemainingTimeUseCase: GetSleepTimerRemainingTimeUseCase,
    private val setSleepTimerUseCase: SetSleepTimerUseCase,
    private val cancelSleepTimerUseCase: CancelSleepTimerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SleepTimerUiState())
    val uiState: StateFlow<SleepTimerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getSleepTimerRemainingTimeUseCase().collect { timerState ->
                _uiState.update { current ->
                    val preset = if (timerState.isActive) {
                        TimerPreset.fromMinutes(timerState.durationMinutes)
                    } else if (current.selectedPreset != TimerPreset.CUSTOM) {
                        TimerPreset.OFF
                    } else {
                        current.selectedPreset
                    }

                    current.copy(
                        isActive = timerState.isActive,
                        remainingTimeFormatted = formatRemainingTime(timerState.remainingTimeMs),
                        durationMinutes = timerState.durationMinutes,
                        selectedPreset = preset
                    )
                }
            }
        }
    }

    fun selectPreset(preset: TimerPreset) {
        if (preset == TimerPreset.CUSTOM) {
            _uiState.update { it.copy(showCustomTimePicker = true, selectedPreset = TimerPreset.CUSTOM) }
        } else {
            _uiState.update { it.copy(selectedPreset = preset, showCustomTimePicker = false) }
            viewModelScope.launch {
                setSleepTimerUseCase(preset.minutes)
            }
        }
    }

    fun showCustomTimePicker() {
        _uiState.update { it.copy(showCustomTimePicker = true) }
    }

    fun dismissCustomTimePicker() {
        _uiState.update { it.copy(showCustomTimePicker = false) }
    }

    fun setCustomTime(hours: Int, minutes: Int) {
        val totalMinutes = hours * 60 + minutes
        _uiState.update {
            it.copy(
                showCustomTimePicker = false,
                customHours = hours,
                customMinutes = minutes,
                selectedPreset = TimerPreset.CUSTOM
            )
        }
        viewModelScope.launch {
            setSleepTimerUseCase(totalMinutes)
        }
    }

    fun cancelTimer() {
        viewModelScope.launch {
            cancelSleepTimerUseCase()
            _uiState.update { it.copy(selectedPreset = TimerPreset.OFF) }
        }
    }

    private fun formatRemainingTime(millis: Long): String {
        if (millis <= 0) return ""
        val totalSecs = millis / 1000
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        return if (hours > 0) {
            String.format("%dh %02dm %02ds", hours, mins, secs)
        } else {
            String.format("%02dm %02ds", mins, secs)
        }
    }
}
