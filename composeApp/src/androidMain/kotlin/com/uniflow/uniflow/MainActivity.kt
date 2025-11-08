package com.uniflow.uniflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    LoginScreenWithValidation {
                        // Here you handle successful login
                        println("Login successful!") // Later navigate to home screen
                    }
                }
            }
        }
    }
}