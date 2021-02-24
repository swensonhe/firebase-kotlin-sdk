package dev.gitlive.firebase.storage

import cocoapods.FirebaseStorage.FIRStorageReference

actual class StorageReference internal constructor(
    val ios: FIRStorageReference
) {
//    actual val bucket: String get() = rethrow { js. }
//    actual val name: String get() = rethrow { js.name }
//    actual val parent: StorageReference get() = rethrow { js.parent }
//    actual val path: String get() = rethrow { js.fullPath }
//    actual val root: StorageReference get() = rethrow { js.root }
//    actual val storage: FirebaseStorage get() = rethrow { js.storage }
//
//    actual fun child() = js
}