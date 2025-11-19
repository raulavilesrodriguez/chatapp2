package com.packt.create_chat.domain.usecases

import com.packt.create_chat.domain.IStoreUsersRepository
import javax.inject.Inject

class ChatExists @Inject constructor(
    private val repository: IStoreUsersRepository
) {
    suspend operator fun invoke(participants: List<String>, groupName: String?): Boolean {
        return repository.chatExists(participants, groupName)
    }
}