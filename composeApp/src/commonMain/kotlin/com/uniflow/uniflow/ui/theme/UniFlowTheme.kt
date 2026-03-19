package com.uniflow.uniflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class UniFlowThemeMode {
    UNIFLOW_DARK,
    EMERALD_STUDY,
    SUNSET_FOCUS,
    MINIMAL_LIGHT
}

@Immutable
data class UniFlowExtraColors(
    val backgroundGradientTop: Color,
    val backgroundGradientBottom: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val chipBackground: Color,
    val chipText: Color,
    val navBarBackground: Color,
    val divider: Color
)

@Immutable
data class UniFlowSpacing(
    val xxs: Float = 4f,
    val xs: Float = 8f,
    val sm: Float = 12f,
    val md: Float = 16f,
    val lg: Float = 20f,
    val xl: Float = 24f,
    val xxl: Float = 32f
)

@Immutable
data class UniFlowRadii(
    val sm: Float = 12f,
    val md: Float = 16f,
    val lg: Float = 20f,
    val xl: Float = 28f,
    val pill: Float = 999f
)

private val LocalUniFlowExtraColors = staticCompositionLocalOf<UniFlowExtraColors> {
    error("UniFlowExtraColors not provided")
}

private val LocalUniFlowSpacing = staticCompositionLocalOf { UniFlowSpacing() }
private val LocalUniFlowRadii = staticCompositionLocalOf { UniFlowRadii() }

object UniFlowTheme {
    val colors: UniFlowExtraColors
        @Composable get() = LocalUniFlowExtraColors.current

    val spacing: UniFlowSpacing
        @Composable get() = LocalUniFlowSpacing.current

    val radii: UniFlowRadii
        @Composable get() = LocalUniFlowRadii.current
}

private data class ThemeBundle(
    val material: androidx.compose.material3.ColorScheme,
    val extra: UniFlowExtraColors
)

private fun themeBundle(mode: UniFlowThemeMode): ThemeBundle {
    return when (mode) {
        UniFlowThemeMode.UNIFLOW_DARK -> ThemeBundle(
            material = darkColorScheme(
                primary = Color(0xFF4F7DF3),
                onPrimary = Color(0xFFFFFFFF),
                background = Color(0xFF0F172A),
                onBackground = Color(0xFFF1F5F9),
                surface = Color(0xFF162033),
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = Color(0xFF1E293B),
                onSurfaceVariant = Color(0xFFB8C1CC),
                outline = Color(0xFF334155)
            ),
            extra = UniFlowExtraColors(
                backgroundGradientTop = Color(0xFF11192B),
                backgroundGradientBottom = Color(0xFF090F1C),
                glassSurface = Color(0xF3243650),
                glassBorder = Color(0x3AFFFFFF),
                cardBorder = Color(0x24FFFFFF),
                textPrimary = Color(0xFFF3F6FB),
                textSecondary = Color(0xFFB8C2D3),
                accent = Color(0xFF3E73F0),
                success = Color(0xFF34D399),
                warning = Color(0xFFFBBF24),
                danger = Color(0xFFF87171),
                chipBackground = Color(0x9C34445F),
                chipText = Color(0xFFF3F6FB),
                navBarBackground = Color(0xCC101828),
                divider = Color(0x26FFFFFF)
            )
        )

        UniFlowThemeMode.EMERALD_STUDY -> ThemeBundle(
            material = darkColorScheme(
                primary = Color(0xFF34D399),
                onPrimary = Color(0xFF06261F),
                background = Color(0xFF0B1E1A),
                onBackground = Color(0xFFECFDF5),
                surface = Color(0xFF102823),
                onSurface = Color(0xFFECFDF5),
                surfaceVariant = Color(0xFF143D36),
                onSurfaceVariant = Color(0xFFA7F3D0),
                outline = Color(0xFF2A5C54)
            ),
            extra = UniFlowExtraColors(
                backgroundGradientTop = Color(0xFF0B1E1A),
                backgroundGradientBottom = Color(0xFF0F2B25),
                glassSurface = Color(0xEE143D36),
                glassBorder = Color(0x3AA7F3D0),
                cardBorder = Color(0x16A7F3D0),
                textPrimary = Color(0xFFECFDF5),
                textSecondary = Color(0xFFA7F3D0),
                accent = Color(0xFF34D399),
                success = Color(0xFF6EE7B7),
                warning = Color(0xFFFBBF24),
                danger = Color(0xFFF87171),
                chipBackground = Color(0x99304E47),
                chipText = Color(0xFFECFDF5),
                navBarBackground = Color(0xE60B1E1A),
                divider = Color(0x22A7F3D0)
            )
        )

        UniFlowThemeMode.SUNSET_FOCUS -> ThemeBundle(
            material = darkColorScheme(
                primary = Color(0xFFFF7A59),
                onPrimary = Color(0xFFFFFFFF),
                background = Color(0xFF1A0F1F),
                onBackground = Color(0xFFFDF2F8),
                surface = Color(0xFF24152B),
                onSurface = Color(0xFFFDF2F8),
                surfaceVariant = Color(0xFF2B1C35),
                onSurfaceVariant = Color(0xFFFBCFE8),
                outline = Color(0xFF4A2F52)
            ),
            extra = UniFlowExtraColors(
                backgroundGradientTop = Color(0xFF1A0F1F),
                backgroundGradientBottom = Color(0xFF25142D),
                glassSurface = Color(0xEE2B1C35),
                glassBorder = Color(0x44FBCFE8),
                cardBorder = Color(0x18FBCFE8),
                textPrimary = Color(0xFFFDF2F8),
                textSecondary = Color(0xFFFBCFE8),
                accent = Color(0xFFFF7A59),
                success = Color(0xFF4ADE80),
                warning = Color(0xFFF59E0B),
                danger = Color(0xFFFB7185),
                chipBackground = Color(0x993B2944),
                chipText = Color(0xFFFDF2F8),
                navBarBackground = Color(0xE61A0F1F),
                divider = Color(0x22FBCFE8)
            )
        )

        UniFlowThemeMode.MINIMAL_LIGHT -> ThemeBundle(
            material = lightColorScheme(
                primary = Color(0xFF4F7DF3),
                onPrimary = Color(0xFFFFFFFF),
                background = Color(0xFFF5F7FB),
                onBackground = Color(0xFF1E293B),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF1E293B),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFFD7DEE7)
            ),
            extra = UniFlowExtraColors(
                backgroundGradientTop = Color(0xFFF5F7FB),
                backgroundGradientBottom = Color(0xFFEFF4FA),
                glassSurface = Color(0xFCFFFFFF),
                glassBorder = Color(0x48C9D5E2),
                cardBorder = Color(0xFFDCE3EC),
                textPrimary = Color(0xFF1E293B),
                textSecondary = Color(0xFF64748B),
                accent = Color(0xFF4F7DF3),
                success = Color(0xFF10B981),
                warning = Color(0xFFF59E0B),
                danger = Color(0xFFEF4444),
                chipBackground = Color(0xFFF3F6FA),
                chipText = Color(0xFF334155),
                navBarBackground = Color(0xF7FFFFFF),
                divider = Color(0xFFD8E0E8)
            )
        )
    }
}

@Composable
fun UniFlowAppTheme(
    mode: UniFlowThemeMode = if (isSystemInDarkTheme()) {
        UniFlowThemeMode.UNIFLOW_DARK
    } else {
        UniFlowThemeMode.MINIMAL_LIGHT
    },
    content: @Composable () -> Unit
) {
    val bundle = themeBundle(mode)

    androidx.compose.runtime.CompositionLocalProvider(
        LocalUniFlowExtraColors provides bundle.extra,
        LocalUniFlowSpacing provides UniFlowSpacing(),
        LocalUniFlowRadii provides UniFlowRadii()
    ) {
        MaterialTheme(
            colorScheme = bundle.material,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}