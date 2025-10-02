package com.packt.chat.domain.usecases

import com.packt.chat.domain.IMessagesRepository
import com.packt.chat.domain.models.Message
import javax.inject.Inject

class SendMessage @Inject constructor(
    private val repository: IMessagesRepository
) {
    suspend operator fun invoke(chatId: String, message: Message){
        repository.sendMessage(chatId, message)
    }
}