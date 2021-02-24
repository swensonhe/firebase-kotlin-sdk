package dev.gitlive.firebase.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationStrategy

expect class StorageReference {
//
//    val bucket: String
//    val name: String
//    val parent: StorageReference
//    val path: String
//    val root: StorageReference
//    val storage: FirebaseStorage
//
//    fun child(pathString: String): StorageReference
//    suspend fun delete(): Unit
//
//    suspend fun getDownloadUrl(): String
//    suspend fun getMetadata(): String
//
//    suspend fun list(other: StorageReference):  List<T>
//    suspend fun listAll(other: StorageReference): List<T>
//
//    suspend fun <T> put(uri: String, metadata: StorageMetadata?, existingUploadUri: String?)
//    suspend fun <T> updateMeta(metadata: StorageMetadata)
//
}