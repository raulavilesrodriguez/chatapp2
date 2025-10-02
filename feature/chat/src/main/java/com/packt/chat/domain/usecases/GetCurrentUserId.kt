package com.packt.chat.domain.usecases

import com.packt.chat.domain.IChatRoomRepository
import javax.inject.Inject

class GetCurrentUserId @Inject constructor(
    private val repository: IChatRoomRepository
) {
    operator fun invoke() = repository.currentUserId
}