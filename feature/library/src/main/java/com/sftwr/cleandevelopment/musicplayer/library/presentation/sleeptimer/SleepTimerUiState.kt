package com.sftwr.cleandevelopment.musicplayer.library.presentation.sleeptimer

data class SleepTimerUiState(
    val isActive: Boolean = false,
    val remainingTimeFormatted: String = "",
    val durationMinutes: Int = 0,
    val selectedPreset: TimerPreset = TimerPreset.OFF,
    val showCustomTimePicker: Boolean = false,
    val customHours: Int = 0,
    val customMinutes: Int = 30
)
