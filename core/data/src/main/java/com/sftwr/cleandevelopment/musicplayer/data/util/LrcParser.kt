package com.sftwr.cleandevelopment.musicplayer.data.util

import com.sftwr.cleandevelopment.musicplayer.model.LyricLine
import java.util.regex.Pattern

object LrcParser {

    // Regex pattern matching [mm:ss.xx] or [mm:ss:xx] or [mm:ss]
    private val TIME_TAG_PATTERN: Pattern = Pattern.compile("\\[(\\d{2,}):(\\d{2})(?:[.:](\\d{2,3}))?]")

    data class ParsedLrc(
        val isSynced: Boolean,
        val lines: List<LyricLine>
    )

    fun parse(rawContent: String): ParsedLrc {
        if (rawContent.isBlank()) {
            return ParsedLrc(isSynced = false, lines = emptyList())
        }

        val resultLines = mutableListOf<LyricLine>()
        var foundAnyTimestamp = false

        val lines = rawContent.lines()
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            // Skip metadata header tags like [ti:Title], [ar:Artist], [al:Album] if they don't contain digits
            val matcher = TIME_TAG_PATTERN.matcher(trimmedLine)
            val timestamps = mutableListOf<Long>()

            while (matcher.find()) {
                val minutesStr = matcher.group(1) ?: "0"
                val secondsStr = matcher.group(2) ?: "0"
                val millisStr = matcher.group(3)

                val minutes = minutesStr.toLongOrNull() ?: 0L
                val seconds = secondsStr.toLongOrNull() ?: 0L

                var millis = 0L
                if (millisStr != null) {
                    millis = when (millisStr.length) {
                        1 -> millisStr.toLongOrNull()?.times(100) ?: 0L
                        2 -> millisStr.toLongOrNull()?.times(10) ?: 0L
                        3 -> millisStr.toLongOrNull() ?: 0L
                        else -> millisStr.take(3).toLongOrNull() ?: 0L
                    }
                }

                val totalMs = (minutes * 60 + seconds) * 1000 + millis
                timestamps.add(totalMs)
                foundAnyTimestamp = true
            }

            if (timestamps.isNotEmpty()) {
                // Strip out all [xx:xx.xx] tags from the line text
                val textOnly = matcher.replaceAll("").trim()
                for (timeMs in timestamps) {
                    resultLines.add(LyricLine(timeMs = timeMs, text = textOnly))
                }
            } else {
                // Ignore metadata headers like [ar:...], [ti:...], [by:...], [offset:...]
                if (trimmedLine.startsWith("[") && trimmedLine.contains(":") && !trimmedLine.first().isDigit()) {
                    val tagKey = trimmedLine.substringAfter("[").substringBefore(":").lowercase()
                    if (tagKey in listOf("ti", "ar", "al", "by", "offset", "length", "re", "ve")) {
                        continue
                    }
                }
                // Plain line without timestamp
                resultLines.add(LyricLine(timeMs = 0L, text = trimmedLine))
            }
        }

        return if (foundAnyTimestamp && resultLines.isNotEmpty()) {
            val sortedLines = resultLines.sortedBy { it.timeMs }
            ParsedLrc(isSynced = true, lines = sortedLines)
        } else {
            // Unsynced plain text
            ParsedLrc(isSynced = false, lines = resultLines)
        }
    }
}
