package com.packt.create_chat.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.packt.domain.user.UserData
import com.packt.ui.ext.normalizeName
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreUsersDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

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

    suspend fun createChat(participants: List<String>): String {
        val chatId = participants.sorted().joinToString(separator = "_")
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
                "createdAt" to FieldValue.serverTimestamp()
            )
            chatRef.set(chatData).await()
        }
        return chatId
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val ORDER_BY_FIELD = "name"
        private const val ORDER_BY_FIELD_LOWER = "nameLowercase"
        private const val CHATS_COLLECTION = "chats"
        private const val MESSAGES_COLLECTION = "messages"
    }
}