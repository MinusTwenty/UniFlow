package com.uniflow.uniflow.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.uniflow.uniflow.ui.theme.GlassChip
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme

private enum class ImportSource {
    DEVICE,
    GOOGLE_DRIVE,
    ICLOUD,
    ONEDRIVE,
    DROPBOX,
    CODE
}

@Composable
fun FileAttachmentDialog(
    lesson: LessonCard,
    onDismiss: () -> Unit
) {
    var infoDialog by remember { mutableStateOf<InfoDialogState?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val importController = rememberAttachmentImportController(
        onImported = { importedLesson, importedFile ->
            if (importedLesson.lessonId == lesson.lessonId) {
                message = "Importálva: ${importedFile.displayName}"
            }
        },
        onError = { error ->
            message = error
        }
    )

    Dialog(onDismissRequest = onDismiss) {
        UniFlowGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fájl csatolás",
                            style = MaterialTheme.typography.headlineSmall,
                            color = UniFlowTheme.colors.textPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${lesson.code} • ${lesson.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UniFlowTheme.colors.textSecondary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Bezárás",
                            tint = UniFlowTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                SectionLabel("Válassz forrást")

                Spacer(Modifier.height(10.dp))

                AttachmentSourceCard(
                    source = ImportSource.DEVICE,
                    title = "Eszközről tallózás",
                    onClick = { importController.importDocument(lesson) },
                    onInfoClick = {
                        infoDialog = InfoDialogState(
                            title = "Eszközről tallózás",
                            description = "Importálható például: PDF, DOCX, PPTX, XLSX, TXT és ZIP."
                        )
                    }
                )

                Spacer(Modifier.height(10.dp))

                AttachmentSourceCard(
                    source = ImportSource.GOOGLE_DRIVE,
                    title = "Google Drive",
                    onClick = { importController.importCloudDocument(lesson) }
                )

                Spacer(Modifier.height(10.dp))

                AttachmentSourceCard(
                    source = ImportSource.ICLOUD,
                    title = "iCloud Drive",
                    onClick = {}
                )

                Spacer(Modifier.height(10.dp))

                AttachmentSourceCard(
                    source = ImportSource.ONEDRIVE,
                    title = "OneDrive",
                    onClick = {}
                )

                Spacer(Modifier.height(10.dp))

                AttachmentSourceCard(
                    source = ImportSource.DROPBOX,
                    title = "Dropbox",
                    onClick = {}
                )

                Spacer(Modifier.height(10.dp))

                AttachmentSourceCard(
                    source = ImportSource.CODE,
                    title = "Kód importálása",
                    onClick = { importController.importCode(lesson) },
                    onInfoClick = {
                        infoDialog = InfoDialogState(
                            title = "Kód importálása",
                            description = "Többféle forráskódot és ZIP projektet is fogadhatunk, például Kotlin, Java, Python, JavaScript, TypeScript, Swift, SQL, JSON és más fájlokat."
                        )
                    }
                )

                message?.let { status ->
                    Spacer(Modifier.height(18.dp))
                    GlassChip(text = status)
                }

                Spacer(Modifier.height(18.dp))

                SectionLabel("Egyéb")

                Spacer(Modifier.height(10.dp))

                SuggestionCard(
                    icon = Icons.Filled.Link,
                    title = "Link import"
                )

                Spacer(Modifier.height(10.dp))

                SuggestionCard(
                    icon = Icons.Filled.PhotoCamera,
                    title = "Fotó / szkennelés"
                )
            }
        }
    }

    infoDialog?.let { state ->
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            confirmButton = {
                TextButton(onClick = { infoDialog = null }) {
                    Text("Rendben")
                }
            },
            title = { Text(state.title) },
            text = { Text(state.description) }
        )
    }

    importController.cloudImportState.value?.let { cloudState ->
        AlertDialog(
            onDismissRequest = importController.dismissCloudImport,
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = importController.dismissCloudImport) {
                    Text("Bezárás")
                }
            },
            title = {
                Text(
                    text = cloudState.title,
                    color = UniFlowTheme.colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when {
                        cloudState.isLoading -> {
                            Text("Drive fájlok betöltése...", color = UniFlowTheme.colors.textSecondary)
                        }

                        cloudState.errorMessage != null -> {
                            Text(cloudState.errorMessage, color = MaterialTheme.colorScheme.error)
                        }

                        cloudState.files.isEmpty() -> {
                            Text("Nem találtunk importálható Drive fájlt.", color = UniFlowTheme.colors.textSecondary)
                        }

                        else -> {
                            cloudState.files.forEach { file ->
                                CloudImportRow(
                                    file = file,
                                    onClick = { importController.selectCloudFile(file) }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

private data class InfoDialogState(
    val title: String,
    val description: String
)

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = UniFlowTheme.colors.textPrimary
    )
}

@Composable
private fun AttachmentSourceCard(
    source: ImportSource,
    title: String,
    onClick: () -> Unit,
    onInfoClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(20.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = UniFlowTheme.colors.glassSurface.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, UniFlowTheme.colors.glassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SourceIcon(source = source)

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = UniFlowTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            onInfoClick?.let { infoClick ->
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Információ",
                    tint = UniFlowTheme.colors.textSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable(onClick = infoClick)
                )
            }
        }
    }
}

@Composable
private fun SourceIcon(source: ImportSource) {
    when (source) {
        ImportSource.DEVICE -> DeviceIcon()
        ImportSource.GOOGLE_DRIVE -> GoogleDriveIcon()
        ImportSource.ICLOUD -> ICloudIcon()
        ImportSource.ONEDRIVE -> OneDriveIcon()
        ImportSource.DROPBOX -> DropboxIcon()
        ImportSource.CODE -> CircleIconBadge(
            background = Color(0x24F97316),
            border = Color(0x80F97316)
        ) {
            Icon(
                imageVector = Icons.Filled.Code,
                contentDescription = null,
                tint = Color(0xFFF97316)
            )
        }
    }
}

@Composable
private fun CircleIconBadge(
    background: Color,
    border: Color,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun DeviceIcon() {
    CircleIconBadge(
        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        border = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    ) {
        Icon(
            imageVector = Icons.Filled.AttachFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ICloudIcon() {
    CircleIconBadge(
        background = Color(0x1A60A5FA),
        border = Color(0x8060A5FA)
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            drawCircle(
                color = Color(0xFF90CAF9),
                radius = size.minDimension * 0.17f,
                center = Offset(size.width * 0.35f, size.height * 0.55f)
            )
            drawCircle(
                color = Color(0xFF90CAF9),
                radius = size.minDimension * 0.21f,
                center = Offset(size.width * 0.52f, size.height * 0.45f)
            )
            drawCircle(
                color = Color(0xFF90CAF9),
                radius = size.minDimension * 0.17f,
                center = Offset(size.width * 0.68f, size.height * 0.56f)
            )
            drawRoundRect(
                color = Color(0xFF90CAF9),
                topLeft = Offset(size.width * 0.24f, size.height * 0.54f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.52f, size.height * 0.18f)
            )
        }
    }
}

@Composable
private fun OneDriveIcon() {
    CircleIconBadge(
        background = Color(0x142563EB),
        border = Color(0x802563EB)
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            drawCircle(
                color = Color(0xFF2563EB),
                radius = size.minDimension * 0.19f,
                center = Offset(size.width * 0.4f, size.height * 0.58f)
            )
            drawCircle(
                color = Color(0xFF3B82F6),
                radius = size.minDimension * 0.23f,
                center = Offset(size.width * 0.55f, size.height * 0.47f)
            )
            drawCircle(
                color = Color(0xFF1D4ED8),
                radius = size.minDimension * 0.16f,
                center = Offset(size.width * 0.7f, size.height * 0.58f)
            )
            drawRoundRect(
                color = Color(0xFF2563EB),
                topLeft = Offset(size.width * 0.25f, size.height * 0.56f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.18f)
            )
        }
    }
}

@Composable
private fun GoogleDriveIcon() {
    CircleIconBadge(
        background = Color(0x1434A853),
        border = Color(0x8034A853)
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val left = Offset(size.width * 0.28f, size.height * 0.76f)
            val top = Offset(size.width * 0.5f, size.height * 0.24f)
            val right = Offset(size.width * 0.72f, size.height * 0.76f)

            val leftPath = Path().apply {
                moveTo(left.x, left.y)
                lineTo(top.x, top.y)
                lineTo(size.width * 0.4f, top.y)
                lineTo(size.width * 0.18f, left.y)
                close()
            }
            val rightPath = Path().apply {
                moveTo(top.x, top.y)
                lineTo(right.x, right.y)
                lineTo(size.width * 0.82f, left.y)
                lineTo(size.width * 0.6f, top.y)
                close()
            }
            val bottomPath = Path().apply {
                moveTo(size.width * 0.18f, left.y)
                lineTo(size.width * 0.82f, left.y)
                lineTo(size.width * 0.72f, size.height * 0.9f)
                lineTo(size.width * 0.28f, size.height * 0.9f)
                close()
            }

            drawPath(leftPath, color = Color(0xFF0F9D58), style = Fill)
            drawPath(rightPath, color = Color(0xFF4285F4), style = Fill)
            drawPath(bottomPath, color = Color(0xFFF4B400), style = Fill)
        }
    }
}

@Composable
private fun DropboxIcon() {
    CircleIconBadge(
        background = Color(0x141D4ED8),
        border = Color(0x801D4ED8)
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            fun diamond(center: Offset, width: Float, height: Float): Path {
                return Path().apply {
                    moveTo(center.x, center.y - height / 2f)
                    lineTo(center.x + width / 2f, center.y)
                    lineTo(center.x, center.y + height / 2f)
                    lineTo(center.x - width / 2f, center.y)
                    close()
                }
            }

            val color = Color(0xFF2563EB)
            drawPath(diamond(Offset(size.width * 0.38f, size.height * 0.38f), size.width * 0.26f, size.height * 0.2f), color, style = Fill)
            drawPath(diamond(Offset(size.width * 0.62f, size.height * 0.38f), size.width * 0.26f, size.height * 0.2f), color, style = Fill)
            drawPath(diamond(Offset(size.width * 0.38f, size.height * 0.62f), size.width * 0.26f, size.height * 0.2f), color, style = Fill)
            drawPath(diamond(Offset(size.width * 0.62f, size.height * 0.62f), size.width * 0.26f, size.height * 0.2f), color, style = Fill)
            drawPath(
                Path().apply {
                    moveTo(size.width * 0.38f, size.height * 0.82f)
                    lineTo(size.width * 0.5f, size.height * 0.72f)
                    lineTo(size.width * 0.62f, size.height * 0.82f)
                    lineTo(size.width * 0.5f, size.height * 0.9f)
                    close()
                },
                color = color,
                style = Fill
            )
        }
    }
}

@Composable
private fun SuggestionCard(
    icon: ImageVector,
    title: String
) {
    val shape = RoundedCornerShape(18.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, UniFlowTheme.colors.divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(UniFlowTheme.colors.chipBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = UniFlowTheme.colors.textPrimary
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = UniFlowTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CloudImportRow(
    file: CloudImportFile,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = UniFlowTheme.colors.chipBackground.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, UniFlowTheme.colors.divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = file.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = UniFlowTheme.colors.textPrimary
            )
            file.detail?.takeIf { it.isNotBlank() }?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = UniFlowTheme.colors.textSecondary
                )
            }
        }
    }
}
