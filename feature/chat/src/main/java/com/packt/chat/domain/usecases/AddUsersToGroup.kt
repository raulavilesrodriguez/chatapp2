package com.packt.chat.domain.usecases

import com.packt.chat.domain.IMessagesRepository
import javax.inject.Inject

class AddUsersToGroup @Inject constructor(
    private val repository: IMessagesRepository
) {
    suspend operator fun invoke(chatId: String, usersToAdd: List<String>) {
        repository.addUsersToGroup(chatId, usersToAdd)
    }
}