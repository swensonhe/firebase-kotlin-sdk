package dev.gitlive.firebase.storage

import dev.gitlive.firebase.*
import kotlinx.coroutines.await

actual val Firebase.storage get() =
    rethrow { dev.gitlive.firebase.storage; FirebaseStorage(firebase.app().storage()) }

actual fun Firebase.storage(url: String) =
    rethrow { dev.gitlive.firebase.storage; FirebaseStorage(firebase.app().storage(url)) }

actual fun Firebase.storage(app: FirebaseApp) =
    rethrow { dev.gitlive.firebase.storage; FirebaseStorage(firebase.storage(app.js)) }

actual fun Firebase.storage(app: FirebaseApp, url: String) =
    rethrow { dev.gitlive.firebase.storage; FirebaseStorage(app.js.storage(url)) }

actual class FirebaseStorage(val js: firebase.storage.Storage) {

    actual val maxOperationRetryTimeMillis = js.maxOperationRetryTime
    actual val maxUploadRetryTimeMillis = js.maxUploadRetryTime

    actual fun getReference(url: String) = rethrow { StorageReference(js.ref(url)) }

    actual fun setMaxOperationRetryTimeMillis(maxTransferRetryMillis: Long) = js.setMaxOperationRetryTime(maxTransferRetryMillis)
    actual fun setMaxUploadRetryTimeMillis(maxTransferRetryMillis: Long) = js.setMaxUploadRetryTime(maxTransferRetryMillis)
}


actual class StorageReference internal constructor(
    val js: firebase.storage.Reference
) {

    actual val bucket: String
        get() = rethrow { js.bucket }
    actual val name: String
        get() = rethrow { js.name }
    actual val parent: StorageReference
        get() = rethrow { StorageReference(js.parent) }
    actual val path: String
        get() = rethrow { js.fullPath}
    actual val root: StorageReference
        get() = rethrow { StorageReference(js.root) }
    actual val storage: FirebaseStorage
        get() = rethrow { FirebaseStorage(js.storage) }

    actual fun child(path: String): StorageReference = rethrow { StorageReference(js.child(path)) }

    actual suspend fun delete(): Unit = rethrow { js.delete().await() }

    actual suspend fun getDownloadUrl(): String = rethrow { js.getDownloadUrl().await() }

    actual suspend fun getMetadata(): StorageMetadata = rethrow { StorageMetadata(js.getMetadata().await()) }

}


// TODO: No download task for JS - download from URL
actual class DownloadTask {
//    class TaskSnapshot internal constructor(
//        val js: firebase.storage.
//    ) {
//        actual val bytesTransferred = android.getBytesTransferred()
//        actual val totalByteCount = android.getTotalByteCount()
//    }
}


actual class UploadTask {
    class TaskSnapshot internal constructor(
        val js: firebase.storage.UploadTask
    ) {
        actual val bytesTransferred = js.snapshot.bytesTransferred
        actual val totalByteCount = js.snapshot.totalBytes
        actual val metadata = js.snapshot.metadata
        actual val uploadSessionUri = js.snapshot.downloadURL
    }
}


actual class ListResult internal constructor(
    val js: firebase.storage.ListResult
){
    actual val items = js.items.asList()
    actual val pageToken = js.nextPageToken.toString()
    actual val prefixes = js.prefixes.asList()
}


actual class StorageMetadata internal constructor(
    val js: firebase.storage.FullMetadata
) {

    val cacheControl = js.cacheControl
    val contentDisposition = js.contentDisposition
    val contentEncoding = js.contentEncoding
    val contentLanguage = js.contentLanguage
    val contentType = js.contentType
    val customMetadata = js.customMetadata
    val md5Hash = js.md5Hash

}


actual class FirebaseStorageException(cause: Throwable, val code: StorageExceptionCode): FirebaseException(code.toString(), cause)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val FirebaseStorageException.code: StorageExceptionCode get() = code

actual enum class StorageExceptionCode {
    UNKNOWN,
    OBJECT_NOT_FOUND,
    BUCKET_NOT_FOUND,
    PROJECT_NOT_FOUND,
    QUOTA_EXCEEDED,
    UNAUTHENTICATED,
    UNAUTHORIZED,
    RETRY_LIMIT_EXCEEDED,
    NON_MATCHING_CHECKSUM,
    DOWNLOAD_SIZE_EXCEEDED,
    CANCELLED,
    INVALID_ARGUMENT,
}


/*
    Helper methods
 */
inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.storage.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: dynamic) {
        throw e as Throwable // TODO: Handle error correctly
    }
}