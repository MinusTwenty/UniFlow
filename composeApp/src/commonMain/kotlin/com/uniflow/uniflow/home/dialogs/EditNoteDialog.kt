package com.uniflow.uniflow.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme

@Composable
fun EditNoteDialog(
    note: LessonNoteUi,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var noteText by remember(note.id) { mutableStateOf(note.content) }

    Dialog(onDismissRequest = onDismiss) {
        UniFlowGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            focusManager.clearFocus()
                        }
                    }
                    .padding(18.dp)
            ) {
                Text(
                    text = "Jegyzet szerkesztése",
                    style = MaterialTheme.typography.headlineSmall,
                    color = UniFlowTheme.colors.textPrimary
                )

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    label = { Text("Jegyzet") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = UniFlowTheme.colors.textPrimary,
                        unfocusedTextColor = UniFlowTheme.colors.textPrimary,
                        focusedLabelColor = UniFlowTheme.colors.textSecondary,
                        unfocusedLabelColor = UniFlowTheme.colors.textSecondary,
                        focusedBorderColor = UniFlowTheme.colors.glassBorder,
                        unfocusedBorderColor = UniFlowTheme.colors.glassBorder,
                        focusedContainerColor = UniFlowTheme.colors.glassSurface,
                        unfocusedContainerColor = UniFlowTheme.colors.glassSurface
                    )
                )

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = { onSave(noteText) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mentés")
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mégse")
                }
            }
        }
    }
}
