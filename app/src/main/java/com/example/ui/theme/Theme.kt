package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppAccentColor

@Composable
fun ZPrivateVpnTheme(
    darkTheme: Boolean = true,
    accentColor: AppAccentColor = AppAccentColor.ROYAL_INDIGO,
    content: @Composable () -> Unit
) {
    val activeAccent = Color(if (darkTheme) accentColor.darkHex else accentColor.lightHex)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = activeAccent,
            onPrimary = Color.White,
            primaryContainer = ObsidianSurfaceElevated,
            onPrimaryContainer = TextPrimaryDark,
            secondary = ProtectedEmerald,
            onSecondary = Color(0xFF022C22),
            secondaryContainer = ProtectedEmeraldSoft,
            onSecondaryContainer = Color(0xFFA7F3D0),
            tertiary = activeAccent,
            onTertiary = Color.White,
            background = ObsidianBackground,
            onBackground = TextPrimaryDark,
            surface = ObsidianSurface,
            onSurface = TextPrimaryDark,
            surfaceVariant = ObsidianSurfaceElevated,
            onSurfaceVariant = TextSecondaryDark,
            outline = ObsidianBorder,
            outlineVariant = Color(0xFF1E3050),
            error = KillSwitchCrimson,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = activeAccent,
            onPrimary = Color.White,
            primaryContainer = DaylightSurfaceElevated,
            onPrimaryContainer = TextPrimaryLight,
            secondary = ProtectedEmeraldLight,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFD1FAE5),
            onSecondaryContainer = Color(0xFF065F46),
            tertiary = activeAccent,
            onTertiary = Color.White,
            background = DaylightBackground,
            onBackground = TextPrimaryLight,
            surface = DaylightSurface,
            onSurface = TextPrimaryLight,
            surfaceVariant = DaylightSurfaceElevated,
            onSurfaceVariant = TextSecondaryLight,
            outline = DaylightBorder,
            outlineVariant = Color(0xFFD8E2EE),
            error = KillSwitchCrimson,
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
