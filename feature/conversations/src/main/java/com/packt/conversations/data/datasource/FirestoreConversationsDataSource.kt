package com.packt.conversations.data.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.packt.data.model.ChatMetadataFirestore
import com.packt.data.model.UserConversationsFirestore
import com.packt.data.model.toUserConversations
import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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
        val userConversationsRef = firestore.collection(USERS_COLLECTION)
            .document(currentUserId)
            .collection(USER_CONVERSATIONS_COLLECTION)
            .orderBy(ORDER_BY_FIELD, Query.Direction.DESCENDING) // más viejos final

        val listener = userConversationsRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.w("FirestoreUserConversations", "Listen failed.", exception)
                close(exception)
                return@addSnapshotListener
            }

            val userConversations = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(UserConversationsFirestore::class.java)?.toUserConversations()
            } ?: emptyList()
            Log.d("userConversations", "Chat con chatId LOCO: $userConversations")
            launch {
                val fullChatMetadataList = userConversations.mapNotNull { userConv ->
                    Log.d("FirestoreConvDS", "Chat con chatId: $userConv")
                    try {
                        if (userConv.chatId.isBlank()) return@mapNotNull null
                        val id = userConv.chatId
                        // Por cada 'chatId', vamos a buscar su documento en la colección '/chats'.
                        val chatDoc = firestore.collection(CHATS_COLLECTION)
                            .document(id)
                            .get()
                            .await()

                        if (chatDoc.exists()) {
                            // Mapeamos el documento del chat a nuestro modelo de datos
                            val chatMetadata = chatDoc.toObject(ChatMetadataFirestore::class.java)
                                ?.toChatMetadata()
                            chatMetadata
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("FirestoreConvDS", "Error fetching details for chat: ${userConv.chatId}", e)
                        null
                    }
                }
                val listChatMetadataSorted = fullChatMetadataList.sortedByDescending { it.updatedAt }
                trySend(listChatMetadataSorted).isSuccess
            }
        }
        awaitClose{ listener.remove() }
    }

    companion object {
        private const val CHATS_COLLECTION = "chats"
        private const val ORDER_BY_FIELD = "updatedAt"
        private const val USERS_COLLECTION = "users"
        private const val USER_CONVERSATIONS_COLLECTION = "conversations"
    }
}