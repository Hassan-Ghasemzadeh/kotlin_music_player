package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case responsible for observing songs belonging to a specific folder path.
 */
class GetFolderSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(folderPath: String): Flow<List<Song>> {
        return musicRepository.getFolderSongs(folderPath)
    }
}
