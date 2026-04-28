package com.uniflow.uniflow.home

import android.app.Activity
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

private data class DriveFolderNode(
    val id: String,
    val name: String
)

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
    var pendingAuthorizationRequest by remember { mutableStateOf<AuthorizationRequest?>(null) }
    var driveFolderStack by remember { mutableStateOf<List<DriveFolderNode>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val authorizationClient = remember {
        Identity.getAuthorizationClient(AndroidAppContextHolder.context)
    }
    val reloadCurrentDriveFolder: () -> Unit = {
        val accessToken = pendingCloudToken
        if (accessToken.isNullOrBlank()) {
            cloudImportState = null
            onError(driveError("GD-SESSION-EXPIRED", "Google Drive session expired. Try again."))
        } else {
            val currentFolder = driveFolderStack.lastOrNull()
            cloudImportState = CloudImportUiState(
                isLoading = true,
                title = currentFolder?.name ?: "Google Drive"
            )
            scope.launch {
                loadDriveFiles(
                    accessToken = accessToken,
                    folderStack = driveFolderStack,
                    updateState = { cloudImportState = it },
                    onError = onError
                )
            }
        }
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
        if (result.resultCode != Activity.RESULT_OK && data == null) {
            cloudImportState = null
            onError(
                driveError(
                    "GD-AUTH-CANCEL",
                    "Google Drive auth was cancelled (resultCode=${result.resultCode}, hasData=${result.data != null})."
                )
            )
            return@rememberLauncherForActivityResult
        }

        if (data == null) {
            cloudImportState = null
            onError(driveError("GD-AUTH-NO-DATA", "Google Drive auth returned no data."))
            return@rememberLauncherForActivityResult
        }

        runCatching { authorizationClient.getAuthorizationResultFromIntent(data) }
            .onSuccess { authResult ->
                val accessToken = authResult.accessToken
                if (accessToken.isNullOrBlank()) {
                    val request = pendingAuthorizationRequest
                    if (request == null) {
                        cloudImportState = null
                        onError(driveError("GD-AUTH-NO-TOKEN", "Google Drive returned no access token."))
                    } else {
                        authorizationClient.authorize(request)
                            .addOnSuccessListener { retriedResult ->
                                val retriedToken = retriedResult.accessToken
                                if (retriedToken.isNullOrBlank()) {
                                    pendingAuthorizationRequest = null
                                    cloudImportState = null
                                    onError(
                                        driveError(
                                            "GD-AUTH-NO-TOKEN",
                                            "Google Drive returned no access token after grant (grantedScopes=${retriedResult.grantedScopes.size})."
                                        )
                                    )
                                } else {
                                    pendingAuthorizationRequest = null
                                    pendingCloudToken = retriedToken
                                    scope.launch {
                                        loadDriveFiles(
                                            accessToken = retriedToken,
                                            folderStack = driveFolderStack,
                                            updateState = { cloudImportState = it },
                                            onError = onError
                                        )
                                    }
                                }
                            }
                            .addOnFailureListener { retryError ->
                                pendingAuthorizationRequest = null
                                cloudImportState = null
                                onError(
                                    driveError(
                                        "GD-AUTH-NO-TOKEN-RETRY",
                                        "Google Drive token retry failed (${retryError::class.java.simpleName}: ${retryError.message ?: "no-message"})."
                                    )
                                )
                            }
                    }
                    return@onSuccess
                }

                pendingAuthorizationRequest = null
                pendingCloudToken = accessToken
                scope.launch {
                    loadDriveFiles(
                        accessToken = accessToken,
                        folderStack = driveFolderStack,
                        updateState = { cloudImportState = it },
                        onError = onError
                    )
                }
            }
            .onFailure { error ->
                val request = pendingAuthorizationRequest
                if (request == null) {
                    cloudImportState = null
                    onError(
                        driveError(
                            "GD-AUTH-RESULT",
                            "Google Drive auth result could not be completed (${error::class.simpleName}: ${error.message ?: "no-message"})."
                        )
                    )
                } else {
                    authorizationClient.authorize(request)
                        .addOnSuccessListener { authResult ->
                            val accessToken = authResult.accessToken
                            if (accessToken.isNullOrBlank()) {
                                pendingAuthorizationRequest = null
                                pendingCloudLesson = null
                                cloudImportState = null
                                onError(
                                    driveError(
                                        "GD-AUTH-NO-TOKEN",
                                        "Google Drive returned no access token after retry (grantedScopes=${authResult.grantedScopes.size})."
                                    )
                                )
                            } else {
                                pendingAuthorizationRequest = null
                                pendingCloudToken = accessToken
                                scope.launch {
                                    loadDriveFiles(
                                        accessToken = accessToken,
                                        folderStack = driveFolderStack,
                                        updateState = { cloudImportState = it },
                                        onError = onError
                                    )
                                }
                            }
                        }
                        .addOnFailureListener { retryError ->
                            pendingAuthorizationRequest = null
                            pendingCloudLesson = null
                            cloudImportState = null
                            onError(
                                driveError(
                                    "GD-AUTH-RETRY",
                                    "Google Drive auth retry failed (${retryError::class.java.simpleName}: ${retryError.message ?: "no-message"})."
                                )
                            )
                        }
                }
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
                    onError(driveError("GD-AUTH-NO-ACTIVITY", "Google Drive auth could not start from this screen."))
                } else {
                    pendingCloudLesson = lesson
                    driveFolderStack = emptyList()
                    cloudImportState = CloudImportUiState(isLoading = true)

                    val request = AuthorizationRequest.builder()
                        .setRequestedScopes(
                            listOf(
                                Scope(DRIVE_READONLY_SCOPE)
                            )
                        )
                        .build()
                    pendingAuthorizationRequest = request

                    authorizationClient.authorize(request)
                        .addOnSuccessListener { authorizationResult ->
                            handleAuthorizationResult(
                                result = authorizationResult,
                                launcher = authorizationLauncher,
                                onTokenReady = { accessToken ->
                                    pendingAuthorizationRequest = null
                                    pendingCloudToken = accessToken
                                    scope.launch {
                                        loadDriveFiles(
                                            accessToken = accessToken,
                                            folderStack = driveFolderStack,
                                            updateState = { cloudImportState = it },
                                            onError = onError
                                        )
                                    }
                                },
                                onError = { message ->
                                    pendingAuthorizationRequest = null
                                    pendingCloudLesson = null
                                    cloudImportState = null
                                    onError(message)
                                }
                            )
                        }
                        .addOnFailureListener {
                            pendingAuthorizationRequest = null
                            pendingCloudLesson = null
                            cloudImportState = null
                            onError(driveError("GD-AUTH-START", "Google Drive authorization request failed."))
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
                    onError(driveError("GD-SESSION-EXPIRED", "Google Drive session expired. Try again."))
                } else {
                    if (file.isNavigateUp) {
                        driveFolderStack = driveFolderStack.dropLast(1)
                        reloadCurrentDriveFolder()
                    } else if (file.isFolder) {
                        driveFolderStack = driveFolderStack + DriveFolderNode(
                            id = file.id,
                            name = file.displayName
                        )
                        reloadCurrentDriveFolder()
                    } else {
                        val visibleFiles = cloudImportState?.files.orEmpty()
                        val currentTitle = driveFolderStack.lastOrNull()?.name ?: "Google Drive"
                        cloudImportState = CloudImportUiState(
                            isLoading = true,
                            title = currentTitle
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
                                        title = currentTitle,
                                        files = visibleFiles,
                                        errorMessage = message
                                    )
                                }
                            )
                        }
                    }
                }
            },
            dismissCloudImport = {
                pendingAuthorizationRequest = null
                pendingCloudLesson = null
                pendingCloudToken = null
                driveFolderStack = emptyList()
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
        onError("Nem sikerÃ¼lt beolvasni a kivÃ¡lasztott fÃ¡jlt.")
        return
    }

    val saved = LessonAttachmentManager.saveForLesson(
        lesson = lesson,
        suggestedName = fileName,
        bytes = bytes
    )

    if (saved == null) {
        onError("Nem sikerÃ¼lt elmenteni a fÃ¡jlt.")
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
                onError(driveError("GD-AUTH-NO-RESOLUTION", "Google Drive auth resolution could not start."))
                return
            }
            launcher.launch(
                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            )
        }
        else -> onError(driveError("GD-AUTH-DENIED", "Google Drive access was denied."))
    }
}

private suspend fun loadDriveFiles(
    accessToken: String,
    folderStack: List<DriveFolderNode>,
    updateState: (CloudImportUiState) -> Unit,
    onError: (String) -> Unit
) {
    val currentFolder = folderStack.lastOrNull()
    val title = currentFolder?.name ?: "Google Drive"
    val result = withContext(Dispatchers.IO) {
        runCatching { fetchDriveFiles(accessToken, folderStack) }
    }

    result
        .onSuccess { files ->
            updateState(
                CloudImportUiState(
                    title = title,
                    files = files
                )
            )
        }
        .onFailure {
            updateState(
                CloudImportUiState(
                    title = title,
                    errorMessage = driveError("GD-LIST-LOAD", "Could not load Google Drive files.")
                )
            )
            onError(driveError("GD-LIST-LOAD", "Could not load Google Drive files."))
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
        runCatching {
            if (!isSupportedDriveImportMime(file.mimeType)) {
                error("Unsupported Drive mimeType: ${file.mimeType}")
            }
            downloadDriveFile(file, accessToken)
        }
    }

    result
        .onSuccess { downloaded ->
            val saved = LessonAttachmentManager.saveForLesson(
                lesson = lesson,
                suggestedName = downloaded.first,
                bytes = downloaded.second
            )
            if (saved == null) {
                onError(driveError("GD-FILE-SAVE", "Could not save imported Google Drive file."))
                return@onSuccess
            }
            onImported(saved)
        }
        .onFailure {
            updateState(
                CloudImportUiState(
                    title = "Google Drive",
                    errorMessage = driveError(
                        if (it.message?.startsWith("Unsupported Drive mimeType:") == true) {
                            "GD-FILE-UNSUPPORTED"
                        } else {
                            "GD-FILE-DOWNLOAD"
                        },
                        buildDriveFailureMessage("Could not download selected Google Drive file.", it)
                    )
                )
            )
            onError(
                driveError(
                    if (it.message?.startsWith("Unsupported Drive mimeType:") == true) {
                        "GD-FILE-UNSUPPORTED"
                    } else {
                        "GD-FILE-DOWNLOAD"
                    },
                    buildDriveFailureMessage("Could not download selected Google Drive file.", it)
                )
            )
        }
}

private fun fetchDriveFiles(
    accessToken: String,
    folderStack: List<DriveFolderNode>
): List<CloudImportFile> {
    val currentFolder = folderStack.lastOrNull()
    val query = if (currentFolder == null) {
        "'root' in parents and trashed=false"
    } else {
        "'${currentFolder.id}' in parents and trashed=false"
    }
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val fields = URLEncoder.encode("files(id,name,mimeType,modifiedTime,size)", "UTF-8")
    val endpoint =
        "https://www.googleapis.com/drive/v3/files?pageSize=100&orderBy=folder,modifiedTime%20desc&q=$encodedQuery&fields=$fields&includeItemsFromAllDrives=true&supportsAllDrives=true"
    val response = performAuthorizedRequest(endpoint, accessToken)
    val json = JSONObject(response)
    val filesJson = json.optJSONArray("files") ?: return emptyList()

    return buildList {
        if (folderStack.isNotEmpty()) {
            add(
                CloudImportFile(
                    id = currentFolder?.id.orEmpty(),
                    displayName = "..",
                    mimeType = "application/vnd.google-apps.folder",
                    detail = "Vissza az elozo mappaba",
                    isNavigateUp = true
                )
            )
        }

        for (index in 0 until filesJson.length()) {
            val item = filesJson.optJSONObject(index) ?: continue
            val mimeType = item.optString("mimeType")
            val isFolder = mimeType == "application/vnd.google-apps.folder"
            if (!isFolder && !isSupportedDriveImportMime(mimeType)) continue
            add(
                CloudImportFile(
                    id = item.optString("id"),
                    displayName = item.optString("name", "Drive fÃ¡jl"),
                    mimeType = mimeType,
                    isFolder = isFolder,
                    detail = buildDriveDetail(
                        mimeType = mimeType,
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

private fun isSupportedDriveImportMime(mimeType: String): Boolean = when (mimeType) {
    "application/pdf",
    "application/zip",
    "text/plain",
    "text/markdown",
    "application/json",
    "application/xml",
    "text/xml",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.google-apps.document",
    "application/vnd.google-apps.presentation",
    "application/vnd.google-apps.spreadsheet" -> true
    else -> false
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
        "application/vnd.google-apps.folder" -> "Mappa"
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
        else -> "FÃ¡jl"
    }
    val modifiedLabel = runCatching {
        OffsetDateTime.parse(modifiedTime)
            .format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
    }.getOrNull()

    val sizeLabel = if (size > 0L) formatSize(size) else null
    return listOfNotNull(typeLabel, modifiedLabel, sizeLabel).joinToString(" â€¢ ")
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
            val body = conn.errorStream?.bufferedReader()?.readText().orEmpty()
            throw IllegalStateException("HTTP ${conn.responseCode}: ${body.take(240)}")
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
            val body = conn.errorStream?.bufferedReader()?.readText().orEmpty()
            throw IllegalStateException("HTTP ${conn.responseCode}: ${body.take(240)}")
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

private fun buildDriveFailureMessage(prefix: String, error: Throwable): String {
    val type = error::class.java.simpleName
    val message = error.message?.replace('\n', ' ')?.take(240) ?: "no-message"
    return "$prefix ($type: $message)"
}

private fun driveError(code: String, message: String): String = "$code: $message"

private const val DRIVE_READONLY_SCOPE = "https://www.googleapis.com/auth/drive.readonly"

