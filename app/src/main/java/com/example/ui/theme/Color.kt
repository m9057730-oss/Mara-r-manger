package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Joyful "Happy Happy" Hospitality Palette (Saffron Marigold, Fresh Mint, Warm Cream)
val SaffronOrange = Color(0xFFF97316)
val SunsetCoral = Color(0xFFFF5E3A)
val GoldenAmber = Color(0xFFFFB020)
val FreshMint = Color(0xFF10B981)
val DeepEmerald = Color(0xFF059669)
val BerryRose = Color(0xFFE11D48)
val SkyTeal = Color(0xFF0EA5E9)

val WarmCreamBg = Color(0xFFFFF9F2)
val WarmCardLight = Color(0xFFFFFFFF)
val SoftPeachContainer = Color(0xFFFFEDD5)
val EspressoText = Color(0xFF231914)
val MutedCocoaText = Color(0xFF6E5A4F)

val DarkBistroBg = Color(0xFF17120F)
val DarkBistroCard = Color(0xFF241C18)
val DarkBistroSurfaceVariant = Color(0xFF332721)
val WarmIvoryText = Color(0xFFFAF3EB)

// Channel Brand Accents (for quick visual scanning)
val ZomatoRed = Color(0xFFE23744)
val SwiggyOrange = Color(0xFFFC8019)
val OtherAppIndigo = Color(0xFF6366F1)
val OwnDeliveryTeal = Color(0xFF0D9488)
val DineInGold = Color(0xFFD97706)
val PickupViolet = Color(0xFF8B5CF6)
val ProfitGreen = Color(0xFF16A34A)
val LossRed = Color(0xFFDC2626)

data class ThemePreset(
    val id: Int,
    val name: String,
    val primaryHex: Long,
    val accentHex: Long,
    val bgHex: Long,
    val cardHex: Long,
    val textHex: Long
)

val HappyThemePresets = listOf(
    ThemePreset(
        id = 0,
        name = "Saffron Joy",
        primaryHex = 0xFFF97316,
        accentHex = 0xFF10B981,
        bgHex = 0xFFFFF9F2,
        cardHex = 0xFFFFFFFF,
        textHex = 0xFF231914
    ),
    ThemePreset(
        id = 1,
        name = "Mint Bistro",
        primaryHex = 0xFF059669,
        accentHex = 0xFFF59E0B,
        bgHex = 0xFFF2FBF7,
        cardHex = 0xFFFFFFFF,
        textHex = 0xFF112A22
    ),
    ThemePreset(
        id = 2,
        name = "Sunny Mango",
        primaryHex = 0xFFEAB308,
        accentHex = 0xFFEA580C,
        bgHex = 0xFFFFFCF0,
        cardHex = 0xFFFFFFFF,
        textHex = 0xFF29200A
    ),
    ThemePreset(
        id = 3,
        name = "Royal Rose",
        primaryHex = 0xFFE11D48,
        accentHex = 0xFF8B5CF6,
        bgHex = 0xFFFFF5F7,
        cardHex = 0xFFFFFFFF,
        textHex = 0xFF2D1219
    ),
    ThemePreset(
        id = 4,
        name = "Ocean Breeze",
        primaryHex = 0xFF0284C7,
        accentHex = 0xFF10B981,
        bgHex = 0xFFF0F9FF,
        cardHex = 0xFFFFFFFF,
        textHex = 0xFF0F2537
    )
)

val SwatchColorOptions = listOf(
    0xFFF97316L, // Saffron
    0xFF059669L, // Emerald
    0xFFE11D48L, // Rose
    0xFF0284C7L, // Sky Blue
    0xFF8B5CF6L, // Violet
    0xFFD97706L, // Amber
    0xFF231914L, // Espresso
    0xFF1E293BL, // Slate
    0xFFFFFFFFL, // White
    0xFFFFF9F2L  // Warm Cream
)
