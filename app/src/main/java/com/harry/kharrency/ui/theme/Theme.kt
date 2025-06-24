package com.harry.kharrency.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = White,
    primaryContainer = Purple60,
    onPrimaryContainer = Purple20,
    
    secondary = PurpleGrey40,
    onSecondary = White,
    secondaryContainer = PurpleGrey80,
    onSecondaryContainer = Purple20,
    
    tertiary = Pink40,
    onTertiary = White,
    tertiaryContainer = Pink80,
    onTertiaryContainer = White,
    
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Grey300,
    
    error = Error,
    onError = White,
    errorContainer = ErrorDark,
    onErrorContainer = ErrorLight,
    
    outline = Grey600,
    outlineVariant = Grey700,
    scrim = Black,
    
    inverseSurface = LightSurface,
    inverseOnSurface = LightOnSurface,
    inversePrimary = Purple60
)

private val LightColorScheme = lightColorScheme(
    primary = Purple80,
    onPrimary = White,
    primaryContainer = Purple20,
    onPrimaryContainer = Purple60,
    
    secondary = PurpleGrey40,
    onSecondary = White,
    secondaryContainer = Purple20,
    onSecondaryContainer = PurpleGrey80,
    
    tertiary = Pink80,
    onTertiary = White,
    tertiaryContainer = Pink40,
    onTertiaryContainer = White,
    
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Grey600,
    
    error = Error,
    onError = White,
    errorContainer = ErrorLight,
    onErrorContainer = ErrorDark,
    
    outline = Grey300,
    outlineVariant = Grey200,
    scrim = Black,
    
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkOnSurface,
    inversePrimary = Purple80
)

@Composable
fun KharrencyTheme(
    themeManager: ThemeManager? = null,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = if (themeManager != null) {
        val savedDarkMode by themeManager.isDarkModeFlow.collectAsState(initial = systemDarkTheme)
        
        // Update theme manager state when system theme changes
        LaunchedEffect(savedDarkMode) {
            themeManager.updateState(savedDarkMode)
        }
        
        savedDarkMode
    } else {
        systemDarkTheme
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}