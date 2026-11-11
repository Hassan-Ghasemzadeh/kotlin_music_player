package com.sftwr.cleandevelopment.musicplayer.data.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage

/**
 * Utility helper for dynamic runtime application locale switching across Android versions.
 */
object LocaleHelper {

    fun applyLanguage(context: Context, language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as? LocaleManager
            if (localeManager != null) {
                localeManager.applicationLocales = LocaleList.forLanguageTags(language.code)
                return
            }
        }
        val appLocales = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }
}
