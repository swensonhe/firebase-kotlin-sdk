@file:JvmName("android")
package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await

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

    actual suspend fun putFile(data: Any): UploadTask = UploadTask(android.putBytes(data as ByteArray))

}

//actual class DownloadTask internal constructor(
//    val android: com.google.firebase.storage.DownloadTask
//) {
//    actual class TaskSnapshot {
//        actual val bytesTransferred = android.getBytesTransferred()
//        actual val totalByteCount = android.getTotalByteCount()
//    }
//}


actual class UploadTask internal constructor(
    val uploadTask: com.google.firebase.storage.UploadTask
){
    actual class TaskSnapshot internal constructor(
        val taskSnapshot: com.google.firebase.storage.UploadTask.TaskSnapshot
    )
        actual val bytesTransferred = uploadTask.snapshot.bytesTransferred
        actual val totalByteCount = uploadTask.snapshot.totalByteCount
        actual val metadata = StorageMetadata(uploadTask.snapshot.metadata!!)
        actual val uploadSessionUri = uploadTask.snapshot.uploadSessionUri
    }
}


actual class ListResult internal constructor(
    val android: com.google.firebase.storage.ListResult
){
    actual val items = android.getItems()
    actual val pageToken = android.getPageToken()
    actual val prefixes = android.getPrefixes()
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


actual typealias FirebaseStorageException = com.google.firebase.storage.StorageException

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