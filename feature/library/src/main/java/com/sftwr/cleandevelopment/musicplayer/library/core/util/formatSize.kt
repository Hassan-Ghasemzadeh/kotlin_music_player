package com.sftwr.cleandevelopment.musicplayer.library.core.util

import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

/**
 * Converts file size in bytes to a human-readable string (KB, MB, GB).
 */
fun Long.formatSize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
    return String.format(
        Locale.US,
        "%.1f %s",
        this / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}