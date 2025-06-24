package com.harry.kharrency.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware gradient colors for backgrounds
 */
@Composable
fun getBackgroundGradient(themeManager: ThemeManager? = null): Brush {
    val isDark = themeManager?.isDarkMode ?: isSystemInDarkTheme()
    return if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                DarkGradientStart,
                DarkGradientMiddle,
                DarkGradientEnd
            ),
            startY = 0f,
            endY = 800f
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                LightGradientStart,
                LightGradientMiddle,
                LightGradientEnd
            ),
            startY = 0f,
            endY = 800f
        )
    }
}

/**
 * Theme-aware header text colors
 */
@Composable
fun getHeaderTextColor(themeManager: ThemeManager? = null): Color {
    val isDark = themeManager?.isDarkMode ?: isSystemInDarkTheme()
    return if (isDark) {
        DarkOnBackground
    } else {
        Color.White
    }
}

/**
 * Theme-aware semi-transparent card colors
 */
@Composable
fun getSemiTransparentCardColor(themeManager: ThemeManager? = null): Color {
    val isDark = themeManager?.isDarkMode ?: isSystemInDarkTheme()
    return if (isDark) {
        DarkSurface.copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.15f)
    }
}

/**
 * Theme-aware card colors
 */
@Composable
fun getCardColor(): Color {
    return MaterialTheme.colorScheme.surface
}

/**
 * Theme-aware text colors for cards
 */
@Composable
fun getCardTextColor(): Color {
    return MaterialTheme.colorScheme.onSurface
} 