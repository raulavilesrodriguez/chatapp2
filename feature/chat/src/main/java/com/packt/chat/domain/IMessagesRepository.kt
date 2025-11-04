package com.packt.chat.domain

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.packt.chat.domain.models.Message
import com.packt.domain.user.UserData
import kotlinx.coroutines.flow.Flow

interface IMessagesRepository {
    suspend fun getMessages(chatId: String, userId: String, since:Timestamp): Flow<List<Message>>
    suspend fun getMessagesPaged(
        chatId: String, userId: String, pageSize:Long, lastDocument: DocumentSnapshot?)
    : Pair<List<Message>, DocumentSnapshot?>
    suspend fun sendMessage(chatId: String, message: Message, participants: List<String>)
    suspend fun disconnect()
    suspend fun observeUser(uid: String): Flow<UserData?>
    suspend fun resetUnreadCount(chatId: String)
    suspend fun setUserActiveInChat(chatId: String)
    suspend fun clearUserActiveStatus(chatId: String)
}