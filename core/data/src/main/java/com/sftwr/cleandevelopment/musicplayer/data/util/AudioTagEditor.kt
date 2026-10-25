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
        val existingArtworks = audioFile.tag?.artworkList?.toList() ?: emptyList()

        val tag = audioFile.createDefaultTag()
        audioFile.tag = tag

        tag.setField(FieldKey.TITLE, title)
        tag.setField(FieldKey.ARTIST, artist)
        tag.setField(FieldKey.ALBUM, album)

        if (year.isNotBlank() && year != "نامشخص") {
            tag.setField(FieldKey.YEAR, year)
        }
        if (genre.isNotBlank() && genre != "نامشخص") {
            tag.setField(FieldKey.GENRE, genre)
        }

        for (artwork in existingArtworks) {
            tag.addField(artwork)
        }

        audioFile.commit()
    }
}