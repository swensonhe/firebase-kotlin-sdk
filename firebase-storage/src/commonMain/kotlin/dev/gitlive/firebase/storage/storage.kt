package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.flow.Flow


/** Returns the [FirebaseStorage] instance of the default [FirebaseApp]. */
expect val Firebase.storage: FirebaseStorage


/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
expect fun Firebase.storage(url: String): FirebaseStorage


/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
expect fun Firebase.storage(app: FirebaseApp): FirebaseStorage


/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
expect fun Firebase.storage(app: FirebaseApp, url: String): FirebaseStorage


expect class FirebaseStorage {
    val maxOperationRetryTimeMillis: Long
    val maxUploadRetryTimeMillis: Long

    fun getReference(url: String): StorageReference
    fun setMaxOperationRetryTimeMillis(maxTransferRetryMillis: Long)
    fun setMaxUploadRetryTimeMillis(maxTransferRetryMillis: Long)
}


expect class StorageReference {
    val bucket: String
    val name: String
    val parent: StorageReference
    val path: String
    val root: StorageReference
    val storage: FirebaseStorage

    fun child(path: String): StorageReference
    suspend fun delete(): Unit

    suspend fun getDownloadUrl(): String
    suspend fun getMetadata(): StorageMetadata

    suspend fun getBytes(maxDownloadSizeBytes: Long): DownloadBytesTask
    suspend fun getFile(destinationFile: Uri): DownloadFileTask
    suspend fun putFile(data: Any): UploadTask
}


expect class DownloadBytesTask {
    suspend fun onSuccess(): Any
    suspend fun onFailed(): FirebaseStorageException
}


expect class DownloadFileTask {
    suspend fun onSuccess(): Any
    suspend fun onFailed(): FirebaseStorageException
}


expect class DownloadFileTaskSnapshot {
    fun getBytesTransferred(): Long
    fun getTotalByteCount(): Long
}


expect class UploadTask {
    fun cancel(): Boolean
    fun pause(): Boolean
    fun resume(): Boolean

    suspend fun onProgress(): Flow<UploadTaskSnapshot>
    suspend fun onPaused(): StorageMetadata
    suspend fun onFailed(): FirebaseStorageException
    suspend fun onComplete(): StorageMetadata
}


expect class UploadTaskSnapshot {
    fun getBytesTransferred(): Long
    fun getTotalByteCount(): Long
    fun getMetadata(): StorageMetadata
}


expect class StorageMetadata {
    val cacheControl: String
    val contentDisposition: String
    val contentEncoding: String
    val contentLanguage: String
    val contentType: String
    //    val customMetadata: String
    val md5Hash: String
}


expect class Uri


expect class FirebaseStorageException: FirebaseException


@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
expect val FirebaseStorageException.code: StorageExceptionCode


expect enum class StorageExceptionCode {
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
    INVALID_ARGUMENT
}