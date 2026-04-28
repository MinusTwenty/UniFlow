@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.uniflow.uniflow.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL.Companion.fileURLWithPath
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.lastPathComponent
import platform.Foundation.writeToURL
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentInteractionControllerDelegateProtocol
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy

actual object LessonAttachmentManager {
    private var previewController: UIDocumentInteractionController? = null
    private var previewDelegate: FilePreviewDelegate? = null

    actual fun listForLesson(lesson: LessonCard): List<ImportedLessonFile> {
        val directory = lessonDirectoryUrl(lesson) ?: return emptyList()
        val files = NSFileManager.defaultManager.contentsOfDirectoryAtURL(
            url = directory,
            includingPropertiesForKeys = null,
            options = 0u,
            error = null
        ) as? List<NSURL> ?: return emptyList()

        return files.mapNotNull { url ->
            val path = url.path ?: return@mapNotNull null
            val size = NSData.dataWithContentsOfURL(url)?.length?.toLong() ?: 0L
            ImportedLessonFile(
                displayName = url.lastPathComponent ?: "imported_file",
                localPath = path,
                byteSize = size
            )
        }
    }

    actual fun saveForLesson(
        lesson: LessonCard,
        suggestedName: String,
        bytes: ByteArray
    ): ImportedLessonFile? {
        val directory = lessonDirectoryUrl(lesson) ?: return null
        NSFileManager.defaultManager.createDirectoryAtURL(
            url = directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )

        val safeName = sanitizeImportedFileName(suggestedName)
        val targetUrl = uniqueFileUrl(directory, safeName)
        val data = bytes.toNSData()
        if (!data.writeToURL(targetUrl, atomically = true)) return null

        return ImportedLessonFile(
            displayName = targetUrl.lastPathComponent ?: safeName,
            localPath = targetUrl.path ?: return null,
            byteSize = bytes.size.toLong()
        )
    }

    actual fun openImportedFile(file: ImportedLessonFile): Boolean {
        val url = NSURL(fileURLWithPath = file.localPath)
        val rootController = topViewController() ?: return false
        val delegate = FilePreviewDelegate(rootController)
        val controller = UIDocumentInteractionController.interactionControllerWithURL(url)
        controller.delegate = delegate
        previewDelegate = delegate
        previewController = controller
        return controller.presentPreviewAnimated(true)
    }
}

@Composable
actual fun rememberAttachmentImportController(
    onImported: (LessonCard, ImportedLessonFile) -> Unit,
    onError: (String) -> Unit
): AttachmentImportController {
    val cloudImportState = remember { mutableStateOf<CloudImportUiState?>(null) }
    val pendingCloudLesson = remember { mutableStateOf<LessonCard?>(null) }

    DisposableEffect(onImported, onError) {
        val center = NSNotificationCenter.defaultCenter
        val successObserver = center.addObserverForName(
            name = GoogleDriveImportCompletedNotification,
            `object` = null,
            queue = null
        ) { notification ->
            val userInfo = notification?.userInfo ?: return@addObserverForName
            val fileName = userInfo["fileName"] as? String
            val tempPath = userInfo["tempPath"] as? String
            val lesson = pendingCloudLesson.value

            if (lesson == null || fileName.isNullOrBlank() || tempPath.isNullOrBlank()) {
                cloudImportState.value = null
                pendingCloudLesson.value = null
                return@addObserverForName
            }

            val data = NSData.dataWithContentsOfURL(fileURLWithPath(tempPath))
            val bytes = data?.toByteArray()
            if (bytes == null) {
                cloudImportState.value = null
                pendingCloudLesson.value = null
                onError("Nem sikerült beolvasni a Google Drive fájlt.")
                return@addObserverForName
            }

            val saved = LessonAttachmentManager.saveForLesson(
                lesson = lesson,
                suggestedName = fileName,
                bytes = bytes
            )
            cloudImportState.value = null
            pendingCloudLesson.value = null

            if (saved == null) {
                onError("Nem sikerült elmenteni a Google Drive fájlt.")
                return@addObserverForName
            }

            onImported(lesson, saved)
        }

        val failureObserver = center.addObserverForName(
            name = GoogleDriveImportFailedNotification,
            `object` = null,
            queue = null
        ) { notification ->
            val message = notification?.userInfo?.get("message") as? String
            cloudImportState.value = null
            pendingCloudLesson.value = null
            onError(message ?: "Nem sikerült kapcsolódni a Google Drive-hoz.")
        }

        onDispose {
            center.removeObserver(successObserver)
            center.removeObserver(failureObserver)
        }
    }

    return remember(onImported, onError) {
        val pickerPresenter = IOSAttachmentPickerPresenter(onImported, onError)
        AttachmentImportController(
            importDocument = { lesson ->
                pickerPresenter.present(
                    lesson = lesson,
                    documentTypes = listOf(
                        "com.adobe.pdf",
                        "org.openxmlformats.wordprocessingml.document",
                        "org.openxmlformats.presentationml.presentation",
                        "org.openxmlformats.spreadsheetml.sheet",
                        "public.plain-text",
                        "public.zip-archive"
                     )
                )
            },
            importCloudDocument = { lesson ->
                pendingCloudLesson.value = lesson
                cloudImportState.value = CloudImportUiState(
                    isLoading = true,
                    title = "Google Drive"
                )
                NSNotificationCenter.defaultCenter.postNotificationName(
                    GoogleDriveImportRequestedNotification,
                    null
                )
            },
            importCode = { lesson ->
                pickerPresenter.present(
                    lesson = lesson,
                    documentTypes = listOf(
                        "public.source-code",
                        "public.plain-text",
                        "public.text",
                        "public.json",
                        "public.xml",
                        "public.zip-archive",
                        "public.data"
                    )
                )
            },
            cloudImportState = cloudImportState,
            selectCloudFile = {},
            dismissCloudImport = {
                cloudImportState.value = null
                pendingCloudLesson.value = null
            }
        )
    }
}

private const val GoogleDriveImportRequestedNotification = "UniFlowGoogleDriveImportRequested"
private const val GoogleDriveImportCompletedNotification = "UniFlowGoogleDriveImportCompleted"
private const val GoogleDriveImportFailedNotification = "UniFlowGoogleDriveImportFailed"

private class FilePreviewDelegate(
    private val viewController: UIViewController
) : NSObject(), UIDocumentInteractionControllerDelegateProtocol {
    override fun documentInteractionControllerViewControllerForPreview(
        controller: UIDocumentInteractionController
    ): UIViewController = viewController
}

private class IOSAttachmentPickerPresenter(
    private val onImported: (LessonCard, ImportedLessonFile) -> Unit,
    private val onError: (String) -> Unit
) {
    private var pendingLesson: LessonCard? = null
    private var delegateRef: AttachmentPickerDelegate? = null

    fun present(lesson: LessonCard, documentTypes: List<String>) {
        val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
        if (rootController == null) {
            onError("Nem sikerült megnyitni a fájlválasztót.")
            return
        }

        pendingLesson = lesson
        val delegate = AttachmentPickerDelegate(
            onPicked = { urls -> importUrls(urls) },
            onCancelled = {
                pendingLesson = null
                delegateRef = null
            }
        )
        delegateRef = delegate

        val picker = UIDocumentPickerViewController(
            documentTypes = documentTypes,
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        )
        picker.delegate = delegate

        var presenter: UIViewController = rootController
        while (presenter.presentedViewController != null) {
            presenter = presenter.presentedViewController!!
        }
        presenter.presentViewController(picker, animated = true, completion = null)
    }

    private fun importUrls(urls: List<NSURL>) {
        val lesson = pendingLesson ?: return
        pendingLesson = null
        delegateRef = null

        val firstUrl = urls.firstOrNull()
        if (firstUrl == null) return

        val fileName = firstUrl.lastPathComponent ?: "imported_file"
        val data = NSData.dataWithContentsOfURL(firstUrl)
        val bytes = data?.toByteArray()
        if (bytes == null) {
            onError("Nem sikerült beolvasni a kiválasztott fájlt.")
            return
        }

        val saved = LessonAttachmentManager.saveForLesson(lesson, fileName, bytes)
        if (saved == null) {
            onError("Nem sikerült elmenteni a fájlt.")
            return
        }

        onImported(lesson, saved)
    }
}

private class AttachmentPickerDelegate(
    val onPicked: (List<NSURL>) -> Unit,
    val onCancelled: () -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        @Suppress("UNCHECKED_CAST")
        onPicked(didPickDocumentsAtURLs.filterIsInstance<NSURL>())
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onCancelled()
    }
}

private fun lessonDirectoryUrl(lesson: LessonCard): NSURL? {
    val documentsDir = NSFileManager.defaultManager.URLsForDirectory(
        directory = NSDocumentDirectory,
        inDomains = NSUserDomainMask
    ).firstOrNull() as? NSURL ?: return null

    return documentsDir
        .URLByAppendingPathComponent("lesson_attachments")
        ?.URLByAppendingPathComponent(lessonAttachmentFolderName(lesson))
}

private fun topViewController(): UIViewController? {
    val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return null
    var presenter: UIViewController = rootController
    while (presenter.presentedViewController != null) {
        presenter = presenter.presentedViewController!!
    }
    return presenter
}

private fun uniqueFileUrl(directory: NSURL, originalName: String): NSURL {
    val dot = originalName.lastIndexOf('.')
    val base = if (dot > 0) originalName.substring(0, dot) else originalName
    val extension = if (dot > 0) originalName.substring(dot) else ""

    var index = 0
    var candidateName = originalName
    var candidateUrl = directory.URLByAppendingPathComponent(candidateName)!!

    while (NSFileManager.defaultManager.fileExistsAtPath(candidateUrl.path!!)) {
        index += 1
        candidateName = "${base}_$index$extension"
        candidateUrl = directory.URLByAppendingPathComponent(candidateName)!!
    }

    return candidateUrl
}

private fun ByteArray.toNSData(): NSData = memScoped {
    this@toNSData.usePinned {
        NSData.create(bytes = it.addressOf(0), length = this@toNSData.size.toULong())
    }
}

private fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(length.toInt())
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
    return result
}
