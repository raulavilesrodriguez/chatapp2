package com.packt.chat.domain

import com.packt.chat.domain.models.Message
import kotlinx.coroutines.flow.Flow

interface IMessagesRepository {
    suspend fun getMessages(chatId: String, userId: String): Flow<List<Message>>
    suspend fun sendMessage(chatId: String, message: Message)
    suspend fun disconnect()
}