@file:JvmName("android")
package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp

actual val Firebase.storage get() =
    FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance())

actual fun Firebase.storage(app: FirebaseApp): FirebaseStorage {
    return FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance(app.android))
}

actual class FirebaseStorage(val android: com.google.firebase.storage.FirebaseStorage) {

    actual val maxOperationRetryTimeMillis = android.getMaxUploadRetryTimeMillis()
    actual val maxUploadRetryTimeMillis = android.getMaxOperationRetryTimeMillis()

    actual fun getReference(url: String) = StorageReference(android.getReference(url))

    actual fun setMaxOperationRetryTimeMillis(maxTransferRetryMillis: Long) = android.setMaxOperationRetryTimeMillis(maxTransferRetryMillis)
    actual fun setMaxUploadRetryTimeMillis(maxTransferRetryMillis: Long) = android.setMaxUploadRetryTimeMillis(maxTransferRetryMillis)
}
