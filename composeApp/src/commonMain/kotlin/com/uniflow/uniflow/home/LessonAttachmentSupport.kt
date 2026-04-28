package com.uniflow.uniflow.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

data class ImportedLessonFile(
    val displayName: String,
    val localPath: String,
    val byteSize: Long
)

data class CloudImportFile(
    val id: String,
    val displayName: String,
    val mimeType: String,
    val detail: String? = null
)

data class CloudImportUiState(
    val isLoading: Boolean = false,
    val title: String = "Google Drive",
    val files: List<CloudImportFile> = emptyList(),
    val errorMessage: String? = null
)

data class AttachmentImportController(
    val importDocument: (LessonCard) -> Unit,
    val importCloudDocument: (LessonCard) -> Unit,
    val importCode: (LessonCard) -> Unit,
    val cloudImportState: State<CloudImportUiState?>,
    val selectCloudFile: (CloudImportFile) -> Unit,
    val dismissCloudImport: () -> Unit
)

expect object LessonAttachmentManager {
    fun listForLesson(lesson: LessonCard): List<ImportedLessonFile>
    fun saveForLesson(
        lesson: LessonCard,
        suggestedName: String,
        bytes: ByteArray
    ): ImportedLessonFile?
    fun openImportedFile(file: ImportedLessonFile): Boolean
}

@Composable
expect fun rememberAttachmentImportController(
    onImported: (LessonCard, ImportedLessonFile) -> Unit,
    onError: (String) -> Unit
): AttachmentImportController

internal fun lessonAttachmentFolderName(lesson: LessonCard): String {
    val titleSlug = lesson.title
        .lowercase()
        .map { ch ->
            when {
                ch.isLetterOrDigit() -> ch
                ch == ' ' || ch == '-' || ch == '_' -> '_'
                else -> '_'
            }
        }
        .joinToString("")
        .replace(Regex("_+"), "_")
        .trim('_')
        .take(32)

    return buildString {
        append("lesson_")
        append(lesson.lessonId)
        if (titleSlug.isNotEmpty()) {
            append('_')
            append(titleSlug)
        }
    }
}

internal fun sanitizeImportedFileName(name: String): String {
    val normalized = name.trim().ifEmpty { "imported_file" }
    return normalized.map { ch ->
        when {
            ch.isLetterOrDigit() -> ch
            ch == '.' || ch == '-' || ch == '_' -> ch
            else -> '_'
        }
    }.joinToString("")
}
