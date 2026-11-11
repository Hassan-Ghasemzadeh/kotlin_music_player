package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import android.content.Context
import com.sftwr.cleandevelopment.musicplayer.domain.launcher.AppStoreLauncher
import com.sftwr.cleandevelopment.musicplayer.domain.model.StoreOption
import javax.inject.Inject

/**
 * Use case responsible for retrieving available app store rating options.
 */
class GetAvailableStoresUseCase @Inject constructor(
    private val launchers: Set<@JvmSuppressWildcards AppStoreLauncher>
) {
    operator fun invoke(context: Context, appPackageName: String): List<StoreOption> {
        val availableLaunchers = launchers.filter { it.isAvailable(context) }
        val targetLaunchers = if (availableLaunchers.isNotEmpty()) availableLaunchers else launchers
        return targetLaunchers.map { it.toStoreOption(appPackageName) }
    }
}
