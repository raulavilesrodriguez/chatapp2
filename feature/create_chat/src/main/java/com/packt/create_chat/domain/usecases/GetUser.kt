package com.packt.create_chat.domain.usecases

import com.packt.create_chat.domain.IStoreUsersRepository
import com.packt.domain.user.UserData
import javax.inject.Inject

class GetUser @Inject constructor(
    private val repository: IStoreUsersRepository
) {
    suspend operator fun invoke(uid: String) : UserData? {
        return repository.getUser(uid)
    }
}