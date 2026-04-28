import Foundation
import SwiftUI
import UIKit
import GoogleSignIn
import UserNotifications

@main
struct iOSApp: App {
    init() {
        GoogleDriveImportBridge.shared.start()
        NotificationPresentationBridge.shared.start()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}

final class NotificationPresentationBridge: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationPresentationBridge()

    func start() {
        UNUserNotificationCenter.current().delegate = self
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .list])
    }
}

final class GoogleDriveImportBridge: NSObject {
    static let shared = GoogleDriveImportBridge()

    private let driveScope = "https://www.googleapis.com/auth/drive.readonly"
    private var observer: NSObjectProtocol?
    private var isStarted = false
    private weak var presenter: UIViewController?

    func start() {
        guard !isStarted else { return }
        isStarted = true

        observer = NotificationCenter.default.addObserver(
            forName: Notification.Name("UniFlowGoogleDriveImportRequested"),
            object: nil,
            queue: .main
        ) { [weak self] notification in
            self?.handleImportRequest(notification)
        }
    }

    private func handleImportRequest(_ notification: Notification) {
        guard let presenter = topViewController() else {
            postFailure("Nem sikerült megnyitni a Google Drive-ot.")
            return
        }

        self.presenter = presenter
        requestDriveAccess(presenter: presenter)
    }

    private func requestDriveAccess(presenter: UIViewController) {
        if let currentUser = GIDSignIn.sharedInstance.currentUser {
            let grantedScopes = Set(currentUser.grantedScopes ?? [])
            if grantedScopes.contains(driveScope) {
                refreshTokenAndPresentFiles(user: currentUser, presenter: presenter)
            } else {
                currentUser.addScopes([driveScope], presenting: presenter) { [weak self] result, error in
                    guard let self else { return }
                    if let error {
                        self.postFailure(error.localizedDescription)
                        return
                    }
                    guard let user = result?.user else {
                        self.postFailure("Nem sikerült engedélyezni a Google Drive hozzáférést.")
                        return
                    }
                    self.refreshTokenAndPresentFiles(user: user, presenter: presenter)
                }
            }
            return
        }

        GIDSignIn.sharedInstance.signIn(
            withPresenting: presenter,
            hint: nil,
            additionalScopes: [driveScope]
        ) { [weak self] result, error in
            guard let self else { return }
            if let error {
                self.postFailure(error.localizedDescription)
                return
            }
            guard let user = result?.user else {
                self.postFailure("Nem sikerült bejelentkezni a Google fiókba.")
                return
            }
            self.refreshTokenAndPresentFiles(user: user, presenter: presenter)
        }
    }

    private func refreshTokenAndPresentFiles(user: GIDGoogleUser, presenter: UIViewController) {
        user.refreshTokensIfNeeded { [weak self] refreshedUser, error in
            guard let self else { return }
            if let error {
                self.postFailure(error.localizedDescription)
                return
            }
            guard let refreshedUser else {
                self.postFailure("Nem sikerült frissíteni a Google hozzáférést.")
                return
            }

            let token = refreshedUser.accessToken.tokenString
            self.browseDriveFolder(
                accessToken: token,
                parentId: nil,
                path: [],
                presenter: presenter
            )
        }
    }

    private func browseDriveFolder(
        accessToken: String,
        parentId: String?,
        path: [DriveFolderSegment],
        presenter: UIViewController
    ) {
        fetchDriveFiles(accessToken: accessToken, parentId: parentId) { [weak self] result in
            guard let self else { return }
            DispatchQueue.main.async {
                switch result {
                case .success(let files):
                    self.presentFileChooser(
                        files: files,
                        path: path,
                        accessToken: accessToken,
                        presenter: presenter
                    )
                case .failure(let error):
                    self.postFailure(error.localizedDescription)
                }
            }
        }
    }

    private func presentFileChooser(
        files: [DriveImportFile],
        path: [DriveFolderSegment],
        accessToken: String,
        presenter: UIViewController
    ) {
        guard !files.isEmpty else {
            if path.isEmpty {
                postFailure("Nem találtunk Drive fájlt a gyökérmappában.")
            } else {
                postFailure("Ebben a mappában nincs megjeleníthető fájl.")
            }
            return
        }

        let alert = UIAlertController(
            title: path.isEmpty ? "Google Drive" : path.last?.name,
            message: path.isEmpty
                ? "Válassz mappát vagy fájlt"
                : "Elérési út: " + (["Gyökér"] + path.map(\.name)).joined(separator: " / "),
            preferredStyle: .actionSheet
        )

        if !path.isEmpty {
            let parentPath = Array(path.dropLast())
            let parentId = parentPath.last?.id
            alert.addAction(UIAlertAction(title: "Vissza", style: .default) { [weak self] _ in
                self?.browseDriveFolder(
                    accessToken: accessToken,
                    parentId: parentId,
                    path: parentPath,
                    presenter: presenter
                )
            })
        }

        for file in files {
            let actionTitle = file.isFolder
                ? "📁 \(file.name)"
                : (file.detail.map { "\(file.name)\n\($0)" } ?? file.name)
            alert.addAction(UIAlertAction(title: actionTitle, style: .default) { [weak self] _ in
                guard let self else { return }
                if file.isFolder {
                    self.browseDriveFolder(
                        accessToken: accessToken,
                        parentId: file.id,
                        path: path + [DriveFolderSegment(id: file.id, name: file.name)],
                        presenter: presenter
                    )
                } else {
                    self.downloadDriveFile(file, accessToken: accessToken)
                }
            })
        }

        alert.addAction(UIAlertAction(title: "Mégse", style: .cancel))

        if let popover = alert.popoverPresentationController {
            popover.sourceView = presenter.view
            popover.sourceRect = CGRect(
                x: presenter.view.bounds.midX,
                y: presenter.view.bounds.maxY - 40,
                width: 1,
                height: 1
            )
        }

        presenter.present(alert, animated: true)
    }

    private func downloadDriveFile(_ file: DriveImportFile, accessToken: String) {
        let endpoint: URL
        let finalName = ensureImportExtension(name: file.name, mimeType: file.mimeType)

        if let exportMime = exportMimeType(for: file.mimeType),
           let encodedMime = exportMime.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
           let url = URL(string: "https://www.googleapis.com/drive/v3/files/\(file.id)/export?mimeType=\(encodedMime)") {
            endpoint = url
        } else if let url = URL(string: "https://www.googleapis.com/drive/v3/files/\(file.id)?alt=media") {
            endpoint = url
        } else {
            postFailure("Nem sikerült előkészíteni a Drive letöltést.")
            return
        }

        var request = URLRequest(url: endpoint)
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")

        URLSession.shared.dataTask(with: request) { [weak self] data, _, error in
            guard let self else { return }
            if error != nil || data == nil {
                self.postFailure("Nem sikerült letölteni a kiválasztott Drive fájlt.")
                return
            }

            let temporaryUrl = FileManager.default.temporaryDirectory
                .appendingPathComponent(UUID().uuidString)
                .appendingPathExtension((finalName as NSString).pathExtension)

            do {
                try data?.write(to: temporaryUrl, options: .atomic)
                self.postSuccess(
                    fileName: finalName,
                    tempPath: temporaryUrl.path
                )
            } catch {
                self.postFailure("Nem sikerült elmenteni a letöltött Drive fájlt.")
            }
        }.resume()
    }

    private func fetchDriveFiles(
        accessToken: String,
        parentId: String?,
        completion: @escaping (Result<[DriveImportFile], Error>) -> Void
    ) {
        let query: String
        if let parentId {
            query = "trashed=false and '\(parentId)' in parents"
        } else {
            query = "trashed=false"
        }
        guard
            let encodedQuery = query.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
            let fields = "files(id,name,mimeType,modifiedTime,size)".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
            let url = URL(string: "https://www.googleapis.com/drive/v3/files?pageSize=100&orderBy=folder,modifiedTime%20desc&q=\(encodedQuery)&fields=\(fields)&includeItemsFromAllDrives=true&supportsAllDrives=true")
        else {
            completion(.success([]))
            return
        }

        var request = URLRequest(url: url)
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error {
                completion(.failure(NSError(
                    domain: "UniFlowDrive",
                    code: 1,
                    userInfo: [NSLocalizedDescriptionKey: "Drive debug: hálózati hiba - \(error.localizedDescription)"]
                )))
                return
            }

            guard let data else {
                completion(.failure(NSError(
                    domain: "UniFlowDrive",
                    code: 2,
                    userInfo: [NSLocalizedDescriptionKey: "Drive debug: üres válasz érkezett a Google API-tól."]
                )))
                return
            }

            do {
                let payload = try JSONSerialization.jsonObject(with: data) as? [String: Any]
                let httpStatus = (response as? HTTPURLResponse)?.statusCode ?? -1
                if let errorPayload = payload?["error"] as? [String: Any] {
                    let message = (errorPayload["message"] as? String) ?? "ismeretlen API hiba"
                    completion(.failure(NSError(
                        domain: "UniFlowDrive",
                        code: 3,
                        userInfo: [NSLocalizedDescriptionKey: "Drive debug: API hiba (\(httpStatus)) - \(message)"]
                    )))
                    return
                }

                let files = (payload?["files"] as? [[String: Any]] ?? []).compactMap { item -> DriveImportFile? in
                    guard
                        let id = item["id"] as? String,
                        let name = item["name"] as? String,
                        let mimeType = item["mimeType"] as? String
                    else {
                        return nil
                    }

                    return DriveImportFile(
                        id: id,
                        name: name,
                        mimeType: mimeType,
                        isFolder: mimeType == "application/vnd.google-apps.folder",
                        detail: self.detailText(
                            mimeType: mimeType,
                            modifiedTime: item["modifiedTime"] as? String,
                            size: (item["size"] as? NSString)?.longLongValue
                        )
                    )
                }
                let sorted = files.sorted {
                    if $0.isFolder != $1.isFolder {
                        return $0.isFolder && !$1.isFolder
                    }
                    return $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending
                }
                if sorted.isEmpty {
                    let responseText = String(data: data, encoding: .utf8)?
                        .replacingOccurrences(of: "\n", with: " ")
                        .prefix(220) ?? ""
                    completion(.failure(NSError(
                        domain: "UniFlowDrive",
                        code: 4,
                        userInfo: [NSLocalizedDescriptionKey: "Drive debug: 0 fájl jött vissza (HTTP \(httpStatus)). Válasz: \(responseText)"]
                    )))
                    return
                }

                completion(.success(sorted))
            } catch {
                let responseText = String(data: data, encoding: .utf8)?
                    .replacingOccurrences(of: "\n", with: " ")
                    .prefix(220) ?? ""
                completion(.failure(NSError(
                    domain: "UniFlowDrive",
                    code: 5,
                    userInfo: [NSLocalizedDescriptionKey: "Drive debug: parse hiba - \(error.localizedDescription). Válasz: \(responseText)"]
                )))
            }
        }.resume()
    }

    private func detailText(mimeType: String, modifiedTime: String?, size: Int64?) -> String {
        let type: String
        switch mimeType {
        case "application/pdf":
            type = "PDF"
        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            type = "DOCX"
        case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
            type = "PPTX"
        case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
            type = "XLSX"
        case "application/vnd.google-apps.document":
            type = "Google Docs"
        case "application/vnd.google-apps.spreadsheet":
            type = "Google Sheets"
        case "application/vnd.google-apps.presentation":
            type = "Google Slides"
        case "application/vnd.google-apps.folder":
            type = "Mappa"
        case "application/zip":
            type = "ZIP"
        default:
            type = "Fájl"
        }

        var parts = [type]
        if let modifiedTime {
            parts.append(String(modifiedTime.prefix(10)))
        }
        if let size, size > 0 {
            parts.append(ByteCountFormatter.string(fromByteCount: size, countStyle: .file))
        }
        return parts.joined(separator: " • ")
    }

    private func exportMimeType(for mimeType: String) -> String? {
        switch mimeType {
        case "application/vnd.google-apps.document":
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        case "application/vnd.google-apps.spreadsheet":
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        case "application/vnd.google-apps.presentation":
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        default:
            return nil
        }
    }

    private func ensureImportExtension(name: String, mimeType: String) -> String {
        if (name as NSString).pathExtension.isEmpty == false {
            return name
        }

        switch mimeType {
        case "application/pdf":
            return "\(name).pdf"
        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
             "application/vnd.google-apps.document":
            return "\(name).docx"
        case "application/vnd.openxmlformats-officedocument.presentationml.presentation",
             "application/vnd.google-apps.presentation":
            return "\(name).pptx"
        case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
             "application/vnd.google-apps.spreadsheet":
            return "\(name).xlsx"
        case "application/zip":
            return "\(name).zip"
        default:
            return "\(name).txt"
        }
    }

    private func postSuccess(fileName: String, tempPath: String) {
        DispatchQueue.main.async {
            NotificationCenter.default.post(
                name: Notification.Name("UniFlowGoogleDriveImportCompleted"),
                object: nil,
                userInfo: [
                    "fileName": fileName,
                    "tempPath": tempPath
                ]
            )
        }
    }

    private func postFailure(_ message: String) {
        DispatchQueue.main.async {
            NotificationCenter.default.post(
                name: Notification.Name("UniFlowGoogleDriveImportFailed"),
                object: nil,
                userInfo: ["message": message]
            )
        }
    }

    private func topViewController() -> UIViewController? {
        guard
            let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
            let root = scene.windows.first(where: { $0.isKeyWindow })?.rootViewController
        else {
            return nil
        }

        var current = root
        while let presented = current.presentedViewController {
            current = presented
        }
        return current
    }
}

private struct DriveImportFile {
    let id: String
    let name: String
    let mimeType: String
    let isFolder: Bool
    let detail: String?
}

private struct DriveFolderSegment {
    let id: String
    let name: String
}
