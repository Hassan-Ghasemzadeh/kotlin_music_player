package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Domain model representing supported application languages.
 */
enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    PERSIAN("fa", "Persian", "فارسی"),
    GERMAN("de", "German", "Deutsch"),
    TURKISH("tr", "Turkish", "Türkçe");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
