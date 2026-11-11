package com.sftwr.cleandevelopment.musicplayer.data.launcher

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.sftwr.cleandevelopment.musicplayer.domain.launcher.AppStoreLauncher
import javax.inject.Inject

class GooglePlayStoreHandler @Inject constructor() : AppStoreLauncher {
    override val storeId: String = "google_play"
    override val storeName: String = "Google Play Store"
    override val packageName: String = "com.android.vending"
    override val marketUriTemplate: String = "market://details?id={package}"
    override val webUrlTemplate: String = "https://play.google.com/store/apps/details?id={package}"

    override fun isAvailable(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun launch(context: Context, appPackageName: String) {
        val marketUri = marketUriTemplate.replace("{package}", appPackageName).toUri()
        val intent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val webUri = webUrlTemplate.replace("{package}", appPackageName).toUri()
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(webIntent)
            } catch (_: Exception) {}
        }
    }
}
