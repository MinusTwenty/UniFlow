package com.uniflow.uniflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.uniflow.uniflow.data.DatabaseDriverFactory
import com.uniflow.uniflow.home.AndroidAppContextHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidAppContextHolder.context = applicationContext
        AndroidAppContextHolder.activity = this
        setContent {
            App(
                databaseDriverFactory = DatabaseDriverFactory(this)
            )
        }
    }
}
