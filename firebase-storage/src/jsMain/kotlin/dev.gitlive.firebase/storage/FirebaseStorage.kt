package dev.gitlive.firebase.storage

import dev.gitlive.firebase.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.promise

actual val Firebase.storage get() =
    rethrow { dev.gitlive.firebase.storage; FirebaseStorage(firebase.app().storage()) }

actual fun Firebase.storage(app: FirebaseApp) =
    rethrow { dev.gitlive.firebase.storage; FirebaseStorage(firebase.app().storage()) }

actual class FirebaseStorage(val js: firebase.storage.Storage) {

    actual val maxOperationRetryTimeMillis = js.maxOperationRetryTime
    actual val maxUploadRetryTimeMillis = js.maxUploadRetryTime

    actual fun getReference(url: String) = rethrow { StorageReference(js.ref(url)) }

    actual fun setMaxOperationRetryTimeMillis(maxTransferRetryMillis: Long) = js.setMaxOperationRetryTime(maxTransferRetryMillis)
    actual fun setMaxUploadRetryTimeMillis(maxTransferRetryMillis: Long) = js.setMaxUploadRetryTime(maxTransferRetryMillis)
}

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