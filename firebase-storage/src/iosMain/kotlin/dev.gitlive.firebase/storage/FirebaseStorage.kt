package dev.gitlive.firebase.storage

import cocoapods.FirebaseStorage.FIRStorage
import dev.gitlive.firebase.*

actual val Firebase.storage get() =
    FirebaseStorage(FIRStorage.storage())

actual fun Firebase.storage(app: FirebaseApp): FirebaseStorage {
    return FirebaseStorage(FIRStorage.storageForApp(app.ios))
}

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