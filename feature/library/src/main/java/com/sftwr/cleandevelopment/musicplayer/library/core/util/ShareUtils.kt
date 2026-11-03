package com.sftwr.cleandevelopment.musicplayer.library.core.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

object ShareUtils {
    fun shareSong(context: Context, song: SongUiModel) {
        try {
            val uri = song.uri.toUri()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooserIntent = Intent.createChooser(shareIntent, "اشتراک‌گذاری آهنگ").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "امکان اشتراک‌گذاری این فایل وجود ندارد", Toast.LENGTH_SHORT)
                .show()
        }
    }
}