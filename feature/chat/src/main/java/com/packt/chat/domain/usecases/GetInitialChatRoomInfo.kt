package com.packt.chat.domain.usecases

import com.packt.chat.domain.IChatRoomRepository
import com.packt.domain.model.ChatMetadata
import javax.inject.Inject

class GetInitialChatRoomInfo @Inject constructor(
    private val repository: IChatRoomRepository
) {
    suspend operator fun invoke(chatId: String): ChatMetadata?{
        return repository.getInitialChatRoomInfo(chatId)
    }
}