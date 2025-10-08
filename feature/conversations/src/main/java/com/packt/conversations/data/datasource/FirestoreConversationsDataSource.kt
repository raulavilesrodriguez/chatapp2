package com.packt.conversations.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.packt.data.model.ChatMetadataFirestore
import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreConversationsDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

    suspend fun getUser(uid: String): UserData? =
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .get().await().toObject()

    fun getConversations() : Flow<List<ChatMetadata>> = callbackFlow {
        if (currentUserId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val chatRef = firestore.collection(CHATS_COLLECTION)
        val query = chatRef
            .whereArrayContains(PARTICIPANTS, currentUserId)
            .orderBy(ORDER_BY_FIELD, Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }
            val conversations = snapshot?.documents?.mapNotNull { doc ->
                val conversation = doc.toObject(ChatMetadataFirestore::class.java)
                conversation?.toChatMetadata()
            } ?: emptyList()
            trySend(conversations).isSuccess
        }

        awaitClose{ listener.remove() }

    }

    companion object {
        private const val CHATS_COLLECTION = "chats"
        private const val PARTICIPANTS = "participants"
        private const val ORDER_BY_FIELD = "updatedAt"
        private const val USERS_COLLECTION = "users"
    }
}