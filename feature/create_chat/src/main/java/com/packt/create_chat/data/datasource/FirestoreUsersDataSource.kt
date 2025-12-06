package com.packt.create_chat.data.datasource

import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.packt.data.model.UserContactsFirestore
import com.packt.data.model.toUserContacts
import com.packt.domain.user.UserData
import com.packt.ui.ext.normalizeName
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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

    fun createChatId(
        participants: List<String>,
        isGroup: Boolean = false,
    ): String{
        return if(isGroup){
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
    }

    suspend fun createChat(
        participants: List<String>,
        chatId: String,
        isGroup: Boolean = false,
        groupName: String? = null,
        groupPhotoUrl: String? = null
    ) {
        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)

        // transactions para asegurar atomicidad, se crea el chat o no se crea nada
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(chatRef)
            val userConvSnapshots = participants.map { userId ->
                val userConvRef = firestore
                    .collection(USERS_COLLECTION).document(userId)
                    .collection(USER_CONVERSATIONS_COLLECTION).document(chatId)
                userConvRef to transaction.get(userConvRef)
            }
            if(!snapshot.exists()){
                val chatData = hashMapOf(
                    "chatId" to chatId,
                    "participants" to participants,
                    "lastMessage" to null,
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "lastMessageSenderId" to null,
                    "lastMessageType" to null,
                    "unreadCount" to emptyMap<String, Int>(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "isGroup" to isGroup,
                    "groupName" to groupName,
                    "groupPhotoUrl" to groupPhotoUrl
                )
                transaction.set(chatRef, chatData)
            }
            // update entradas de SUBCOLLECTION CONVERSATION for each user
            userConvSnapshots.forEach { (userConvRef, userConvSnapshot) ->
                if (!userConvSnapshot.exists()) {
                    // Si el usuario no tiene la entrada (porque la borró o es un chat nuevo), se crea.
                    transaction.set(userConvRef, mapOf(
                        "chatId" to chatId,
                        "clearedTimestamp" to FieldValue.serverTimestamp(),
                        "blocked" to false,
                        "updatedAt" to FieldValue.serverTimestamp() // Para ordenar la lista de chats
                    ))
                } else {
                    // para que el chat suba al principio de la lista de conversations
                    transaction.update(userConvRef, "updatedAt", FieldValue.serverTimestamp())
                }
            }
        }.await()
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

    suspend fun addContact(number:String): Boolean{
        val query = firestore.collection(USERS_COLLECTION)
            .whereEqualTo("number", number)
            .get()
            .await()

        if (!query.isEmpty){
            val user = query.documents[0].toObject(UserData::class.java)
            val uid = user?.uid
            if (uid != null && uid != currentUserId) {
                val contactRef = firestore.collection(USERS_COLLECTION)
                    .document(currentUserId)
                    .collection(USER_CONTACTS_COLLECTION).document(uid)
                val contactData = hashMapOf(
                    "uid" to uid
                )
                contactRef.set(contactData).await()
                return true
            }
            return false
        }
        return false
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
        private const val USERS_COLLECTION = "users"
        private const val ORDER_BY_FIELD = "name"
        private const val ORDER_BY_FIELD_LOWER = "nameLowercase"
        private const val CHATS_COLLECTION = "chats"
        private const val USER_CONVERSATIONS_COLLECTION = "conversations"
        private const val USER_CONTACTS_COLLECTION = "contacts"
    }
}