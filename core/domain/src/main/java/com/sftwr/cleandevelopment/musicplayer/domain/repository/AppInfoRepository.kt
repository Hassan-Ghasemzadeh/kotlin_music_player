package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.AppInfo

/**
 * Domain repository contract for retrieving application information and metadata.
 */
interface AppInfoRepository {
    /**
     * Retrieves application metadata info.
     */
    suspend fun getAppInfo(): AppInfo
}
