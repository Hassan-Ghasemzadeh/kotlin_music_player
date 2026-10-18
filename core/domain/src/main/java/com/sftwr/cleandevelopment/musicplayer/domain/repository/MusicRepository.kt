package com.sftwr.cleandevelopment.musicplayer.domain.repository


import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    // استفاده از Flow باعث می‌شود هر زمان فایلی اضافه/کم شد، لیست آپدیت شود
    fun getDeviceSongs(): Flow<List<Song>>
}