package com.sftwr.cleandevelopment.musicplayer.library.core.util


import java.util.Locale

// تبدیل به Extension Function روی نوع داده Long
fun Long.formatTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}