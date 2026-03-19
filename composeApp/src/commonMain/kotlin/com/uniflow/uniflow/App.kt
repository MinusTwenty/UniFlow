package com.uniflow.uniflow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.russhwolf.settings.Settings
import com.uniflow.uniflow.auth.DbAuthRepository
import com.uniflow.uniflow.data.DatabaseDriverFactory
import com.uniflow.uniflow.data.provideDatabase
import com.uniflow.uniflow.home.HomeTop
import com.uniflow.uniflow.home.LessonCard
import com.uniflow.uniflow.home.StudentInfo
import com.uniflow.uniflow.settings.ThemeSettings
import com.uniflow.uniflow.ui.settings.SettingsScreen
import com.uniflow.uniflow.ui.theme.UniFlowAppTheme
import com.uniflow.uniflow.ui.theme.UniFlowBackground

private enum class MainTab {
    HOME,
    SETTINGS
}

@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory
) {
    val themeSettings = remember { ThemeSettings(Settings()) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf(themeSettings.getSavedTheme()) }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    val db = remember { provideDatabase(databaseDriverFactory) }
    val authRepository = remember { DbAuthRepository(db) }

    UniFlowAppTheme(mode = selectedTheme) {
        UniFlowBackground {
            if (!isLoggedIn) {
                LoginScreenWithValidation(
                    repository = authRepository,
                    onLoginSuccess = { isLoggedIn = true }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == MainTab.HOME,
                                onClick = { selectedTab = MainTab.HOME },
                                label = { Text("Főoldal") },
                                icon = {}
                            )
                            NavigationBarItem(
                                selected = selectedTab == MainTab.SETTINGS,
                                onClick = { selectedTab = MainTab.SETTINGS },
                                label = { Text("Beállítások") },
                                icon = {}
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        MainTab.HOME -> {
                            HomeTop(
                                modifier = Modifier.padding(innerPadding),
                                student = StudentInfo(
                                    uniShort = "UJS",
                                    fullName = "Pástó Vilmos Márk",
                                    weekType = ""
                                ),
                                location = "Tornaterem",
                                building = "B épület",
                                dateText = "2025.09.30.",
                                nextRoom = "G312",
                                nextTeacher = "XY",
                                upcoming = demoLessons(),
                                nowTime = ""
                            )
                        }

                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                modifier = Modifier.padding(innerPadding),
                                selectedTheme = selectedTheme,
                                onThemeSelected = {
                                    selectedTheme = it
                                    themeSettings.saveTheme(it)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun demoLessons(): List<LessonCard> = listOf(
    LessonCard(code = "TOR", time = "12:15-13:00", room = "B-02", teacher = "Tanár: XY"),
    LessonCard(code = "PS1", time = "13:00-13:45", room = "B-03", teacher = "Tanár: XY"),
    LessonCard(code = "OS1", time = "14:15-15:00", room = "C-12", teacher = "Tanár: XY"),
    LessonCard(code = "OSM", time = "15:00-15:55", room = "C-13", teacher = "Tanár: XY")
)