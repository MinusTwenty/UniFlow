package com.uniflow.uniflow.settings

import com.russhwolf.settings.Settings
import com.uniflow.uniflow.ui.theme.UniFlowThemeMode

private const val THEME_KEY = "selected_theme"

class ThemeSettings(
    private val settings: Settings
) {
    fun getSavedTheme(): UniFlowThemeMode {
        val saved = settings.getStringOrNull(THEME_KEY) ?: return UniFlowThemeMode.UNIFLOW_DARK

        return runCatching {
            UniFlowThemeMode.valueOf(saved)
        }.getOrElse {
            UniFlowThemeMode.UNIFLOW_DARK
        }
    }

    fun saveTheme(theme: UniFlowThemeMode) {
        settings.putString(THEME_KEY, theme.name)
    }
}