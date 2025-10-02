package com.packt.chat.data.repository

import com.packt.chat.data.datasource.FirestoreChatsDataSource
import com.packt.chat.domain.IMessagesRepository
import com.packt.chat.domain.models.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessagesRepository @Inject constructor(
    private val dataSource: FirestoreChatsDataSource
) : IMessagesRepository {

    override suspend fun getMessages(chatId: String, userId: String): Flow<List<Message>> {
        return dataSource.getMessages(chatId, userId)
    }

    override suspend fun sendMessage(chatId: String, message: Message) {
        dataSource.sendMessage(chatId, message)
    }

    override suspend fun disconnect() {
        //do nothing, Firestore data source is disconnected as soon as the flow has no subscribers
    }
}