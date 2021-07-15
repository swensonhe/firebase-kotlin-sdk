@file:JvmName("android")
package dev.gitlive.firebase.storage

import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageException
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.safeOffer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.net.URI


actual val Firebase.storage get() =
    FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance())


actual fun Firebase.storage(url: String) =
    FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance(url))


actual fun Firebase.storage(app: FirebaseApp) =
    FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance(app.android))


actual fun Firebase.storage(app: FirebaseApp, url: String) =
    FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance(app.android, url))


actual class FirebaseStorage(val android: com.google.firebase.storage.FirebaseStorage) {
    actual val maxOperationRetryTimeMillis = android.getMaxUploadRetryTimeMillis()
    actual val maxUploadRetryTimeMillis = android.getMaxOperationRetryTimeMillis()

    actual fun getReference(url: String) = StorageReference(android.getReference(url))

    actual fun setMaxOperationRetryTimeMillis(maxTransferRetryMillis: Long) = android.setMaxOperationRetryTimeMillis(maxTransferRetryMillis)
    actual fun setMaxUploadRetryTimeMillis(maxTransferRetryMillis: Long) = android.setMaxUploadRetryTimeMillis(maxTransferRetryMillis)
}


actual class StorageReference internal constructor(
    val android: com.google.firebase.storage.StorageReference
) {
    actual val bucket: String
        get() = android.bucket
    actual val name: String
        get() = android.name
    actual val parent: StorageReference
        get() = StorageReference(android.parent!!)
    actual val path: String
        get() = android.path
    actual val root: StorageReference
        get() = StorageReference(android.root)
    actual val storage: FirebaseStorage
        get() = FirebaseStorage(android.storage)

    actual fun child(path: String): StorageReference = StorageReference(android.child(path))

    actual suspend fun delete(): Unit = android.delete().await().run { Unit }

    actual suspend fun getDownloadUrl(): String = android.downloadUrl.await().run { Unit }
    actual suspend fun getMetadata(): StorageMetadata = StorageMetadata(android.getMetadata().await().run { Unit })

    actual suspend fun getBytes(maxDownloadSizeBytes: Long): DownloadBytesTask = DownloadBytesTask(android.getBytes(maxDownloadSizeBytes))
    actual suspend fun getFile(destinationFile: Uri): DownloadFileTask = DownloadFileTask(android.getFile(destinationFile))
    actual suspend fun putFile(data: Any): UploadTask = UploadTask(android.putBytes(data as ByteArray))
}


actual class DownloadBytesTask internal constructor(
    val android: com.google.android.gms.tasks.Task<ByteArray>
) {
    suspend fun onSuccess(): ByteArray {
        val deferred = CompletableDeferred<Result<ByteArray>>()
        android.addOnCompleteListener { taskSnapshot ->
            taskSnapshot.result.let { deferred.complete(Result.success(ByteArray(it))) }
        }
        return deferred.await().getOrThrow()
    }

    suspend fun onFailed(): FirebaseStorageException {
        val deferred = CompletableDeferred<Result<FirebaseStorageException>>()
        android.addOnFailureListener { exception ->
            deferred.complete(Result.failure(exception))
        }
        return deferred.await().getOrThrow()
    }
}


actual class DownloadFileTask internal constructor(
    val android: com.google.firebase.storage.FileDownloadTask
) {
    suspend fun onSuccess(): DownloadFileTaskSnapshot {
        val deferred = CompletableDeferred<Result<DownloadFileTaskSnapshot>>()
        android.addOnCompleteListener { taskSnapshot ->
            taskSnapshot.result?.let { deferred.complete(Result.success(DownloadFileTaskSnapshot(it))) }
        }
        return deferred.await().getOrThrow()
    }

    suspend fun onFailed(): FirebaseStorageException {
        val deferred = CompletableDeferred<Result<FirebaseStorageException>>()
        android.addOnFailureListener { exception ->
            deferred.complete(Result.failure(exception))
        }
        return deferred.await().getOrThrow()
    }
}


actual class UploadTask internal constructor(
    val android: com.google.firebase.storage.UploadTask
){
    actual fun cancel() = android.cancel()
    actual fun pause() = android.pause()
    actual fun resume() = android.resume()

    actual suspend fun onProgress() = callbackFlow<UploadTaskSnapshot> {
        val listener =
            OnProgressListener<com.google.firebase.storage.UploadTask.TaskSnapshot> { snapshot ->
                snapshot.let { safeOffer(UploadTaskSnapshot(snapshot)) }
            }
        android.addOnProgressListener(listener)
        awaitClose { android.removeOnProgressListener(listener) }
    }

    actual suspend fun onPaused(): StorageMetadata {
        val deferred = CompletableDeferred<Result<StorageMetadata>>()
        android.addOnPausedListener { taskSnapshot ->
            taskSnapshot.metadata?.let {
                deferred.complete(Result.success(StorageMetadata(it)))
            }
        }
        return deferred.await().getOrThrow()
    }

    actual suspend fun onFailed(): FirebaseStorageException {
        val deferred = CompletableDeferred<Result<FirebaseStorageException>>()
        android.addOnFailureListener { exception ->
            deferred.complete(Result.failure(exception))
        }
        return deferred.await().getOrThrow()
    }

    actual suspend fun onComplete(): StorageMetadata {
        val deferred = CompletableDeferred<Result<StorageMetadata>>()
        android.addOnCompleteListener { taskSnapshot ->
            taskSnapshot.result?.metadata?.let {
                deferred.complete(Result.success(StorageMetadata(it)))
            }
        }
        return deferred.await().getOrThrow()
    }
}


actual class DownloadFileTaskSnapshot internal constructor(
    val android: com.google.firebase.storage.FileDownloadTask.TaskSnapshot
) {
    actual fun getBytesTransferred() = android.bytesTransferred
    actual fun getTotalByteCount() = android.totalByteCount
}


actual class UploadTaskSnapshot internal constructor(
    val android: com.google.firebase.storage.UploadTask.TaskSnapshot
) {
    actual fun getBytesTransferred() = android.bytesTransferred
    actual fun getTotalByteCount() = android.totalByteCount
    actual fun getMetadata() = StorageMetadata(android.metadata!!)
}


actual class StorageMetadata internal constructor(
    val android: com.google.firebase.storage.StorageMetadata
) {
    actual val cacheControl = android.getCacheControl()
    actual val contentDisposition = android.getContentDisposition()
    actual val contentEncoding = android.getContentEncoding()
    actual val contentLanguage = android.getContentLanguage()
    actual val contentType = android.getContentType()
    //    actual val customMetadata = android.getContentDisposition()
    actual val md5Hash = android.getMd5Hash()
}


actual typealias Uri = android.net.Uri


actual typealias FirebaseStorageException = StorageException


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