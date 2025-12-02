package com.packt.create_chat.domain.usecases

import com.packt.create_chat.domain.IStoreUsersRepository
import javax.inject.Inject

class CreateChatId @Inject constructor(
    private val repository: IStoreUsersRepository
) {
    suspend operator fun invoke(participants: List<String>, isGroup: Boolean): String {
        return repository.createChatId(participants, isGroup)
    }
}