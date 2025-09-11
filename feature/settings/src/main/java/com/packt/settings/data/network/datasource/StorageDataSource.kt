package com.packt.settings.data.network.datasource

import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StorageDataSource @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) {
    suspend fun uploadPhoto(localPhoto: String, remotePath: String) {

        val storageRef = firebaseStorage.reference.child(remotePath)

        storageRef.putFile(localPhoto.toUri()).await()

    }

    suspend fun downloadUrlPhoto(remotePath: String) : String {

        val storageRef = firebaseStorage.reference.child(remotePath)

        val downloadUrl = storageRef.downloadUrl.await().toString()
        
        return downloadUrl

    }
}