package com.sftwr.cleandevelopment.musicplayer.data.launcher

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.sftwr.cleandevelopment.musicplayer.domain.launcher.AppStoreLauncher
import javax.inject.Inject

class BazaarStoreHandler @Inject constructor() : AppStoreLauncher {
    override val storeId: String = "bazaar"
    override val storeName: String = "Cafe Bazaar"
    override val packageName: String = "com.farsitel.bazaar"
    override val marketUriTemplate: String = "bazaar://details?id={package}"
    override val webUrlTemplate: String = "https://cafebazaar.ir/app/{package}"

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
        val intent = Intent(Intent.ACTION_EDIT, marketUri).apply {
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
