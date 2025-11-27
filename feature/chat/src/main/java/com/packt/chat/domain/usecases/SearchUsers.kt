package com.packt.chat.domain.usecases

import com.packt.chat.domain.IMessagesRepository
import com.packt.domain.user.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchUsers @Inject constructor(
    private val repository: IMessagesRepository
) {
    suspend operator fun invoke(namePrefix: String): Flow<List<UserData>>{
        return repository.searchUsers(namePrefix)
    }
}