package com.uniflow.uniflow.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.DecimalFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

internal object AndroidAppContextHolder {
    lateinit var context: Context
    var activity: ComponentActivity? = null
}

actual object LessonAttachmentManager {
    actual fun listForLesson(lesson: LessonCard): List<ImportedLessonFile> {
        val directory = lessonDirectory(lesson) ?: return emptyList()
        if (!directory.exists()) return emptyList()

        return directory.listFiles()
            ?.sortedByDescending { it.lastModified() }
            ?.map {
                ImportedLessonFile(
                    displayName = it.name,
                    localPath = it.absolutePath,
                    byteSize = it.length()
                )
            }
            .orEmpty()
    }

    actual fun saveForLesson(
        lesson: LessonCard,
        suggestedName: String,
        bytes: ByteArray
    ): ImportedLessonFile? {
        val directory = lessonDirectory(lesson) ?: return null
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val target = uniqueFile(directory, sanitizeImportedFileName(suggestedName))
        target.writeBytes(bytes)

        return ImportedLessonFile(
            displayName = target.name,
            localPath = target.absolutePath,
            byteSize = target.length()
        )
    }

    actual fun openImportedFile(file: ImportedLessonFile): Boolean {
        val context = AndroidAppContextHolder.context
        val targetFile = File(file.localPath)
        if (!targetFile.exists()) return false

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, guessMimeType(targetFile.name))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}

@Composable
actual fun rememberAttachmentImportController(
    onImported: (LessonCard, ImportedLessonFile) -> Unit,
    onError: (String) -> Unit
): AttachmentImportController {
    var pendingLesson by remember { mutableStateOf<LessonCard?>(null) }
    var cloudImportState by remember { mutableStateOf<CloudImportUiState?>(null) }
    var pendingCloudLesson by remember { mutableStateOf<LessonCard?>(null) }
    var pendingCloudToken by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val authorizationClient = remember {
        Identity.getAuthorizationClient(AndroidAppContextHolder.context)
    }

    val documentLauncher = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        val lesson = pendingLesson ?: return@rememberLauncherForActivityResult
        pendingLesson = null
        importUriIntoLesson(
            lesson = lesson,
            uri = uri,
            onImported = onImported,
            onError = onError
        )
    }

    val codeLauncher = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        val lesson = pendingLesson ?: return@rememberLauncherForActivityResult
        pendingLesson = null
        importUriIntoLesson(
            lesson = lesson,
            uri = uri,
            onImported = onImported,
            onError = onError
        )
    }

    val authorizationLauncher = rememberLauncherForActivityResult(StartIntentSenderForResult()) { result ->
        val lesson = pendingCloudLesson
        if (lesson == null) {
            cloudImportState = null
            return@rememberLauncherForActivityResult
        }

        val data = result.data
        if (data == null) {
            cloudImportState = null
            onError("A Google Drive engedélykérés megszakadt.")
            return@rememberLauncherForActivityResult
        }

        runCatching { authorizationClient.getAuthorizationResultFromIntent(data) }
            .onSuccess { authResult ->
                val accessToken = authResult.accessToken
                if (accessToken.isNullOrBlank()) {
                    cloudImportState = null
                    onError("Nem kaptunk hozzáférési tokent a Google Drive-hoz.")
                    return@onSuccess
                }

                pendingCloudToken = accessToken
                scope.launch {
                    loadDriveFiles(
                        accessToken = accessToken,
                        updateState = { cloudImportState = it },
                        onError = onError
                    )
                }
            }
            .onFailure {
                cloudImportState = null
                onError("Nem sikerült befejezni a Google Drive kapcsolódást.")
            }
    }

    return remember(onImported, onError) {
        AttachmentImportController(
            importDocument = { lesson ->
                pendingLesson = lesson
                documentLauncher.launch(
                    arrayOf(
                        "application/pdf",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "text/plain",
                        "application/zip"
                    )
                )
            },
            importCloudDocument = { lesson ->
                if (AndroidAppContextHolder.activity == null) {
                    onError("Nem sikerült elindítani a Google Drive kapcsolódást.")
                } else {
                    pendingCloudLesson = lesson
                    cloudImportState = CloudImportUiState(isLoading = true)

                    val request = AuthorizationRequest.builder()
                        .setRequestedScopes(
                            listOf(
                                Scope(DRIVE_READONLY_SCOPE)
                            )
                        )
                        .build()

                    authorizationClient.authorize(request)
                        .addOnSuccessListener { authorizationResult ->
                            handleAuthorizationResult(
                                result = authorizationResult,
                                launcher = authorizationLauncher,
                                onTokenReady = { accessToken ->
                                    pendingCloudToken = accessToken
                                    scope.launch {
                                        loadDriveFiles(
                                            accessToken = accessToken,
                                            updateState = { cloudImportState = it },
                                            onError = onError
                                        )
                                    }
                                },
                                onError = { message ->
                                    pendingCloudLesson = null
                                    cloudImportState = null
                                    onError(message)
                                }
                            )
                        }
                        .addOnFailureListener {
                            pendingCloudLesson = null
                            cloudImportState = null
                            onError("Nem sikerült kapcsolódni a Google Drive-hoz.")
                        }
                }
            },
            importCode = { lesson ->
                pendingLesson = lesson
                codeLauncher.launch(
                    arrayOf(
                        "text/*",
                        "application/json",
                        "application/xml",
                        "application/zip",
                        "*/*"
                    )
                )
            },
            cloudImportState = androidx.compose.runtime.derivedStateOf { cloudImportState },
            selectCloudFile = { file ->
                val lesson = pendingCloudLesson
                val accessToken = pendingCloudToken
                if (lesson == null || accessToken.isNullOrBlank()) {
                    cloudImportState = null
                    onError("A Google Drive munkamenet lejárt, próbáld újra.")
                } else {
                    cloudImportState = CloudImportUiState(
                        isLoading = true,
                        title = "Google Drive"
                    )
                    scope.launch {
                        importDriveFile(
                            lesson = lesson,
                            file = file,
                            accessToken = accessToken,
                            onImported = { importedFile ->
                                pendingCloudLesson = null
                                pendingCloudToken = null
                                cloudImportState = null
                                onImported(lesson, importedFile)
                            },
                            updateState = { cloudImportState = it },
                            onError = { message ->
                                cloudImportState = CloudImportUiState(
                                    title = "Google Drive",
                                    errorMessage = message
                                )
                            }
                        )
                    }
                }
            },
            dismissCloudImport = {
                pendingCloudLesson = null
                pendingCloudToken = null
                cloudImportState = null
            }
        )
    }
}

private fun importUriIntoLesson(
    lesson: LessonCard,
    uri: Uri?,
    onImported: (LessonCard, ImportedLessonFile) -> Unit,
    onError: (String) -> Unit
) {
    if (uri == null) return

    val context = AndroidAppContextHolder.context
    val resolver = context.contentResolver
    val fileName = queryDisplayName(context, uri) ?: "imported_file"
    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }

    if (bytes == null) {
        onError("Nem sikerült beolvasni a kiválasztott fájlt.")
        return
    }

    val saved = LessonAttachmentManager.saveForLesson(
        lesson = lesson,
        suggestedName = fileName,
        bytes = bytes
    )

    if (saved == null) {
        onError("Nem sikerült elmenteni a fájlt.")
        return
    }

    onImported(lesson, saved)
}

private fun lessonDirectory(lesson: LessonCard): File? {
    val baseDir = AndroidAppContextHolder.context.filesDir ?: return null
    return File(File(baseDir, "lesson_attachments"), lessonAttachmentFolderName(lesson))
}

private fun uniqueFile(directory: File, originalName: String): File {
    val dot = originalName.lastIndexOf('.')
    val base = if (dot > 0) originalName.substring(0, dot) else originalName
    val extension = if (dot > 0) originalName.substring(dot) else ""

    var index = 0
    var candidate = File(directory, originalName)
    while (candidate.exists()) {
        index += 1
        candidate = File(directory, "${base}_$index$extension")
    }
    return candidate
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
    }
    return uri.lastPathSegment?.substringAfterLast('/')
}

private fun guessMimeType(fileName: String): String {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "pdf" -> "application/pdf"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "txt", "md", "kt", "java", "py", "js", "ts", "html", "css", "json", "xml", "sql", "swift", "go", "rs", "c", "cpp", "cs", "php" -> "text/plain"
        "zip" -> "application/zip"
        else -> "*/*"
    }
}

private fun handleAuthorizationResult(
    result: AuthorizationResult,
    launcher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>,
    onTokenReady: (String) -> Unit,
    onError: (String) -> Unit
) {
    when {
        !result.accessToken.isNullOrBlank() -> onTokenReady(result.accessToken!!)
        result.hasResolution() -> {
            val pendingIntent = result.pendingIntent
            if (pendingIntent == null) {
                onError("Nem sikerült elindítani a Google Drive engedélykérést.")
                return
            }
            launcher.launch(
                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            )
        }
        else -> onError("Nem kaptunk hozzáférést a Google Drive-hoz.")
    }
}

private suspend fun loadDriveFiles(
    accessToken: String,
    updateState: (CloudImportUiState) -> Unit,
    onError: (String) -> Unit
) {
    val result = withContext(Dispatchers.IO) {
        runCatching { fetchDriveFiles(accessToken) }
    }

    result
        .onSuccess { files ->
            updateState(
                CloudImportUiState(
                    title = "Google Drive",
                    files = files
                )
            )
        }
        .onFailure {
            updateState(
                CloudImportUiState(
                    title = "Google Drive",
                    errorMessage = "Nem sikerült betölteni a Drive fájlokat."
                )
            )
            onError("Nem sikerült betölteni a Drive fájlokat.")
        }
}

private suspend fun importDriveFile(
    lesson: LessonCard,
    file: CloudImportFile,
    accessToken: String,
    onImported: (ImportedLessonFile) -> Unit,
    updateState: (CloudImportUiState) -> Unit,
    onError: (String) -> Unit
) {
    val result = withContext(Dispatchers.IO) {
        runCatching { downloadDriveFile(file, accessToken) }
    }

    result
        .onSuccess { downloaded ->
            val saved = LessonAttachmentManager.saveForLesson(
                lesson = lesson,
                suggestedName = downloaded.first,
                bytes = downloaded.second
            )
            if (saved == null) {
                onError("Nem sikerült elmenteni a Drive fájlt.")
                return@onSuccess
            }
            onImported(saved)
        }
        .onFailure {
            updateState(
                CloudImportUiState(
                    title = "Google Drive",
                    errorMessage = "Nem sikerült letölteni a kiválasztott Drive fájlt."
                )
            )
            onError("Nem sikerült letölteni a kiválasztott Drive fájlt.")
        }
}

private fun fetchDriveFiles(accessToken: String): List<CloudImportFile> {
    val query = "trashed=false"
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val fields = URLEncoder.encode("files(id,name,mimeType,modifiedTime,size)", "UTF-8")
    val endpoint =
        "https://www.googleapis.com/drive/v3/files?pageSize=100&orderBy=folder,modifiedTime%20desc&q=$encodedQuery&fields=$fields&includeItemsFromAllDrives=true&supportsAllDrives=true"
    val response = performAuthorizedRequest(endpoint, accessToken)
    val json = JSONObject(response)
    val filesJson = json.optJSONArray("files") ?: return emptyList()

    return buildList {
        for (index in 0 until filesJson.length()) {
            val item = filesJson.optJSONObject(index) ?: continue
            add(
                CloudImportFile(
                    id = item.optString("id"),
                    displayName = item.optString("name", "Drive fájl"),
                    mimeType = item.optString("mimeType"),
                    detail = buildDriveDetail(
                        mimeType = item.optString("mimeType"),
                        modifiedTime = item.optString("modifiedTime"),
                        size = item.optLong("size", -1L)
                    )
                )
            )
        }
    }
}

private fun downloadDriveFile(file: CloudImportFile, accessToken: String): Pair<String, ByteArray> {
    val exportedMime = googleWorkspaceExportMime(file.mimeType)
    val targetName = ensureImportExtension(file.displayName, file.mimeType)
    val endpoint = if (exportedMime != null) {
        val encodedMime = URLEncoder.encode(exportedMime, "UTF-8")
        "https://www.googleapis.com/drive/v3/files/${file.id}/export?mimeType=$encodedMime"
    } else {
        "https://www.googleapis.com/drive/v3/files/${file.id}?alt=media"
    }

    return targetName to performAuthorizedBinaryRequest(endpoint, accessToken)
}

private fun googleWorkspaceExportMime(mimeType: String): String? = when (mimeType) {
    "application/vnd.google-apps.document" ->
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    "application/vnd.google-apps.presentation" ->
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    "application/vnd.google-apps.spreadsheet" ->
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    else -> null
}

private fun ensureImportExtension(name: String, mimeType: String): String {
    val trimmed = name.trim().ifEmpty { "drive_import" }
    if (trimmed.contains('.')) return trimmed

    val extension = when (mimeType) {
        "application/pdf" -> ".pdf"
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.google-apps.document" -> ".docx"
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/vnd.google-apps.presentation" -> ".pptx"
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.google-apps.spreadsheet" -> ".xlsx"
        "application/zip" -> ".zip"
        else -> ".txt"
    }

    return "$trimmed$extension"
}

private fun buildDriveDetail(
    mimeType: String,
    modifiedTime: String,
    size: Long
): String {
    val typeLabel = when (mimeType) {
        "application/pdf" -> "PDF"
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "DOCX"
        "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "PPTX"
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "XLSX"
        "application/msword" -> "DOC"
        "application/vnd.ms-powerpoint" -> "PPT"
        "application/vnd.ms-excel" -> "XLS"
        "application/vnd.google-apps.document" -> "Google Docs"
        "application/vnd.google-apps.presentation" -> "Google Slides"
        "application/vnd.google-apps.spreadsheet" -> "Google Sheets"
        "application/zip" -> "ZIP"
        else -> "Fájl"
    }
    val modifiedLabel = runCatching {
        OffsetDateTime.parse(modifiedTime)
            .format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
    }.getOrNull()

    val sizeLabel = if (size > 0L) formatSize(size) else null
    return listOfNotNull(typeLabel, modifiedLabel, sizeLabel).joinToString(" • ")
}

private fun formatSize(size: Long): String {
    val kilo = 1024.0
    val mega = kilo * 1024.0
    val giga = mega * 1024.0
    val formatter = DecimalFormat("#.#")
    return when {
        size >= giga -> "${formatter.format(size / giga)} GB"
        size >= mega -> "${formatter.format(size / mega)} MB"
        size >= kilo -> "${formatter.format(size / kilo)} KB"
        else -> "$size B"
    }
}

private fun performAuthorizedRequest(
    endpoint: String,
    accessToken: String
): String {
    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        setRequestProperty("Authorization", "Bearer $accessToken")
        setRequestProperty("Accept", "application/json")
        connectTimeout = 15000
        readTimeout = 15000
    }

    return connection.useAndDisconnect { conn ->
        val stream = if (conn.responseCode in 200..299) {
            conn.inputStream
        } else {
            throw IllegalStateException(conn.errorStream?.bufferedReader()?.readText().orEmpty())
        }
        stream.bufferedReader().use { it.readText() }
    }
}

private fun performAuthorizedBinaryRequest(
    endpoint: String,
    accessToken: String
): ByteArray {
    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        setRequestProperty("Authorization", "Bearer $accessToken")
        connectTimeout = 15000
        readTimeout = 30000
    }

    return connection.useAndDisconnect { conn ->
        val stream = if (conn.responseCode in 200..299) {
            conn.inputStream
        } else {
            throw IllegalStateException(conn.errorStream?.bufferedReader()?.readText().orEmpty())
        }
        stream.use { it.readBytes() }
    }
}

private inline fun <T> HttpURLConnection.useAndDisconnect(block: (HttpURLConnection) -> T): T {
    return try {
        block(this)
    } finally {
        disconnect()
    }
}

private const val DRIVE_READONLY_SCOPE = "https://www.googleapis.com/auth/drive.readonly"
