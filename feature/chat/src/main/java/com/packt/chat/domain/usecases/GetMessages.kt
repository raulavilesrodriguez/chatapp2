package com.packt.chat.domain.usecases

import com.google.firebase.Timestamp
import com.packt.chat.domain.IMessagesRepository
import com.packt.chat.domain.models.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessages @Inject constructor(
    private val repository: IMessagesRepository
) {
    suspend operator fun invoke(
        chatId: String, userId: String, since:Timestamp
    ): Flow<List<Message>> {
        return repository.getMessages(chatId = chatId, userId = userId, since = since)
    }
}