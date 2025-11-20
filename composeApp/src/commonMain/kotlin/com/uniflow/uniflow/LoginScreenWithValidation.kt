package com.uniflow.uniflow

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.uniflow.uniflow.auth.FakeAuthRepository
import com.uniflow.uniflow.auth.LoginRequest
import kotlinx.coroutines.launch

@Composable
fun LoginScreenWithValidation(
    onLoginSuccess: () -> Unit
) {
    val repository = remember { FakeAuthRepository() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("UniFlow", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Ais azonosító") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Jelszó") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
            Text("Emlékezz rám")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                message = ""
                scope.launch {
                    try {
                        repository.login(
                            LoginRequest(email = email, password = password, rememberMe = rememberMe)
                        )
                        message = "Sikeres belépés"

                        onLoginSuccess()
                    } catch (e: Exception) {
                        message = "Sikertelen belépés"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Bejelentkezés..." else "Belépés")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = { onLoginSuccess() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Átugrás (teszt)")
        }

        if (message.isNotEmpty()) {
            val msgColor =
                if (message == "Sikeres belépés") MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error

            Text(
                text = message,
                color = msgColor
            )
        }
    }
}
