package com.sftwr.cleandevelopment.musicplayer.domain.model

sealed class TimerDuration {
    abstract val minutes: Int
    val millis: Long get() = minutes * 60 * 1000L

    object Off : TimerDuration() {
        override val minutes: Int = 0
    }

    object ThirtyMinutes : TimerDuration() {
        override val minutes: Int = 30
    }

    object OneHour : TimerDuration() {
        override val minutes: Int = 60
    }

    object OneHourThirtyMinutes : TimerDuration() {
        override val minutes: Int = 90
    }

    object TwoHours : TimerDuration() {
        override val minutes: Int = 120
    }

    data class Custom(override val minutes: Int) : TimerDuration()

    companion object {
        fun fromMinutes(minutes: Int): TimerDuration {
            return when (minutes) {
                0 -> Off
                30 -> ThirtyMinutes
                60 -> OneHour
                90 -> OneHourThirtyMinutes
                120 -> TwoHours
                else -> if (minutes > 0) Custom(minutes) else Off
            }
        }
    }
}
