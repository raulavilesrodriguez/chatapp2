package com.packt.chat.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.packt.chat.data.model.FirestoreMessageModel
import com.packt.chat.domain.models.Message
import com.packt.data.model.ChatMetadataFirestore
import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreChatsDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

    suspend fun getUser(uid: String): UserData? =
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .get().await().toObject()

    fun getMessages(chatId: String, userId: String): Flow<List<Message>> = callbackFlow {

        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)
            .collection(MESSAGES_COLLECTION)

        // Create a query to get the messages ordered by timestamp (ascending)
        val query = chatRef
            //.whereNotEqualTo(FILTER_BY_FIELD, null)
            .orderBy(ORDER_BY_FIELD, Query.Direction.ASCENDING)

        // Add a snapshot listener to the query to listen for real-time updates
        val listenerRegistration = query.addSnapshotListener { snapshot, exception ->
            // If there's an exception, close the Flow with the exception
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            // Convert the snapshot to a list of Message objects
            val messages = snapshot?.documents?.mapNotNull { doc ->
                val message = doc.toObject(FirestoreMessageModel::class.java)
                message?.copy(doc.id)
            } ?: emptyList()

            val domainMessages = messages.map { it.toDomain(userId, chatId) }

            // Emit the list of messages to the Flow
            trySend(domainMessages).isSuccess
        }

        // When the Flow is no longer needed, remove the snapshot listener
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun sendMessage(chatId: String, message: Message){
        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)
        val messagesRef = chatRef.collection(MESSAGES_COLLECTION)
        val messageModel = FirestoreMessageModel.fromDomain(message)
        messagesRef.add(messageModel).await()

        // update metadata of the chat
        val updates = mapOf(
            "lastMessage" to message.content,
            "lastMessageSenderId" to message.senderId,
            "lastMessageType" to message.contentType.name,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        chatRef.update(updates).await()
    }

    suspend fun getInitialChatRoomInfo(chatId:String): ChatMetadata? {
        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)
        val snapshot = chatRef.get().await()
        if (snapshot.exists()) {
            val metadataFirestore = snapshot.toObject(ChatMetadataFirestore::class.java)
            val metaData = metadataFirestore?.toChatMetadata()
            return metaData
        } else {
            return null
        }
    }

    companion object {
        private const val CHATS_COLLECTION = "chats"
        private const val MESSAGES_COLLECTION = "messages"
        private const val ORDER_BY_FIELD = "timestamp"
        private const val FILTER_BY_FIELD = "lastMessage"
        private const val USERS_COLLECTION = "users"
    }
}