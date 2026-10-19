package com.sftwr.cleandevelopment.musicplayer.library.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri


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
        null
    } finally {
        try {
            retriever.release()
        } catch (_: Exception) {
        }
    }
}