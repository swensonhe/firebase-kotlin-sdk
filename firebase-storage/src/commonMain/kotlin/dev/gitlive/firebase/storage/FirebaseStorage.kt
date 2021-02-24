package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp

/** Returns the [FirebaseStorage] instance of the default [FirebaseApp]. */
expect val Firebase.storage: FirebaseStorage

/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
expect fun Firebase.storage(app: FirebaseApp): FirebaseStorage

expect class FirebaseStorage {
    val maxOperationRetryTimeMillis: Long
    val maxUploadRetryTimeMillis: Long

    fun getReference(url: String): StorageReference

    fun setMaxOperationRetryTimeMillis(maxTransferRetryMillis: Long)
    fun setMaxUploadRetryTimeMillis(maxTransferRetryMillis: Long)
}