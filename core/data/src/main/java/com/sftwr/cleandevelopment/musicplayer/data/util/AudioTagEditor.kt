package com.sftwr.cleandevelopment.musicplayer.data.util

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import javax.inject.Inject

class AudioTagEditor @Inject constructor() {

    fun updateTags(
        file: File,
        title: String,
        artist: String,
        album: String,
        year: String,
        genre: String
    ) {
        val audioFile = AudioFileIO.read(file)
        val tag = audioFile.tagOrCreateAndSetDefault

        if (title.isNotBlank()) {
            tag.setField(FieldKey.TITLE, title)
        }
        if (artist.isNotBlank()) {
            tag.setField(FieldKey.ARTIST, artist)
        }
        if (album.isNotBlank()) {
            tag.setField(FieldKey.ALBUM, album)
        }

        if (year.isNotBlank() && year != "نامشخص" && year != "Unknown") {
            try {
                tag.setField(FieldKey.YEAR, year)
            } catch (_: Exception) {
            }
        }
        if (genre.isNotBlank() && genre != "نامشخص" && genre != "Unknown") {
            try {
                tag.setField(FieldKey.GENRE, genre)
            } catch (_: Exception) {
            }
        }

        audioFile.commit()
    }
}