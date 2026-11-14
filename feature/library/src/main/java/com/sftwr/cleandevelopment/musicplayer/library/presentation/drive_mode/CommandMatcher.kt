package com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode

import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import java.util.Locale

enum class PlayerCommand {
    PLAY, PAUSE, NEXT, PREVIOUS, UNKNOWN
}

/**
 * Utility responsible for multilingual keyword matching, normalization,
 * and command parsing for Drive Mode voice recognition.
 */
object CommandMatcher {

    private val playKeywords = mapOf(
        AppLanguage.ENGLISH to listOf("play", "start", "resume"),
        AppLanguage.PERSIAN to listOf("پخش", "شروع", "ادامه", "برو", "اجرا"),
        AppLanguage.GERMAN to listOf("spielen", "starten", "weiter"),
        AppLanguage.TURKISH to listOf("oynat", "başlat", "devam")
    )

    private val pauseKeywords = mapOf(
        AppLanguage.ENGLISH to listOf("pause", "stop"),
        AppLanguage.PERSIAN to listOf("توقف", "ایست", "پاز"),
        AppLanguage.GERMAN to listOf("pausieren", "stoppen", "halt"),
        AppLanguage.TURKISH to listOf("duraklat", "dur", "beklet")
    )

    private val nextKeywords = mapOf(
        AppLanguage.ENGLISH to listOf("next", "skip"),
        AppLanguage.PERSIAN to listOf("بعدی", "جلو"),
        AppLanguage.GERMAN to listOf("nächste", "weiter"),
        AppLanguage.TURKISH to listOf("sonraki", "geç")
    )

    private val previousKeywords = mapOf(
        AppLanguage.ENGLISH to listOf("previous", "back"),
        AppLanguage.PERSIAN to listOf("قبلی", "عقب", "قبلو"),
        AppLanguage.GERMAN to listOf("vorherige", "zurück"),
        AppLanguage.TURKISH to listOf("önceki", "geri")
    )

    fun match(rawText: String, language: AppLanguage): PlayerCommand {
        val normalized = rawText.lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), "") // Remove punctuation/diacritics
            .trim()

        val defaultEnglish = emptyList<String>()
        val pKeywords = playKeywords[language] ?: playKeywords[AppLanguage.ENGLISH] ?: defaultEnglish
        val paKeywords = pauseKeywords[language] ?: pauseKeywords[AppLanguage.ENGLISH] ?: defaultEnglish
        val nKeywords = nextKeywords[language] ?: nextKeywords[AppLanguage.ENGLISH] ?: defaultEnglish
        val prevKeywords = previousKeywords[language] ?: previousKeywords[AppLanguage.ENGLISH] ?: defaultEnglish

        return when {
            pKeywords.any { normalized.contains(it) } -> PlayerCommand.PLAY
            paKeywords.any { normalized.contains(it) } -> PlayerCommand.PAUSE
            nKeywords.any { normalized.contains(it) } -> PlayerCommand.NEXT
            prevKeywords.any { normalized.contains(it) } -> PlayerCommand.PREVIOUS
            else -> PlayerCommand.UNKNOWN
        }
    }

    fun getBcp47LocaleTag(language: AppLanguage): String {
        return when (language) {
            AppLanguage.PERSIAN -> "fa-IR"
            AppLanguage.GERMAN -> "de-DE"
            AppLanguage.TURKISH -> "tr-TR"
            AppLanguage.ENGLISH -> "en-US"
        }
    }
}
