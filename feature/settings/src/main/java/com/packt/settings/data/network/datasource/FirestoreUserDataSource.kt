package com.packt.settings.data.network.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.packt.domain.user.UserData
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    suspend fun save(user: UserData) {
        val userMap = hashMapOf(
            "uid" to user.uid,
            "name" to user.name,
            "nameLowercase" to user.nameLowercase,
            "number" to user.number,
            "photoUrl" to user.photoUrl,
            "fcmToken" to user.fcmToken
        )
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(userMap, SetOptions.merge()).await()
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
        val userMap = hashMapOf(
            "uid" to user.uid,
            "name" to user.name,
            "nameLowercase" to user.nameLowercase,
            "number" to user.number,
            "photoUrl" to user.photoUrl,
            "fcmToken" to user.fcmToken
        )
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(userMap, SetOptions.merge()).await()
    }

    suspend fun storeFCMToken(uid: String, token: String){
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .update(FCM_TOKEN_FIELD, token)
            .await()
    }

    suspend fun clearFCMToken(uid: String){
        if (uid.isEmpty()) return
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        userRef.update(FCM_TOKEN_FIELD, null).await()
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val FCM_TOKEN_FIELD = "fcmToken"
    }
}

