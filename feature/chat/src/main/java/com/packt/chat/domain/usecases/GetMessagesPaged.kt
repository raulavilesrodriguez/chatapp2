package com.packt.chat.domain.usecases

import com.google.firebase.firestore.DocumentSnapshot
import com.packt.chat.domain.IMessagesRepository
import com.packt.chat.domain.models.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesPaged @Inject constructor(
    private val repository: IMessagesRepository
) {
    suspend operator fun invoke(
        chatId: String, userId: String, pageSize: Long, lastDocument: DocumentSnapshot?
    )
    : Pair<List<Message>, DocumentSnapshot?>{
        return repository
            .getMessagesPaged(
                chatId = chatId, userId = userId, pageSize = pageSize, lastDocument = lastDocument)
    }
}