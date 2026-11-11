package com.sftwr.cleandevelopment.musicplayer.library.presentation.sleeptimer

enum class TimerPreset(val minutes: Int, val label: String) {
    OFF(0, "Off"),
    MIN_30(30, "30 minutes"),
    HOUR_1(60, "1 hour"),
    HOUR_1_30(90, "1 hour 30 mins"),
    HOUR_2(120, "2 hours"),
    CUSTOM(-1, "Custom");

    companion object {
        fun fromMinutes(minutes: Int): TimerPreset {
            return when (minutes) {
                0 -> OFF
                30 -> MIN_30
                60 -> HOUR_1
                90 -> HOUR_1_30
                120 -> HOUR_2
                else -> if (minutes > 0) CUSTOM else OFF
            }
        }
    }
}
