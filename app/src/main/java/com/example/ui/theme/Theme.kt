package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppColorTheme
import com.example.data.model.AppThemeMode

fun getAppColorScheme(colorTheme: AppColorTheme, isDark: Boolean): ColorScheme {
    return when (colorTheme) {
        AppColorTheme.EMERALD -> if (isDark) {
            darkColorScheme(
                primary = EmeraldPrimaryDark,
                onPrimary = Color(0xFF003822),
                secondary = EmeraldSecondaryDark,
                onSecondary = Color(0xFF003822),
                tertiary = EmeraldTertiaryDark,
                background = EmeraldBackgroundDark,
                surface = EmeraldSurfaceDark,
                surfaceVariant = EmeraldSurfaceVariantDark,
                onBackground = Color(0xFFE1E9E4),
                onSurface = Color(0xFFE1E9E4)
            )
        } else {
            lightColorScheme(
                primary = EmeraldPrimaryLight,
                onPrimary = Color.White,
                secondary = EmeraldSecondaryLight,
                onSecondary = Color.White,
                tertiary = EmeraldTertiaryLight,
                background = EmeraldBackgroundLight,
                surface = EmeraldSurfaceLight,
                surfaceVariant = EmeraldSurfaceVariantLight,
                onBackground = Color(0xFF141F19),
                onSurface = Color(0xFF141F19)
            )
        }
        AppColorTheme.SAPPHIRE -> if (isDark) {
            darkColorScheme(
                primary = SapphirePrimaryDark,
                secondary = SapphireSecondaryDark,
                tertiary = SapphireTertiaryDark,
                background = SapphireBackgroundDark,
                surface = SapphireSurfaceDark,
                surfaceVariant = SapphireSurfaceVariantDark,
                onBackground = Color(0xFFE2E8F0),
                onSurface = Color(0xFFE2E8F0)
            )
        } else {
            lightColorScheme(
                primary = SapphirePrimaryLight,
                secondary = SapphireSecondaryLight,
                tertiary = SapphireTertiaryLight,
                background = SapphireBackgroundLight,
                surface = SapphireSurfaceLight,
                surfaceVariant = SapphireSurfaceVariantLight,
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF0F172A)
            )
        }
        AppColorTheme.AMBER -> if (isDark) {
            darkColorScheme(
                primary = AmberPrimaryDark,
                secondary = AmberSecondaryDark,
                tertiary = AmberTertiaryDark,
                background = AmberBackgroundDark,
                surface = AmberSurfaceDark,
                surfaceVariant = AmberSurfaceVariantDark,
                onBackground = Color(0xFFFDF4E7),
                onSurface = Color(0xFFFDF4E7)
            )
        } else {
            lightColorScheme(
                primary = AmberPrimaryLight,
                secondary = AmberSecondaryLight,
                tertiary = AmberTertiaryLight,
                background = AmberBackgroundLight,
                surface = AmberSurfaceLight,
                surfaceVariant = AmberSurfaceVariantLight,
                onBackground = Color(0xFF291E10),
                onSurface = Color(0xFF291E10)
            )
        }
        AppColorTheme.PURPLE -> if (isDark) {
            darkColorScheme(
                primary = PurplePrimaryDark,
                secondary = PurpleSecondaryDark,
                tertiary = PurpleTertiaryDark,
                background = PurpleBackgroundDark,
                surface = PurpleSurfaceDark,
                surfaceVariant = PurpleSurfaceVariantDark,
                onBackground = Color(0xFFF3E8FF),
                onSurface = Color(0xFFF3E8FF)
            )
        } else {
            lightColorScheme(
                primary = PurplePrimaryLight,
                secondary = PurpleSecondaryLight,
                tertiary = PurpleTertiaryLight,
                background = PurpleBackgroundLight,
                surface = PurpleSurfaceLight,
                surfaceVariant = PurpleSurfaceVariantLight,
                onBackground = Color(0xFF1E112A),
                onSurface = Color(0xFF1E112A)
            )
        }
    }
}

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    colorTheme: AppColorTheme = AppColorTheme.EMERALD,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = getAppColorScheme(colorTheme, isDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
