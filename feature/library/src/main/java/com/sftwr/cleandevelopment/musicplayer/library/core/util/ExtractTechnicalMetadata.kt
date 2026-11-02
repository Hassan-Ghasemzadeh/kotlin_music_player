package com.sftwr.cleandevelopment.musicplayer.library.core.util


import android.media.MediaMetadataRetriever
import android.os.Build

fun extractTechnicalMetadata(path: String): TechnicalMetadata {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(path)

        val bitrateBps =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()
        val bitrateFormatted = bitrateBps?.let { "${it / 1000} kbps" } ?: ""

        val sampleRateHz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()
        } else null
        val sampleRateFormatted = sampleRateHz?.let { "${it / 1000.toFloat()} kHz" } ?: ""

        val mimeType =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "نامشخص"

        TechnicalMetadata(
            bitrate = bitrateFormatted,
            sampleRate = sampleRateFormatted,
            mimeType = mimeType
        )
    } catch (e: Exception) {
        TechnicalMetadata("", "", "نامشخص")
    } finally {
        retriever.release()
    }
}

data class TechnicalMetadata(
    val bitrate: String,
    val sampleRate: String,
    val mimeType: String
)