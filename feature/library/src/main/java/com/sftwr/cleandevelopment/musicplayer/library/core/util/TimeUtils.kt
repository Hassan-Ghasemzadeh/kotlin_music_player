package com.sftwr.cleandevelopment.musicplayer.library.core.util


import java.util.Locale

// تبدیل به Extension Function روی نوع داده Long
fun Long.formatTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}
/**
 * Extension function to convert a formatted time string (e.g. "03:20") into milliseconds as a Float.
 */
fun String.toMillis(): Float {
    return try {
        val parts = this.split(":")
        if (parts.size == 2) {
            val minutes = parts[0].toFloatOrNull() ?: 0f
            val seconds = parts[1].toFloatOrNull() ?: 0f
            ((minutes * 60f) + seconds) * 1000f
        } else {
            0f
        }
    } catch (e: Exception) {
        0f
    }
}