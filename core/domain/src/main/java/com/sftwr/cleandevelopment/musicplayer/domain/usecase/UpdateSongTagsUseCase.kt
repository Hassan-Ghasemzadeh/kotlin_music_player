package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.UpdateTagResult
import javax.inject.Inject

/**
 * Use case responsible for orchestrating the song metadata tag update operation.
 */
class UpdateSongTagsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(
        songId: Long,
        title: String,
        artist: String,
        album: String,
        year: String,
        genre: String
    ): UpdateTagResult {
        return musicRepository.updateSongTags(
            songId = songId,
            title = title,
            artist = artist,
            album = album,
            year = year,
            genre = genre
        )
    }
}