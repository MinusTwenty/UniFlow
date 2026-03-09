package com.uniflow.uniflow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme
import com.uniflow.uniflow.ui.theme.UniFlowThemeMode

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    selectedTheme: UniFlowThemeMode,
    onThemeSelected: (UniFlowThemeMode) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Beállítások",
            style = MaterialTheme.typography.headlineMedium,
            color = UniFlowTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        UniFlowGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Téma kiválasztása",
                    style = MaterialTheme.typography.titleMedium,
                    color = UniFlowTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Jelenlegi téma: ${themeLabel(selectedTheme)}",
                    color = UniFlowTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                ThemeButton(
                    text = "UniFlow Dark",
                    onClick = { onThemeSelected(UniFlowThemeMode.UNIFLOW_DARK) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ThemeButton(
                    text = "Emerald Study",
                    onClick = { onThemeSelected(UniFlowThemeMode.EMERALD_STUDY) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ThemeButton(
                    text = "Sunset Focus",
                    onClick = { onThemeSelected(UniFlowThemeMode.SUNSET_FOCUS) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ThemeButton(
                    text = "Minimal Light",
                    onClick = { onThemeSelected(UniFlowThemeMode.MINIMAL_LIGHT) }
                )
            }
        }
    }
}

@Composable
private fun ThemeButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

private fun themeLabel(mode: UniFlowThemeMode): String {
    return when (mode) {
        UniFlowThemeMode.UNIFLOW_DARK -> "UniFlow Dark"
        UniFlowThemeMode.EMERALD_STUDY -> "Emerald Study"
        UniFlowThemeMode.SUNSET_FOCUS -> "Sunset Focus"
        UniFlowThemeMode.MINIMAL_LIGHT -> "Minimal Light"
    }
}