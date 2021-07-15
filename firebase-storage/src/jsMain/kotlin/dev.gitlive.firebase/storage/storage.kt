package dev.gitlive.firebase.storage

import dev.gitlive.firebase.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow


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
        get() = js.bucket
    actual val name: String
        get() = js.name
    actual val parent: StorageReference
        get() = StorageReference(js.parent)
    actual val path: String
        get() = js.fullPath
    actual val root: StorageReference
        get() = StorageReference(js.root)
    actual val storage: FirebaseStorage
        get() = FirebaseStorage(js.storage)

    actual fun child(path: String): StorageReference = StorageReference(js.child(path))

    actual suspend fun delete(): Unit = js.delete().await()

    actual suspend fun getDownloadUrl(): String = rethrow { js.getDownloadUrl().await() }
    actual suspend fun getMetadata(): StorageMetadata = rethrow { StorageMetadata(js.getMetadata().await()) }

//    actual suspend fun getBytes(maxDownloadSizeBytes: Long): DownloadBytesTask = DownloadBytesTask(js.put(maxDownloadSizeBytes))
//    actual suspend fun getFile(destinationFile: Uri): DownloadFileTask = DownloadFileTask(js.put(destinationFile))
    actual suspend fun putFile(data: Any): UploadTask = UploadTask(js.put(data as ByteArray, null))
}


actual class UploadTask internal constructor(
    val js: firebase.storage.UploadTask
){
    actual fun cancel() = js.cancel()
    actual fun pause() = js.pause()
    actual fun resume() = js.resume()

    actual suspend fun onProgress() = callbackFlow<UploadTaskSnapshot> {
        val listener = js.on("state_changed", { snapshot ->
            if (snapshot.state == "running") {
                safeOffer(UploadTaskSnapshot(snapshot))
            }
        }, { Unit })
        awaitClose { rethrow { js.off("state_changed", listener) } }
    }

    actual suspend fun onPaused(): StorageMetadata {
        val deferred = CompletableDeferred<Result<StorageMetadata>>()
        val listener = js.on("state_changed", { snapshot ->
            if (snapshot.state == "paused") {
                deferred.complete(Result.success(StorageMetadata(snapshot.metadata)))
            }
        }, { Unit })
        return deferred.await().getOrThrow()
    }

    actual suspend fun onFailed(): FirebaseStorageException {
        val deferred = CompletableDeferred<Result<FirebaseStorageException>>()
        val listener = js.on("state_changed", { Unit }, {
            deferred.complete(Result.success(FirebaseStorageException(it)))
        })
        return deferred.await().getOrThrow()
    }

    actual suspend fun onComplete(): StorageMetadata {
        val deferred = CompletableDeferred<Result<FirebaseStorageException>>()
        val listener = js.on("state_changed", { Unit }, { Unit }, {
            deferred.complete(Result.success(""))
        })
        return deferred.await().getOrThrow()
    }
}


// TODO: Might have to use ktor here?
//actual class DownloadFileTaskSnapshot internal constructor(
//    val js: firebase.storage.
//) {
//    actual fun getBytesTransferred() = android.bytesTransferred
//    actual fun getTotalByteCount() = android.totalByteCount
//}


actual class UploadTaskSnapshot internal constructor(
    val js: firebase.storage.UploadTaskSnapshot
)  {
    actual fun getBytesTransferred() = js.bytesTransferred
    actual fun getTotalByteCount() = js.totalBytes
    actual fun getMetadata() = StorageMetadata(js.metadata)
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