package com.packt.create_chat.data.datasource

import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.packt.domain.user.UserData
import com.packt.ui.ext.normalizeName
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreUsersDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) {
    val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

    suspend fun getUser(uid: String): UserData? =
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .get().await().toObject()

    fun getUsers(): Flow<List<UserData>> = callbackFlow {
        val query = firestore.collection(USERS_COLLECTION)
            .orderBy(ORDER_BY_FIELD, Query.Direction.ASCENDING) // ASCENDING para orden alfabético A-Z

        val listenerRegistration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { document ->
                document.toObject(UserData::class.java)
            } ?: emptyList()
            trySend(users).isSuccess
        }

        awaitClose { listenerRegistration.remove() }
    }

    fun searchUsers(namePrefix: String): Flow<List<UserData>> = callbackFlow {

        val query = firestore.collection(USERS_COLLECTION)
            .orderBy(ORDER_BY_FIELD_LOWER, Query.Direction.ASCENDING)
            .startAt(namePrefix.normalizeName())
            .endAt(namePrefix.normalizeName() + "\uf8ff")

        val listenerRegistration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { document ->
                document.toObject(UserData::class.java)
            } ?: emptyList()
            trySend(users).isSuccess
        }

        awaitClose { listenerRegistration.remove() }

    }

    suspend fun createChat(participants: List<String>, isGroup: Boolean = false): String {
        val chatId = if(isGroup){
            // id unico and random
            firestore.collection(CHATS_COLLECTION).document().id
        } else {
            // to chats 1 to 1
            if (participants.size == 1) {
                "${participants[0]}_self"
            } else {
                participants.sorted().joinToString("_")
            }
        }

        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)
        val snapshot = chatRef.get().await()

        if(!snapshot.exists()){
            val chatData = hashMapOf(
                "chatId" to chatId,
                "participants" to participants,
                "lastMessage" to null,
                "updatedAt" to null,
                "lastMessageSenderId" to null,
                "lastMessageType" to null,
                "unreadCount" to emptyMap<String, Int>(),
                "createdAt" to FieldValue.serverTimestamp(),
                "isGroup" to isGroup
            )
            chatRef.set(chatData).await()
        }
        return chatId
    }

    suspend fun updateChatInfo(
        chatId: String,
        groupName: String? = null,
        groupPhotoUrl: String? = null
    ){
        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)

        // update ChatMetadataFirestore
        val updates = mutableMapOf<String, Any?>()
        groupName?.let { updates["groupName"] = it }
        groupPhotoUrl?.let { updates["groupPhotoUrl"] = it }
        updates["updatedAt"] = FieldValue.serverTimestamp()

        if(updates.keys.size > 1){
            chatRef.update(updates).await()
        }
    }

    suspend fun chatExists(participants: List<String>, groupName: String?): Boolean{
        val querySnapshot = firestore.collection(CHATS_COLLECTION)
            .whereEqualTo("isGroup", true)
            .whereArrayContains("participants", currentUserId)
            .whereEqualTo("groupName", groupName)
            .get()
            .await()

        // Si no se encontró ningún grupo con ese nombre, podemos crearlo.
        if (querySnapshot.isEmpty) {
            return false
        }
        // Usamos Sets para comparar sin importar el orden.
        val newParticipantsSet = participants.toSet()

        // 'any' itera sobre los documentos encontrados y devuelve 'true' si la condición se cumple para al menos uno.
        return querySnapshot.documents.any { document ->
            // Obtenemos la lista de participantes del documento de la base de datos.
            val existingParticipants = document.get("participants") as? List<*>
            if (existingParticipants != null) {
                // Comparamos si el Set de participantes es idéntico.
                existingParticipants.toSet() == newParticipantsSet
            } else {
                false
            }
        }
    }

    suspend fun uploadPhoto(localPhoto: String, remotePath: String) {

        val storageRef = firebaseStorage.reference.child(remotePath)

        storageRef.putFile(localPhoto.toUri()).await()

    }

    suspend fun downloadUrlPhoto(remotePath: String) : String {

        val storageRef = firebaseStorage.reference.child(remotePath)

        val downloadUrl = storageRef.downloadUrl.await().toString()

        return downloadUrl

    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val ORDER_BY_FIELD = "name"
        private const val ORDER_BY_FIELD_LOWER = "nameLowercase"
        private const val CHATS_COLLECTION = "chats"
    }
}