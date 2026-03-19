package com.uniflow.uniflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.uniflow.uniflow.auth.FakeAuthApi
import com.uniflow.uniflow.auth.LoginRequest
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreenWithValidation(
    repository: FakeAuthApi,
    onLoginSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val colors = UniFlowTheme.colors

    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "UniFlow",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(28.dp))

        UniFlowGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("AIS azonosító") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedLabelColor = colors.textSecondary,
                        unfocusedLabelColor = colors.textSecondary,
                        cursorColor = colors.accent,
                        focusedBorderColor = colors.glassBorder,
                        unfocusedBorderColor = colors.glassBorder,
                        focusedContainerColor = colors.glassSurface,
                        unfocusedContainerColor = colors.glassSurface
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Jelszó") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedLabelColor = colors.textSecondary,
                        unfocusedLabelColor = colors.textSecondary,
                        cursorColor = colors.accent,
                        focusedBorderColor = colors.glassBorder,
                        unfocusedBorderColor = colors.glassBorder,
                        focusedContainerColor = colors.glassSurface,
                        unfocusedContainerColor = colors.glassSurface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colors.accent,
                            uncheckedColor = colors.textSecondary,
                            checkmarkColor = colors.textPrimary
                        )
                    )

                    Text(
                        text = "Emlékezz rám",
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        isLoading = true
                        message = ""

                        scope.launch {
                            try {
                                repository.login(
                                    LoginRequest(
                                        identifier = identifier,
                                        password = password,
                                        rememberMe = rememberMe
                                    )
                                )
                                message = "Sikeres belépés"
                                onLoginSuccess()
                            } catch (_: Exception) {
                                message = "Sikertelen belépés"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = if (isLoading) "Bejelentkezés..." else "Belépés",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { onLoginSuccess() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Átugrás (beta)",
                        color = colors.textSecondary
                    )
                }

                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    val msgColor =
                        if (message == "Sikeres belépés") colors.success
                        else colors.danger

                    Text(
                        text = message,
                        color = msgColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}