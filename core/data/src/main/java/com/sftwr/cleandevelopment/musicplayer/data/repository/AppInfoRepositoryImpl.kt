package com.sftwr.cleandevelopment.musicplayer.data.repository

import android.content.Context
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppInfo
import com.sftwr.cleandevelopment.musicplayer.domain.repository.AppInfoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInfoRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AppInfoRepository {

    override suspend fun getAppInfo(): AppInfo = withContext(Dispatchers.IO) {
        var version = "v3.0.4"
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val pkgVersion = pInfo.versionName
            if (!pkgVersion.isNullOrBlank()) {
                version = if (pkgVersion.startsWith("v")) pkgVersion else "v$pkgVersion"
            }
        } catch (_: Exception) {
        }

        AppInfo(
            appName = "Music Player",
            versionName = version,
            developerName = "Code Ara Studio",
            githubUrl = "https://github.com/Hassan-Ghasemzadeh"
        )
    }
}
