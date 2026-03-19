package com.uniflow.uniflow

import androidx.compose.ui.window.ComposeUIViewController
import com.uniflow.uniflow.data.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController { App(
    databaseDriverFactory = DatabaseDriverFactory()
) }