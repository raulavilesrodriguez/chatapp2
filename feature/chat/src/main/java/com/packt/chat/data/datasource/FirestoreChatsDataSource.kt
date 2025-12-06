package com.packt.chat.data.datasource

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.packt.chat.data.model.FirestoreMessageModel
import com.packt.chat.domain.models.Message
import com.packt.data.model.ChatMetadataFirestore
import com.packt.data.model.UserContactsFirestore
import com.packt.data.model.UserConversationsFirestore
import com.packt.data.model.toUserContacts
import com.packt.domain.model.ChatMetadata
import com.packt.domain.user.UserData
import com.packt.ui.ext.normalizeName
import com.packt.ui.snackbar.SnackbarManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
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

    fun getMessages(
        chatId: String,
        userId: String,
        since:Timestamp
    ): Flow<List<Message>> = callbackFlow {

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
                doc.toObject(FirestoreMessageModel::class.java)?.copy(id = doc.id)
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

        val userConversationDoc = firestore
            .collection(USERS_COLLECTION).document(userId)
            .collection(USER_CONVERSATIONS_COLLECTION).document(chatId)
            .get().await()

        val clearedTimestamp = if(userConversationDoc.exists()){
            userConversationDoc.toObject(UserConversationsFirestore::class.java)?.clearedTimestamp
        }else{
            null
        }

        // to show los mensajes desde clearedTimestamp
        if (clearedTimestamp != null){
            query = query.whereGreaterThan(ORDER_BY_FIELD, clearedTimestamp)
        }

        // continue the consult from the last document
        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }

        val snapshot = query.get().await()

        // Convert the snapshot to a list of Message objects
        val messages = snapshot.documents.mapNotNull { doc ->
            val message = doc.toObject(FirestoreMessageModel::class.java)
            message?.copy(id = doc.id)
        }

        val domainMessages = messages.map {
            it.toDomain(userId, chatId).copy(firestoreTimestamp = it.timestamp)
        }
        val lastVisible = snapshot?.documents?.lastOrNull()

        return domainMessages to lastVisible
    }

    suspend fun sendMessage(chatId: String, message: Message, participants: List<String>){
        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)
        val messagesRef = chatRef.collection(MESSAGES_COLLECTION)
        val messageModel = FirestoreMessageModel.fromDomain(message)
        //messagesRef.add(messageModel).await()

        // para asegurar que todo acurra de manera atomica todo al mismo tiempo
        firestore.runTransaction { transaction ->
            val otherParticipantsIds = participants.filter { it != currentUserId }
            val recipientDocs = otherParticipantsIds.map {
                val userRef = firestore.collection(USERS_COLLECTION).document(it)
                transaction.get(userRef)
            }

            // update ChatMetadataFirestore
            val updates = mutableMapOf(
                "lastMessage" to message.content,
                "lastMessageSenderId" to message.senderId,
                "lastMessageType" to message.contentType.name,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            recipientDocs.forEach { userDoc ->
                val recipient = userDoc.toObject(UserData::class.java)
                val recipientId = userDoc.id
                //  Si el destinatario NO está en este chat, incrementa su contador.
                if(recipient?.activeInChatId != chatId){
                    // notacion de punto (dot) para actualizar un campo anidado en un mapa
                    // unreadCount es un mapa por eso se usa el punto
                    updates["unreadCount.$recipientId"] = FieldValue.increment(1)
                }
                val userConversationsRef = firestore.collection(USERS_COLLECTION)
                    .document(userDoc.id)
                    .collection(USER_CONVERSATIONS_COLLECTION)
                    .document(chatId)

                val userConvSnapshot = transaction.get(userConversationsRef)
                if(!userConvSnapshot.exists()){
                    // tiempo currentUser
                    val now = Date()
                    //    para asegurar que el nuevo mensaje siempre sea 'posterior'.
                    val clearedTimestampWithOffset = Timestamp(Date(now.time - 100))

                    val userConversationsUpdate = mutableMapOf(
                        "chatId" to chatId,
                        "clearedTimestamp" to clearedTimestampWithOffset,
                        "blocked" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    transaction.set(userConversationsRef, userConversationsUpdate, SetOptions.merge())
                } else {
                    transaction.update(userConversationsRef, "updatedAt", FieldValue.serverTimestamp())
                }
            }
            // Add the new message to the messages collection
            transaction.set(messagesRef.document(), messageModel)
            transaction.update(chatRef, updates)
        }.await()
    }

    suspend fun updateGroupChatDetails(
        chatId: String,
        newGroupName: String?,
        newGroupPhotoUrl: String?
        ) {
        if (chatId.isBlank() || (newGroupName == null && newGroupPhotoUrl == null)) {
            Log.w("FirestoreChatsDS", "No hay datos para actualizar o el chatId está vacío.")
            return
        }
        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)

        val updates = mutableMapOf<String, Any?>()
        newGroupName?.let { updates["groupName"] = it }
        newGroupPhotoUrl?.let { updates["groupPhotoUrl"] = it }
        updates["updatedAt"] = FieldValue.serverTimestamp()

        try {
            chatRef.update(updates).await()
        } catch (e: Exception) {
            Log.e("FirestoreChatsDS", "Error al actualizar detalles del grupo $chatId", e)
            throw e
        }
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

    fun observeChatMetadata(chatId: String): Flow<ChatMetadata?> = callbackFlow {
        if(chatId.isEmpty()){
            trySend(null)
            close()
            return@callbackFlow
        }

        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)

        val listener = chatRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.w("FirestoreChatsDS", "Listen failed $chatId", exception)
                close(exception) // Cierra el Flow con error
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val metadataFirestore = snapshot.toObject(ChatMetadataFirestore::class.java)
                val metaData = metadataFirestore?.toChatMetadata()
                trySend(metaData).isSuccess
            } else {
                trySend(null).isSuccess // Emite null si el chat no existe
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun resetUnreadCount(chatId: String) {
        if (currentUserId.isEmpty()) return

        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)
        val update = mapOf("unreadCount.$currentUserId" to 0)

        try {
            chatRef.update(update).await()
        } catch (e: Exception) {
            Log.e("FirestoreChatsDS", "Error resetting unread count for chat $chatId", e)
        }
    }

    suspend fun setUserActiveInChat(chatId: String){
        if(currentUserId.isEmpty()) return

        firestore.collection(USERS_COLLECTION).document(currentUserId)
            .update(UPDATE_FIELD, chatId).await()
    }

    suspend fun clearUserActiveStatus(chatId: String){
        if(currentUserId.isEmpty() || chatId.isEmpty()) return

        val userRef = firestore.collection(USERS_COLLECTION)
            .document(currentUserId)

        firestore.runTransaction { transaction ->
            val userSnapshot = transaction.get(userRef)
            val currentUserData = userSnapshot.toObject(UserData::class.java)

            if(currentUserData?.activeInChatId == chatId){
                transaction.update(userRef, UPDATE_FIELD, null)
            }
        }.await()
    }

    suspend fun deleteChatForCurrentUser(chatId: String){
        if(currentUserId.isEmpty()) return

        firestore.collection(USERS_COLLECTION).document(currentUserId)
            .collection(USER_CONVERSATIONS_COLLECTION)
            .document(chatId).delete().await()
    }

    suspend fun addUsersToGroup(chatId: String, usersToAdd: List<String>){
        if(usersToAdd.isEmpty()) return
        // referencia al chat
        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)

        firestore.runTransaction { transaction ->
            val chatSnapshot = transaction.get(chatRef)
            val chatData = chatSnapshot.toObject(ChatMetadataFirestore::class.java)

            if(chatData == null){
                SnackbarManager.showMessage("El chat con ID $chatId no existe. No se pueden añadir usuarios.")
                return@runTransaction
            }
            val currentParticipants = chatData.participants.orEmpty().toSet()
            val newParticipants = usersToAdd.filter { !currentParticipants.contains(it) }

            if(newParticipants.isEmpty()){
                SnackbarManager.showMessage("No hay usuarios nuevos para añadir.")
                return@runTransaction
            }
            // arrayUnion con una lista de argumentos variables (*newParticipants.toTypedArray())
            transaction.update(chatRef,
                "participants", FieldValue.arrayUnion(*newParticipants.toTypedArray()))
            transaction.update(chatRef, "updatedAt", FieldValue.serverTimestamp())

            newParticipants.forEach { userId ->
                val userConvRef = firestore.collection(USERS_COLLECTION).document(userId)
                    .collection(USER_CONVERSATIONS_COLLECTION).document(chatId)

                val userConversationsUpdate = mutableMapOf(
                    "chatId" to chatId,
                    "clearedTimestamp" to FieldValue.serverTimestamp(),
                    "blocked" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                transaction.set(userConvRef, userConversationsUpdate)
            }
        }.await()
    }

    suspend fun leftUserFromGroup(chatId: String){
        if(currentUserId.isEmpty()) return
        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)
        val userConvRef = firestore.collection(USERS_COLLECTION).document(currentUserId)
            .collection(USER_CONVERSATIONS_COLLECTION).document(chatId)

        try {
            chatRef.update(
                "participants", FieldValue.arrayRemove(currentUserId),
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
            userConvRef.delete().await()
            // revisar si chat quedo vacio
            val finalChatSnapshot = chatRef.get().await()
            val remainingParticipants = finalChatSnapshot.get("participants") as? List<*>
            if (remainingParticipants.isNullOrEmpty()) {
                chatRef.delete().await()
            }
        } catch (e: Exception){
            Log.e("FirestoreChatsDS", "Error al salir del grupo $currentUserId", e)
        }
    }

    fun getUsers(): Flow<List<UserData>> = callbackFlow {
        val query = firestore.collection(USERS_COLLECTION)
            .orderBy(ORDER_BY_FIELD_USERS, Query.Direction.ASCENDING) // ASCENDING para orden alfabético A-Z

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
            .orderBy(ORDER_BY_FIELD_LOWER_USERS, Query.Direction.ASCENDING)
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

    fun searchContacts(namePrefix: String): Flow<List<UserData>> = callbackFlow {
        val contactRef = firestore.collection(USERS_COLLECTION)
            .document(currentUserId)
            .collection(USER_CONTACTS_COLLECTION)

        val listener = contactRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreUserContacts SEARCH", "Listen of SEARCH failed CONTACTOS.", error)
                close(error)
                return@addSnapshotListener
            }
            val userContacts = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(UserContactsFirestore::class.java)?.toUserContacts()
            } ?: emptyList()

            if(userContacts.isEmpty()){
                trySend(emptyList()).isSuccess
                return@addSnapshotListener
            }

            launch {
                val usersTotal: MutableList<UserData> = mutableListOf()
                val currentUser = getUser(currentUserId)
                if(currentUser != null) usersTotal.add(currentUser)

                val fullUserDataList = userContacts.mapNotNull { userContact ->
                    try {
                        val uid = userContact.uid
                        val userDoc = firestore.collection(USERS_COLLECTION)
                            .document(uid)
                            .get()
                            .await()
                        if(userDoc.exists()){
                            val user = userDoc.toObject(UserData::class.java)
                            user
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("FirestorUserContacts", "Error fetching CONTACTOS for chat: ${userContact.uid}", e)
                        null
                    }
                }
                usersTotal.addAll(fullUserDataList)
                val filteredContacts = if(namePrefix.isBlank()){
                    usersTotal
                } else {
                    val normalizedPrefix = namePrefix.normalizeName()
                    usersTotal.filter { it.nameLowercase?.startsWith(normalizedPrefix) == true }
                }
                val sortedFilteredContacts = filteredContacts.sortedBy { it.nameLowercase }
                trySend(sortedFilteredContacts).isSuccess
            }
        }
        awaitClose{ listener.remove() }
    }

    fun getContacts(): Flow<List<UserData>> = callbackFlow {
        val contactRef = firestore.collection(USERS_COLLECTION)
            .document(currentUserId)
            .collection(USER_CONTACTS_COLLECTION)
        val listener = contactRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreUserContacts", "Listen failed CONTACTOS.", error)
                close(error)
                return@addSnapshotListener
            }
            val userContacts = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(UserContactsFirestore::class.java)?.toUserContacts()
            } ?: emptyList()
            launch {
                val usersTotal: MutableList<UserData> = mutableListOf()
                val currentUser = getUser(currentUserId)
                if(currentUser != null) usersTotal.add(currentUser)

                val fullUserDataList = userContacts.mapNotNull { userContact ->
                    try {
                        val uid = userContact.uid
                        val userDoc = firestore.collection(USERS_COLLECTION)
                            .document(uid)
                            .get()
                            .await()
                        if(userDoc.exists()){
                            val user = userDoc.toObject(UserData::class.java)
                            user
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("FirestorUserContacts", "Error fetching CONTACTOS for chat: ${userContact.uid}", e)
                        null
                    }
                }
                usersTotal.addAll(fullUserDataList)
                val fullUserDataListSorted = usersTotal.sortedBy { it.nameLowercase }
                trySend(fullUserDataListSorted).isSuccess
            }
        }
        awaitClose{ listener.remove() }
    }

    companion object {
        private const val CHATS_COLLECTION = "chats"
        private const val MESSAGES_COLLECTION = "messages"
        private const val ORDER_BY_FIELD = "timestamp"
        private const val USERS_COLLECTION = "users"
        private const val UPDATE_FIELD = "activeInChatId"
        private const val USER_CONVERSATIONS_COLLECTION = "conversations"
        private const val ORDER_BY_FIELD_USERS = "name"
        private const val ORDER_BY_FIELD_LOWER_USERS = "nameLowercase"
        private const val USER_CONTACTS_COLLECTION = "contacts"
    }
}