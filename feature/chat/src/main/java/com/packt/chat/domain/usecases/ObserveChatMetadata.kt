package com.packt.chat.domain.usecases

import com.packt.chat.domain.IMessagesRepository
import com.packt.domain.model.ChatMetadata
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveChatMetadata @Inject constructor(
    private val repository: IMessagesRepository
) {
    suspend operator fun invoke(chatId: String): Flow<ChatMetadata?> =
        repository.observeChatMetadata(chatId)
}