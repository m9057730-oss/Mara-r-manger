package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

private val DineJoyShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DineJoyTheme(
    primaryColor: Color = SaffronOrange,
    accentColor: Color = FreshMint,
    backgroundColor: Color = WarmCreamBg,
    cardColor: Color = WarmCardLight,
    textColor: Color = EspressoText,
    darkTheme: Boolean = false,
    hasWallpaper: Boolean = false,
    content: @Composable () -> Unit
) {
    val onPrimary = if (primaryColor.luminance() > 0.55f) Color(0xFF1F1610) else Color.White
    val onSecondary = if (accentColor.luminance() > 0.55f) Color(0xFF10251C) else Color.White

    val colorScheme = if (darkTheme) {
        val darkCard = if (hasWallpaper) DarkBistroCard.copy(alpha = 0.88f) else DarkBistroCard
        darkColorScheme(
            primary = primaryColor,
            onPrimary = onPrimary,
            primaryContainer = primaryColor.copy(alpha = 0.22f),
            onPrimaryContainer = WarmIvoryText,
            secondary = accentColor,
            onSecondary = onSecondary,
            secondaryContainer = accentColor.copy(alpha = 0.22f),
            onSecondaryContainer = WarmIvoryText,
            tertiary = GoldenAmber,
            background = if (hasWallpaper) Color.Transparent else DarkBistroBg,
            onBackground = if (textColor == EspressoText) WarmIvoryText else textColor,
            surface = darkCard,
            onSurface = if (textColor == EspressoText) WarmIvoryText else textColor,
            surfaceVariant = DarkBistroSurfaceVariant,
            onSurfaceVariant = WarmIvoryText.copy(alpha = 0.78f),
            outline = WarmIvoryText.copy(alpha = 0.16f)
        )
    } else {
        val lightCard = if (hasWallpaper) cardColor.copy(alpha = 0.90f) else cardColor
        lightColorScheme(
            primary = primaryColor,
            onPrimary = onPrimary,
            primaryContainer = primaryColor.copy(alpha = 0.14f),
            onPrimaryContainer = textColor,
            secondary = accentColor,
            onSecondary = onSecondary,
            secondaryContainer = accentColor.copy(alpha = 0.14f),
            onSecondaryContainer = textColor,
            tertiary = GoldenAmber,
            background = if (hasWallpaper) Color.Transparent else backgroundColor,
            onBackground = textColor,
            surface = lightCard,
            onSurface = textColor,
            surfaceVariant = primaryColor.copy(alpha = 0.08f),
            onSurfaceVariant = textColor.copy(alpha = 0.72f),
            outline = textColor.copy(alpha = 0.14f)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DineJoyTypography,
        shapes = DineJoyShapes,
        content = content
    )
}
