package com.packt.create_chat.domain.usecases

import com.packt.create_chat.domain.IStoreUsersRepository
import javax.inject.Inject

class CreateChat @Inject constructor(
    private val repository: IStoreUsersRepository
) {
    suspend operator fun invoke(participants: List<String>): String {
        return repository.createChat(participants)
    }
}