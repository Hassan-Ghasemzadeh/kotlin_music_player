package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import android.content.Context
import com.sftwr.cleandevelopment.musicplayer.domain.launcher.AppStoreLauncher
import javax.inject.Inject

/**
 * Use case responsible for launching a specific store to rate the app.
 */
class LaunchStoreUseCase @Inject constructor(
    private val launchers: Set<@JvmSuppressWildcards AppStoreLauncher>
) {
    operator fun invoke(context: Context, storeId: String, appPackageName: String) {
        val launcher = launchers.find { it.storeId == storeId }
        launcher?.launch(context, appPackageName)
    }
}
