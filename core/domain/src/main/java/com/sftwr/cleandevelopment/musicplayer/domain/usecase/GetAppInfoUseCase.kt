package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.AppInfo
import com.sftwr.cleandevelopment.musicplayer.domain.repository.AppInfoRepository
import javax.inject.Inject

/**
 * Use case responsible for fetching application metadata and version details.
 */
class GetAppInfoUseCase @Inject constructor(
    private val appInfoRepository: AppInfoRepository
) {
    /**
     * Executes the use case to retrieve app info.
     */
    suspend operator fun invoke(): AppInfo {
        return appInfoRepository.getAppInfo()
    }
}
