package com.packt.chat.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.packt.chat.data.datasource.FirestoreChatsDataSource
import com.packt.chat.domain.IMessagesRepository
import com.packt.chat.domain.models.Message
import com.packt.domain.user.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessagesRepository @Inject constructor(
    private val dataSource: FirestoreChatsDataSource
) : IMessagesRepository {

    override suspend fun getMessages(
        chatId: String, userId: String, since:Timestamp
    ): Flow<List<Message>> {
        return dataSource.getMessages(chatId, userId, since)
    }

    override suspend fun getMessagesPaged(
        chatId: String,
        userId: String,
        pageSize: Long,
        lastDocument: DocumentSnapshot?
    ): Pair<List<Message>, DocumentSnapshot?> {
        return dataSource.getMessagesPaged(chatId, userId, pageSize, lastDocument)
    }

    override suspend fun sendMessage(chatId: String, message: Message) {
        dataSource.sendMessage(chatId, message)
    }

    override suspend fun disconnect() {
        //do nothing, Firestore data source is disconnected as soon as the flow has no subscribers
    }

    override suspend fun observeUser(uid: String): Flow<UserData?> {
        return dataSource.observeUser(uid)
    }
}