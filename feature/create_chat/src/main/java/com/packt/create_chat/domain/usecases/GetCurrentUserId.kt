package com.packt.create_chat.domain.usecases

import com.packt.create_chat.domain.IStoreUsersRepository
import javax.inject.Inject

class GetCurrentUserId @Inject constructor(
    private val repository: IStoreUsersRepository
) {
    operator fun invoke() = repository.currentUserId
}