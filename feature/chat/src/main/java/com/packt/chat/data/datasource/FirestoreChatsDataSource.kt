package com.packt.chat.data.datasource

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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

    fun getMessages(chatId: String, userId: String, since:Timestamp): Flow<List<Message>> = callbackFlow {

        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)
            .collection(MESSAGES_COLLECTION)

        // Create a query to get the messages ordered by timestamp (ascending)
        val query = chatRef
            .orderBy(ORDER_BY_FIELD, Query.Direction.ASCENDING)
            .whereGreaterThan(ORDER_BY_FIELD, since)

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

            val domainMessages = messages.map {
                it.toDomain(userId, chatId).copy(firestoreTimestamp = it.timestamp)
            }

            // Emit the list of messages to the Flow
            trySend(domainMessages).isSuccess
        }

        // When the Flow is no longer needed, remove the snapshot listener
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getMessagesPaged(
        chatId: String,
        userId: String,
        pageSize: Long,
        lastDocument: DocumentSnapshot? = null
    ) : Pair<List<Message>, DocumentSnapshot?>  {

        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)
            .collection(MESSAGES_COLLECTION)

        var query = chatRef
            .orderBy(ORDER_BY_FIELD, Query.Direction.DESCENDING) // más viejos final
            .limit(pageSize)

        // continue the consult from the last document
        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }

        val snapshot = query.get().await()

        // Convert the snapshot to a list of Message objects
        val messages = snapshot.documents.mapNotNull { doc ->
            val message = doc.toObject(FirestoreMessageModel::class.java)
            message?.copy(doc.id)
        }

        val domainMessages = messages.map {
            it.toDomain(userId, chatId).copy(firestoreTimestamp = it.timestamp)
        }
        val lastVisible = snapshot?.documents?.lastOrNull()

        return domainMessages to lastVisible
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

    fun observeUser(uid: String): Flow<UserData?> = callbackFlow {
        val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)

        val listener = userDocRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                close(exception) // Cierra el Flow con error
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                // Emite el nuevo objeto UserData cada vez que cambia
                trySend(snapshot.toObject(UserData::class.java)).isSuccess
            } else {
                trySend(null).isSuccess // Emite null si el usuario no existe
            }
        }
        // Cuando el Flow se cancela, se elimina el listener
        awaitClose { listener.remove() }
    }

    companion object {
        private const val CHATS_COLLECTION = "chats"
        private const val MESSAGES_COLLECTION = "messages"
        private const val ORDER_BY_FIELD = "timestamp"
        private const val USERS_COLLECTION = "users"
    }
}