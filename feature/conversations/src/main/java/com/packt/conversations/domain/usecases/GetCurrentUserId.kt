package com.packt.conversations.domain.usecases

import com.packt.conversations.domain.IConversationsRepository
import javax.inject.Inject

class GetCurrentUserId @Inject constructor(
    private val repository: IConversationsRepository
) {
    operator fun invoke() = repository.currentUserId
}