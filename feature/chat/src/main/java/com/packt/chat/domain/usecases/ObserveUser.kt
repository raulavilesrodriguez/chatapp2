package com.packt.chat.domain.usecases

import com.packt.chat.domain.IMessagesRepository
import com.packt.domain.user.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUser @Inject constructor(
    private val repository: IMessagesRepository
) {
    suspend operator fun invoke(uid: String): Flow<UserData?> =
        repository.observeUser(uid)
}