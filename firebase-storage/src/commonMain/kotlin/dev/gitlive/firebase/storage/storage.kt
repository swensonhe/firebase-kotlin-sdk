package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

/*
 Classes to complete:

 [X] - Storage (Kotlin, Objective-C, JS)
 [ ] - DownloadTask (Kotlin, Objective-C, JS)
 [ ] - ListResult (Kotlin, Objective-C, JS)
 [X] - Metadata (Kotlin, Objective-C, JS)
 [ ] - ObservableTask (Kotlin, Objective-C, JS)
 [X] - Reference (Kotlin, Objective-C, JS)
 [ ] - Task (Kotlin, Objective-C, JS)
 [ ] - TaskSnapshot (Kotlin, Objective-C, JS)
 [ ] - UploadTask (Kotlin, Objective-C, JS)
 */

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

//    suspend fun getFile(data: Any): DownloadTask

    suspend fun putFile(data: Any): UploadTask
}


//expect class DownloadTask {
//    class TaskSnapshot {
//        val bytesTransferred: Long
//        val totalByteCount: Long
//    }
//}


expect class ListResult {
    val items: List<StorageReference>
    val pageToken: String
    val prefixes: List<StorageReference>
}


expect class UploadTask {
    class TaskSnapshot {
        val bytesTransferred: Long
        val totalByteCount: Long
        val metadata: StorageMetadata
        val uploadSessionUri: String
    }
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