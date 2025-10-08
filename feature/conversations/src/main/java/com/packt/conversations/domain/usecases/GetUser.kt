package com.packt.conversations.domain.usecases


import com.packt.conversations.domain.IConversationsRepository
import com.packt.domain.user.UserData
import javax.inject.Inject

class GetUser @Inject constructor(
    private val repository: IConversationsRepository
) {
    suspend operator fun invoke(uid:String) : UserData?{
        return repository.getUser(uid)
    }
}