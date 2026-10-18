package com.sftwr.cleandevelopment.musicplayer.domain.repository


import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getDeviceSongs(): Flow<List<Song>>
}