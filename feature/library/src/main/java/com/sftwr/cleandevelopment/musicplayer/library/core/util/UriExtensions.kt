package com.sftwr.cleandevelopment.musicplayer.library.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi


/**
 * Extension function on Uri to extract embedded album art as a ByteArray using Context.
 */
fun Uri.extractAlbumArt(context: Context): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, this)
        val artBytes = retriever.embeddedPicture

        if (artBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
            bitmap
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        try {
            retriever.release()
        } catch (_: Exception) {
        }
    }
}

fun String.extractAlbumArt(): ByteArray? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(this)
        retriever.embeddedPicture
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        try {
            retriever.release()
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
    }
}


data class SongExtraMetadata(
    val genre: String?,
    val year: String?,
    val bitrate: String?,
    val sampleRate: String?,
    val mimeType: String?
)

@RequiresApi(Build.VERSION_CODES.S)
fun String.extractExtraMetadata(): SongExtraMetadata {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(this)
        SongExtraMetadata(
            genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
            year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR),
            bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE),
            sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE),
            mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        )
    } catch (e: Exception) {
        e.printStackTrace()
        SongExtraMetadata(null, null, null, null, null)
    } finally {
        try {
            retriever.release()
        } catch (_: Exception) {
        }
    }
}