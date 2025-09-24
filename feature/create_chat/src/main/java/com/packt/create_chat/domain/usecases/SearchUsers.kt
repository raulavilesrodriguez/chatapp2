package com.packt.create_chat.domain.usecases

import com.packt.create_chat.domain.IStoreUsersRepository
import javax.inject.Inject

class SearchUsers @Inject constructor(
    private val repository: IStoreUsersRepository
) {
    suspend operator fun invoke(namePrefix: String) = repository.searchUsers(namePrefix)
}