package com.sftwr.cleandevelopment.musicplayer.domain.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Domain enum representing 15 distinct, hand-crafted color themes/palettes.
 * Each palette defines core color tokens for primary background, surface/card background,
 * accent/neon highlight, and vertical gradient tones.
 */
enum class ColorPalette(
    val id: String,
    val displayName: String,
    val nativeName: String,
    val primaryBackgroundHex: Long,
    val secondaryBackgroundHex: Long,
    val surfaceCardHex: Long,
    val accentHex: Long,
) {
    NEON_DARK(
        id = "NEON_DARK",
        displayName = "Neon Dark (Default)",
        nativeName = "نئون تاریک (پیش‌فرض)",
        primaryBackgroundHex = 0xFF0F0B21,
        secondaryBackgroundHex = 0xFF181035,
        surfaceCardHex = 0xFF2C1454,
        accentHex = 0xFF91D8FB
    ),
    EMERALD_CYBER(
        id = "EMERALD_CYBER",
        displayName = "Emerald Cyber",
        nativeName = "زمردی سایبر",
        primaryBackgroundHex = 0xFF051C14,
        secondaryBackgroundHex = 0xFF0A2E22,
        surfaceCardHex = 0xFF0F4735,
        accentHex = 0xFF00FF9D
    ),
    SUNSET_GLOW(
        id = "SUNSET_GLOW",
        displayName = "Sunset Glow",
        nativeName = "غروب آفتاب",
        primaryBackgroundHex = 0xFF1C0B0E,
        secondaryBackgroundHex = 0xFF2C1116,
        surfaceCardHex = 0xFF451921,
        accentHex = 0xFFFF6B4A
    ),
    ELECTRIC_PURPLE(
        id = "ELECTRIC_PURPLE",
        displayName = "Electric Purple",
        nativeName = "بنفش الکتریک",
        primaryBackgroundHex = 0xFF120924,
        secondaryBackgroundHex = 0xFF1C0D38,
        surfaceCardHex = 0xFF2E135B,
        accentHex = 0xFFB866FF
    ),
    CYBER_CYAN(
        id = "CYBER_CYAN",
        displayName = "Cyber Cyan",
        nativeName = "فیروزه‌ای سایبر",
        primaryBackgroundHex = 0xFF081820,
        secondaryBackgroundHex = 0xFF0D2532,
        surfaceCardHex = 0xFF14374A,
        accentHex = 0xFF00E5FF
    ),
    RUBY_CRIMSON(
        id = "RUBY_CRIMSON",
        displayName = "Ruby Crimson",
        nativeName = "یاقوتی سرخ",
        primaryBackgroundHex = 0xFF1A080C,
        secondaryBackgroundHex = 0xFF2B0C13,
        surfaceCardHex = 0xFF42121E,
        accentHex = 0xFFFF2A55
    ),
    GOLDEN_LUXURY(
        id = "GOLDEN_LUXURY",
        displayName = "Golden Luxury",
        nativeName = "طلایی لوکس",
        primaryBackgroundHex = 0xFF1A1608,
        secondaryBackgroundHex = 0xFF2B230C,
        surfaceCardHex = 0xFF423712,
        accentHex = 0xFFFFD700
    ),
    ROSE_VELVET(
        id = "ROSE_VELVET",
        displayName = "Rose Velvet",
        nativeName = "رز مخملی",
        primaryBackgroundHex = 0xFF1F0A15,
        secondaryBackgroundHex = 0xFF311022,
        surfaceCardHex = 0xFF491733,
        accentHex = 0xFFFF69B4
    ),
    LIME_PULSE(
        id = "LIME_PULSE",
        displayName = "Lime Pulse",
        nativeName = "لیموئی درخشان",
        primaryBackgroundHex = 0xFF0E1A08,
        secondaryBackgroundHex = 0xFF172B0D,
        surfaceCardHex = 0xFF234213,
        accentHex = 0xFFA6FF00
    ),
    CORAL_BREEZE(
        id = "CORAL_BREEZE",
        displayName = "Coral Breeze",
        nativeName = "مرجانی ساحلی",
        primaryBackgroundHex = 0xFF1C0D15,
        secondaryBackgroundHex = 0xFF2D1422,
        surfaceCardHex = 0xFF451E34,
        accentHex = 0xFFFF7F50
    ),
    DEEP_INDIGO(
        id = "DEEP_INDIGO",
        displayName = "Deep Indigo",
        nativeName = "نیلی عمیق",
        primaryBackgroundHex = 0xFF0A0D21,
        secondaryBackgroundHex = 0xFF111636,
        surfaceCardHex = 0xFF1A2254,
        accentHex = 0xFF6C5CE7
    ),
    MINT_FROST(
        id = "MINT_FROST",
        displayName = "Mint Frost",
        nativeName = "نعنایی یخی",
        primaryBackgroundHex = 0xFF071C19,
        secondaryBackgroundHex = 0xFF0E2E2A,
        surfaceCardHex = 0xFF15453F,
        accentHex = 0xFF55E6C1
    ),
    AMBER_EMBER(
        id = "AMBER_EMBER",
        displayName = "Amber Ember",
        nativeName = "کهربایی گرم",
        primaryBackgroundHex = 0xFF1C1306,
        secondaryBackgroundHex = 0xFF2E1F0A,
        surfaceCardHex = 0xFF47300F,
        accentHex = 0xFFFFB142
    ),
    NEON_VIOLET(
        id = "NEON_VIOLET",
        displayName = "Neon Violet",
        nativeName = "بنفش نئون",
        primaryBackgroundHex = 0xFF180724,
        secondaryBackgroundHex = 0xFF270B3B,
        surfaceCardHex = 0xFF3D125C,
        accentHex = 0xFFE056FD
    ),
    MIDNIGHT_OCEAN(
        id = "MIDNIGHT_OCEAN",
        displayName = "Midnight Ocean",
        nativeName = "اقیانوس نیمه‌شب",
        primaryBackgroundHex = 0xFF06121E,
        secondaryBackgroundHex = 0xFF0B1F33,
        surfaceCardHex = 0xFF11304F,
        accentHex = 0xFF48DBFB
    );

    val primaryBackground: Color
        get() = Color(primaryBackgroundHex)

    val secondaryBackground: Color
        get() = Color(secondaryBackgroundHex)

    val surfaceCard: Color
        get() = Color(surfaceCardHex)

    val accent: Color
        get() = Color(accentHex)

    val gradientBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                primaryBackground,
                secondaryBackground,
                surfaceCard
            )
        )

    companion object {
        fun fromId(id: String?): ColorPalette {
            if (id.isNullOrEmpty()) return NEON_DARK
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NEON_DARK
        }
    }
}
