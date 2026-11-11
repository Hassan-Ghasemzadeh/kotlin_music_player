package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Data class representing the operational state of the Sleep Timer.
 *
 * @property isActive True if a sleep timer is currently running in background or foreground.
 * @property targetEndTimeMs Epoch timestamp in milliseconds when the timer is scheduled to expire.
 * @property durationMinutes Total initial duration configured for the active timer in minutes.
 * @property remainingTimeMs Calculated remaining time in milliseconds until expiration (0 if inactive or expired).
 */
data class SleepTimerState(
    val isActive: Boolean = false,
    val targetEndTimeMs: Long = 0L,
    val durationMinutes: Int = 0,
    val remainingTimeMs: Long = 0L
) {
    val remainingMinutes: Int
        get() = (remainingTimeMs / (1000 * 60)).toInt()

    val remainingSeconds: Int
        get() = ((remainingTimeMs / 1000) % 60).toInt()
}
