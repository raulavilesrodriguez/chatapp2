package com.packt.create_chat.domain.usecases

import com.packt.create_chat.domain.IStoreUsersRepository
import javax.inject.Inject

class AddContact @Inject constructor(
    private val repository: IStoreUsersRepository
) {
    suspend operator fun invoke(number:String): Boolean = repository.addContact(number)
}