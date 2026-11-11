package com.sftwr.cleandevelopment.musicplayer.domain.launcher

import android.content.Context
import com.sftwr.cleandevelopment.musicplayer.domain.model.StoreOption

/**
 * Strategy interface for app store launchers, allowing extensible support for
 * multiple stores (CafeBazaar, Myket, Google Play, etc.) adhering to the Open/Closed Principle.
 */
interface AppStoreLauncher {
    val storeId: String
    val storeName: String
    val packageName: String
    val marketUriTemplate: String
    val webUrlTemplate: String

    /**
     * Checks whether the store application is installed or supported on the device.
     */
    fun isAvailable(context: Context): Boolean

    /**
     * Launches the store app or falls back to the web browser to rate the app.
     */
    fun launch(context: Context, appPackageName: String)

    /**
     * Converts the launcher into its domain [StoreOption] representation.
     */
    fun toStoreOption(appPackageName: String): StoreOption {
        return StoreOption(
            id = storeId,
            name = storeName,
            packageName = packageName,
            marketUri = marketUriTemplate.replace("{package}", appPackageName),
            webUrl = webUrlTemplate.replace("{package}", appPackageName)
        )
    }
}
