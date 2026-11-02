package com.sftwr.cleandevelopment.musicplayer.data.util

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AudioTagEditorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun updateTags_updatesAudioFileMetadataSuccessfully() {
        val testFile = tempFolder.newFile("test_song.mp3")
        // Write valid MPEG-1 Layer 3 frames (128kbps, 44.1kHz = 417 bytes per frame)
        val frameHeader = byteArrayOf(0xFF.toByte(), 0xFB.toByte(), 0x90.toByte(), 0x64.toByte())
        val framePayload = ByteArray(413)
        val oneFrame = frameHeader + framePayload
        val mp3Data = oneFrame + oneFrame + oneFrame + oneFrame
        testFile.writeBytes(mp3Data)

        val tagEditor = AudioTagEditor()
        tagEditor.updateTags(
            file = testFile,
            title = "Awesome Song",
            artist = "Super Artist",
            album = "Best Album",
            year = "2024",
            genre = "Rock"
        )

        val audioFile = AudioFileIO.read(testFile)
        val tag = audioFile.tag

        assertEquals("Awesome Song", tag.getFirst(FieldKey.TITLE))
        assertEquals("Super Artist", tag.getFirst(FieldKey.ARTIST))
        assertEquals("Best Album", tag.getFirst(FieldKey.ALBUM))
        assertEquals("2024", tag.getFirst(FieldKey.YEAR))
        assertEquals("Rock", tag.getFirst(FieldKey.GENRE))
    }
}
