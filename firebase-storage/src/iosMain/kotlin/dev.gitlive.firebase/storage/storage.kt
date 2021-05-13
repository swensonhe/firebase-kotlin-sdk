package dev.gitlive.firebase.storage

import cocoapods.FirebaseStorage.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSError

actual val Firebase.storage get() =
    FirebaseStorage(FIRStorage.storage())

actual fun Firebase.storage(url: String) =
    FirebaseStorage(FIRStorage.storageWithURL(url))

actual fun Firebase.storage(app: FirebaseApp) =
    FirebaseStorage(FIRStorage.storageForApp(app.ios))

actual fun Firebase.storage(app: FirebaseApp, url: String) =
    FirebaseStorage(FIRStorage.storageForApp(app.ios, url))

actual class FirebaseStorage(val ios: FIRStorage) {

    actual val maxOperationRetryTimeMillis = ios.maxOperationRetryTime.toLong()
    actual val maxUploadRetryTimeMillis = ios.maxUploadRetryTime.toLong()

    actual fun getReference(url: String) = StorageReference(ios.referenceForURL(url))

    actual fun setMaxOperationRetryTimeMillis(maxTransferRetryMillis: Long) {
        ios.maxOperationRetryTime = maxTransferRetryMillis.toDouble()
    }
    actual fun setMaxUploadRetryTimeMillis(maxTransferRetryMillis: Long) {
        ios.maxUploadRetryTime = maxTransferRetryMillis.toDouble()
    }
}


actual class StorageReference internal constructor(
    val ios: FIRStorageReference
) {

    actual val bucket: String
        get() = ios.bucket
    actual val name: String
        get() = ios.name
    actual val parent: StorageReference
        get() = StorageReference(ios.parent()!!) // TODO: Find safer way for this
    actual val path: String
        get() = ios.fullPath
    actual val root: StorageReference
        get() = StorageReference(ios.root())
    actual val storage: FirebaseStorage
        get() = FirebaseStorage(ios.storage)

    actual fun child(path: String): StorageReference = StorageReference(ios.child(path))

    actual suspend fun delete() = await { ios.deleteWithCompletion { Unit } }

    actual suspend fun getDownloadUrl(): String = awaitResult {
        ios.downloadURLWithCompletion(completion = { url, _ -> url?.absoluteString })
    }

    actual suspend fun getMetadata(): StorageMetadata = awaitResult {
        ios.metadataWithCompletion(completion = { metadata, _ -> metadata?.let { StorageMetadata(it) } })
    }

    actual suspend fun putFile(data: Any): Uplo

}


expect class DownloadTask {
    expect class TaskSnapshot internal constructor(
        val ios: FIRStorageDownloadTask
    ) {
        val bytesTransferred = ios.snapshot.progress
        val totalByteCount = ios.snapshot.progress

    }
}


expect class ListResult internal constructor(
    val ios: FIRStorageListResult
) {

    val items = ios.items()
    val pageToken = ios.pageToken()
    val prefixes = ios.prefixes()

}


expect class UploadTask internal constructor(
    val ios: FIRStorageUploadTask
){
    class TaskSnapshot {
        val bytesTransferred: Long
        val totalByteCount: Long
        val metadata: StorageMetadata
        val uploadSessionUri: String
    }
}



actual class StorageMetadata internal constructor(
    val ios: FIRStorageMetadata
) {

    val cacheControl = ios.cacheControl
    val contentDisposition = ios.contentDisposition
    val contentEncoding = ios.contentEncoding
    val contentLanguage = ios.contentLanguage
    val contentType = ios.contentType
    val customMetadata = ios.customMetadata
    val md5Hash = ios.md5Hash

}


actual class FirebaseStorageException(message: String, val code: StorageExceptionCode) : FirebaseException(message)

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

fun NSError.toException() = when(domain) {
    FIRStorageErrorDomain -> when(code) {
        FIRStorageErrorCodeUnknown -> StorageExceptionCode.UNKNOWN
        FIRStorageErrorCodeObjectNotFound -> StorageExceptionCode.OBJECT_NOT_FOUND
        FIRStorageErrorCodeBucketNotFound -> StorageExceptionCode.BUCKET_NOT_FOUND
        FIRStorageErrorCodeProjectNotFound -> StorageExceptionCode.PROJECT_NOT_FOUND
        FIRStorageErrorCodeQuotaExceeded -> StorageExceptionCode.QUOTA_EXCEEDED
        FIRStorageErrorCodeUnauthenticated -> StorageExceptionCode.UNAUTHENTICATED
        FIRStorageErrorCodeUnauthorized -> StorageExceptionCode.UNAUTHORIZED
        FIRStorageErrorCodeRetryLimitExceeded -> StorageExceptionCode.RETRY_LIMIT_EXCEEDED
        FIRStorageErrorCodeNonMatchingChecksum -> StorageExceptionCode.NON_MATCHING_CHECKSUM
        FIRStorageErrorCodeDownloadSizeExceeded -> StorageExceptionCode.DOWNLOAD_SIZE_EXCEEDED
        FIRStorageErrorCodeCancelled -> StorageExceptionCode.CANCELLED
        FIRStorageErrorCodeInvalidArgument -> StorageExceptionCode.INVALID_ARGUMENT
        else -> StorageExceptionCode.UNKNOWN
    }
    else -> StorageExceptionCode.UNKNOWN
}.let { FirebaseStorageException(description!!, it) }


/*
    Helper methods
 */
suspend inline fun <T> await(function: (callback: (NSError?) -> Unit) -> T): T {
    val job = CompletableDeferred<Unit>()
    val result = function { error ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException()) // TODO: Handle error correctly
        }
    }
    job.await()
    return result
}

suspend inline fun <reified T> awaitResult(function: (callback: (T?, NSError?) -> Unit) -> Unit): T {
    val job = CompletableDeferred<T?>()
    function { result, error ->
        if(error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    return job.await() as T
}