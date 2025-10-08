package com.packt.conversations.domain.usecases

import com.packt.conversations.domain.IConversationsRepository
import com.packt.domain.model.ChatMetadata
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversations @Inject constructor(
    private val repository: IConversationsRepository
) {
    suspend operator fun invoke() : Flow<List<ChatMetadata>> = repository.getConversations()
}