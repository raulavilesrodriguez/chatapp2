package com.packt.chat.domain.usecases

import com.packt.chat.domain.IChatRoomRepository
import com.packt.domain.user.UserData
import javax.inject.Inject

class GetUser @Inject constructor(
    private val repository: IChatRoomRepository
) {
    suspend operator fun invoke(uid: String) : UserData? {
        return repository.getUser(uid)
    }
}