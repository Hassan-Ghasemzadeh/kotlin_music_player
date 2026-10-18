package com.sftwr.cleandevelopment.musicplayer.domain.usecase


import javax.inject.Inject
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow

class GetDeviceSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return musicRepository.getDeviceSongs()
    }
}