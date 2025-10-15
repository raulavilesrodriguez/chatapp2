package com.packt.settings.data.network.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.packt.domain.user.UserData
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    suspend fun save(user: UserData) {
        firestore.collection(USERS_COLLECTION)
            .document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): UserData? =
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .get().await().toObject()



    suspend fun delete(uid: String) {
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .delete().await()
    }

    suspend fun update(user: UserData) {
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(user).await()
    }

    suspend fun storeFCMToken(uid: String, token: String){
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .update(FCM_TOKEN_FIELD, token)
            .await()
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val FCM_TOKEN_FIELD = "fcmToken"
    }
}

